package org.zhvtsv.display;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.map.GridReaderLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.StyleLayer;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.*;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.SafeAction;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.ContrastMethod;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class ImageLab {

    private StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    private JMapFrame frame;
    private GridCoverage2DReader reader;

    public static void displayGeoTIFFAndInfos(String pathToGeoTIFF) throws Exception {
        ImageLab me = new ImageLab();
        me.getLayersAndDisplay(pathToGeoTIFF);
    }

    private void getLayersAndDisplay(String pathToGeoTIFF) throws Exception {
        System.out.println("Path to geoTIF");
        System.out.println(pathToGeoTIFF);
        File imageFile = new File(pathToGeoTIFF);
        displayLayers(imageFile);
    }

    private void displayLayers(File rasterFile) throws Exception {
        AbstractGridFormat format = GridFormatFinder.findFormat(rasterFile);
        reader = format.getReader(rasterFile);

        // Check if the reader is of type GeoTiffReader
        if (reader instanceof GeoTiffReader) {
            System.out.println("The file is a GeoTIFF.");
        } else {
            System.out.println("The file is not a GeoTIFF.");
            throw new RuntimeException("File is not GeoTIFF");
        }

        printSomeData(rasterFile);

        // Initially display the raster in greyscale using the
        // data from the first image band
//        Style rasterStyle = createGreyscaleStyle(1);
        Style rasterStyle = createRGBStyle();

        // Set up a MapContent with the one layers
        final MapContent map = new MapContent();
        map.setTitle("GeoTIFF Raster");

        Layer rasterLayer = new GridReaderLayer(reader, rasterStyle);
        System.out.println(rasterLayer);
        map.addLayer(rasterLayer);

        // Create a JMapFrame with a menu to choose the display style for the
        frame = new JMapFrame(map);
        frame.setSize(800, 600);
        frame.enableStatusBar(true);
//        frame.enableTool(JMapFrame.Tool.ZOOM, JMapFrame.Tool.PAN, JMapFrame.Tool.RESET);
        frame.enableToolBar(true);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        JMenu menu = new JMenu("Raster");
        menuBar.add(menu);

        menu.add(new SafeAction("Grayscale display") {
            public void action(ActionEvent e) throws Throwable {
                Style style = createGreyscaleStyle();
                if (style != null) {
                    ((StyleLayer) map.layers().get(0)).setStyle(style);
                    frame.repaint();
                }
            }
        });

        menu.add(new SafeAction("RGB display") {
            public void action(ActionEvent e) throws Throwable {
                Style style = createRGBStyle();
                if (style != null) {
                    ((StyleLayer) map.layers().get(0)).setStyle(style);
                    frame.repaint();
                }
            }
        });
        // Finally display the map frame.
        // When it is closed the app will exit.
        frame.setVisible(true);
    }

    private Style createGreyscaleStyle() {
        GridCoverage2D cov = null;
        try {
            cov = reader.read(null);
        } catch (IOException giveUp) {
            throw new RuntimeException(giveUp);
        }
        int numBands = cov.getNumSampleDimensions();
        Integer[] bandNumbers = new Integer[numBands];
        for (int i = 0; i < numBands; i++) {
            bandNumbers[i] = i + 1;
        }
        Object selection = JOptionPane.showInputDialog(frame, "Band to use for greyscale display", "Select an image band", JOptionPane.QUESTION_MESSAGE, null, bandNumbers, 1);
        if (selection != null) {
            int band = ((Number) selection).intValue();
            return createGreyscaleStyle(band);
        }
        return null;
    }

    private Style createGreyscaleStyle(int band) {
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
        SelectedChannelType sct = sf.createSelectedChannelType(String.valueOf(band), ce);

        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);
    }

    private Style createRGBStyle() {
        GridCoverage2D cov = null;
        try {
            cov = reader.read(null);
        } catch (IOException giveUp) {
            throw new RuntimeException(giveUp);
        }
        // We need at least three bands to create an RGB style
        int numBands = cov.getNumSampleDimensions();

        if (numBands < 3) {
            return null;
        }
        // Get the names of the bands
        String[] sampleDimensionNames = new String[numBands];
        for (int i = 0; i < numBands; i++) {
            GridSampleDimension dim = cov.getSampleDimension(i);
            sampleDimensionNames[i] = dim.getDescription().toString();
        }
        final int RED = 0, GREEN = 1, BLUE = 2;
        int[] channelNum = {-1, -1, -1};
        // We examine the band names looking for "red...", "green...", "blue...".
        // Note that the channel numbers we record are indexed from 1, not 0.
        for (int i = 0; i < numBands; i++) {
            String name = sampleDimensionNames[i].toLowerCase();
            if (name != null) {
                if (name.matches("red.*")) {
                    channelNum[RED] = i + 1;
                } else if (name.matches("green.*")) {
                    channelNum[GREEN] = i + 1;
                } else if (name.matches("blue.*")) {
                    channelNum[BLUE] = i + 1;
                }
            }
        }
        // If we didn't find named bands "red...", "green...", "blue..."
        // we fall back to using the first three bands in order
        if (channelNum[RED] < 0 || channelNum[GREEN] < 0 || channelNum[BLUE] < 0) {
            channelNum[RED] = 1;
            channelNum[GREEN] = 2;
            channelNum[BLUE] = 3;
        }
        // Now we create a RasterSymbolizer using the selected channels
        SelectedChannelType[] sct = new SelectedChannelType[cov.getNumSampleDimensions()];
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
        for (int i = 0; i < 3; i++) {
            sct[i] = sf.createSelectedChannelType(String.valueOf(channelNum[i]), ce);
        }
        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct[RED], sct[GREEN], sct[BLUE]);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);
    }

    private void printSomeData(File rasterFile) throws IOException, FactoryException, TransformException {
        GeoTiffFormat geoTiffFormat = new GeoTiffFormat();

        GridCoverage2D coverage = geoTiffFormat.getReader(rasterFile).read(null);

        GridGeometry2D gridGeometry = coverage.getGridGeometry();

        System.out.println("Grid Geometry: ");
        System.out.println(gridGeometry);

        // Get the GridEnvelope
        GridEnvelope gridEnvelope = gridGeometry.getGridRange();
        System.out.println("Grid Envelope");
        System.out.println(gridEnvelope);
        Envelope2D bbox = coverage.getEnvelope2D();
        //        me.printBBOX( bbox );

        int nOver = coverage.getNumOverviews();
        if (nOver > 0) {
            System.out.println("" + nOver + " overviews");
        }
        GridEnvelope gridRange2D = coverage.getGridGeometry().getGridRange();
        System.out.println("dimension: " + gridRange2D.getDimension());
        for (int i = 0; i < gridRange2D.getDimension(); i++) {
            System.out.println("dimension " + i + " pixel range " + gridRange2D.getLow(i) + " - " + gridRange2D.getHigh(i));
        }

        CoordinateReferenceSystem wgs84 = DefaultGeographicCRS.WGS84;
        CoordinateReferenceSystem target = coverage.getCoordinateReferenceSystem();// CRS.decode("EPSG:3997",
        // true);
        GeometryFactory gf = new GeometryFactory();
        MathTransform targetToWgs = CRS.findMathTransform(target, wgs84);
        int count = 0;
        for (int j = gridRange2D.getLow(1); j < gridRange2D.getHigh(1); j++) {
            for (int i = gridRange2D.getLow(0); i < gridRange2D.getHigh(0); i++) {

                if (count++ > 10)
                    return;
                GridCoordinates2D coord = new GridCoordinates2D(i, j);
                DirectPosition p = coverage.getGridGeometry().gridToWorld(coord);
                Point point = gf.createPoint(new Coordinate(p.getOrdinate(0), p.getOrdinate(1)));
                Geometry wgsP = JTS.transform(point, targetToWgs);
                System.out.format("(%d %d) -> POINT(%.2f %.2f) -> POINT(%.2f %.2f)%n", i, j, point.getCoordinate().x, point.getCoordinate().y, wgsP.getCentroid().getCoordinate().x, wgsP.getCentroid().getCoordinate().y);
            }
        }
    }

}