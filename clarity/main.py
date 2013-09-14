#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import logging
import webapp2
import os
import jinja2
import re
import urllib
from google.appengine.ext import ndb

MAIN_DIR = os.path.dirname(__file__)
PAGES_DIR = os.path.join(MAIN_DIR, 'pages')

JINJA_ENV = jinja2.Environment(loader=jinja2.FileSystemLoader(PAGES_DIR))

class Presentation(ndb.Model):
    drive_id = ndb.StringProperty()

class MainHandler(webapp2.RequestHandler):
    """
    Serves the main app.
    """
    def get(self):
        template = JINJA_ENV.get_template('main.html')
        self.response.out.write(template.render({}))

class CreateHandler(webapp2.RequestHandler):
    """
    Handles create requests of presentations from the browser side.
    """
    parse_url_regex = re.compile(r'presentation/d/([a-zA-Z0-9\-]+)')
    def post(self):
        drive_url = self.request.get('driveurl')
        logging.info("Received the drive url: %s", drive_url)
        match = CreateHandler.parse_url_regex.search(drive_url)
        drive_id = match.group(1)
        presentation = Presentation(drive_id = drive_id)
        presentation_id = presentation.put().id()
        logging.info('New presentation (url %s): id %d' % (drive_id, presentation_id))
        qstring = urllib.urlencode({
            'id' : presentation_id,
            'driveid' : drive_id,
        })
        self.redirect('/view?' + qstring)

class ViewHandler(webapp2.RequestHandler):
    """
    Shows the web view of the presentation (mainly JS)
    """
    def get(self):
        drive_id = self.request.get('driveid')
        presentation_id = int(self.request.get('id'))
        self.response.out.write('Should show doc presentation %s (id %d)' % (drive_id, presentation_id))

class GlassHandler(webapp2.RequestHandler):
    """
    Handles glass queries and commands
    """

    def post(self):
        action = self.request.get('action')
        logging.info('glass posted action ' + action)
        if action == 'init':
            presentation_id = int(self.request.get('id'))
            presentation = ndb.get(presentation_id)
            loggin.info(presentation_id)
            self.response.out.write(presentation.drive_id)
            # TODO: javascript comm stuff
        elif action == 'change_slide':
            slide = int(self.request.get('slide'))
            # TODO: javascript comm stuff

app = webapp2.WSGIApplication([
    ('/', MainHandler),
    ('/create', CreateHandler),
    ('/view', ViewHandler),
    ('/glass', GlassHandler),
], debug=True)
