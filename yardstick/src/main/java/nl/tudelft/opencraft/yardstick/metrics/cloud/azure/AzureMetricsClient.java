package nl.tudelft.opencraft.yardstick.metrics.cloud.azure;

import com.typesafe.config.Config;
import nl.tudelft.opencraft.yardstick.metrics.cloud.CloudMetricsClient;
import org.apache.http.client.methods.HttpGet;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

public class AzureMetricsClient extends CloudMetricsClient {
    private final Instant startTime;
    private final Instant endTime;
    private final String metricType;
    private final String namespace;
    private final String statisticType;
    private final AzureRestApiWrapper azureRestClient;
    private final String apiVersion;
    private final String resourceId;


    public AzureMetricsClient(@NotNull Config config, Instant startTime, Instant endTime) {
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

    public String getTimeSpanFormat(){
        String out = "";
        out += DateTimeFormatter.ISO_INSTANT.format(this.startTime);
        out += "/";
        out += DateTimeFormatter.ISO_INSTANT.format(this.endTime);
        return out;
    }


    public void run() {
        HttpGet request = this.azureRestClient.createMetricRequest(resourceId,
                this.statisticType,
                this.apiVersion,
                this.region,
                this.getTimeSpanFormat(),
                this.metricType,
                this.namespace
        );
        LinkedHashMap<Instant, Double> metricResult = null;
        AzureMetricData metricData = this.azureRestClient.getMetrics(request);
        if (metricData.hasData()) {
            this.timestamps = metricData.getTimestamps();
            this.values = metricData.getValues();
            this.logger.info(String.format("Successfully stored metric data with %d values.", this.values.size()));
        }
        else {
            this.logger.severe("No metric data has been retrieved.");
        }

    }
}




