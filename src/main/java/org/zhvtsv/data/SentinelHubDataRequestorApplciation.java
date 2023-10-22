package org.zhvtsv.data;


import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;

import java.io.File;

import static org.zhvtsv.data.JsonRequestPayloadSampleData.JSON_BODY_SENTINEL_HUB;

public class SentinelHubDataRequestorApplciation {
    public static void main(String[] args) throws Exception {
        Configuration config = new Configurations().properties(new File("application.properties"));
        String token = config.getString("sentinel.oauth2.token");
        String sentinelHubProcessApiUrl = config.getString("sentinel.process-api.url");

        //Saves the image to local file system under satelliteData.tif
        SatelliteImageryHttpClient satelliteImageryHttpClient = new SatelliteImageryHttpClient(sentinelHubProcessApiUrl, token);
        satelliteImageryHttpClient.requestSatelliteDataInGeoTIFF(JSON_BODY_SENTINEL_HUB);

    }
}