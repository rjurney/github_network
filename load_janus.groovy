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

// Create edges between companies
edgesFilename = "../data/users_repos.jsonl"
edgesReader = new BufferedReader(new FileReader(edgesFilename));

while((json = edgesReader.readLine()) != null)
{
  document = jsonSlurper.parseText(json)

  // Add edges to graph
  user = g.V().has('userName', document.user).next()
  repo = g.V().has('repoName', document.repo).next()

  user.addEdge("forked", repo)
  graph.tx().commit()

  print("-")
}


g.V().hasLabel('user').count()
g.V().hasLabel('repo').count()
g.E().count()