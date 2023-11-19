package org.zhvtsv.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zhvtsv.models.stac.Feature;
import org.zhvtsv.models.stac.mappers.StacResponseMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class STACClient {
    private String url;
    private HttpClient httpClient;

    // TODO: Add refresh token functionality and remove the stupid Copernicus Identity Provider
    public STACClient(String url) {
        this.url = url;
        this.httpClient = HttpClient.newBuilder().build();
    }

    public List<Feature> getItems(String boundingBox, String dateTimeRange) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.url + "bbox=" + boundingBox + "&datetime=" + dateTimeRange))
                .GET()
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            System.out.println(response.body());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        JSONObject json = new JSONObject(response.body());
        JSONArray featuresJsonArray = json.getJSONArray("features");
        List<Feature> features = new ArrayList<>();
        for (int i = 0; i < featuresJsonArray.length(); i++) {
            JSONObject featureJSON = featuresJsonArray.getJSONObject(i);
            try {
                Feature f = StacResponseMapper.getStacResponse(featureJSON.toString());
                String downloadUrl = featureJSON.getJSONObject("assets").getJSONObject("visual").getString("href");
                f.setAssetUrl(downloadUrl);
                features.add(f);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return features;
    }

}
