#!/bin/bash
set -e
REMOTE_USER=epearson
REMOTE_HOST=epearsonone
ALLELE_CODE_URL=https://bioinformatics.bethematchclinical.org/HLA/alpha.v3.zip
REMOTE_DIR=$(ssh $REMOTE_USER@$REMOTE_HOST "docker inspect epitope-service | jq \".[].Volumes[\\\"/var/lib/epitope-service\\\"]\"")
curl $ALLELE_CODE_URL | ssh $REMOTE_USER@$REMOTE_HOST "sudo sh -c \"cat - >$REMOTE_DIR/alpha.v3.zip\""
