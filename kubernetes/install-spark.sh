#!/bin/bash

kubectl apply -f namespace.yaml
kubectl config set-context --current --namespace easy-money

kubectl apply -f storage-secret.yaml
kubectl apply spark/spark-config-env.yaml

helm repo add bitnami https://charts.bitnami.com/bitnami

helm repo update

helm install spark -f spark/values.yaml bitnami/spark
