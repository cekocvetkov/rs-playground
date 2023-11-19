package org.zhvtsv.models.stac;

import java.util.Arrays;

public class Geometry {
    private double[][][] coordinates;

    public double[][][] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[][][] coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < coordinates[0].length; i++) {
            sb.append(Arrays.toString(coordinates[0][i]));
        }

        return "Geometry{" +
                "coordinates=" + sb +
                '}';
    }
}
