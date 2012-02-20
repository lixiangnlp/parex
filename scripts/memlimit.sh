#!/usr/bin/env bash

if [[ $# < 2 ]] ; then
  echo "limit resident and virtual memory";
  echo "usage: $0 memsizeGB cmd [args]";
  exit 1;
fi

MEM=$(($1*1024*1024))
ulimit -d $MEM -m $MEM -v $MEM
${@:2}
