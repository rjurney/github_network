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

# Load all Github events for the year 2017
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

#
# Split our events out by type
#

# See https://developer.github.com/v3/activity/events/types/#forkevent
fork_events = github_events.filter(lambda x: "type" in x and x["type"] == "ForkEvent")
own_events = github_events.filter(lambda x: "type" in x and x["type"] == "ForkEvent")

# See https://developer.github.com/v3/activity/events/types/#watchevent
star_events = github_events.filter(lambda x: "type" in x and x["type"] == "WatchEvent")

#
# Create simple records suitable for output to a graph database
#

# Get the user and repo for each ForkEvent, linking users with repos by their forks
fork_events = fork_events.map(
    lambda x: frozendict(
        {
            "user": x["actor"]["login"] if "actor" in x and "login" in x["actor"] else None,
            "repo": x["repo"]["name"] if "repo" in x and "name" in x["repo"] else None
        }
    )
)
fork_events = fork_events.filter(lambda x: x["user"] is not None and x["repo"] is not None)

# Get the user and repo for each WatchEvent, linking users with repos by their stars
star_events = star_events.map(
    lambda x: frozendict(
        {
            "user": x["actor"]["login"] if "actor" in x and "login" in x["actor"] else None,
            "repo": x["repo"]["name"] if "repo" in x and "name" in x["repo"] else None
        }
    )
)
star_events = star_events.filter(lambda x: x["user"] is not None and x["repo"] is not None)

own_events = own_events.filter(lambda x: "repo" in x and "name" in x["repo"] and "/" in x["repo"]["name"])
own_events = own_events.map(
    lambda x: frozendict(
        {
            "owner": x["repo"]["name"].split("/")[0] if "repo" in x and "name" in x["repo"] else None,
            "repo": x["repo"]["name"] if "repo" in x and "name" in x["repo"] else None
        }
    )
)
own_events = own_events.filter(lambda x: x["owner"] is not None and x["repo"] is not None)

#
# Serialize as JSON to disk: user-forked-repo and user-startted-repo links...
# as well as the repo and user entities themselves.
#

def json_serialize(obj):
    """Serialize objects as dicts instead of strings"""
    if isinstance(obj, frozendict):
        return dict(obj)

fork_events_lines = fork_events.map(lambda x: json.dumps(x, default=json_serialize))
fork_events_lines.saveAsTextFile("data/users_forked_repos.json")

star_events_lines = star_events.map(lambda x: json.dumps(x, default=json_serialize))
star_events_lines.saveAsTextFile("data/users_starred_repos.json")

own_events = own_events.distinct()
own_events_lines = own_events.map(lambda x: json.dumps(x, default=json_serialize))
own_events_lines.saveAsTextFile("data/users_owned_repos.json")

# We must get any repos appearing in either event type
fork_repos = fork_events.map(lambda x: frozendict({"repo": x["repo"]}))
own_repos  = star_events.map(lambda x: frozendict({"repo": x["repo"]}))
star_repos = star_events.map(lambda x: frozendict({"repo": x["repo"]}))
repos = sc.union([fork_repos, star_repos, own_repos])
repos = repos.distinct()
repos_lines = repos.map(lambda x: json.dumps(x, default=json_serialize))
repos_lines.saveAsTextFile("data/repos.json")

# We must get any users appearing in either event type
fork_users = fork_events.map(lambda x: frozendict({"user": x["user"]}))
own_users  =  own_events.map(lambda x: frozendict({"user": x["owner"]}))
star_users = star_events.map(lambda x: frozendict({"user": x["user"]}))
users = sc.union([fork_users, star_users, own_users])
users = users.distinct()
users_lines = users.map(lambda x: json.dumps(x, default=json_serialize))
users_lines.saveAsTextFile("data/users.json")
