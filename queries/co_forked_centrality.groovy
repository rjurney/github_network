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

// Get the top 20 by co_forked degree centrality
g.V().
  hasLabel('repo').
  group().
  by('repoName').
  as('repo').
  by(
    inE('co_forked').
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

// Get top 20 by co_forked eigenvector centrality
g.V().hasLabel('repo').
  repeat(
    groupCount('m').
      by('repoName').
      out('co_forked').
      timeLimit(600000)
  ).
  times(10).
  cap('m').
  order(local).
  by(values, decr).
  limit(local, 20).
  next()

// Write eigenvector centrality to graph property
count = 0

g.V().hasLabel('repo').
  repeat(
    groupCount('m').
      by().
      out('co_forked').
      timeLimit(1000)
  ).
  times(5).
  cap('m').
  unfold().
  as('kv').
  select(keys).
  property(
    'co_fork_eigen', 
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

// Betweeness Centrality
g.withSack(0).V().hasLabel('repo').store("x").repeat(both('co_forked').simplePath()).emit().path().
           group().by(project("a","b").by(limit(local, 1)).
                                       by(tail(local, 1))).
                   by(order().by(count(local))).
                   select(values).as("shortestPaths").
                   select("x").unfold().as("v").
                   select("shortestPaths").
                     map(unfold().filter(unfold().where(eq("v"))).count()).
                     sack(sum).sack().as("betweeness").
                   select("v","betweeness").
                   order().
                   by("betweeness", decr).
                   limit(20)

// Closeness Centrality
g.withSack(1f).V().hasLabel('repo').repeat(both('co_forked').simplePath()).emit().path().
           group().by(project("a","b").by(limit(local, 1)).
                                       by(tail(local, 1))).
                   by(order().by(count(local))).
           select(values).unfold().
           project("v","length").
             by(limit(local, 1)).
             by(count(local).sack(div).sack()).
           group().by(select("v")).by(select("length").sum()).
           order().
           by("")
