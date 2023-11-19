package org.zhvtsv.models.stac;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class Feature {
    private String id;
    private double[] boundingBox;
    private Geometry geometry;

    public Feature(String id, double[] boundingBox, Geometry geometry) {
        this.id = id;
        this.geometry = geometry;
    }

    public Feature(String id, double[] boundingBox) {
        this.id = id;
        this.boundingBox = boundingBox;
        this.geometry = null;
    }

    public Feature() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @JsonProperty("bbox")
    public double[] getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(double[] boundingBox) {
        this.boundingBox = boundingBox;
    }

    @Override
    public String toString() {
        return "Feature{" +
                "id='" + id + '\'' +
                ", boundingBox=" + Arrays.toString(boundingBox) +
                ", geometry=" + geometry +
                '}';
    }
}
