package com.mateuszwiater.csc375.a3.region;

import com.mateuszwiater.csc375.a3.alloy.Alloy;

import java.util.ArrayList;

public class Region {
    private int x1;
    private int y1;
    private int x2;
    private int y2;

    public Region(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public static ArrayList<Region> getRegions(int numberOfRegions, int height, int width, int leftBorder, int rightBorder) {
        ArrayList<Region> regions = new ArrayList<>();
        int regionOffset = width % numberOfRegions;
        int regionWidth = (width - regionOffset) / numberOfRegions;

        for(int i = 0; i < (numberOfRegions - 1); i++) {
            regions.add(new Region((i * regionWidth) + leftBorder, 0, (i * regionWidth) + regionWidth - 1 + leftBorder, height - 1));
        }
        regions.add(new Region(((numberOfRegions - 1) * regionWidth) + leftBorder, 0, ((numberOfRegions - 1) * regionWidth) + regionWidth - 1 + regionOffset - rightBorder, height - 1));

        return regions;
    }

    public static ArrayList<Region> getRegions(ArrayList<Alloy> alloys, int height, int width) {
        int totalServerCores = 0;
        // Calculate the total number of server cores
        for(Alloy alloy : alloys) {
            totalServerCores += alloy.getCores();
        }

        ArrayList<Region> regions = getRegions(totalServerCores, height, width, 0, 0);
        ArrayList<Region> adjustedRegions = new ArrayList<>();

        int previousIteration = 0;
        for(Alloy alloy : alloys) {
            adjustedRegions.add(new Region(regions.get(previousIteration).getX1(),0,regions.get(previousIteration + alloy.getCores() - 1).getX2(),height - 1));
            previousIteration += alloy.getCores();
        }

        return adjustedRegions;
    }
}
