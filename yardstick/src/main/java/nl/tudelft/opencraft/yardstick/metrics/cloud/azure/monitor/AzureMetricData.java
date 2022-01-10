package nl.tudelft.opencraft.yardstick.metrics.cloud.azure.monitor;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class AzureMetricData {
    private List<Instant> timestamps = new ArrayList<>();
    private List<Double> values = new ArrayList<>();

    public AzureMetricData() {}

    public AzureMetricData(JSONObject metrics, String aggregation_type) throws JSONException {
        JSONArray out = metrics
                .getJSONArray("value")
                .getJSONObject(0)
                .getJSONArray("timeseries")
                .getJSONObject(0)
                .getJSONArray("data");
        // out is now an array of {
        //                            "timeStamp": "2021-12-11T13:54:00Z",
        //                            "average": 72689664.0
        //                        },
        // Or a timestamp without value
        LinkedHashMap<Instant, Double> data = new LinkedHashMap<>();
        double value;
        for (int i = 0; i < out.length(); i++){
            value = -1;
            if (out.getJSONObject(i).has(aggregation_type)) {
                value = Double.parseDouble(out.getJSONObject(i).getString(aggregation_type));
            }
            this.timestamps.add(Instant.parse(out.getJSONObject(i).getString("timeStamp")));
            this.values.add(value);
        }
    }

    public List<Instant> getTimestamps() {
        return timestamps;
    }

    public List<Double> getValues() {
        return values;
    }

    public boolean hasData() {
        return (this.values.size() == this.timestamps.size() && this.values.size() > 0);
    }
}
