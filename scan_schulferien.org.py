# coding=utf-8
from requests import *
import re

def sluggify(s):
    return re.sub('[^a-z0-9_-]', '', re.sub('[\\s\']', '-', s.lower().replace('ä', 'ae').replace('ö', 'oe').replace('ü', 'ue').replace('ß', 'ss')))

countries = ['deutschland', 'oesterreich', 'schweiz', 'belgien', 'bosnien-herzegowina', 'daenemark', 'frankreich', 'italien', 'neuseeland', 'neuseeland', 'holland', 'norwegen', 'polen', 'slowakei', 'tschechien']
years = set(['2019', '2020'])
key = 'MiGcW0caRfdQoIHiIFqciByx8oIjmOQHGvTHgfc9MNrDIGVbiiTYxxMLjlMn_EuGfJM2iLzjX6pwaXak99Jqz7o9E5oCGE61KP3yG7H_qk0'

url_tpl = 'https://www.schulferien.org/{}/ical/'

dl_url_tpl_base = 'https://www.schulferien.org/media/ical'
dl_url_tpl_per_state = dl_url_tpl_base + '/{}/{}_{}_{}.ics?k={}'
dl_url_tpl = dl_url_tpl_base + '/{}/feiertage_{}.ics?k={}'

s = Session()

urls = []
for country in countries:
    url = url_tpl.format(country)
    result = s.get(url)
    for (state, slug, year, kind_name) in [(state, sluggify(state), year, kind) for (kind, year, state) in set(re.findall('<a\s+[^>]+"iCal (Feiertage|Schulferien) (\d\d\d\d) ([^"]+)"', result.text))]:
        kind = 'ferien' if kind_name == 'Schulferien' else 'feiertage'
        if year in years:
            url = dl_url_tpl_per_state.format(country, kind, slug, year, key)
            print(url)
            urls.append((country + '_' + slug + '_' + kind + '_' + year + ".ics", url))
    for year in years:
        url = dl_url_tpl.format(country, year, key)
        print(url)
        urls.append((country + '_feiertage_' + year + ".ics", url))

for filename, url in urls:
    result = s.get(url)
    print('Getting: ' + filename + ' from ' + url)
    if result.status_code == 200:
        with open('data/schulferien.org/' + filename, 'wb') as f:
            f.write(result.content)



