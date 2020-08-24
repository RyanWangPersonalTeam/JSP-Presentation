FROM openjdk
#proxy configuration if necessary
ENV http_proxy=****
ENV https_proxy=****
# intall mvn
RUN yum install -y wget 
RUN wget https://mirrors.estointernet.in/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz
RUN tar -xvf apache-maven-3.6.3-bin.tar.gz
RUN mv apache-maven-3.6.3 /opt/
ENV M2_HOME="/opt/apache-maven-3.6.3"
ENV PATH="$M2_HOME/bin:$PATH"
RUN export PATH
RUN source etc/profile

RUN makdir my-service
WORKDIR /my-service
COPY . .
