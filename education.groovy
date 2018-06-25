// Setup our database on top of cassandra/elasticsearch
graph = JanusGraphFactory.build().
  set("storage.backend", "cassandra").
  set("storage.hostname", "127.0.0.1").
  set("storage.cassandra.keyspace", "github_graph").
  set("index.search.backend", "elasticsearch").
  open()

// Just sample some repos
jsonStr = JsonOutput.toJson(
  g.V()
    .has('repoName', 'rjurney/Agile_Data_Code_2')
    .as('repo1')
    .in('forked')
    .out('forked')
    .where(neq('repo1'))
    .values()
)

file = new File("data/repo_sample.json")
file.write(jsonStr)

