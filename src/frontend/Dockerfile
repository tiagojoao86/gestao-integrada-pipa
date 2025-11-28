# Frontend multi-stage Dockerfile
# Stage 1: build with node
FROM node:18-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
COPY . ./
RUN npm ci --silent && npm run build -- --configuration production

# Stage 2: serve with nginx
FROM nginx:stable-alpine
# install envsubst for runtime variable substitution
RUN apk add --no-cache gettext
# Copy built files; Angular may output into a subfolder like 'browser' -> copy its contents to nginx root
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html
# fallback: if browser folder missing, copy dist directly
COPY --from=builder /app/dist/frontend /usr/share/nginx/html
# copy template nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf.template
# entrypoint that substitutes env vars into the nginx config
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
EXPOSE 80
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
