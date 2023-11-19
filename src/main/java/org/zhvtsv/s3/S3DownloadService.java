package org.zhvtsv.s3;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class S3DownloadService {
    private static Configuration getConfig() throws ConfigurationException {
        Configuration config = new Configurations().properties(new File("application.properties"));
        String token = config.getString("sentinel.oauth2.token");
        String sentinelHubProcessApiUrl = config.getString("sentinel.process-api.url");
        return config;
    }

    private static AwsBasicCredentials basicAWSCredentials() {
        Configuration config = null;
        try {
            config = new Configurations().properties(new File("application.properties"));
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        String awsAccessKey = config.getString("aws.s3.accessKey");
        System.out.println(awsAccessKey);
        String awsSecretKey = config.getString("aws.s3.secretKey");
        System.out.println(awsSecretKey);

        return AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
    }


    private static S3Client amazonS3() throws ConfigurationException, URISyntaxException {
        S3Client s3 = S3Client.builder().forcePathStyle(true).credentialsProvider(S3DownloadService::basicAWSCredentials)
                .region(Region.of(Region.US_EAST_1.id())).endpointOverride(new URI("https://eodata.dataspace.copernicus.eu")).build();

        return s3;
    }

    public static void main(String[] args) throws ConfigurationException, IOException, URISyntaxException {
        // Specify your S3 bucket name and the key (object key) of the file you want to download
        String bucketName = "eodata.dataspace.copernicus.eu";
        downloadZipFile();
//        S3Client s3 = amazonS3();
//
//
////        s3.setEndpoint("https://eodata.dataspace.copernicus.eu");  //ECS IP Address
//
////        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder().bucket("eodata").delimiter().build()
//        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket("eodata").key("S2A_MSIL1C_20231116T100301_N0509_R122_T33TUG_20231116T120121.SAFE/").build();
//        ResponseInputStream<GetObjectResponse> responseResponseBytes = s3.getObject(getObjectRequest);
//
//        byte[] data = responseResponseBytes.readAllBytes();
//
//        // Write the data to a local file.
//        java.io.File myFile = new java.io.File("S2A_MSIL2A_20230719T100031_N0509_R122_T32TQM_20230719T134659.SAFE");
//        OutputStream os = new FileOutputStream(myFile);
//        os.write(data);
//        System.out.println("Successfully obtained bytes from an S3 object");
//        os.close();
    }

    public static void downloadZipFile() {
        try {
            URL url = new URL("https://zipper.dataspace.copernicus.eu/odata/v1/Products(04698ee2-2a59-4c0c-8cb8-e6f61be68b67)/$value");
            URLConnection conn = url.openConnection();
            InputStream in = conn.getInputStream();
            FileOutputStream out = new FileOutputStream("test.zip");
            byte[] b = new byte[1024];
            int count;
            while ((count = in.read(b)) >= 0) {
                out.write(b, 0, count);
            }
            out.flush();
            out.close();
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
