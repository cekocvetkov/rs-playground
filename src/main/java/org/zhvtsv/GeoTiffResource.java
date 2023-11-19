package org.zhvtsv;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URL;


@Path("/geotiff")
public class GeoTiffResource {
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getGeoTiff() {
        URL resource = getClass().getClassLoader().getResource("NAIP03.tif");
        assert resource != null;
//        File imageFile = new File(getClass().getClassLoader().getResourceAsStream("NAIP03.tif"));

        return Response.ok(getClass().getClassLoader().getResourceAsStream("responseBG.tif"))
                .header("Content-Type", "image/tiff")
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Headers",
                        "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Methods",
                        "GET, POST, PUT, DELETE, OPTIONS, HEAD").build();
    }
}
