::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Build all images
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
@echo off
rem If services are on, turn down
docker-compose down
rem Delete stopped containers
FOR /F %%A IN ('docker ps -a -q') DO docker rm %%A
rem Delete useless images
FOR /F %%A IN ('docker images -qa -f "dangling=true"') DO docker rmi %%A
rem Create image of web api
cd ./WebAPI/WebAPI
docker build -t jsp-api --label jsp-api .
cd ../../
rem Create image of angular app
cd ./web-app
docker build -f ./base.Dockerfile -t jsp-angular-build --label jsp-angular-build .
docker build -f ./prod.Dockerfile -t jsp-angular-prod --label jsp-angular-prod .
cd ../
rem Create image of java program
cd ./algorithm-service/CustomSolver
docker build -t jsp-al-service --label jsp-al-service .
cd ../../

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Start up all services
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
docker-compose up -d --remove-orphans

echo Install completely, open http://localhost:9902/ to test