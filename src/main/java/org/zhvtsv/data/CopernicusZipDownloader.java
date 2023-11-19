package org.zhvtsv.data;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class CopernicusZipDownloader {

    private HttpClient httpClient;
    private String token;

    public CopernicusZipDownloader() throws IOException, InterruptedException {
        this.httpClient = HttpClient.newBuilder().build();
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

        HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());
        this.token = json.getString("access_token");
    }

    public void downloadZip(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers("Authorization", "Bearer " + this.token)
                .GET()
                .build();

        String outputPath = "new.zip";

        try {
            HttpResponse<InputStream> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            System.out.println(response);
            System.out.println(response.statusCode());
            System.out.println(response.headers().map());
            if (response.statusCode() == 200) {
                try (InputStream inputStream = response.body(); FileOutputStream outputStream = new FileOutputStream(outputPath)) {
                    IOUtils.copy(inputStream, outputStream);
                    System.out.println("Zip saved to " + outputPath);
                }
            } else {
                System.err.println("HTTP Request failed with status code: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
