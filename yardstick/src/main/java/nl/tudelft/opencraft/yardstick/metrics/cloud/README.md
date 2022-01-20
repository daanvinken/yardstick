# Usage of AWS (or Azure) metrics with yardstick

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

### 2. Make sure to have your AWS cli set up with `aws configure`

### 3. In the [main yardstick file]() there are some notes on how to use it without an actual experiment.
You basically comment out the thread to start and can modify the amount of 
minutes/hours you go want to go back in time for those metrics.

### 4. It should print metrics to the terminal (stdout) during the experiment and they will be stored in a csv in the same directory.


