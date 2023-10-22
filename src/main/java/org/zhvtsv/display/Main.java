package org.zhvtsv.display;


import java.net.URL;

public class Main {
    //Change this to your file name
    private static final String PATH_TO_IMAGE_IN_RESOURCES = "NAIP03.tif";

    public static void main(String[] args) throws Exception {
        //Read some geoTIF image and display it with the help of geo tools
        URL resource = Main.class.getClassLoader().getResource(PATH_TO_IMAGE_IN_RESOURCES);
        assert resource != null;
        ImageLab.displayGeoTIFFAndInfos(resource.getPath());
    }
}