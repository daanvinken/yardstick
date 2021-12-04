package nl.tudelft.opencraft.yardstick.metrics.cloud.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.*;
import nl.tudelft.opencraft.yardstick.metrics.cloud.CloudMetricsClient;

import java.time.Instant;
import java.util.Date;

public class AwsCpuUtilization extends CloudMetricsClient {
    private Date startTime;
    private Date endTime;

    public AwsCpuUtilization(String name, Date startTime, Date endTime) {
        super(name);
        this.startTime = startTime;
        this.endTime = endTime;
        this.run();
    }

    public void run() {
        final AmazonCloudWatch cw =
                AmazonCloudWatchClientBuilder.defaultClient();

        try {

        }
        catch(AmazonServiceException awsException){
            awsException.printStackTrace();
        }
    }
}


