#!/usr/bin/env bash

if [[ $# < 2 ]] ; then
  echo >&2 "limit resident and virtual memory";
  echo >&2 "usage: $0 memsizeGB cmd [args]";
  exit 1;
fi

MEM=$(($1*1024*1024))
ulimit -d $MEM -m $MEM -v $MEM
${@:2}

if [[ $? != 0 ]] ; then
  echo >&2 "Non-zero return code (likely due to memory limit) - exiting 1";
  exit 1;
fi
