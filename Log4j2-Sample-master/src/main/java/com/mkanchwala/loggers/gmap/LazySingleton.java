package com.mkanchwala.loggers.gmap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class LazySingleton {

    private static volatile Client client = null;

    private LazySingleton() {
    }

    public static Client getClient() {
        if (client == null) {
            synchronized (LazySingleton.class) {
                client = ClientBuilder.newClient();
            }
        }
        return client;
    }
}

