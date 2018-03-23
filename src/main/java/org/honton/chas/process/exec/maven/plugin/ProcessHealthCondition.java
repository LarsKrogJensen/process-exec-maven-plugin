package org.honton.chas.process.exec.maven.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.maven.plugin.logging.Log;

import java.util.concurrent.TimeUnit;

public class ProcessHealthCondition {
    private static final int SECONDS_BETWEEN_CHECKS = 1;

    private final Log log;
    private final HealthCheckUrl healthCheckUrl;
    private final int timeoutInSeconds;

    public ProcessHealthCondition(Log log, HealthCheckUrl healthCheckUrl, int timeoutInSeconds) {
        this.log = log;
        this.healthCheckUrl = healthCheckUrl;
        this.timeoutInSeconds = timeoutInSeconds;

    }

    public void awaitHealthy() {
        if (healthCheckUrl.getUrl() == null) {
            // Wait for timeout seconds to let the process come up
            sleep(timeoutInSeconds);
            return;
        }
        final long start = System.currentTimeMillis();
        while ((System.currentTimeMillis() - start) / 1000 < timeoutInSeconds) {
            if (isHealthy()) {
                log.info("Health check succeeded, " + healthCheckUrl.getUrl().toExternalForm());
                return;
            }
            sleep(SECONDS_BETWEEN_CHECKS);
        }
        throw new RuntimeException("Process was not healthy even after " + timeoutInSeconds + " seconds");
    }

    private boolean isHealthy() {
        try {
            log.debug("Invoking health url: " + healthCheckUrl.getUrl().toExternalForm());
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(healthCheckUrl.getUrl().toURI());
            HttpResponse response = client.execute(request);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode tree = mapper.readTree(response.getEntity().getContent());
            String status = tree.get("status").asText();
            return response.getStatusLine().getStatusCode() == 200 && isStatusOK(status);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return false;
        }
    }

    private boolean isStatusOK(String status) {
        return status.equalsIgnoreCase("RUNNING") || status.equalsIgnoreCase("OK");
    }

    private void sleep(int seconds) {
        try {
            log.debug("waiting for " + seconds + " seconds");
            Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
