package org.zhvtsv.data;

import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeoTiffDwonloader {
    public static void downloadTif(String url) {
        String requestUrl = url;

        HttpClient client = HttpClient.newBuilder().build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .GET()
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
                }
            } else {
                System.err.println("HTTP Request failed with status code: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
