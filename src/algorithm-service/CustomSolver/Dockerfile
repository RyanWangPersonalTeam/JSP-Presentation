FROM ubuntu:20.04
# proxy configuration if necessary
# ENV http_proxy=***
# ENV https_proxy=***
# install java
RUN apt-get update
RUN apt-get -y install default-jdk
RUN apt-get install -y wget 
# install maven
RUN wget https://mirrors.estointernet.in/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz
RUN tar -xvf apache-maven-3.6.3-bin.tar.gz
RUN mv apache-maven-3.6.3 /opt/
ENV M2_HOME="/opt/apache-maven-3.6.3"
ENV PATH="$M2_HOME/bin:$PATH"
RUN export PATH
RUN ["/bin/bash","-c", "source /etc/profile"]
RUN mkdir my-service
WORKDIR /my-service
COPY . .
# If need proxy, change proxy configuration in 'mavensetting.xml' then replace the maven setting.xml
RUN mv mavensetting.xml $M2_HOME/conf/settings.xml
# Download or-tools
RUN wget https://github.com/google/or-tools/releases/download/v7.8/or-tools_ubuntu-20.04_v7.8.7959.tar.gz --no-check-certificate
RUN tar -zxvf or-tools_ubuntu-20.04_v7.8.7959.tar.gz
RUN mv or-tools_Ubuntu-20.04-64bit_v7.8.7959 src/or-tools
RUN rm or-tools_ubuntu-20.04_v7.8.7959.tar.gz
# Or-tools' jar files are local dependencies, we need install them into local
RUN mvn install:install-file '-Dfile=./src/or-tools/lib/com.google.ortools.jar' '-DgroupId=com.google.ortools' '-DartifactId=ortools-java' '-Dversion=1.0.0'  '-Dpackaging=jar'
RUN mvn install:install-file '-Dfile=./src/or-tools/lib/protobuf.jar' '-DgroupId=com.google.protobuf' '-DartifactId=protobuf-java' '-Dversion=3.6.1'  '-Dpackaging=jar'
# Package 
RUN mvn compile
RUN mvn package
WORKDIR /my-service/target
# Let our java program works as service 
RUN mv ../jsp-algorithm.service /etc/systemd/system/jsp-algorithm.service
RUN mv ../startup /my-service/target/startup
RUN apt-get -y install systemctl
RUN apt-get install -y dos2unix
RUN dos2unix startup

# Run program in background
RUN systemctl daemon-reload
RUN systemctl enable jsp-algorithm.service
CMD systemctl start jsp-algorithm ; tail -f  /var/log/journal/jsp-algorithm.service.log


