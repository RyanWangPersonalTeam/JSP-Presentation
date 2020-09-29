
####################################################
## Create image for web-api
####################################################
FROM mcr.microsoft.com/dotnet/core/sdk:3.1
#proxy configuration if necessary
#ENV http_proxy=***
#ENV https_proxy=***
#certificate configuration 
ENV ASPNETCORE_URLS=https://+;http://+
ENV ASPNETCORE_Kestrel__Certificates__Default__Password="123456"
ENV ASPNETCORE_Kestrel__Certificates__Default__Path=/my-api/out/certificate/my-api.pfx
RUN mkdir my-api
WORKDIR /my-api
COPY . .
RUN apt-get update && apt-get install -y --no-install-recommends gss-ntlmssp
RUN dotnet restore
RUN dotnet publish -c Release -o out
WORKDIR /my-api/out
CMD dotnet WebAPI.dll --urls "http://0.0.0.0:5000;https://0.0.0.0:5001" 