package nl.tudelft.opencraft.yardstick.metrics.cloud;
import nl.tudelft.opencraft.yardstick.logging.GlobalLogger;
import nl.tudelft.opencraft.yardstick.logging.SubLogger;

public abstract class CloudMetricsClient implements Runnable {
    protected final SubLogger logger;

    public CloudMetricsClient(String name) {
        this.logger = GlobalLogger.getLogger().newSubLogger("CloudMetrics").newSubLogger(name);
    }

    public abstract void run();
}
