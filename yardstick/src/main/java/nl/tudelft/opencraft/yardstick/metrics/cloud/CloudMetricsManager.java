package nl.tudelft.opencraft.yardstick.metrics.cloud;


import com.typesafe.config.Config;
import kotlin.NotImplementedError;
import nl.tudelft.opencraft.yardstick.metrics.cloud.aws.AwsMetricsClient;
import nl.tudelft.opencraft.yardstick.metrics.cloud.azure.AzureMetricsClient;

import java.text.MessageFormat;
import java.time.Instant;

public class CloudMetricsManager {
    private final Config config;
    private final Instant startTime;
    private final Instant endTime;
    private final String platform;

    public CloudMetricsManager(Config config, Instant startTime, Instant endTime) {
    this.config = config;
    this.startTime = startTime;
    this.endTime = endTime;
    this.platform = config.getString("platform");
    }

    public void start() {
        if (this.platform.equals("aws")) {
            AwsMetricsClient awsClient = new AwsMetricsClient(
                    this.config.getConfig("aws"),
                    this.startTime,
                    this.endTime
            );
            awsClient.run();
        }
        else if (this.platform.equals("azure")) {
            AzureMetricsClient azureClient = new AzureMetricsClient(
                    this.config.getConfig("azure"),
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
