#!/usr/bin/env python

import sys, os, re
import json
from datetime import datetime
import time

epoch = datetime.utcfromtimestamp(0)

def unix_time_millis(dt):
    return (dt - epoch).total_seconds() * 1000.0

from selenium import webdriver
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities

BASE_URL = 'https://github.com'

dcap = dict(DesiredCapabilities.PHANTOMJS)
dcap['phantomjs.page.settings.userAgent'] = (
    'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/53 '
    '(KHTML, like Gecko) Chrome/15.0.87'
)
driver = webdriver.PhantomJS(desired_capabilities=dcap)

start = unix_time_millis(datetime.now())

records = []

with open("data/repos.jsonl") as f:
  lines = [x for x in f]

  prev_to_go = 0.0
  for i, line in enumerate(lines):
    time.sleep(0.1)
    record = json.loads(line)
    
    repo_name = record['repo']
    repo_filename = repo_name.replace('/','#')
    repo_path = 'data/html/{}.html'.format(repo_filename)
    github_url = '{}/{}'.format(
      BASE_URL,
      repo_name
    )

    now = unix_time_millis(datetime.now())
    so_far = now - start
    per_i = so_far / (i + 1)
    to_go = per_i * (len(lines) - i)
    estimate_delta = abs(to_go - prev_to_go)
    prev_to_go = to_go

    print('Fetching {:,} of {:,}. It has taken {:.1f} so far. Estimate {:,.1f} to go! Delta is {:,.2f}'.format(
      i, 
      len(lines),
      so_far,
      to_go,
      estimate_delta
    ))

    records.append((
      i,
      len(lines),
      so_far,
      to_go,
      estimate_delta
    ))

    if i % 1000 == 0:
      log_filename = 'data/experiment.jsonl'
      print('Writing progress to file \'{}\' ...'.format(log_filename))
      with open(log_filename, 'w') as out_f:
        for record in records:
          out_f.write(
            json.dumps(record) + '\n'
          )

    driver.get(github_url)

    with open(repo_path, 'w') as f2:
      f2.write(driver.page_source)
