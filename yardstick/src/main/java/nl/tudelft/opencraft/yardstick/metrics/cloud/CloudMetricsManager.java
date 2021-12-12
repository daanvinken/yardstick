package nl.tudelft.opencraft.yardstick.metrics.cloud;


import com.typesafe.config.Config;
import kotlin.NotImplementedError;
import nl.tudelft.opencraft.yardstick.metrics.cloud.aws.AwsMetricsClient;
import nl.tudelft.opencraft.yardstick.metrics.cloud.aws.ListAvailableMetrics;
import nl.tudelft.opencraft.yardstick.metrics.cloud.azure.AzureMetricsClient;

import java.text.MessageFormat;
import java.time.LocalDateTime;

public class CloudMetricsManager {
    private final Config config;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String platform;

    public CloudMetricsManager(Config config, LocalDateTime startTime, LocalDateTime endTime) {
    this.config = config;
    this.startTime = startTime;
    this.endTime = endTime;
    this.platform = config.getString("yardstick.player-emulation.arguments.cloud-metrics.platform");

    }

    public void start() {
        if (this.platform.equals("aws")) {
            AwsMetricsClient awsClient = new AwsMetricsClient(
                    this.config,
                    this.startTime,
                    this.endTime
            );
            awsClient.run();
        }
        else if (this.platform.equals("azure")) {
            AzureMetricsClient azureClient = new AzureMetricsClient(
                    this.config,
                    this.startTime,
                    this.endTime
            );
            azureClient.run();
        }
        else {
            throw new NotImplementedError(MessageFormat.format("Metrics for platform type '{0}' are not implemented.", platform));
        }
    }

}
