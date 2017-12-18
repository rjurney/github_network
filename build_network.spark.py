import sys, os, json
from frozendict import frozendict

# If there is no SparkSession, create the environment
try:
    sc and spark
except NameError as e:
    import findspark
    
    findspark.init()
    import pyspark
    import pyspark.sql
    
    sc = pyspark.SparkContext()
    spark = pyspark.sql.SparkSession(sc).builder.appName("Extract Network").getOrCreate()

github_lines = sc.textFile("data/2017*.json.gz")

# Apply the function to every record
def parse_json(line):
    record = None
    try:
        record = json.loads(line)
    except json.JSONDecodeError as e:
        sys.stderr.write(str(e))
        record = {"error": "Parse error"}
    return record

github_events = github_lines.map(parse_json)
github_events = github_events.filter(lambda x: "error" not in x)

fork_events = github_events.filter(lambda x: "type" in x and x["type"] == "ForkEvent")

fork_events = fork_events.map(
    lambda x: frozendict(
        {
            "user": x["actor"]["login"] if "actor" in x and "login" in x["actor"] else None,
            "repo": x["repo"]["name"] if "repo" in x and "name" in x["repo"] else None
        }
    )
)
fork_events = fork_events.filter(lambda x: x["user"] is not None and x["repo"] is not None)

def json_serialize(obj):
    """Serialize objects as dicts instead of strings"""
    if isinstance(obj, frozendict):
        return dict(obj)

fork_events_lines = fork_events.map(lambda x: json.dumps(x, default=json_serialize))
fork_events_lines.saveAsTextFile("data/users_repos.jsonl")

repos = fork_events.map(lambda x: frozendict({"repo": x["repo"]}))
repos = repos.distinct()
repos_lines = repos.map(lambda x: json.dumps(x, default=json_serialize))
repos_lines.saveAsTextFile("data/repos.jsonl")

users = fork_events.map(lambda x: frozendict({"user": x["user"]}))
users = users.distinct()
users_lines = users.map(lambda x: json.dumps(x, default=json_serialize))
users_lines.saveAsTextFile("data/users.jsonl")
