// Setup our database on top of berkeleydb (for now)
conf = new BaseConfiguration()
conf.setProperty("storage.directory", "/Users/rjurney/Software/github_network/data")
conf.setProperty("storage.backend", "berkeleyje")
graph = TitanFactory.open(conf)

// Setup JSON Reading of MongoDB mongodump data
jsonSlurper = new JsonSlurper()

// Add user nodes to graph
users_filename = "/Users/rjurney/Software/github_network/data/users.jsonl"
users_reader = new BufferedReader(new FileReader(users_filename));

while((json = users_reader.readLine()) != null)
{
  document = jsonSlurper.parseText(json)

  println(document.user)
  v = graph.addVertex('user')
  v.property("userName", document.user)
}

// Add repo nodes to graph
repos_filename = "/Users/rjurney/Software/github_network/data/repos.jsonl"
repos_reader = new BufferedReader(new FileReader(repos_filename))

while((json = repos_reader.readLine()) != null)
{
  document = jsonSlurper.parseText(json)

  println(document.repo)
  v = graph.addVertex('repo')
  v.property("repoName", document.repo)
}

// Get a graph traverser
g = graph.traversal()

// Create edges between companies
edges_filename = "/Users/rjurney/Software/github_network/data/users_repos.jsonl"
edges_reader = new BufferedReader(new FileReader(edges_filename));

while((json = edges_reader.readLine()) != null)
{
  document = jsonSlurper.parseText(json)

  // Add edges to graph
  user = g.V().has('userName', document.user).next()
  repo = g.V().has('repoName', document.repo).next()

  user.addEdge("forked", repo, 'fork_count', document.fork_count)
}
