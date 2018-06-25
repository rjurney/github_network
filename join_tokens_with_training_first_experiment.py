import sys, os, re
import json, csv

import keras

# Load our HTML tokens/repo names
with open('data/html/documents.jsonl') as f:
  docs = []
  for line in f:
    doc = json.loads(line)
    docs.append(doc)

# Load our =~ 80 initial training data set, repo/is_edu fields
repo_set = set()
with open('data/html/first_training.csv') as f:
  csv_reader = csv.DictReader(f, fieldnames=['repo', 'is_edu'])
  raw_training_data = {}
  for record in csv_reader:
    repo = record['repo']
    is_edu = int(record['is_edu'])
    raw_training_data[repo] = is_edu
    repo_set.add(repo)

# Inner join the two datasets
experiment_data = []
for doc in docs:
  repo = doc['repo']
  if repo in repo_set:
    doc['is_edu'] = raw_training_data[repo]
    experiment_data.append(doc)
    print(doc)

# Store to disk
with open('data/html/first_training.jsonl', 'w') as f:
  for doc in experiment_data:
    f.write(
      json.dumps(doc) + '\n'
    )

# Now we have repo/readme_words/is_edu

