package nl.tudelft.opencraft.yardstick.metrics.cloud.aws;

import nl.tudelft.opencraft.yardstick.metrics.cloud.CloudMetricsClient;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsRequest;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Metric;

import java.util.Date;

public class ListAvailableMetrics extends CloudMetricsClient {
    public ListAvailableMetrics(String name, String namespace) {
        super(name, namespace);

        this.run();
    }

    public void run() {
        boolean done = false;
        String nextToken = null;
        /* List available metrics */
        try {
            while (!done) {

                ListMetricsResponse response;

                if (nextToken == null) {
                    ListMetricsRequest request = ListMetricsRequest.builder()
                            .namespace(namespace)
                            .build();

                    response = cw.listMetrics(request);
                } else {
                    ListMetricsRequest request = ListMetricsRequest.builder()
                            .namespace(namespace)
                            .nextToken(nextToken)
                            .build();

                    response = cw.listMetrics(request);
                }

                for (Metric metric : response.metrics()) {
                    this.logger.info("Retrieved metric: " + metric.metricName());
                }

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
    }
}
