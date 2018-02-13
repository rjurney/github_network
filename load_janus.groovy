// Setup our database on top of cassandra/elasticsearch
graph = JanusGraphFactory.build().
  set("storage.backend", "cassandra").
  set("storage.hostname", "127.0.0.1").
  set("storage.cassandra.keyspace", "github_graph").
  set("storage.batch-loading", true).
  set("storage.buffer-size", 10000).
  set("index.search.backend", "elasticsearch").
  open()

// Get a graph traverser
g = graph.traversal()

// Setup JSON Reading of MongoDB mongodump data
jsonSlurper = new JsonSlurper()

// Add user nodes to graph
// usersFilename = "../github_network/data/users.jsonl"
usersFilename = "../data/users.jsonl"
usersReader = new BufferedReader(new FileReader(usersFilename));

while((json = usersReader.readLine()) != null)
{
  document = jsonSlurper.parseText(json)

  v = graph.addVertex('user')
  v.property("userName", document.user)
  graph.tx().commit()

  print("U")
}

// Add repo nodes to graph
// reposFilename = "../github_network/data/repos.jsonl"
reposFilename = "../data/repos.jsonl"
reposReader = new BufferedReader(new FileReader(reposFilename))

while((json = reposReader.readLine()) != null)
{
  document = jsonSlurper.parseText(json)

  v = graph.addVertex('repo')
  v.property("repoName", document.repo)
  graph.tx().commit()

  print("R")
}

// Create forked edges between users and repos
// forkEdgesFilename = "../github_network/data/users_forked_repos.jsonl"
forkEdgesFilename = "../data/users_forked_repos.jsonl"
forkEdgesReader = new BufferedReader(new FileReader(forkEdgesFilename));

i = 0
while((json = forkEdgesReader.readLine()) != null)
{
  document = jsonSlurper.parseText(json)

  // Fetch the user and repo
  user = g.V().has('userName', document.user).next()
  repo = g.V().has('repoName', document.repo).next()

  user.addEdge("forked", repo)
  graph.tx().commit()

  if((i % 1000) == 0) {
    print("On edge number: " + i + "\n")
  }
  i = i + 1
}

// Create starred edges between users and repos
// starEdgesFilename = "../github_network/data/users_starred_repos.jsonl"
starEdgesFilename = "../data/users_starred_repos.jsonl"
starEdgesReader = new BufferedReader(new FileReader(starEdgesFilename));

i = 0
while((json = starEdgesReader.readLine()) != null)
{
  document = jsonSlurper.parseText(json)

  // Fetch the user and repo
  user = g.V().has('userName', document.user).next()
  repo = g.V().has('repoName', document.repo).next()

  user.addEdge("starred", repo)
  graph.tx().commit()

  if((i % 1000) == 0) {
    print("On edge number: " + i + "\n")
  }
  i = i + 1
}

// Create fan edges between users and other users
// ownEdgesFilename = "../github_network/data/users_own_repos.jsonl"
ownEdgesFilename = "../data/users_own_repos.jsonl"
ownEdgesReader = new BufferedReader(new FileReader(ownEdgesFilename));

i = 0
while((json = ownEdgesReader.readLine()) != null)
{
  document = jsonSlurper.parseText(json)

  // Fetch the user and repo
  user = g.V().has('userName', document.user).next()
  repo = g.V().has('repoName', document.repo).next()

  user.addEdge("owned", repo)
  graph.tx().commit()

  if((i % 1000) == 0) {
    print("On edge number: " + i + "\n")
  }
  i = i + 1
}

// Setup sessions so we can remember variables
:remote connect tinkerpop.server conf/remote.yaml session

userCount = g.V().hasLabel('user').count().next()
print(userCount)
assert(userCount == 4067599)

repoCount = g.V().hasLabel('repo').count().next()
print(repoCount)
assert(repoCount == 4071996)

forkedCount = g.E().hasLabel('forked').count().next()
print(forkedCount)
assert(forkedCount == 11366334)

starredCount = g.E().hasLabel('starred').count().next()
print(starredCount)
assert(starredCount == 31870088)
