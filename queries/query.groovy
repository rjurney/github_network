// Setup our database on top of cassandra/elasticsearch
graph = JanusGraphFactory.build().
  set("storage.backend", "cassandra").
  set("storage.hostname", "127.0.0.1").
  set("storage.cassandra.keyspace", "github_graph").
  set("index.search.backend", "elasticsearch").
  open()

// Get a graph traverser
g = graph.traversal()

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
      timeLimit(1000)).
    times(5).
  cap('m').
  unfold().as('kv').
  select(keys).
    property('eigen', select('kv').select(values))


// Degree centrality
olap_g.
  V().
  hasLabel('user').
  group().
  by().
  by(bothE().count()).
  label('count').
  order().
  by('count', decr)

// Degree centrality
g.
  V().
  group().
  by().
  by(bothE().count()).
  label('count').
  order().
  by('count', decr)