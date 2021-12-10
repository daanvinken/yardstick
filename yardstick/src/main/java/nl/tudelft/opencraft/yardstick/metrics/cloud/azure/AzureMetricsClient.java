package nl.tudelft.opencraft.yardstick.metrics.cloud.azure;

import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.core.management.AzureEnvironment;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

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


    public AzureMetricsClient(@NotNull Config config, LocalDateTime startTime, LocalDateTime endTime) {
        super(config.getString("metric-name"), config.getString("namespace"));
        this.startTime = startTime;
        this.endTime = endTime;
        this.metricType = config.getString("metric-type");
        this.namespace = config.getString("namespace");
        this.clusterName = config.getString("cluster-name");
        this.period = Integer.parseInt(config.getString("period"));
        this.statisticType = config.getString("statistic");

        this.logger.info(String.format("Retrieving Azure metric '%s' for %s to %s with period '%ds'",
                this.metricType,
                this.startTime.toString(),
                this.endTime.toString(),
                this.period));
        this.createDefaultAzureCredential();
    }

    public void run() {
    }

    public void createDefaultAzureCredential() {
        UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder()
                .clientId("11c174dc-1945-4a9a-a36b-c79a0f246b9b")
                .username("vinkendaan@gmail.com")
                .password("")
                .build();

// Azure SDK client builders accept the credential as a parameter.
        SecretClient client = new SecretClientBuilder()
                .vaultUrl("https://opencraft.vault.azure.net")
                .credential(usernamePasswordCredential)
                .buildClient();

    }

//        KeyVaultSecret secret = secretClient.setSecret("<secret-name>", "<secret-value>");
//        System.out.printf("Secret created with name \"%s\" and value \"%s\"%n", secret.getName(), secret.getValue());

    // List operations don't return the secrets with value information. So, for each returned secret we call getSecret to
// get the secret with its value information.
//for (SecretProperties secretProperties : secretClient.listPropertiesOfSecrets()) {
//        KeyVaultSecret secretWithValue = secretClient.getSecret(secretProperties.getName(), secretProperties.getVersion());
//        System.out.printf("Retrieved secret with name \"%s\" and value \"%s\"%n", secretWithValue.getName(),
//                secretWithValue.getValue());
}




