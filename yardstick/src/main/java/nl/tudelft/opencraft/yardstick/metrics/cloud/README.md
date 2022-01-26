
## Please note this is the initial try for retrieving metrics from Azure, where it was found out later on that it could only do so for a time interval of 1 min. Check out the other try [here](https://github.com/daanvinken/ApplicationInsights-Java).
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


# Usage of Azure metrics with yardstick

### 1. Make sure to have your application conf correctly set up, adding the following inside `player-emulation.arguments`:
```lombok.config
cloud-metrics {
                enabled = true
                platform = aws
                aws {
                    name = "exampleRunName"
                    // Set to true to show the available metric types
                    show-available = true
                    // e.g. MemoryUtilized, CpuUtilized, ContainerInstanceCount etc.
                    metric-types = ["MemoryUtilized", "CpuUtilized"]
                    namespace = "ECS/ContainerInsights"
                    statistic = "Maximum"
                    period = 1
                    cluster-name = "default"
                    auth = ${yardstick.game.servo.environment}
                }
                azure {
                    name = "exampleRunName"
                    // e.g. MemoryUsage, CpuUsage
                    metric-type = "MemoryUsage"
                    namespace = "Microsoft.ContainerInstance/containerGroups"
                    statistic = "average"
                    api-version = "2021-05-01"
                    resource-id = "/subscriptions/aef5e381-8c16-46ae-b76e-e260992d50bd/resourceGroups/daanvirg/providers/Microsoft.ContainerInstance/containerGroups/servo-player-username-0252d8e0hallodsf"
                    region = "westeurope"
                    auth = ${yardstick.game.servo.environment}
                }
            }

```
Make sure to upadate the resource-id and region to comply with your own account.

### 2. Make sure to have your Azure cli set up and filled in the application.conf content for Azure as follows:
```lombok.config
game {
        architecture = "servo"
        // Used for the servo-alpha game architecture
        servo {
            // Configuration that should be passed to Servo services should be placed in a 'servo' object at the root of this config
            version = 1
            build {
                git = "git@github.com:atlarge-research/opencraft-servo.git"
                commit = "<TODO>"
            }
            environment {
                AWS_ACCOUNT_ID=750873051000
                AWS_REGION = eu-central-1
                LAMBDA_MEMORY = 512
                FARGATE_MEMORY = 8192
                FARGATE_CPU = 2048

                AZURE_SUBSCRIPTION_ID = "<TODO>"
                AZURE_CLIENT_ID = "<TODO>"
                AZURE_CLIENT_SECRET = "<TODO>"
                AZURE_TENANT_ID = "<TODO>"
            }
        }
    }
```

### 3. In the [main yardstick file](https://github.com/daanvinken/yardstick/blob/feature/aws_metrics/yardstick/src/main/java/nl/tudelft/opencraft/yardstick/Yardstick.java) there are some notes on how to use it without an actual experiment.
You basically comment out the thread to start and can modify the amount of
minutes/hours you go want to go back in time for those metrics.

### 4. Make sure to compile  (again) `mvn clean package`.

### 5.  In the file [AzureMetricsClient.java](https://github.com/daanvinken/yardstick/blob/azure_metrics/yardstick/src/main/java/nl/tudelft/opencraft/yardstick/metrics/cloud/azure/monitor/AzureMetricsClient.java)  you can find the futher implementation and where the metrics are being retrieved. 


