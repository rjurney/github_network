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

// Inspect a top 20 degree centrality of fans
g.V().
  hasLabel('user').
  group().
  by('userName').
  as('owner').
  by(
    inE('fan').
    count()
  ).
  as('degree').
  select('owner','degree').
  order().
  by(
    select('degree'),
    decr
  ).
  limit(20)

// Inspect a top 20 eigenvector centrality of fans
g.V().
  hasLabel('user').
  repeat(
    groupCount('m').
    by('userName').
    out('fan').
    timeLimit(30000)
  ).
  times(5).
  cap('m').
  order(local).
  by(values, decr).
  limit(local, 20).
  next()

// Compute degree centrality of users via user-fan->user degree
// and write it to the 'degree' node property
count = 0

g.V().
  hasLabel('user').
  group().
  by().
  by(
    inE('fan').
    count()
  ).
  unfold().
  as('kv').
  select(keys).
  property(
    'fan_degree', 
    select('kv').
      select(values)
  ).
  choose(
    filter{it->count+=1; count%1000 == 0},
    __.map{it->println(count); g.tx().commit(); it.get()},
    __.identity()
  ).
  iterate()

println(count)

// Calculate eigenvector centrality on the user-fan->user edges
// and write it to the 'eigen' node property
count = 0

g.V().
  hasLabel('user').
  repeat(
    groupCount('m').
    by().
    out('fan').
    timeLimit(30000)
  ).
  times(5).
  cap('m').
  unfold().
  as('kv').
  select(keys).
  property(
    'fan_eigen',
    select('kv').
    select(values)
  ).
  choose(
    filter{it->count+=1; count%1000 == 0},
    __.map{it->println(count); g.tx().commit(); it.get()},
    __.identity()
  ).
  iterate()

println(count)
