package nl.tudelft.opencraft.yardstick.metrics.cloud.azure;

import nl.tudelft.opencraft.yardstick.logging.GlobalLogger;
import nl.tudelft.opencraft.yardstick.logging.SubLogger;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class AzureRestApiWrapper {
    private final SubLogger logger;
    private final CloseableHttpClient httpClient;
    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private String bearer_token;
    private final String authUrl = "https://login.microsoftonline.com/%s/oauth2/token";
    private final String metricUrl = "https://management.azure.com/%s/providers/Microsoft.Insights/metrics";

    public AzureRestApiWrapper(String tenantId, String clientId, String clientSecret) {
        this.httpClient = HttpClients.createDefault();
        this.logger = GlobalLogger.getLogger().newSubLogger("AzureApiWrapper");
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authenticate();
    }

    public void authenticate() {
        HttpPost request = this.createAuthRequest();
        JSONObject response = this.getResponse(request);
        try {
            this.bearer_token = response.getString("access_token");
        } catch (JSONException ex) {
            throw new ParseException("Unable to parse bearer token from auth response.");
        }
        this.logger.info("Successfully authenticated for Azure REST API.");
    }

    private HttpPost createAuthRequest() {
        String reqUrl = String.format(this.authUrl, tenantId);
        HttpPost request = new HttpPost(reqUrl);
        try {
            request.addHeader("Content-Type", "application/x-www-form-urlencoded");

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "client_credentials"));
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("resource", "https://management.azure.com/"));

            request.setEntity(new UrlEncodedFormEntity(params));
        } catch (UnsupportedEncodingException ex) {
            this.logger.severe(ex.toString());
        }
        return request;
    }

    public void getMetrics(HttpGet request) {
        JSONObject response = this.getResponse(request);
        System.out.println(response);
    }

    public HttpGet createMetricRequest(String resourceId,
                            String aggregation,
                            String apiVersion,
                            String timeSpan,
                            String metricType,
                            String namespace
                            ) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("metricnames", metricType));
        params.add(new BasicNameValuePair("aggregation", aggregation));
        params.add(new BasicNameValuePair("api-version", apiVersion));
        params.add(new BasicNameValuePair("metricnamespace", namespace));
        params.add(new BasicNameValuePair("timespan", timeSpan));
        // TDDO region is currently hardcoded application wide (aws/azure)
        params.add(new BasicNameValuePair("region", "westeurope"));
        params.add(new BasicNameValuePair("resultType", "TimeSeriesElement"));

        String reqUrl = String.format(this.metricUrl, resourceId);
        HttpGet request = new HttpGet(reqUrl+"?"+ URLEncodedUtils.format(params, "utf-8"));
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.addHeader("Authorization", "Bearer " + this.bearer_token);

    return request;
    }

    public JSONObject getResponse(HttpRequestBase request) {
        int responseCode = 0;
        String responseMsg = "";
        JSONObject jsonResponse = new JSONObject();
        try {
            CloseableHttpResponse response = this.httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            responseCode = response.getStatusLine().getStatusCode();
            responseMsg = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            this.logger.severe(ex.getMessage());
        }

        if (responseCode != 200) {
            this.logger.severe(String.format("Errror performing request to '%s':\n" +
                            "Statuscode: \n%s\n" +
                            "Response: \n%s\n",
                    request.getURI(),
                    responseCode,
                    responseMsg));
        } else {
            try {
                jsonResponse = new JSONObject(responseMsg);
            } catch (JSONException ex) {
                this.logger.severe("Unable to parse JSON.");
                this.logger.severe(ex.getMessage());
            }
        }
        return jsonResponse;
    }


}
