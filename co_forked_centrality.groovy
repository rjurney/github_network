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

// Write eigenvector centrality to graph property 
g.V().hasLabel('repo').
  repeat(
      groupCount('m').
        by().
      out('co_forked').
      timeLimit(1000)).
    times(5).
  cap('m').
  unfold().as('kv').
  select(keys).
    property('eigen', select('kv').select(values))
