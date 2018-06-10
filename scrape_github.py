#!/usr/bin/env python

import sys, os, re
import json
import time

from selenium import webdriver
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities

BASE_URL = 'https://github.com'

dcap = dict(DesiredCapabilities.PHANTOMJS)
dcap['phantomjs.page.settings.userAgent'] = (
    'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/53 '
    '(KHTML, like Gecko) Chrome/15.0.87'
)
driver = webdriver.PhantomJS(desired_capabilities=dcap)

with open("data/repos_sample_0.01.jsonl") as f:
  for line in f:
    time.sleep(0.1)
    record = json.loads(line)
    
    repo_name = record['repo']
    repo_filename = repo_name.replace('/','#')
    repo_path = 'data/html/{}.html'.format(repo_filename)
    github_url = '{}/{}'.format(
      BASE_URL,
      repo_name
    )

    print('Fetching {} ...'.format(github_url))
    driver.get(github_url)

    with open(repo_path, "w") as f2:
      f2.write(driver.page_source)
