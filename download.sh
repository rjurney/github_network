#!/usr/bin/env bash

mkdir data
cd data
wget http://data.githubarchive.org/2017-{01..12}-{01..31}-{0..23}.json.gz
cd ..
