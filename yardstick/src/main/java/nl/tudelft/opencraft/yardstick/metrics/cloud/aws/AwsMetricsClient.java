package nl.tudelft.opencraft.yardstick.metrics.cloud.aws;

import com.typesafe.config.Config;
import nl.tudelft.opencraft.yardstick.metrics.cloud.CloudMetricsClient;

import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class AwsMetricsClient extends CloudMetricsClient {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String metricType;
    private final String namespace;
    private final String clusterName;
    private final int period;
    private final String statisticType;
    private final Config metricConfig;


    public AwsMetricsClient(@NotNull Config config, LocalDateTime startTime, LocalDateTime endTime) {
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

        if (this.period < 60) {
            this.logger.warning("Please note you need to have high-precision metrics enabled " +
                    "in order to use a period below 60. This can result in additional charges.");
        }

        if (metricConfig.getBoolean("show-available")) {
            ListAvailableMetrics availableMetrics = new ListAvailableMetrics(
                    "AvailableMetrics",
                    metricConfig.getString("namespace"),
                    metricConfig.getString("cluster-name")
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
//            dims.add(Dimension.builder()
//                    .name("TaskDefinitionFamily")
//                    .value("opencraft-servo-player-task")
//                    .build()
//            );


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
                    .startTime(this.startTime.toInstant(ZoneOffset.UTC))
                    .endTime(this.endTime.toInstant(ZoneOffset.UTC))
                    .metricDataQueries(dq)
                    .build();

            GetMetricDataResponse response = cw.getMetricData(getMetReq);
            List<MetricDataResult> data = response.metricDataResults();

            for (MetricDataResult item : data) {
                this.logger.info("Status " + item.statusCode().toString());
                if (item.hasValues() && item.hasTimestamps()) {
                    // TODO is there a situation where we retreive multiple values?
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


