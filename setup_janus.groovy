graph = JanusGraphFactory.build().set("storage.backend", "berkeleyje").set("storage.directory", "data/graph").open();

g = graph.traversal()

// Setup our graph schema
mgmt = graph.openManagement()

// Vertex labels
user = mgmt.makeVertexLabel('user').make()
repo = mgmt.makeVertexLabel('repo').make()

// Node properties
userName = mgmt.makePropertyKey('userName').dataType(String.class).cardinality(Cardinality.SINGLE).make()
repoName = mgmt.makePropertyKey('repoName').dataType(String.class).cardinality(Cardinality.SINGLE).make()

// Indexes
mgmt.buildIndex('byUserNameUnique', Vertex.class).addKey(userName).unique().buildCompositeIndex()
mgmt.buildIndex('byRepoNameUnique', Vertex.class).addKey(repoName).unique().buildCompositeIndex()

// Relationships
forked = mgmt.makeEdgeLabel('forked').multiplicity(SIMPLE).make()

// Commit changes
mgmt.commit()
