package org.zhvtsv.data;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.net.http.HttpRequest.BodyPublishers.ofString;

public enum CopernicusIdentityProvider {

    INSTANCE;

    private String accessToken;

    private CopernicusIdentityProvider() {
        HttpClient httpClient = HttpClient.newBuilder().build();
        Configuration config = null;
        try {
            config = new Configurations().properties(new File("application.properties"));
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        String loginUrl = config.getString("copernicus.identity-api.url");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("username", config.getString("copernicus.identity-api.username"));
        parameters.put("password", config.getString("copernicus.identity-api.password"));
        parameters.put("grant_type", config.getString("copernicus.identity-api.grant-type"));
        parameters.put("client_id", config.getString("copernicus.identity-api.client-id"));

        String form = parameters.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(loginUrl))
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .POST(ofString(form))
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        JSONObject json = new JSONObject(response.body());
        this.accessToken = json.getString("access_token");
    }

    public CopernicusIdentityProvider getInstance() {
        return INSTANCE;
    }

    public String getAccessToken() {
        return accessToken;
    }
}