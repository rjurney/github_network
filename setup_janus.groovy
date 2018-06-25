// Setup our database on top of cassandra/elasticsearch
graph = JanusGraphFactory.build().
  set("storage.backend", "cassandra").
  set("storage.hostname", "127.0.0.1").
  set("storage.cassandra.keyspace", "github_graph").
  set("index.search.backend", "elasticsearch").
  open()

g = graph.traversal()

// Setup our graph schema
mgmt = graph.openManagement()

// Vertex labels
user = mgmt.makeVertexLabel('user').make()
repo = mgmt.makeVertexLabel('repo').make()

// Identifier node properties
name = mgmt.makePropertyKey('name').dataType(String.class).cardinality(Cardinality.SINGLE).make()
userName = mgmt.makePropertyKey('userName').dataType(String.class).cardinality(Cardinality.SINGLE).make()
repoName = mgmt.makePropertyKey('repoName').dataType(String.class).cardinality(Cardinality.SINGLE).make()

// Indexes
mgmt.buildIndex('byNameUnique', Vertex.class).addKey(name).unique().buildCompositeIndex()
mgmt.buildIndex('byUserNameUnique', Vertex.class).addKey(userName).unique().buildCompositeIndex()
mgmt.buildIndex('byRepoNameUnique', Vertex.class).addKey(repoName).unique().buildCompositeIndex()

// Metric node properties
fanDegreeCentrality = mgmt.makePropertyKey('fan_degree').dataType(Integer.class).make()
fanEigenvectorCentrality = mgmt.makePropertyKey('fan_eigen').dataType(Integer.class).make()
coforkEigenvectorCentrality = mgmt.makePropertyKey('co_fork_eigen').dataType(Integer.class).make()
stars = mgmt.makePropertyKey('stars').dataType(Integer.class).make()

// Relationships
forked = mgmt.makeEdgeLabel('forked').multiplicity(SIMPLE).make()
co_forked = mgmt.makeEdgeLabel('co_forked').multiplicity(SIMPLE).make()
co_forked_two = mgmt.makeEdgeLabel('co_forked_2').multiplicity(SIMPLE).make()
starred = mgmt.makeEdgeLabel('starred').multiplicity(SIMPLE).make()
owned = mgmt.makeEdgeLabel('owned').multiplicity(SIMPLE).make()
fan = mgmt.makeEdgeLabel('fan').multiplicity(SIMPLE).make()

// Commit changes
mgmt.commit()

// Close graph
graph.close()
