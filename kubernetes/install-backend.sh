#!/bin/bash

kubectl apply -f namespace.yaml
kubectl config set-context --current --namespace easy-money

helm repo add bitnami https://charts.bitnami.com/bitnami

helm repo update

helm install postgresql -f postgres/values.yaml bitnami/postgresql
helm install kafka -f kafka/values.yaml bitnami/kafka
helm install minio -f minio/values.yaml bitnami/minio
helm install backend-application ./backend-application
