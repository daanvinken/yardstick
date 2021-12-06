package nl.tudelft.opencraft.yardstick.metrics.cloud.aws;

import nl.tudelft.opencraft.yardstick.metrics.cloud.CloudMetricsClient;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;
import software.amazon.awssdk.services.cloudwatchevents.model.CloudWatchEventsException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class AwsCpuUtilization extends CloudMetricsClient {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;


    public AwsCpuUtilization(String name, LocalDateTime startTime, LocalDateTime endTime, String namespace) {
        super(name, namespace);
        this.startTime = startTime;
        this.endTime = endTime;
        this.metricName = "CpuUtilized";
        this.namespace = "ECS/ContainerInsights";

        this.logger.info(String.format("Retrieving metric 'CPUUtilization' for %s to %s.",
                this.startTime.toString(),
                this.endTime.toString()));

        this.run();
    }

    public void run() {
        List<MetricDataQuery> dq = new ArrayList<MetricDataQuery>();

        /* Retrieve CPU utilization for given timerange */
        try {
            List<Dimension> dims = new ArrayList<Dimension>();

            dims.add(Dimension.builder()
                    .name("ClusterName")
                    .value("default")
                    .build()
            );

            Metric met = Metric.builder()
                    .metricName(this.metricName)
                    .namespace(this.namespace)
                    .dimensions(dims)
                    .build();

            MetricStat metStat = MetricStat.builder()
                    .stat("Maximum")
                    .period(1)
                    .metric(met)
                    .build();
            this.logger.info(this.name + "_" + this.metricName);
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
                this.logger.info("Retrieved metric: " + item.label() + " with status " + item.statusCode().toString());
                if (item.hasValues() && item.hasTimestamps()) {
                    // TODO save values
                    this.logger.info(item.values().toString());
                    this.logger.info(item.timestamps().toString());
                } else {
                    this.logger.warning("No data could be retrieved from AWS for the given metric and timerange.");
                }

            }

        } catch (CloudWatchException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

    }
}


