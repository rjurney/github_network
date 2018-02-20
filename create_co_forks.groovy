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

// g.V().
//   hasLabel('repo').
//   as('repo1').
//   in('forked').
//   out('forked').
//   where(neq('repo1')).
//   as('repo2').
//   addE('co_forked').
//   to('repo1').
//   iterate()

//
// All in one go!
//
count = 0

g.V().
  hasLabel('repo').
  as('repo1').
  in('forked').
  out('forked').
  where(neq('repo1')).
  as('repo2').
  addE('co_forked').
  to('repo1').
  choose(
    filter{it->count+=1; count%1000 == 0},
    __.map{it->println(count); g.tx().commit(); it.get()},
    __.identity()
  )

//
// Manual iteration
//
edgePairs = g.V().
  hasLabel('repo').
  as('repo1').
  in('forked').
  out('forked').
  where(neq('repo1')).
  as('repo2').
  select('repo1', 'repo2')

i = 0
import java.text.NumberFormat
for(java.util.LinkedHashMap edgePair: edgePairs) {
  edgePair.repo1.addEdge("co_forked", edgePair.repo2)

  if((i % 1000) == 0) {
    graph.tx().commit()
    print("Committed edge number: " + NumberFormat.getIntegerInstance().format(i) + "\n")
  }
  
  i = i + 1
}

assert(g.E().hasLabel('co_forked').count() == )

//
// Eliminating super nodes first
//

// Limit enormous projects
edgePairs = g.V().
  hasLabel('repo').
  where(
    inE('forked').
    count().
    is(lt(100))
  ).
  as('repo1').
  in('forked').
  out('forked').
  where(neq('repo1')).
  as('repo2').
  select('repo1', 'repo2')

//
// User centrality
// 
g.V().
  hasLabel('repo').
  as('repo1').
  in('forked').
  where(
    outE('forked').
    count().
    is(lt(50))
  ).
  where(
    neq('repo1')
  ).
  as('repo2').
  select('repo1', 'repo2')

//
// Some analytical queries
//

// Highest degree of users and repos in project
g.V().hasLabel('user').group().by().by(outE().hasLabel('forked').count()).max()

// Largest group of users who co-forked a project
g.V().
  hasLabel('user').
  group().
  by().
  by(
    outE().
      hasLabel('forked').
      inV().
      select('repoName')
  ).
  as('repoName').
  count().
  as('count').
  select('repoName', 'count')

//
// Use SparkGraphComputer
//
:plugin use tinkerpop.hadoop
:plugin use tinkerpop.spark

// I edited the keyspace in this file to github_graph
olap_graph = GraphFactory.open("conf/hadoop-graph/read-cassandra.properties")

// Get a graph traverser
olap_g = olap_graph.traversal().withComputer(SparkGraphComputer)

// Test things out with a vertex count
// assert(olap_g.V().count().next() == 8139595)

// Add co-forked edges between nodes
edgePairs = olap_g.V().
  hasLabel('repo').
  as('repo1').
  in('forked').
  out('forked').
  //where(neq('repo1')).
  as('repo2')

// Setup our OLTP graph instance and OLTP traversal
oltp_graph = JanusGraphFactory.build().
  set("storage.backend", "cassandra").
  set("storage.hostname", "127.0.0.1").
  set("storage.cassandra.keyspace", "github_graph").
  set("storage.batch-loading", true).
  set("storage.buffer-size", 10000).
  open()
oltp_g = oltp_graph.traversal()

// Define the co_forked output file... filesystem caching!
olap_output_file = new File("data/co_forked.jsonl")
lineJson = JsonOutput.toJson()

for(edgePair : egdePairs) {
  repo1 = oltp_g.V().has('repoName', edgePair.repo1).next()
  repo2 = oltp_g.V().has('repoName', edgePair.repo2).next()

  // Out of heap space
  repo1.addEdge("co_forked", repo2)
  graph.tx().commit()

  // edgeJson = JsonOutput.toJson(egdePairs)
  // olap_output_file.write(edgeJson + "\n")

  print("-")
}

// Limit co-forking by centrality filters
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

g.V().


g.V().
  has(
    'user',
    'fan_eigen'
  ).
