package com.mkanchwala.loggers.gmap;

import java.util.List;

/**
 *
 * @author SAMIR-PC
 */
public class DistanceInfo {

    private List<DistanceElements> elements;

    public DistanceInfo(List<DistanceElements> elements) {
        this.elements = elements;
    }

    public DistanceInfo() {
    }

    public List<DistanceElements> getElements() {
        return elements;
    }

    public void setElements(List<DistanceElements> elements) {
        this.elements = elements;
    }

}
