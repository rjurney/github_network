# Github Network Experiments

In this project I'll be experimenting with Github data as a network, doing things like ranking projects in importance, applying novel methods to the Github network, etc.

## Setup

Setup is pretty simple, but uses multiple tools and a lot of data (for one machine).

### Downloading the Data

The dataset we use in this project is all the Github events for the year 2017. You could extend this backwards (or forwards) in time to expand the scope. This data is approximately 135GB. 

Run the following command in bash to download the data:

```bash
mkdir data
cd data
wget http://data.githubarchive.org/2017-{01..12}-{01..31}-{0..23}.json.gz
```

Mac OS X can have problems with wget. If you have trouble using the instructions here, check out those at the [Github Archive](https://www.githubarchive.org/). Some versions of bash (OS X) have trouble running this wget command.

### Extracting a Fork Network

We use PySpark to extract a network of (user)--forked-->(repo) to start with, or in other words a user/repository fork network. We will extract from this a user/user network and a repo/repo network, and work with these for different purposes.

Run the following command to run PySpark locally on your machine: 

```bash
spark-submit --master local[8] ./build_network.spark.py
```

Note that a non-pyspark, local script also exists. You may run it as follows, but it is ill suited to a year's worth of data:

```bash
gzcat data/2017*.json.gz | ./build_network.py
```

### Setting up the Graph DB

We're using JanusGraph instead of Titan as Titan is not actively developed. Run the following commands in the Gremlin shell. 

You can setup the database using [`setup_janus.groovy`](setup_janus.groovy). Next you can load the data using [`load_janus.groovy`](load_titan.groovy).

Now you're ready to query!

