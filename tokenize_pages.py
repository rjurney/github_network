import sys, os, re
import glob
from bs4 import BeautifulSoup
import json

import nltk
nltk.download('punkt')
nltk.download('stopwords')
from string import punctuation
from nltk.corpus import stopwords

stop_words = stopwords.words('english') + list(punctuation) + ['readme.md', 'readme', 'http', 'ftp', 'https']


def tokenize_tag(tag):
  """Tokenize a BeautifulSoup tag into a bag of words"""
  sentences = nltk.sent_tokenize(tag.text)
  sentence_words = []
  for sentence in sentences:
    words = nltk.casual_tokenize(sentence)
    lower_words = [w.lower() for w in words]
    filtered_words = [w for w in lower_words if w not in stop_words and not w.isdigit() and len(w) > 2]
    sentence_words += filtered_words
  return sentence_words


def get_json_path(file_path):
  """Go from data/html/... to data/html/text/..."""
  file_path_parts = file_path.split('/')
  data_html = '/'.join(file_path_parts[0:2]) # data/html

  filename = file_path_parts[2] # Agile_Data_Code_2.html
  json_name = re.sub('html$', 'json', filename) # Agile_Data_Code_2.json

  text_path = '{}/text/{}'.format(
    data_html,
    json_name
  )
  return text_path, filename


# Open every HTML file and extract the README text
records = []
for file_path in glob.glob('data/html/*.html'):
  
  with open(file_path) as f:

    sys.stderr.write('.')
    sys.stderr.flush()
    
    html = f.read()
    soup = BeautifulSoup(html, 'lxml')

    readme_div = soup.find('div', {'id': 'readme'})
    if readme_div:

      readme_words = []

      # A README is rendered simply as HTML tags
      all_text_tags = readme_div.find_all(['p', 'li', 'h1', 'h2', 'h3', 'h4'])
      for tag in all_text_tags:
        words = tokenize_tag(tag)
        readme_words += words

      # Save as a JSON object with the repo name and a bag of words
      text_file_path, filename = get_json_path(file_path)    
      with_slash = filename.replace('#', '/')
      repo_name = re.sub('\.html$', '', with_slash)

      record = {'repo': repo_name, 'readme_words': readme_words}
      records.append(record)

      # # Later we can also handle code
      # all_code = readme_div.find_all('code')
      # for code in all_code:
      #   print(code.text)

# Save to one large file
out_path = 'data/html/documents.jsonl'
with open(out_path, 'w') as out_f:
  for record in records:
    out_f.write(json.dumps(record) + '\n')

print("Wrote {:,} documents to {} :)".format(
  len(records),
  out_path
))
