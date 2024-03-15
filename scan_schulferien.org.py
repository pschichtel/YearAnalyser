#!/usr/bin/env python

import os

from requests import *
import re
import html
from functools import reduce


def sluggify(str, country):
    pattern_substitutions = [
        ('[\\s\']', '-'),
        ('[^a-z0-9_-]', ''),
    ]

    string_substitutions = [
        ('ø', 'o'),
        ('ä', 'ae'),
        ('ö', 'oe'),
        ('ü', 'ue'),
        ('ß', 'ss')
    ]

    if country == 'norwegen':
        string_substitutions.insert(0, ('ö', 'o'))
        string_substitutions.insert(0, ('süd-', 'sor-'))
        string_substitutions.insert(0, ('west-', 'vest-'))
        string_substitutions.append((' und ', ' og '))
        string_substitutions.append(('trondelag', 'trndelag'))

    if country == 'daenemark':
        string_substitutions.insert(0, ('ø', ''))

    lower = str.lower()
    string_replaced = reduce(lambda s, sub: s.replace(sub[0], sub[1]), string_substitutions, lower)
    patterns_replaced = reduce(lambda s, sub: re.sub(sub[0], sub[1], s), pattern_substitutions, string_replaced)

    return patterns_replaced


countries = {'deutschland': 'Germany',
             'oesterreich': 'Austria',
             'schweiz': 'Schweiz',
             'belgien': 'Belgium',
             'bosnien-herzegowina': 'Bosnia and Herzegovina',
             'daenemark': 'Denmark',
             'frankreich': 'France',
             'italien': 'Italy',
             'neuseeland': 'New Zealand',
             'holland': 'Netherlands',
             'norwegen': 'Norway',
             'polen': 'Poland',
             'slowakei': 'Slovakia',
             'tschechien': 'Czech Republic'}
years = {'2023', '2024', '2025'}
# looks like a secret, but it really isn't
key = 'BR90NCGhfxv-Z91HKSscn3lajiq-uy0cN02pJYlzhSRzQvXS8dngqqQbPOCgOgmM3L1aqZFcX76KQrIu6fv01MqOOVQJUMyIra5tB2m2crM'

url_tpl = 'https://www.schulferien.org/{}/ical/'

dl_url_tpl_base = 'https://www.schulferien.org/media/ical'
dl_url_tpl_per_state = dl_url_tpl_base + '/{}/{}_{}_{}.ics?k={}'
dl_url_tpl = dl_url_tpl_base + '/{}/feiertage_{}.ics?k={}'

s = Session()

urls = []
for country, country_name in countries.items():
    url = url_tpl.format(country)
    result = s.get(url)
    for (state, year, kind_name) in [(html.unescape(state), year, kind) for (kind, year, state) in set(re.findall('<a\s+[^>]+"iCal (Feiertage|Schulferien) (\d\d\d\d) ([^"]+)"', result.text))]:
        slug = sluggify(state, country)
        kind = 'ferien' if kind_name == 'Schulferien' else 'feiertage'
        if year in years:
            url = dl_url_tpl_per_state.format(country, kind, slug, year, key)
            print("{} / {} / {} -> {}".format(country, state, year, url))
            urls.append((state + '.ics', url, country_name, year))
    for year in years:
        url = dl_url_tpl.format(country, year, key)
        print(url)
        urls.append(('Holidays.ics', url, country_name, year))

for filename, url, country_name, year in urls:
    result = s.get(url)
    print('Getting: ' + filename + ' from ' + url)
    target_base = os.path.join('data', 'schulferien.org', str(year), country_name)
    os.makedirs(target_base, exist_ok=True)
    target = os.path.join(target_base, filename)
    if result.status_code == 200:
        with open(target, 'wb') as f:
            f.write(result.content)
    else:
        print(f'Download of {url} failed: {result.status_code}')



