conf = new BaseConfiguration()
conf.setProperty("storage.directory", "/Users/rjurney/Software/github_network/data")
conf.setProperty("storage.backend", "berkeleyje")
graph = TitanFactory.open(conf)

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
