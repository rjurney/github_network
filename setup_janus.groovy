// Setup our database on top of berkeleydb (for now)
graph = JanusGraphFactory.build()\
  .set("storage.backend", "berkeleyje")\
  .set("storage.directory", "data/fork_graph")\
  .open();

g = graph.traversal()

// Setup our graph schema
mgmt = graph.openManagement()

// Vertex labels
user = mgmt.makeVertexLabel('user').make()
repo = mgmt.makeVertexLabel('repo').make()

// Identifier node properties
userName = mgmt.makePropertyKey('userName').dataType(String.class).cardinality(Cardinality.SINGLE).make()
repoName = mgmt.makePropertyKey('repoName').dataType(String.class).cardinality(Cardinality.SINGLE).make()

// Metric node properties
degreeCentrality = mgmt.makePropertyKey('degree').dataType(Integer.class).make()
eigenvectorCentrality = mgmt.makePropertyKey('eigen').dataType(Integer.class).make()
stars = mgmt.makePropertyKey('stars').dataType(Integer.class).make()

// Indexes
mgmt.buildIndex('byUserNameUnique', Vertex.class).addKey(userName).unique().buildCompositeIndex()
mgmt.buildIndex('byRepoNameUnique', Vertex.class).addKey(repoName).unique().buildCompositeIndex()

// Relationships
forked = mgmt.makeEdgeLabel('forked').multiplicity(SIMPLE).make()
co_forked = mgmt.makeEdgeLabel('co_forked').multiplicity(SIMPLE).make()
starred = mgmt.makeEdgeLabel('starred').multiplicity(SIMPLE).make()

// Commit changes
mgmt.commit()
