kubectl apply -f namespace.yaml
kubectl config set-context --current --namespace easy-money

helm repo add bitnami https://charts.bitnami.com/bitnami

helm repo update

kubectl apply -f postgres/postgres-secret.yaml
helm install postgres -f postgres/values.yaml bitnami/postgresql

helm install kafka -f kafka/values.yaml bitnami/kafka

helm install spark -f spark/values.yaml bitnami/spark

