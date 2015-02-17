FROM ubuntu:14.04.1
MAINTAINER Erik Pearson

RUN apt-get update \
	&& apt-get -y dist-upgrade \
	&& apt-get -y install openjdk-7-jdk

ADD /epitope-service /opt/epitope-service

EXPOSE 8080
EXPOSE 8081

WORKDIR /opt/epitope-service
CMD java -jar epitope-dropwizard-0.0.1-SNAPSHOT.jar server internal.yml

