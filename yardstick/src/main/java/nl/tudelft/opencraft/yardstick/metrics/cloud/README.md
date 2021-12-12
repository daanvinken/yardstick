# Set up azure credentials

Sign in and create a servicce principal
```bash
az login

az ad sp create-for-rbac --name yardstick --role Contributor
```
Now set up the following environment variables in your `application.conf`:
* `AZURE_SUBSCRIPTION_ID`: Use the id value from `az account show` in the Azure CLI 2.0.
* `AZURE_CLIENT_ID`: Use the appId value from the output taken from a service principal output. 
* `AZURE_CLIENT_SECRET`: Use the password value from the service principal output.
* `AZURE_TENANT_ID`: Use the tenant value from the service principal output.
