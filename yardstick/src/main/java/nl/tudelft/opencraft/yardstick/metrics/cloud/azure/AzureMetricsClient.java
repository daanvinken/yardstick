package nl.tudelft.opencraft.yardstick.metrics.cloud.azure;

import com.typesafe.config.Config;
import nl.tudelft.opencraft.yardstick.metrics.cloud.CloudMetricsClient;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class AzureMetricsClient extends CloudMetricsClient {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String metricType;
    private final String namespace;
    private final String clusterName;
    private final int period;
    private final String statisticType;
    private final Config metricConfig;
    private final AzureRestApiWrapper azureRestClient;


    public AzureMetricsClient(@NotNull Config config, LocalDateTime startTime, LocalDateTime endTime) {
        // TODO fix this long config string
        super(config.getString("yardstick.player-emulation.arguments.cloud-metrics.metric-name"),
                config.getString("yardstick.player-emulation.arguments.cloud-metrics.namespace"));
        this.metricConfig = config.getConfig("yardstick.player-emulation.arguments.cloud-metrics");
        this.startTime = startTime;
        this.endTime = endTime;
        this.metricType = metricConfig.getString("metric-type");
        this.namespace = metricConfig.getString("namespace");
        this.clusterName = metricConfig.getString("cluster-name");
        this.period = Integer.parseInt(metricConfig.getString("period"));
        this.statisticType = metricConfig.getString("statistic");

        this.logger.info(String.format("Retrieving Azure metric '%s' for %s to %s with period '%ds'",
                this.metricType,
                this.startTime.toString(),
                this.endTime.toString(),
                this.period));
        String tenantId =  config.getString("yardstick.game.servo.environment.AZURE_TENANT_ID");
        String clientId =  config.getString("yardstick.game.servo.environment.AZURE_CLIENT_ID");
        String clientSecret =  config.getString("yardstick.game.servo.environment.AZURE_CLIENT_SECRET");

        this.azureRestClient = new AzureRestApiWrapper();
        this.azureRestClient.authenticate(tenantId,
                clientId,
                clientSecret);
    }

    public void run() {
    }
}




