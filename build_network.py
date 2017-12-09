#!/usr/bin/env python

import sys, os, re
import json
from collections import defaultdict

# # Count the commits for each push to each repo by each user
# users_repos_counts = defaultdict(lambda:defaultdict(int))
# for line in sys.stdin:
#     record = json.loads(line)
#     if "type" in record and record["type"] == "PushEvent":
#         user = record["actor"]["login"]
#         repo = record["repo"]["name"]
#         commit_count = len(record["payload"]["commits"])
#
#         users_repos_counts[user][repo] += commit_count
#
# # Produce json of same
# for user, repos_counts in users_repos_counts.items():
#     for repo, commit_count in repos_counts.items():
#         print(
#             json.dumps(
#                 {
#                     "user": user,
#                     "repo": repo,
#                     "commit_count": commit_count
#                 }
#             )
#         )
#

# Calculate the fork count directly
# repos_forks = defaultdict(int)
# for line in sys.stdin:
#     record = json.loads(line)
#     if "type" in record and record["type"] == "ForkEvent":
#         # user = record["actor"]["login"]
#         original_repo = record["repo"]["name"]
#         # new_forked_repo = record["payload"]["forkee"]["full_name"]
#         repos_forks[original_repo] += 1
#
# for repo, fork_count in repos_forks.items():
#     print(
#         json.dump(
#             {
#                 "repo": repo,
#                 "fork_count": fork_count
#             }
#         )
#     )

users_repos_f = open("data/users_repos.jsonl", "w")
users_repos_forks = defaultdict(lambda:defaultdict(int))
for line in sys.stdin:
    record = json.loads(line)
    if "type" in record and record["type"] == "ForkEvent":
        user = record["actor"]["login"]
        original_repo = record["repo"]["name"]
        users_repos_forks[user][original_repo] += 1

for user, repos_forks in users_repos_forks.items():
    for repo, fork_count in repos_forks.items():
        users_repos_f.write(
            json.dumps(
                {
                    "user": user,
                    "repo": repo,
                    "fork_count": fork_count
                }
            ) + "\n"
        )
users_repos_f.close()

users_f = open("data/users.jsonl", "w")
repos_dict = defaultdict(int)
repos_f = open("data/repos.jsonl", "w")
for user, repos_forks in users_repos_forks.items():
    users_f.write(
        json.dumps(
            {
                "user": user
            }
        ) + "\n"
    )
    for repo, fork_count in repos_forks.items():
        repos_dict[repo] += fork_count

for repo, fork_count in repos_dict.items():
    repos_f.write(
        json.dumps(
            {
                "repo": repo,
                "fork_count": fork_count
            }
        ) + "\n"
    )

users_f.close()
repos_f.close()
