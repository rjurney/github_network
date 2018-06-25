:plugin use tinkerpop.hadoop
:plugin use tinkerpop.spark

// I edited the keyspace in this file to github_graph
graph = GraphFactory.open('conf/hadoop-graph/read-cassandra-3.properties')

g = graph.traversal().withComputer(SparkGraphComputer)

a = g.V().count().next()
println(a)

g.V().
  hasLabel('repo').
  as('repo').
  repeat(
    inE('forked').
    outV().
    outE('forked').
    inV().
    where(
      neq('repo')
    ).
    groupCount('m').
    by('repoName')
  ).
  times(3).
  cap('m').
  order(local).
  by(values, decr).
  limit(local, 50).
  next()
  