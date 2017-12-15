import sys, os, json
from frozendict import frozendict

github_lines = sc.textFile("data/*.json.gz")

# Apply the function to every record
github_events = github_lines.map(json.loads)

fork_events = github_events.map(lambda x: frozendict({"user": x["actor"]["login"], "repo": x["repo"]["name"]}))
fork_events_lines = fork_events.map(lambda x: json.dumps(x))
fork_events.saveAsTextFile("data/users_repos.jsonl")

repos = fork_events.map(lambda x: frozendict({"repo": x["repo"]}))
repos = repos.distinct()
repos_lines = repos.map(lambda x: json.dumps(x))
repos_lines.saveAsTextFile("data/repos.jsonl")

users = fork_events.map(lambda x: frozendict({"user": x["user"]}))
users = users.distinct()
users_lines = users.map(lambda x: json.dumps(x))
users_lines.saveAsTextFile("data/users.jsonl")