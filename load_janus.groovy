// Setup our database on top of berkeleydb (for now)
graph = JanusGraphFactory.build()\
  .set("storage.backend", "berkeleyje")\
  .set("storage.directory", "data/fork_graph")\
  .set("storage.batch-loading", true)\
  .set("storage.buffer-size", 10000)\
  .open();

// Setup JSON Reading of MongoDB mongodump data
jsonSlurper = new JsonSlurper()

// Add user nodes to graph
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

// Get a graph traverser
g = graph.traversal()

// Create forked edges between repos and nodes
forkEdgesFilename = "../data/users_forked_repos.jsonl"
forkEdgesReader = new BufferedReader(new FileReader(forkEdgesFilename));

while((json = forkEdgesReader.readLine()) != null)
{
  document = jsonSlurper.parseText(json)

  // Add edges to graph
  user = g.V().has('userName', document.user).next()
  repo = g.V().has('repoName', document.repo).next()

  user.addEdge("forked", repo)
  graph.tx().commit()

  print("-")
}

// Create starred edges between repos and nodes
starEdgesFilename = "../data/users_starred_repos.jsonl"
starEdgesReader = new BufferedReader(new FileReader(starEdgesFilename));

while((json = starEdgesReader.readLine()) != null)
{
  document = jsonSlurper.parseText(json)

  // Add edges to graph
  user = g.V().has('userName', document.user).next()
  repo = g.V().has('repoName', document.repo).next()

  user.addEdge("starred", repo)
  graph.tx().commit()

  print("-")
}

// Setup sessions so we can remember variables
:remote connect tinkerpop.server conf/remote.yaml session

userCount = g.V().hasLabel('user').count().next()
assert(userCount == 4067599)

repoCount = g.V().hasLabel('repo').count().next()
assert(repoCount == 4071996)

forkedCount = g.E().hasLabel('forked').count().next()
assert(forkedCount == 11366334)

starredCount = g.E().hasLabel('starred').count().next()
assert(starredCount == 31870088)
