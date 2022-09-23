# Helm Chart

## Install/Upgrade for local testing

```shell
helm upgrade --install managed-identity-wallets-local -f values.yaml -f values-local.yaml .
```

Pod for getting the Keycloak token:

```shell
kubectl run --rm -it client --image alpine

# Within the pod:
curl --location --request POST 'http://catenax-keycloak/realms/catenax/protocol/openid-connect/token' \
    --header 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode 'client_id=ManagedIdentityWallets' \
    --data-urlencode 'grant_type=client_credentials' \
    --data-urlencode 'client_secret=ManagedIdentityWallets-Secret' \
    --data-urlencode 'scope=openid'
```

## K8s-Namespace specific configs:

Values:

```yaml
acapy:
  databaseHost: "managed-identity-wallets-local-postgresql.<NAMESPACE>"
```
