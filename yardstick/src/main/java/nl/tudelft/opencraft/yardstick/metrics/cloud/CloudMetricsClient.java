package nl.tudelft.opencraft.yardstick.metrics.cloud;
import nl.tudelft.opencraft.yardstick.logging.GlobalLogger;
import nl.tudelft.opencraft.yardstick.logging.SubLogger;
import nl.tudelft.opencraft.yardstick.workload.WorkloadDumper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public abstract class CloudMetricsClient implements Runnable {
    protected final SubLogger logger;
    protected final CloudWatchClient cw;
    protected final String name;
    protected String metricName;
    protected String namespace;
    protected List<Double> values;
    protected List<Instant> timestamps;

    public CloudMetricsClient(String name, String namespace) {
        this.name = name;
        this.logger = GlobalLogger.getLogger().newSubLogger("CloudMetrics").newSubLogger(name);
        this.namespace = namespace;
        this.cw = CloudWatchClient.builder().region(Region.EU_CENTRAL_1).build();
    }

    public abstract void run();

    public void save_metrics() {
        if (this.timestamps == null || this.timestamps.isEmpty()
            || this.values == null || this.values.isEmpty()) {
            logger.warning("No values were retrieved during the experiment.");
            return;
        }

        //TODO acutally save values in a suitable file


    }
}
