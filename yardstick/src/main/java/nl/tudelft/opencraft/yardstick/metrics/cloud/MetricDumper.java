package nl.tudelft.opencraft.yardstick.metrics.cloud;

import nl.tudelft.opencraft.yardstick.logging.GlobalLogger;
import nl.tudelft.opencraft.yardstick.logging.SubLogger;
import nl.tudelft.opencraft.yardstick.workload.PacketEntryWriter;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MetricDumper {
    private static final SubLogger LOGGER = GlobalLogger.getLogger().newSubLogger("WorkloadDumper");
    private final File metricFolder = new File("metrics");
//    private final Map<String, PacketEntryWriter> queues = new ConcurrentHashMap<>();

    /**
     * Creates a new WorkloadDumper.
     */
    public MetricDumper() {
        if (!metricFolder.exists() && !metricFolder.mkdirs()) {
            LOGGER.severe("Could not create folder: " + metricFolder.getPath());
            throw new RuntimeException(new IOException("Could not create folder: " + metricFolder.getPath()));
        }

        // Clear the previous dumps
//        for (File file : metricFolder.listFiles()) {
//            LOGGER.info("Deleting previous metrics: " + file.getName());
//            if (!file.delete()) {
//                LOGGER.warning("Could not delete file: " + file.getPath());
//            }
//        }
    }


}
