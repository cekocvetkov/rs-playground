package org.zhvtsv.data;

import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpRequest.BodyPublishers.ofString;

public class SatelliteImageryHttpClient {
    private String url;
    private String token;

    public SatelliteImageryHttpClient(String url, String token) {
        this.url = url;
        this.token = token;
    }

    public String requestSatelliteDataInGeoTIFF(String payload) {
        String requestUrl = this.getUrl();

        HttpClient client = HttpClient.newBuilder().build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .headers("Authorization", "Bearer " + this.getToken(), "Content-Type", "application/json", "Accept", "image/tiff")
                .POST(ofString(payload))
                .build();

        String outputPath = "satelliteData.TIF";

        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            System.out.println(response);
            System.out.println(response.statusCode());
            System.out.println(response.headers().map());
            if (response.statusCode() == 200) {
                try (InputStream inputStream = response.body(); FileOutputStream outputStream = new FileOutputStream(outputPath)) {
                    IOUtils.copy(inputStream, outputStream);
                    System.out.println("TIFF image saved to " + outputPath);
                    return outputPath;
                }
            } else {
                System.err.println("HTTP Request failed with status code: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
