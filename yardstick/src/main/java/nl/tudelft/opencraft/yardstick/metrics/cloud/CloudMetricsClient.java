package nl.tudelft.opencraft.yardstick.metrics.cloud;
import nl.tudelft.opencraft.yardstick.logging.GlobalLogger;
import nl.tudelft.opencraft.yardstick.logging.SubLogger;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

public abstract class CloudMetricsClient implements Runnable {
    protected final SubLogger logger;
    protected final CloudWatchClient cw;
    protected final String name;
    protected String metricName;
    protected String namespace;

    public CloudMetricsClient(String name, String namespace) {
        this.name = name;
        this.logger = GlobalLogger.getLogger().newSubLogger("CloudMetrics").newSubLogger(name);
        this.namespace = namespace;
        this.cw = CloudWatchClient.builder().region(Region.EU_CENTRAL_1).build();
    }

    public abstract void run();
}
