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

// Create the user-forks->user edges, printing a counter/committing transactions every 1,000 records
count = 0

g.V().
  hasLabel('user').
  as('user1').
  out('owned').
  in('forked').
  as('user2').
  addE('fan').
  to('user1').
  choose(
      filter{it->count+=1; count%1000 == 0},
      __.map{it->println(count); g.tx().commit(); it.get()},
      __.identity()
  ).
  iterate()

println(count)

// Verify the count is correct
fanCount = g.E().
  hasLabel('fan').
  count().
  next()
assert(fanCount == 9451177)
