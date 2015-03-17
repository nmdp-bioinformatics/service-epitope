#!/bin/sh

set -x

SERVICE_URL=http://epearsonone:8080/matches/

#echo $SERVICE_URL

#curl -v \
curl  \
  -X POST \
  --data \@- \
  --header "Content-Type: application/json;charset=UTF-8" \
  --header "Accept: application/json;charset=UTF-8" \
  ${SERVICE_URL} \
  <<EOF
[
  {
    "recipient": "01:01+01:01",
    "donor": "01:01+02:01"
  },
  {
    "recipient": "01:01+03:01",
    "donor": "01:01+02:01",
    "token": "result2"
  },
  {
    "recipient": "HLA-DPB1*09:01+HLA-DPB1*10:01",
    "recipientRace": "CAU",
    "donor": "HLA-DPB1*17:01+HLA-DPB1*17:01",
    "donorRace": "AFA"
  },
  {
    "recipient": "HLA-DPB1*03:FYKD+HLA-DPB1*14:001",
    "recipientRace": "CAU",
    "donor": "HLA-DPB1*17:01+HLA-DPB1*17:01",
    "donorRace": "AFA"
  }
]
EOF
