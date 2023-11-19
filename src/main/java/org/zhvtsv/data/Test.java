package org.zhvtsv.data;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        CopernicusZipDownloader copernicusZipDownloader = new CopernicusZipDownloader();
        copernicusZipDownloader.downloadZip("https://zipper.dataspace.copernicus.eu/odata/v1/Products(4c6c9409-5683-567c-afb0-580fd6ef5cf1)/$value");


//        CopernicusSTACClient stacClient = new CopernicusSTACClient();
//        List<Feature> features = stacClient.getItems("", "");

//        stacClient.downloadFileFromZip("https://zipper.dataspace.copernicus.eu/odata/v1/Products(7b9d186d-f522-5dae-a6e0-12088b43da31)/$value");
//        stacClient.listContents();
    }
}
