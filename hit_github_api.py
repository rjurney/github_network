#!/usr/bin/env python

import sys, os, re
import json, csv
import jsonlines
import time

from github import Github
from github.GithubException import UnknownObjectException

token = os.environ.get('GITHUB_TOKEN')
github = Github(token)
 
def get_records_80():
  """Open the first 80 record training dataset"""
  with jsonlines.open('data/html/first_training.jsonl') as reader:
    records = [record for record in reader]
    return records

def get_records_1000():
  """Open the first 1,000 'exploit' dataset"""
  with open('data/html/first_exploit_set.jsonl') as f:
    records = [json.loads(line) for line in f]
    return records

def fetch_github_api(records, i=0):
  """Fetch a list of repositories with 'repo' as a full repository name from Github, starting at i (default 0)"""

  # While loop enables restarts as we discover new exceptions, only have 5K API calls/day
  docs = []
  try:
    while i < len(records):
      record = records[i]
      
      repo_name = record['repo']
      print('Fetching {} {} ...'.format(i, repo_name))
      try:
        repo = github.get_repo(repo_name, lazy=False)
      except UnknownObjectException as e:
        print('Repo {} missing!'.format(repo_name))

      doc = {
        'archived': repo.archived,
        'description': repo.description,
        'is_fork': repo.fork,
        'forks': repo.forks,
        'has_downloads': repo.has_downloads,
        'has_issues': repo.has_issues,
        'has_wiki': repo.has_wiki,
        'homepage': repo.homepage,
        'language': repo.language,
        'network_count': repo.network_count,
        'open_issues': repo.open_issues,
        'size': repo.size,
        'stargazers': repo.stargazers_count,
        'subscribers': repo.subscribers_count,
        'watchers': repo.watchers_count
      }

      # Fetch the languages of the project
      # record.languages_url

      # combine/add repo/is_edu/readme_words
      doc.update(record) 

      docs.append(doc)
      i += 1
  except Exception as e:
    print(str(e))
    print("Unknown exception, returning early!")
    return i, docs

  return i, docs

progress, eighty_records = fetch_github_api( get_records_80() )
with jsonlines.open('data/html/first_training_enriched.jsonl', mode='w') as writer:
  for record in eighty_records:
    writer.write( record )

progress, thousand_records = fetch_github_api( get_records_1000() )
with jsonlines.open('data/html/first_exploit_set_enriched.jsonl', mode='w') as writer:
  for record in thousand_records:
    writer.write( record )
