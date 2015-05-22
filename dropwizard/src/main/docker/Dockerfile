FROM java:7
MAINTAINER Erik Pearson

RUN mkdir /opt/epitope-service

ADD ${project.build.finalName}.jar /opt/epitope-service/${project.build.finalName}.jar

RUN mkdir /var/opt/epitope-service \
    && cd /var/opt/epitope-service \
    && jar -xf /opt/epitope-service/${project.build.finalName}.jar internal.yml

VOLUME /var/opt/epitope-service

EXPOSE 8080
EXPOSE 8081

WORKDIR /var/opt/epitope-service
CMD java -jar /opt/epitope-service/${project.build.finalName}.jar server internal.yml

