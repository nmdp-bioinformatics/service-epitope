#!/bin/sh
docker kill epitope-service
docker rm epitope-service
docker run --name epitope-service -d -p 8080:8080 -p 8081:8081 epitope-service:1.0-snapshot

