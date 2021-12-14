package nl.tudelft.opencraft.yardstick.metrics.cloud.azure;

import com.typesafe.config.Config;
import nl.tudelft.opencraft.yardstick.metrics.cloud.CloudMetricsClient;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jettison.json.JSONArray;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class AzureMetricsClient extends CloudMetricsClient {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String metricType;
    private final String namespace;
    private final String statisticType;
    private final AzureRestApiWrapper azureRestClient;
    private final String apiVersion;
    private final String resourceId;


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

        this.apiVersion =  config.getString("api-version");
        this.resourceId = config.getString("resource-id");
        this.region = config.getString("region");
    }

    public void run() {
        HttpGet request = this.azureRestClient.createMetricRequest(resourceId,
                this.statisticType,
                this.apiVersion,
                this.region,
                "2021-12-09T02:20:00Z/2021-12-12T04:20:00Z",
                this.metricType,
                this.namespace
        );
        JSONArray metricResult = null;
        metricResult = this.azureRestClient.getMetrics(request);
        this.logger.info(metricResult.toString());

    }
}




