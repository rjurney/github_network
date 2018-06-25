:plugin use tinkerpop.hadoop
:plugin use tinkerpop.spark

// I edited the keyspace in this file to github_graph
graph = GraphFactory.open('conf/hadoop-graph/read-cassandra-3.properties')

g = graph.traversal().withComputer(SparkGraphComputer)

a = g.V().count().next()
println(a)

