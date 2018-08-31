package com.mkanchwala.loggers.gmap;


import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

//https://gitlab.com/samiragayarov/distanceMatrixDemoInJava
//https://stackoverflow.com/questions/48475803/how-to-get-distance-from-google-distance-matrix-api-in-java

//Google Map Key : https://console.developers.google.com/apis/credentials/key/47?authuser=0&project=guestlist-1528051618022&pli=1

public class ApartmentClient {

    private static final String BASE_URI = "https://maps.googleapis.com/maps/api";
    private static final String API_KEY = "AIzaSyCOqekJA9O2fjhAivTI90gojOMudMfitOU";

    /**
     * end point for read distanceMatrix
     */
    private WebTarget distanceMatrix;

    public ApartmentClient() {
        Client client = LazySingleton.getClient();
        distanceMatrix = client.target(BASE_URI + "/distancematrix");

    }

    public DirectionResponse getDirectionInfo(String origins, String destionations) throws IOException {
        return distanceMatrix.path("/json")
                .queryParam("units", "imperial")
                .queryParam("origins", origins)
                .queryParam("destinations", destionations)
                .queryParam("key", API_KEY)
                .request()
                .get()
                .readEntity(DirectionResponse.class);
    }
    
    public static void main(String[] args) throws IOException {
        ApartmentClient wc = new ApartmentClient();
        
        //DirectionResponse direction = wc.getDirectionInfo("19.247720,72.850071", "19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609|19.252947,72.842609");
        DirectionResponse direction = wc.getDirectionInfo("19.247720,72.850071", "19.0517349,72.83282680000002|19.1092611,72.82559719999995");

        
        System.out.println("Duration in hours :"+direction.getRows().get(0).getElements().get(0).getDuration().getText());
        System.out.println("Distance  in metr : "+direction.getRows().get(0).getElements().get(0).getDistance().getValue());
        System.out.println("Distance  in metr : "+direction.getRows().get(0).getElements().get(1).getDistance().getValue());
    }
}

