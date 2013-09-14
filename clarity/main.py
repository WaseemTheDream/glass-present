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
import json
import re
import urllib
import uuid

from google.appengine.api import channel
from google.appengine.ext import ndb

from utils import get_metadata

MAIN_DIR = os.path.dirname(__file__)

JINJA_ENV = jinja2.Environment(loader=jinja2.FileSystemLoader(MAIN_DIR))

class Presentation(ndb.Model):
    drive_id = ndb.StringProperty()
    slides = ndb.TextProperty()

class MainHandler(webapp2.RequestHandler):
    """
    Serves the main app.
    """
    def get(self):
        template = JINJA_ENV.get_template('index.html')
        self.response.out.write(template.render({}))

class PresentationHandler(webapp2.RequestHandler):
    """
    Handles create requests of presentations from the browser side.
    """
    parse_url_regex = re.compile(r'presentation/d/([a-zA-Z0-9\-_]+)')

    def post(self):
        drive_url = json.loads(self.request.body)['driveurl']
        logging.info("Received the drive url: %s", drive_url)
        drive_id = self.parse_url(drive_url)

        presentation = \
            Presentation.query(Presentation.drive_id == drive_id).get()
        if presentation is None:
            presentation = Presentation(drive_id=drive_id)

        slides = get_metadata(drive_id)
        slides_str = json.dumps(slides)
        logging.info(slides_str)
        presentation.slides = slides_str
        presentation_id = presentation.put().id()

        self.response.write(json.dumps({
            'presentation_id': str(presentation_id),
        }));

    def get(self, presentation_id):
        presentation = Presentation.get_by_id(int(presentation_id))
        if not presentation:
            self.abort(404)

        presenter_id = str(uuid.uuid4())

        token = channel.create_channel(make_channel_name(
            presentation_id=presentation_id,
            presenter_id=presenter_id,
        ))

        slides_decoded = json.loads(presentation.slides)
        out = {
            'presentation_id': str(presentation_id),
            'presenter_id': presenter_id,
            "slides": slides_decoded,
            "token": token,
        }
        self.response.write(json.dumps(out))

    @staticmethod
    def parse_url(url):
        parse_url_regex = re.compile(r'presentation/d/([a-zA-Z0-9\-_]+)')
        match = parse_url_regex.search(url)
        drive_id = match.group(1)
        return drive_id


def make_channel_name(presentation_id=None, presenter_id=None):
    return str(presentation_id) + str(presenter_id)


class ControllerHandler(webapp2.RequestHandler):
    """
    Handles remote control queries and commands
    """

    def get(self, presentation_id):
        presentation = Presentation.get_by_id(int(presentation_id))
        if not presentation:
            self.abort(404)
        presenter_id = self.request.get('presenter_id')

        slides_decoded = json.loads(presentation.slides)

        channel.send_message(make_channel_name(
            presentation_id=presentation_id,
            presenter_id=presenter_id,
        ), json.dumps({
            'event': 'glass connected',
        }))
        out = {
            'presentation_id': str(presentation_id),
            'slides': slides_decoded
        }
        self.response.write(json.dumps(out))

    def post(self):
        page_id = self.request.get('page_id')
        presentation_id = self.request.get('presentation_id')
        presenter_id = self.request.get('presenter_id')
        logging.info('presentation_id: %s', presentation_id)
        logging.info('presenter_id: %s', presenter_id)
        presentation = Presentation.get_by_id(int(presentation_id))
        logging.info(presentation)
        if not presentation:
            self.abort(404)

        channel.send_message(make_channel_name(
            presentation_id=presentation_id,
            presenter_id=presenter_id,
        ), json.dumps({
            'event': 'slide changed',
            'page_id': page_id,
        }))

        self.response.out.write(presentation.drive_id)



app = webapp2.WSGIApplication([
    webapp2.Route('/', MainHandler),
    webapp2.Route('/api/presentation', PresentationHandler),
    webapp2.Route('/api/presentation/<presentation_id:\d+>', PresentationHandler),
    webapp2.Route('/api/controller', ControllerHandler),
    webapp2.Route('/api/controller/<presentation_id:\d+>', ControllerHandler),
], debug=True)



# Legacy code
class GlassHandler(webapp2.RequestHandler):
    """
    Handles glass queries and commands
    """

    def post(self):
        action = self.request.get('action')
        logging.info('glass posted action ' + action)

        if action == 'init':
            presentation_id = int(self.request.get('id'))
            presentation = Presentation.get_by_id(presentation_id)
            logging.info(presentation_id)

            # Send message to browser client
            channel.send_message(make_channel_name(
                presentation_id=presentation_id,
                presenter_id=presenter_id,
            ), json.dumps({'status': 'connected'}))

            self.response.out.write(presentation.slides)

        elif action == 'change_slide':
            slide = int(self.request.get('slide'))
            presentation = Presentation.get_by_id(presentation_id)

            channel.send_message(make_channel_name(
                presentation_id=presentation_id,
                presenter_id=presenter_id,
            ), json.dumps({
                    'status': 'slide changed',
                    'slide': slide,
                })
            )

            self.response.out.write(presentation.drive_id)