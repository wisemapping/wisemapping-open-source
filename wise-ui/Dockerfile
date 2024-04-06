FROM node:18.12.1-buster-slim AS builder

# Set the working directory in the container
WORKDIR /app

ARG VERSION="6.0.1"

# Install dependencies
RUN mkdir webapp && npm pack @wisemapping/webapp@${VERSION} && tar -xvzf wisemapping-webapp-${VERSION}.tgz -C webapp

# Use Nginx as the production server
FROM nginx:latest
LABEL maintainer="Paulo Gustavo Veiga <pveiga@wisemapping.com>"

## Copy the built React app to Nginx's web server directory
COPY --from=builder /app/webapp/package/dist/* /usr/share/nginx/html/

ADD nginx.conf .
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80 for the Nginx server
EXPOSE 80

# Start Nginx when the container runs
CMD ["nginx", "-g", "daemon off;"]