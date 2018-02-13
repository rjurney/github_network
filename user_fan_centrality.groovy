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

// Compute degree centrality of users via user-fan->user degree
// and write it to the 'degree' node property
g.V().
  hasLabel('user').
  group().
  by().
  by(
    inE().
    count()
  ).
  unfold().
  as('kv').
  select(keys).
  property(
    'degree', 
    select('kv').
    select(values)
  )

// Calculate eigenvector centrality on the user-fan->user edges
// and write it to the 'eigen' node property
g.V().
  hasLabel('user').
  repeat(
    groupCount('m').
    by().
    out('fan').
    timeLimit(10000)
  ).
  times(5).
  cap('m').
  unfold().
  as('kv').
  select(keys).
  property(
    'eigen',
    select('kv').
    select(values)
  )

