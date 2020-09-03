###################################
## prod image
###################################

FROM nginx:1.18.0
# ENV http_proxy=***
# ENV https_proxy=***
COPY --from=jsp-angular-build /my-angular/dist/web-app /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
