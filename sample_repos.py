import sys, os, re
import json
import math
import random

FILE_NAME = "data/repos_sample_2_0.01.jsonl"
SAMPLE_RATIO = 0.01

with open("data/repos.jsonl") as f:
  records = [json.loads(x) for x in f]

count = len(records)
sample_count = math.ceil(count * SAMPLE_RATIO)
sample_records = random.sample(records, sample_count)

assert len(sample_records) == sample_count

with open(FILE_NAME, "w") as f:
  for record in sample_records:
    f.write(json.dumps(record) + "\n")

print("Sampled {} records from {} original records.".format(
  sample_count,
  count
))
