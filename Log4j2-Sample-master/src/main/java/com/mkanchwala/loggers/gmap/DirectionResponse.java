package com.mkanchwala.loggers.gmap;

import java.util.List;

/**
 *
 * @author SAMIR-PC
 */
public class DirectionResponse {
    
    private List<String> destination_addresses;
    private List<String> origin_addresses;
    private List<DistanceInfo> rows;
    private String status;

    public DirectionResponse() {
    }

    public DirectionResponse(List<String> destination_addresses, List<String> origin_addresses, List<DistanceInfo> rows, String status) {
        this.destination_addresses = destination_addresses;
        this.origin_addresses = origin_addresses;
        this.rows = rows;
        this.status = status;
    }

    public List<String> getDestination_addresses() {
        return destination_addresses;
    }

    public void setDestination_addresses(List<String> destination_addresses) {
        this.destination_addresses = destination_addresses;
    }

    public List<String> getOrigin_addresses() {
        return origin_addresses;
    }

    public void setOrigin_addresses(List<String> origin_addresses) {
        this.origin_addresses = origin_addresses;
    }

    public List<DistanceInfo> getRows() {
        return rows;
    }

    public void setRows(List<DistanceInfo> rows) {
        this.rows = rows;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    
    
}

