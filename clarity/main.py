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

MAIN_DIR = os.path.dirname(__file__)

JINJA_ENV = jinja2.Environment(loader=jinja2.FileSystemLoader(MAIN_DIR))

class MainHandler(webapp2.RequestHandler):
    """
    Serves the main app.
    """
    def get(self):
        template = JINJA_ENV.get_template('index.html')
        self.response.out.write(template.render({}))

class CreateHandler(webapp2.RequestHandler):
    """
    Handles create requests of presentations from the browser side.
    """
    def post(self):
        drive_url = self.request.get('driveurl')
        logging.info("Received the drive url: %s", drive_url)


app = webapp2.WSGIApplication([
    ('/', MainHandler),
    ('/create', CreateHandler)
], debug=True)
