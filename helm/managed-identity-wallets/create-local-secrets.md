For the Creation of Local secrets, just run the following commands:

Secret Name: catenax-managed-identity-wallets-secrets

TODO Ändern des hosts in Localhost
```bash
kubectl -n managed-identity-wallets create secret generic catenax-managed-identity-wallets-secrets \
  --from-literal=cx-db-jdbc-url='jdbc:postgresql://managed-identity-wallets-local-postgresql:5432/postgres?user=postgres&password=cx-password' \
  --from-literal=cx-auth-client-id='Custodian' \
  --from-literal=bpdm-auth-client-id='testID' \
  --from-literal=bpdm-auth-client-secret='testSecret' \
  --from-literal=cx-auth-client-secret='Custodian-Secret'
```
OLD
```bash
kubectl -n managed-identity-wallets create secret generic catenax-managed-identity-wallets-secrets \
  --from-literal=cx-db-jdbc-url='jdbc:postgresql:localhost//:5432/miwdev?user=miwdevuser&password=^cXnF61qM1kf' \
  --from-literal=cx-auth-client-id='Custodian' \
  --from-literal=bpdm-auth-client-id='testID' \
  --from-literal=bpdm-auth-client-secret='testSecret' \
  --from-literal=cx-auth-client-secret='Custodian-Secret'
```

Secret Name: catenax-managed-identity-wallets-acapy-secrets
```bash
kubectl -n managed-identity-wallets create secret generic catenax-managed-identity-wallets-acapy-secrets \
  --from-literal=acapy-wallet-key='issuerKeySecret19' \
  --from-literal=acapy-agent-wallet-seed='00000000000000000000000111111119' \
  --from-literal=acapy-jwt-secret='jwtSecret19' \
  --from-literal=acapy-db-account='postgres' \
  --from-literal=acapy-db-password='cx-password' \
  --from-literal=acapy-db-admin='postgres' \
  --from-literal=acapy-db-admin-password='cx-password' \
  --from-literal=acapy-admin-api-key='Hj23iQUsstG!dde'
```

Secret Name: catenax-managed-identity-wallets-postgresql
```bash
kubectl -n managed-identity-wallets create secret generic catenax-managed-identity-wallets-postgresql \
--from-literal=password='cx-password' \
--from-literal=postgres-password='cx-password' \
--from-literal=user='postgres'
```

Secret Name: catenax-managed-identity-wallets-acapy-postgresql
```bash
kubectl -n managed-identity-wallets create secret generic catenax-managed-identity-wallets-acapy-postgresql \
--from-literal=password='cx-password' \
--from-literal=postgres-password='cx-password' \
--from-literal=user='postgres'
```


After Setting up the Secrets and building the MIW - Image.

Then run the Helm Charts

Expose via loadbalancer

```bash
kubectl -n managed-identity-wallets apply -f dev-assets/kube-local-lb.yaml
```
