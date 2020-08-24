###################################
## prod image
###################################
FROM nginx:1.18.0
COPY --from=my-angular-build /my-angular/dist/web-app /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]