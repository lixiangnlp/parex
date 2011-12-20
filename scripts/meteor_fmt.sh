#!/usr/bin/env bash

# Convert Parex paraphrase tables to Meteor format

perl -F'\s\|\|\|\s' -lane 'print "@F[2]\n@F[0]\n@F[1]"'
