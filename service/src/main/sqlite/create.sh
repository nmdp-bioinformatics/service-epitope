#!/bin/sh
if [ "Z$1" = "Z" ] ; then echo "Usage $0: <database>"; exit 1; fi
./allele_group.sh $1
./race_freq.sh $1
./detail_race.sh $1
#./allele_code_map.sh $1
