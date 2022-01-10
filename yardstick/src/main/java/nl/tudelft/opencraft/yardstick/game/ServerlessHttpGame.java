package nl.tudelft.opencraft.yardstick.game;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import lombok.Data;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import nl.tudelft.opencraft.yardstick.logging.GlobalLogger;
import nl.tudelft.opencraft.yardstick.logging.SubLogger;

public class ServerlessHttpGame implements GameArchitecture {

    private final Random random = new Random(System.currentTimeMillis());
    private final URI address;
    private final SubLogger logger;

    public ServerlessHttpGame(URI address) {

        this.address = address;
        this.logger = GlobalLogger.getLogger().newSubLogger("ServerlessHttpGame").newSubLogger("azure_test");
        logger.info("Starting ServerlessHttpGame!");
    }

    @Override
    public CompletableFuture<InetSocketAddress> getAddressForPlayer() {
        String id = String.valueOf(random.nextInt());
        var client = HttpClient.newHttpClient();
        NamingRequest namingRequest = new NamingRequest("servo/player:USERNAME" +
                "=" + id, Action.GET, Source.EXTERNAL);
        var request = HttpRequest.newBuilder(address)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(namingRequest)))
                .build();
        var retryPolicy = new RetryPolicy<NamingResponse>()
                .withMaxAttempts(-1)
                .withMaxDuration(Duration.ofMinutes(1))
                .withDelay(Duration.ofSeconds(3))
                .handleResultIf(r -> r.getStatus() != Status.RUN)
                .handleResultIf(r -> r.getHostname().isBlank());
        return Failsafe.with(retryPolicy).getAsync(() -> {
            HttpResponse<String> rawResponse;
            try {
                rawResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
                logger.info(rawResponse.body().toString());
                var gson = new Gson().fromJson(rawResponse.body(), NamingResponse.class);
                logger.info(gson.getHostname().toString());
//                logger.info(gson.getPort().toString());


            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Gson().fromJson(rawResponse.body(), NamingResponse.class);
        }).thenApply(n -> new InetSocketAddress(n.getHostname(), n.getPort()));
    }

    private enum Source {
        INTERNAL, EXTERNAL
    }

    private enum Action {
        GET, STOP
    }

    @Data
    private static class NamingRequest {
        private final String name;
        private final Action action;
        private final Source source;
    }

    private enum Status {
        START, RUN, STOP
    }

    @Data
    private static class NamingResponse {
        private final Status status;
        private final String hostname;
        private final int port;
    }
}
