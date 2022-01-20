package nl.tudelft.opencraft.yardstick.metrics.cloud.aws;

import com.typesafe.config.Config;
import nl.tudelft.opencraft.yardstick.metrics.cloud.CloudMetricsClient;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class AwsMetricsClient extends CloudMetricsClient {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final List<String> metricTypes;
    private final String namespace;
    private final String clusterName;
    private final int period;
    private final String statisticType;
    private ArrayList<List<Double>> values;
    private List<Instant> timestamps;




    public AwsMetricsClient(@NotNull Config config, LocalDateTime startTime, LocalDateTime endTime) {
        super(config.getString("name"), config.getString("namespace"));
        this.startTime = startTime;
        this.endTime = endTime;
        this.metricTypes = config.getStringList("metric-types");
        this.namespace = config.getString("namespace");
        this.clusterName = config.getString("cluster-name");
        this.period = Integer.parseInt(config.getString("period"));
        this.statisticType = config.getString("statistic");

        this.values = new ArrayList<>();
        this.timestamps = new ArrayList<>();

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

        this.logger.info(String.format("Retrieving AWS metric '%s' for %s to %s with period '%ds'",
                this.metricTypes,
                this.startTime.toString(),
                this.endTime.toString(),
                this.period));
        }
    }

    private void writeMetricsToCSV() {
        BufferedWriter bw = null;
        try {
            File outFile = new File(this.name + ".csv");
            FileOutputStream fos = new FileOutputStream(outFile);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            List<String> current_line = new ArrayList<>();

            /* Write headers */
            current_line.add("Timestamp");
            current_line.addAll(this.metricTypes);

            /* Write current_line to csv */
            for (int i = 0; i < this.timestamps.size(); i++) {
                bw.write(String.join(",", current_line));
                bw.newLine();

                current_line.clear();
                for (List<Double> value : this.values) {
                    current_line.add(String.valueOf(value.get(i)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeMetricsToArrays(List<MetricDataResult> metricResult) {
        /* For now only loop once because we request 1 metric */
        for (MetricDataResult item : metricResult) {
            this.logger.info("Metric retrieval AWS status code: " + item.statusCode().toString());
            if (item.hasValues() && item.hasTimestamps()) {
                this.values.add(item.values());
                this.timestamps = item.timestamps();
                this.logger.info("Writing to CSV " + item.id());
                this.logger.info(this.values.toString());
                this.logger.info(this.timestamps.toString());
            } else {
                this.logger.warning("No data could be retrieved from AWS for the given metric and timerange.");
            }

        }
    }

    public void run() {
        List<MetricDataQuery> dq = new ArrayList<>();
        List<Dimension> dims = new ArrayList<>();

        /* Retrieve ONE metric for given timerange */
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

            for (String metricType : this.metricTypes) {
                Metric met = Metric.builder()
                        .metricName(metricType)
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
                        .id("data_" + metricType)
                        .returnData(true)
                        .build();

                dq.add(dataQuery);
            }


            GetMetricDataRequest getMetReq = GetMetricDataRequest.builder()
                    .startTime(this.startTime.toInstant(ZoneOffset.UTC))
                    .endTime(this.endTime.toInstant(ZoneOffset.UTC))
                    .metricDataQueries(dq)
                    .build();

            GetMetricDataResponse response = cw.getMetricData(getMetReq);
            List<MetricDataResult> data = response.metricDataResults();

            this.writeMetricsToArrays(data);
            this.writeMetricsToCSV();

        } catch (CloudWatchException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

    }
}


