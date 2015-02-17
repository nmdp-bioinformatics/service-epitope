#!/bin/bash
set -ex
MULTIMODULE_DIR=`cd .. >/dev/null; pwd`
TARGET_DIR=`pwd`/epitope-service
rm -rf $TARGET_DIR
mkdir $TARGET_DIR
cp $MULTIMODULE_DIR/dropwizard/target/epitope-dropwizard-*.jar $TARGET_DIR
(
  cd $TARGET_DIR
  jar -xf epitope-dropwizard-*.jar internal.yml
  cat internal.yml | sed -e "s/jdbc:sqlite::resource:/jdbc:sqlite:/" >internal.yml.new
  mv internal.yml.new internal.yml
)
(
  cd $MULTIMODULE_DIR/service/src/main/sqlite
  ./create.sh $TARGET_DIR/epitope-service.db
)
#docker build -t epitope-service:1.0-snapshot .
