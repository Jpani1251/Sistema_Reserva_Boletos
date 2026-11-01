#!/bin/sh
set -e

# Extract local DNS resolver from /etc/resolv.conf (Azure or Docker)
DNS_RESOLVER=$(awk 'BEGIN{ORS=" "} $1=="nameserver" {if ($2 ~ ":") {print "["$2"]"} else {print $2}}' /etc/resolv.conf)
DNS_RESOLVER="${DNS_RESOLVER% }"
if [ -n "$DNS_RESOLVER" ]; then
    sed -i "s|resolver 127.0.0.11|resolver $DNS_RESOLVER|g" /etc/nginx/conf.d/default.conf
fi

# Backend hosts
MS_USUARIOS_HOST=${MS_USUARIOS_HOST:-"http://ms-usuarios:8081"}
MS_LUGARES_HOST=${MS_LUGARES_HOST:-"http://ms-lugares:8082"}
MS_EVENTOS_HOST=${MS_EVENTOS_HOST:-"http://ms-eventos:8083"}
MS_COMPRAS_HOST=${MS_COMPRAS_HOST:-"http://ms-compras:8084"}

case "$MS_USUARIOS_HOST" in http://*|https://*) ;; *) MS_USUARIOS_HOST="http://$MS_USUARIOS_HOST" ;; esac
case "$MS_LUGARES_HOST" in http://*|https://*) ;; *) MS_LUGARES_HOST="http://$MS_LUGARES_HOST" ;; esac
case "$MS_EVENTOS_HOST" in http://*|https://*) ;; *) MS_EVENTOS_HOST="http://$MS_EVENTOS_HOST" ;; esac
case "$MS_COMPRAS_HOST" in http://*|https://*) ;; *) MS_COMPRAS_HOST="http://$MS_COMPRAS_HOST" ;; esac

sed -i "s|\"http://ms-usuarios:8081\"|\"$MS_USUARIOS_HOST\"|g" /etc/nginx/conf.d/default.conf
sed -i "s|\"http://ms-lugares:8082\"|\"$MS_LUGARES_HOST\"|g" /etc/nginx/conf.d/default.conf
sed -i "s|\"http://ms-eventos:8083\"|\"$MS_EVENTOS_HOST\"|g" /etc/nginx/conf.d/default.conf
sed -i "s|\"http://ms-compras:8084\"|\"$MS_COMPRAS_HOST\"|g" /etc/nginx/conf.d/default.conf

exec nginx -g "daemon off;"
