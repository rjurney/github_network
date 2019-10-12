import sys, os, re
import jsonlines

eighty_records = [record for record in jsonlines.open('data/html/first_training_enriched.jsonl')]
thousand_records = [record for record in jsonlines.open('data/html/first_exploit_set_enriched.jsonl')]

def extract_training_fields(record):
  """Convert a record fetched from Github to one that can be loaded by notebooks/First Repo Classifier Experiment.ipynb"""

  record = record.copy()

  # Convert booleans to binary integers
  BOOLEAN_FIELDS = [
    'archived',
    'has_downloads',
    'has_issues',
    'has_wiki',
    'is_fork',
  ]
  for field in BOOLEAN_FIELDS:
    record[field] = int(record[field])
  
  # Delete fields we're not using yet
  REMOVED_FIELDS = [
    'description',
    'language',
    'homepage'
  ]
  for field in REMOVED_FIELDS:
    del record[field]
  
  return record

eighty_processed_records = [extract_training_fields(record) for record in eighty_records]
with jsonlines.open('data/html/first_training_enriched_processed.jsonl', mode='w') as writer:
  for record in eighty_processed_records:
    writer.write( record )

thousand_processed_records = [extract_training_fields(record) for record in thousand_records]
with jsonlines.open('data/html/first_exploit_set_enriched_processed.jsonl', mode='w') as writer:
  for record in thousand_processed_records:
    writer.write( record )
