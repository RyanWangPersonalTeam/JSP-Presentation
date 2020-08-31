
###################################
## base image
###################################
FROM node:12.16.1 
#proxy configuration if necessary
ENV http_proxy=***
ENV https_proxy=***
RUN mkdir my-angular
WORKDIR /my-angular

ENV PATH /my-angular/node_modules/.bin:$PATH

COPY package.json /my-angular/package.json
RUN npm install
RUN npm install -g @angular/cli@8.3.25

COPY . /my-angular

RUN ng build --prod
