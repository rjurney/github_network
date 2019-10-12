import sys, os, json
from frozendict import frozendict
from pyspark.sql.functions import col

# If there is no SparkSession, create the environment
try:
    sc and spark
except NameError as e:
    import findspark
    
    findspark.init()
    import pyspark
    import pyspark.sql
    
    sc = pyspark.SparkContext()
    spark = pyspark.sql.SparkSession(sc).builder.appName("Extract Network").getOrCreate()

#
# Load the data...
#
# Load the CSV from Libraries.io archive for 3/2018
repository_dependencies = spark.read.csv('data/repository_dependencies-1.2.0-2018-03-12.csv', header=True, inferSchema=True)
projects = spark.read.csv('data/projects_with_repository_fields-1.2.0-2018-03-12.csv', header=True, inferSchema=True)
repositories = spark.read.csv('data/repositories-1.2.0-2018-03-12.csv', header=True, inferSchema=True)
dependencies = spark.read.csv('data/dependencies-1.2.0-2018-03-12.csv', header=True, inferSchema=True)

from pyspark.sql.functions import col, split
maven_dependencies = dependencies.filter(dependencies['Dependency Platform'] == 'Maven')
project_names = maven_dependencies.select(col('Project Name').alias('Name'))
dep_names = maven_dependencies.select(col('Dependency Name').alias('Name'))
total_names = project_names.union(dep_names)
total_projects = total_names.withColumn('Project', split(total_names['Name'], ':')[0])
final_names = total_projects.distinct()
final_names.write.format('json').save('data/maven_projects.json')

# Write dependencies as JSON
maven_dependencies = maven_dependencies.withColumn('Project', split(dependencies['Project Name'], ':')[0])
maven_dependencies = maven_dependencies.withColumn('Dependency', split(dependencies['Dependency Name'], ':')[0])
maven_dependencies = maven_dependencies.select('Project', 'Dependency')
unique_maven_depencenies = maven_dependencies.distinct()

unique_maven_depencenies.write.format('json').save('data/maven_dependencies.json')


from pyspark.sql.functions import col, split
python_dependencies = dependencies.filter(dependencies['Dependency Platform'] == 'Pypi')

project_names = python_dependencies.select(col('Project Name').alias('Name'))
dep_names = python_dependencies.select(col('Dependency Name').alias('Name'))
total_names = project_names.union(dep_names)
final_names = total_names.distinct()
final_names.write.format('json').save('data/pypi_projects.json')

# Write dependencies as JSON
python_dependencies = python_dependencies.select(col('Project Name').alias('Project'), col('Dependency Name').alias('Dependency'))
unique_python_depencenies = python_dependencies.distinct()

unique_python_depencenies.write.format('json').save('data/pypi_dependencies.json')

#

# #
# # Write the non-fork Repos to JSON
# #
# # Write non-forked repos to json
non_null_repos = repositories.filter(repositories['Fork'] == False)
non_fork_projects = projects.filter(projects['Repository Fork?'] == False)
# non_null_repos.write.format('json').save('data/non_fork_repositories.jsonl')

#
# Filter repo dependencies to those that aren't themselves forks
#
# Alias to distinguish between common fields after the join
project_deps = repository_dependencies.alias('project_deps')
projects = non_fork_projects.alias('projects')

# Join repository dependencies with non fork repositories to filter repo_deps by FROM[Fork] == False
deps_with_projects = project_deps.join(
  projects,
  col('project_deps.ID') == col('projects.ID')
)
deps_with_projects.count() # 

# Verify everything looks right
deps_with_projects.select(
  'project_deps.ID', 
  'project_deps.Repository Name with Owner',
  'projects.ID', 
  'projects.Name', 
).show(20, False)

# Join the other side of repo_deps with non_null repos to filter repo_deps by TO[Fork] == False
final_deps_projects = deps_with_projects.join(
  non_null_repos,
  col('repo_deps.Dependency Project ID') == col('repos.ID')
)
final_deps_reops.select(
  'repos.ID',
  'repos.Name',
  'repo_deps.Dependency Project ID',
  'repo_deps.Dependency Name'
).show(20, False)

# Restore original repository_dependencies columns
raw_repo_deps = non_fork_deps.select(
  ['repo_deps.{}'.format(name) for name in repo_deps.columns]
)
raw_repo_deps.show(20, False)

# Write deps to json
repo_deps.write.format('json').save('data/repository_dependencies.jsonl')

#
# Test...
#
other_deps_with_repos = repo_deps.join(
  repos, 
  repo_deps['repo_deps.Dependency Project ID'] == repos['repos.ID']
)
