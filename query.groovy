// Setup our database on top of berkeleydb (for now)
graph = JanusGraphFactory.build()\
  .set("storage.backend", "berkeleyje")\
  .set("storage.directory", "data/fork_graph")\
  .set("storage.batch-loading", true)\
  .set("storage.buffer-size", 10000)\
  .open();

// Get a graph traverser
g = graph.traversal()

// Get a network of users from the network of user -> repos
repoRepoJson = new groovy.json.JsonBuilder(g.V().hasLabel('repo').as('repo1').in('forked').out('forked').where(neq('repo1')).as('repo2').select('repo1','repo2').by('projectName').dedup()).toPrettyString()

/* g.V()\
  .hasLabel('repo')\
  .as('repo1')\
  .in('forked')\
  .out('forked')\
  .where(neq('repo1'))\
  .as('repo2')\
  .select('repo1','repo2')\
  .by('projectName')\
  .dedup(); */

// Write out to a JSON File
repoRepoFile = new File("data/repo-repo.jsonl")
repoRepoFile.write(repoRepoJson)
repoRepoFile.close()

