package nl.tudelft.opencraft.yardstick.metrics.cloud.aws;

import com.typesafe.config.Config;
import nl.tudelft.opencraft.yardstick.metrics.cloud.CloudMetricsClient;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AwsMetricsClient extends CloudMetricsClient {
    private final Instant startTime;
    private final Instant endTime;
    private final String metricType;
    private final String namespace;
    private final String clusterName;
    private final int period;
    private final String statisticType;


    public AwsMetricsClient(@NotNull Config config, Instant startTime, Instant endTime) {
        super(config.getString("name"),
                config.getString("namespace"));
        this.startTime = startTime;
        this.endTime = endTime;
        this.metricType = config.getString("metric-type");
        this.namespace = config.getString("namespace");
        this.clusterName = config.getString("cluster-name");
        this.period = Integer.parseInt(config.getString("period"));
        this.statisticType = config.getString("statistic");

        if (this.period < 60) {
            this.logger.warning("Please note you need to have high-precision metrics enabled " +
                    "in order to use a period below 60. This can result in additional charges.");
        }

        if (config.getBoolean("show-available")) {
            ListAvailableMetrics availableMetrics = new ListAvailableMetrics(
                    "AvailableMetrics",
                    config.getString("namespace"),
                    config.getString("cluster-name")
            );
            availableMetrics.run();
        }

        this.logger.info(String.format("Retrieving AWS metric '%s' for %s to %s with period '%ds'",
                this.metricType,
                this.startTime.toString(),
                this.endTime.toString(),
                this.period));
    }

    public void run() {
        List<MetricDataQuery> dq = new ArrayList<>();
        List<Dimension> dims = new ArrayList<>();

        /* Retrieve CPU utilization for given timerange */
        try {
            dims.add(Dimension.builder()
                    .name("ClusterName")
                    .value(this.clusterName)
                    .build()
            );

        /* For metrics produced by certain AWS services, such as Amazon EC2,
        CloudWatch can aggregate data across dimensions.
        For example, if you search for metrics in the AWS/EC2 namespace but do not
        specify any dimensions, CloudWatch aggregates all data for the specified
        metric to create the statistic that you requested.
        CloudWatch does not aggregate across dimensions for your custom metrics.
        */
            Metric met = Metric.builder()
                    .metricName(this.metricType)
                    .namespace(this.namespace)
                    .dimensions(dims)
                    .build();

            MetricStat metStat = MetricStat.builder()
                    .stat(this.statisticType)
                    .period(this.period)
                    .metric(met)
                    .build();

            MetricDataQuery dataQuery = MetricDataQuery.builder()
                    .metricStat(metStat)
                    .id("foo3")
                    .returnData(true)
                    .build();

            dq.add(dataQuery);

            GetMetricDataRequest getMetReq = GetMetricDataRequest.builder()
                    .startTime(this.startTime)
                    .endTime(this.endTime)
                    .metricDataQueries(dq)
                    .build();

            GetMetricDataResponse response = cw.getMetricData(getMetReq);
            List<MetricDataResult> data = response.metricDataResults();

            for (MetricDataResult item : data) {
                this.logger.info("Status " + item.statusCode().toString());
                if (item.hasValues() && item.hasTimestamps()) {
                    this.values = item.values();
                    this.timestamps = item.timestamps();
                    this.logger.info(item.values().toString());
                    this.logger.info(item.timestamps().toString());
                } else {
                    this.logger.warning("No data could be retrieved from AWS for the given metric and timerange.");
                }

            }

        } catch (CloudWatchException e) {
            this.logger.warning(e.awsErrorDetails().errorMessage());
            this.logger.warning("Exiting...");
            System.exit(1);
        }

    }
}


