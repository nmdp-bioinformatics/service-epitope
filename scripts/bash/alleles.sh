#!/bin/sh

SERVICE_URL=http://epearsonone:8080/alleles/

#echo $SERVICE_URL

#curl -v \
curl  \
  -X POST \
  --data \@- \
  --header "Content-Type: application/json;charset=UTF-8" \
  --header "Accept: application/json;charset=UTF-8" \
  ${SERVICE_URL} \
  <<EOF 
{
  "alleles": [
    "HLA-DPB1*13:01", 
    "HLA-DPB1*105:01"
  ]
}
EOF

