package nl.tudelft.opencraft.yardstick.metrics.cloud.aws;

import nl.tudelft.opencraft.yardstick.metrics.cloud.CloudMetricsClient;
import software.amazon.awssdk.services.cloudwatch.model.*;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListAvailableMetrics extends CloudMetricsClient {
    public ListAvailableMetrics(String name, String namespace, String clusterName) {
        super(name, namespace);
    }

    public void run() {
        boolean done = false;
        String nextToken = null;
        List<String> metricNames = new ArrayList<String>();
        /* List available metrics */
        try {
            while (!done) {

                ListMetricsResponse response;
                ListMetricsRequest request;

                if (nextToken == null) {
                    request = ListMetricsRequest.builder()
                            .namespace(namespace)
                            .build();

                } else {
                    request = ListMetricsRequest.builder()
                            .namespace(namespace)
                            .nextToken(nextToken)
                            .build();

                }
                response = cw.listMetrics(request);

                 metricNames.addAll(response.metrics().stream()
                        .map(Metric::metricName)
                        .distinct()
                        .collect(Collectors.toList()));
                if (response.nextToken() == null) {
                    done = true;
                } else {
                    nextToken = response.nextToken();
                }
            }

        } catch (CloudWatchException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        // Only print distinct ones for given namespace
        metricNames = metricNames.stream()
                .distinct()
                .collect(Collectors.toList());
        this.logger.info("Available metrics: " + metricNames);
    }
}
