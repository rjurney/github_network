#!/usr/bin/env bash

mkdir -p data/github
cd data/github
wget http://data.githubarchive.org/2017-{01..12}-{01..31}-{0..23}.json.gz
wget http://data.githubarchive.org/2018-{01..12}-{01..31}-{0..23}.json.gz
wget http://data.githubarchive.org/2019-{01..12}-{01..31}-{0..23}.json.gz
wget http://data.githubarchive.org/2020-{01..12}-{01..31}-{0..23}.json.gz
cd ../..
