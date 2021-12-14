package nl.tudelft.opencraft.yardstick.metrics.cloud.azure;

import com.typesafe.config.Config;
import nl.tudelft.opencraft.yardstick.metrics.cloud.CloudMetricsClient;
import org.apache.http.client.methods.HttpGet;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class AzureMetricsClient extends CloudMetricsClient {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String metricType;
    private final String namespace;
    private final String statisticType;
    private final AzureRestApiWrapper azureRestClient;


    public AzureMetricsClient(@NotNull Config config, LocalDateTime startTime, LocalDateTime endTime) {
        super(config.getString("name"),
                config.getString("namespace"));
        this.startTime = startTime;
        this.endTime = endTime;
        this.metricType = config.getString("metric-type");
        this.namespace = config.getString("namespace");
        this.statisticType = config.getString("statistic");

        //TODO add period to request/logging?
        this.logger.info(String.format("Retrieving Azure metric '%s' for %s to %s.",
                this.metricType,
                this.startTime.toString(),
                this.endTime.toString())
                );
        String tenantId =  config.getString("auth.AZURE_TENANT_ID");
        String clientId =  config.getString("auth.AZURE_CLIENT_ID");
        String clientSecret =  config.getString("auth.AZURE_CLIENT_SECRET");

        this.azureRestClient = new AzureRestApiWrapper(tenantId,
                                                        clientId,
                                                        clientSecret);

        String apiVersion =  config.getString("api-version");
        String resourceId = config.getString("resource-id");
        HttpGet request = this.azureRestClient.createMetricRequest(resourceId,
                                                statisticType,
                                                apiVersion,
                                                "2021-12-09T02:20:00Z/2021-12-12T04:20:00Z",
                                                this.metricType,
                                                this.namespace
                                        );
        this.azureRestClient.getMetrics(request);


    }

    public void run() {
    }
}




