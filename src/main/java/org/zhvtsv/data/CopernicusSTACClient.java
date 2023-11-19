package org.zhvtsv.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zhvtsv.models.stac.Feature;
import org.zhvtsv.models.stac.mappers.StacResponseMapper;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class CopernicusSTACClient {
    private String accessToken;
    private HttpClient httpClient;

    // TODO: Add refresh token functionality and remove the stupid Copernicus Identity Provider
    public CopernicusSTACClient() {
        this.accessToken = CopernicusIdentityProvider.INSTANCE.getAccessToken();
        this.httpClient = HttpClient.newBuilder().build();
    }

    public List<Feature> getItems(String boundingBox, String dateTimeRange) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://catalogue.dataspace.copernicus.eu/stac/collections/SENTINEL-2/items?bbox=25.280706,43.533429,25.333495,43.557754&datetime=2021-12-31T09:59:31.293Z/2023-12-31T09:59:31.293Z"))
                .headers("Authorization", "Bearer " + this.accessToken)
                .GET()
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
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
                System.out.println(f);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
//            features.add(new Feature(featureJSON.getString("id"), new BoundingBox(featureJSON.get)))
        }


//        System.out.println(feaures.get(0));
        return null;
    }

    public void downloadFileFromZip(String url) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .headers("Authorization", "Bearer " + this.accessToken)
                .GET().build();
        try {
            HttpResponse<InputStream> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            System.out.println(response);
            System.out.println(response.body().readAllBytes());
            ZipInputStream zipInputStream = new ZipInputStream(response.body());
            System.out.println(zipInputStream);
            System.out.println(zipInputStream.getNextEntry());
            ZipEntry temp = null;
            while ((temp = zipInputStream.getNextEntry()) != null) {
                System.out.println(temp.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void listZipFiles() {
        try {
            FileInputStream fs = new FileInputStream("/Users/zezko/Documents/bakk/rs-playground/5b642df0-6b1f-54af-ba67-165b33d6e9c8.zip");
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fs));

            ZipEntry ze = null;
            while ((ze = zipInputStream.getNextEntry()) != null) {
                System.out.println(ze.getName());
            }
            zipInputStream.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listContents() {
        ZipFile zipFile = null;
        File file = new File("/Users/zezko/Documents/bakk/rs-playground/5b642df0-6b1f-54af-ba67-165b33d6e9c8.zip");
        try {
            zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                System.out.println("ZIP Entry: " + entryName);
            }
            zipFile.close();
        } catch (IOException ioException) {
            System.out.println("Error opening zip file" + ioException);
        }
    }

}
