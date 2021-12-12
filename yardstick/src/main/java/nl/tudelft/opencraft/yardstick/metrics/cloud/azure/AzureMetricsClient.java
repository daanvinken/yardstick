package nl.tudelft.opencraft.yardstick.metrics.cloud.azure;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.typesafe.config.Config;
import nl.tudelft.opencraft.yardstick.metrics.cloud.CloudMetricsClient;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class AzureMetricsClient extends CloudMetricsClient {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String metricType;
    private final String namespace;
    private final String clusterName;
    private final int period;
    private final String statisticType;
    private final Config metricConfig;


    public AzureMetricsClient(@NotNull Config config, LocalDateTime startTime, LocalDateTime endTime) {
        // TODO fix this long config string
        super(config.getString("yardstick.player-emulation.arguments.cloud-metrics.metric-name"),
                config.getString("yardstick.player-emulation.arguments.cloud-metrics.namespace"));
        this.metricConfig = config.getConfig("yardstick.player-emulation.arguments.cloud-metrics");
        this.startTime = startTime;
        this.endTime = endTime;
        this.metricType = metricConfig.getString("metric-type");
        this.namespace = metricConfig.getString("namespace");
        this.clusterName = metricConfig.getString("cluster-name");
        this.period = Integer.parseInt(metricConfig.getString("period"));
        this.statisticType = metricConfig.getString("statistic");

        this.logger.info(String.format("Retrieving Azure metric '%s' for %s to %s with period '%ds'",
                this.metricType,
                this.startTime.toString(),
                this.endTime.toString(),
                this.period));
        this.createDefaultAzureCredential(config);
    }

    public void run() {
    }

    public void createDefaultAzureCredential(Config config) {
        TokenCredential credential = new EnvironmentCredentialBuilder()
                .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
                .build();
        String tenantId =  config.getString("yardstick.game.servo.environment.AZURE_TENANT_ID");
        String subscriptionId =  config.getString("yardstick.game.servo.environment.AZURE_SUBSCRIPTION_ID");

        AzureProfile profile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);
        AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();
    }
}




