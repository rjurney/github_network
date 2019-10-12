// Libraries.io Graph
graph = JanusGraphFactory.build().
  set("storage.backend", "cassandra").
  set("storage.hostname", "127.0.0.1").
  set("storage.cassandra.keyspace", "libraries_graph").
  set("storage.batch-loading", true).
  set("storage.buffer-size", 10000).
  set("index.search.backend", "elasticsearch").
  open()

g = graph.traversal()

// Setup our graph schema
mgmt = graph.openManagement()

// Vertex labels
// user = mgmt.makeVertexLabel('user').make()
// repo = mgmt.makeVertexLabel('repo').make()
project = mgmt.makeVertexLabel('project').make()

// |ID    |Platform|Name                                           |Created Timestamp      |Updated Timestamp      |Description                                                                                                                             |Keywords|Homepage URL                                              |Licenses  |Repository URL                                                                                                       |Versions Count|SourceRank|Latest Release Publish Timestamp|Latest Release Number|Package Manager ID|Dependent Projects Count|Language|Status|Last synced Timestamp  |Dependent Repositories Count|Repository ID|Repository Host Type|Repository Name with Owner        |Repository Description                                                                                                                                                                                                                                                                                                                                                                                  |Repository Fork?|Repository Created Timestamp|Repository Updated Timestamp|Repository Last pushed Timestamp|Repository Homepage URL     |Repository Size|Repository Stars Count|Repository Language|Repository Issues enabled?|Repository Wiki enabled?|Repository Pages enabled?|Repository Forks Count|Repository Mirror URL|Repository Open Issues Count|Repository Default branch|Repository Watchers Count|Repository UUID                       |Repository Fork Source Name with Owner|Repository License|Repository Contributors Count|Repository Readme filename|Repository Changelog filename|Repository Contributing guidelines filename|Repository License filename|Repository Code of Conduct filename|Repository Security Threat Model filename|Repository Security Audit filename|Repository Status|Repository Last Synced Timestamp|Repository SourceRank|Repository Display Name|Repository SCM type|Repository Pull requests enabled?|Repository Logo URL|Repository Keywords|

// Identifier node properties
id = mgmt.makePropertyKey('ID').dataType(Integer.class).cardinality(Cardinality.SINGLE).make()
name = mgmt.makePropertyKey('name').dataType(String.class).cardinality(Cardinality.SINGLE).make()
// userName = mgmt.makePropertyKey('userName').dataType(String.class).cardinality(Cardinality.SINGLE).make()
// repoName = mgmt.makePropertyKey('repoName').dataType(String.class).cardinality(Cardinality.SINGLE).make()
// projectName = mgmt.makePropertyKey('projectName').dataType(String.class).cardinality(Cardinality.SINGLE).make()

// Indexes
mgmt.buildIndex('byNameUnique', Vertex.class).addKey(name).unique().buildCompositeIndex()
// mgmt.buildIndex('byUserNameUnique', Vertex.class).addKey(userName).unique().buildCompositeIndex()
// mgmt.buildIndex('byRepoNameUnique', Vertex.class).addKey(repoName).unique().buildCompositeIndex()
// mgmt.buildIndex('byProjectNameUnique', Vertex.class).addKey(projectName).unique().buildCompositeIndex()

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
depends = mgmt.makeEdgeLabel('depends').multiplicity(SIMPLE).make()

// Commit changes
mgmt.commit()

// Close graph
graph.close()
