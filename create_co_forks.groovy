// Setup our database on top of cassandra/elasticsearch
graph = JanusGraphFactory.build().
  set("storage.backend", "cassandra").
  set("storage.hostname", "127.0.0.1").
  set("storage.cassandra.keyspace", "github_graph").
  set("storage.batch-loading", true).
  set("storage.buffer-size", 10000).
  set("index.search.backend", "elasticsearch").
  open()

g = graph.traversal()

// Compute co_forked edges
g.V().
  hasLabel('repo').
  as('repo1').
  in('forked').
  has('fan_eigen').
  where(
    outE('forked').
      count().
      is(
        lt(50)
      )
  ).
  out('forked').
  where(neq('repo1')).
  as('repo2').
  addE('co_forked').
  to('repo1').
  choose(
    filter{it->count+=1; count%1000 == 0},
    __.map{it->println(count); g.tx().commit(); it.get()},
    __.identity()
  ).
  iterate()
