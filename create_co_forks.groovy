// Setup our database on top of berkeleydb (for now)
graph = JanusGraphFactory.build()\
  .set("storage.backend", "berkeleyje")\
  .set("storage.directory", "data/fork_graph")\
  .set("storage.batch-loading", true)\
  .set("storage.buffer-size", 10000)\
  .open()

// Get a graph traverser
g = graph.traversal()

// Add co-forked edges between nodes
g.V().\
  hasLabel('repo').\
  store('x').\
  as('repo1').\
  in('forked').\
  out('forked').\
  where(without('x')).\
  as('repo2').\
  addE('co_forked').\
  to('repo1')

