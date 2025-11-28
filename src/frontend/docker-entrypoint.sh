#!/bin/sh
set -e

# Ensure BACKEND_URL has a default
: "${BACKEND_URL:=http://backend:8080}"
export BACKEND_URL
# Substitute env vars in the nginx config template
if [ -f /etc/nginx/conf.d/default.conf.template ]; then
  envsubst '$BACKEND_URL' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf
fi

# Execute the container command
exec "$@"
