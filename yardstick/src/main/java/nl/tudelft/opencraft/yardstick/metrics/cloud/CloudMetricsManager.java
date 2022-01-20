package nl.tudelft.opencraft.yardstick.metrics.cloud;


import com.typesafe.config.Config;
import nl.tudelft.opencraft.yardstick.metrics.cloud.aws.AwsMetricsClient;

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
    this.platform = config.getString("platform");

    this.start();

    }

    public void start() {
        if (this.platform.equals("aws")) {
            AwsMetricsClient x = new AwsMetricsClient(
                    this.config.getConfig("aws"),
                    this.startTime,
                    this.endTime
            );
            x.run();
        }
        else {
            throw new IllegalArgumentException(MessageFormat.format("Metrics for platform ''{0}'' does not exist", platform));
        }
    }

}
