import urllib2, logging, re

from HTMLParser import HTMLParser

class GDParser(HTMLParser):
    pageidregex = re.compile(r'pageid=(.+?)&')
    regex1 = re.compile(r'background: url\((.+)\);')
    def __init__ (self, driveid):
        self.driveid = driveid
        self.pageids = []
        self.imgurls = []
        self.notes = []
        self.noteson = False
        self.reset()
    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == 'section':
            if attrs.get('class', '') == 'slide-content':
                stylestr = attrs['style']
                url = HTMLParser().unescape(GDParser.regex1.search(stylestr).group(1))
                pageid = GDParser.pageidregex.search(url).group(1)
                imgurl = 'https://docs.google.com/presentation/d/%s/export/png?id=%s&pageid=%s' % (self.driveid, self.driveid, pageid)
                self.pageids.append(pageid)
                self.imgurls.append(imgurl)
                self.notes.append('')
                self.noteson = False
            if attrs.get('class', '') == 'slide-notes':
                self.curaccum = ''
                self.noteson = True
    def handle_endtag(self, tag):
        pass
    def handle_data(self, data):
        if self.noteson:
            self.notes[-1] += HTMLParser().unescape(data) + ' '
    def get_slides(self):
        ret = []
        for i in range(len(self.pageids)):
            ret.append({'img_url': self.imgurls[i], 'page_id': self.pageids[i], 'speaker_notes': self.notes[i]})
        return ret

def get_metadata (drive_id):
    drive_url = 'https://docs.google.com/presentation/d/%s/htmlpresent' % drive_id
    html_str = None
    logging.info('parsing from ' + drive_url)
    infile = urllib2.urlopen(drive_url)
    html_str = infile.read().splitlines()[-1]
    infile.close()
    logging.info(html_str)
    parser = GDParser(drive_id)
    parser.feed(html_str)
    slides = parser.get_slides()
    return slides
