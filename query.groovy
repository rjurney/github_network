// Setup our database on top of berkeleydb (for now)
graph = JanusGraphFactory.build()\
  .set("storage.backend", "berkeleyje")\
  .set("storage.directory", "data/fork_graph")\
  .set("storage.batch-loading", true)\
  .set("storage.buffer-size", 1000)\
  .open();

// Get a graph traverser
g = graph.traversal()

// Add co-forked edges between nodes
g.V().
  hasLabel('repo').
  store('x').
  as('repo1').
  in('forked').
  out('forked').
  where(without('x')).
  as('repo2').
  addE('co_forked').to('repo1')

// Degree centrality of co_forked edges - never returns
g.V().hasLabel('repo').group().by().by(bothE('co_forked').count())
g.V().hasLabel('repo').group().by().by(bothE('co_forked').count()).order(local).by(values, decr).limit(local, 20).next()

// Eigenvector centrality
g.V().
  hasLabel('repo').
  repeat(
    groupCount('m').
    by('repoName').
    out('co_forked').
    timeLimit(10000)
  ).
  times(5).
  cap('m').
  order(local).
  by(values, decr).
  limit(local, 10).
  next()

// Write eigenvector centrality to graph property 
g.V().hasLabel('repo').
  repeat(
      groupCount('m').
        by().
      out('co_forked').
      timeLimit(10000)).
    times(5).
  cap('m').
  unfold().as('kv').
  select(keys).
    property('eigenvectorCentrality', select('kv').select(values))

