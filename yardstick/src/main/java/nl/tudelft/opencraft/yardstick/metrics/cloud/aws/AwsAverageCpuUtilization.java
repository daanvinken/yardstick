package nl.tudelft.opencraft.yardstick.metrics.cloud.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import nl.tudelft.opencraft.yardstick.metrics.cloud.CloudMetricsClient;

import java.util.Date;

public class AwsAverageCpuUtilization extends CloudMetricsClient {
    private Date startTime;
    private Date endTime;

    public AwsAverageCpuUtilization(String name, Date startTime, Date endTime) {
        super(name);
        this.startTime = startTime;
        this.endTime = endTime;
        this.run();
    }

    public void run() {
        final AmazonCloudWatch cw =
                AmazonCloudWatchClientBuilder.defaultClient();

        Dimension dim = new Dimension()
                .withName("default")
                .withValue("i-1234");
        try {
            GetMetricStatisticsRequest metricRequest = new GetMetricStatisticsRequest()
                    .withMetricName("CPUUtilization")
                    .withNamespace("AWS/ECS")
                    .withStartTime(this.startTime)
                    .withEndTime(this.endTime)
                    .withPeriod(1200)
                    .withStatistics("Average")
                    .withDimensions(dim);

            GetMetricStatisticsResult metricResult = cw.getMetricStatistics(metricRequest);

            this.logger.info(metricRequest.toString());
            this.logger.info("Size : " + metricResult.getDatapoints().size());
        }
        catch(AmazonServiceException awsException){
            awsException.printStackTrace();
        }
    }
}


