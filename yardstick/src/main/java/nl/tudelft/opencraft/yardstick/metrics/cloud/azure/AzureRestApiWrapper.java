package nl.tudelft.opencraft.yardstick.metrics.cloud.azure;

import nl.tudelft.opencraft.yardstick.logging.GlobalLogger;
import nl.tudelft.opencraft.yardstick.logging.SubLogger;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
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
    private String bearer_token = "";
    private HttpPost request;
    private CloseableHttpResponse response;

    public AzureRestApiWrapper() {
        this.httpClient = HttpClients.createDefault();
        this.logger = GlobalLogger.getLogger().newSubLogger("AzureApiWrapper");
    }

    public void authenticate(String tenantId, String clientId, String clientSecret) {
        String authUrl = "https://login.microsoftonline.com/%s/oauth2/token";
        String reqUrl = String.format(authUrl, tenantId);
        try {
            this.request = new HttpPost(reqUrl);
            request.addHeader("Content-Type", "application/x-www-form-urlencoded");

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("grant_type", "client_credentials"));
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("resource", "https://management.azure.com/"));

            this.request.setEntity(new UrlEncodedFormEntity(params));
            this.logger.info("Requesting bearer token for authentication with Azure Rest API");
        } catch (UnsupportedEncodingException ex) {
            this.logger.severe(ex.toString());
        }

        try {
            this.response = httpClient.execute(this.request);
            HttpEntity entity = response.getEntity();
            int responseCode = response.getStatusLine().getStatusCode();
            String responseMsg = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            if(responseCode != 200) {
                this.logger.severe(String.format("Could not retrieve bearer token.\n" +
                                "Statuscode: %s\n" +
                                "Response: %s ",
                        responseCode,
                        responseMsg));
                this.logger.severe("Exiting");
                System.exit(1);
            }
            else {
                JSONObject jsonRespone = new JSONObject(responseMsg);
                this.bearer_token = jsonRespone.getString("access_token");
            }
        } catch (IOException | JSONException ex) {
            this.logger.severe(ex.toString());
        } finally {
            try {
                if (this.response != null) {
                    response.close();
                }
            } catch (IOException e) {
                this.logger.warning("Could not close request.");
            }
        }
    }
}
