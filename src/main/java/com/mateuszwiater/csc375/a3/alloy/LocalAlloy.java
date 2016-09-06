package com.mateuszwiater.csc375.a3.alloy;

import com.mateuszwiater.csc375.a3.region.Cell;
import com.mateuszwiater.csc375.a3.region.Neighbor;
import com.mateuszwiater.csc375.a3.region.Region;
import com.mateuszwiater.csc375.a3.util.Global;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class LocalAlloy extends Alloy {
    private Cell[][] currentAlloy;
    private Cell[][] oldAlloy;

    private Global global;

    private ForkJoinPool forkJoinPool;

    private RegionCalculator regions;

    public LocalAlloy() {
        super(0, Runtime.getRuntime().availableProcessors());
    }

    @Override
    public void prepare(Global global) {
        this.global         = global;
        this.forkJoinPool   = new ForkJoinPool();
        this.leftBorder     = new double[global.getLeftBorder() * global.getAlloyHeight()];
        this.rightBorder    = new double[global.getRightBorder() * global.getAlloyHeight()];
        this.currentAlloy   = new Cell[global.getAlloyHeight()][global.getAlloyWidth() + global.getLeftBorder() + global.getRightBorder()];
        this.regions        = new RegionCalculator(Region.getRegions(forkJoinPool.getParallelism(), currentAlloy.length, currentAlloy[0].length, global.getLeftBorder(), global.getRightBorder()));

        System.out.println("Alloy Width: " + global.getAlloyWidth() + " Alloy Height: " + global.getAlloyHeight());

        // Generate the heatMap
        for(int i = 0; i < currentAlloy.length; i++ ) {
            for(int j = 0; j < currentAlloy[0].length; j++) {
                if((i == 0) && (j == 0) && (global.getLeftBorder() != 1)) {
                    // Set the top left cell
                    currentAlloy[i][j] = new Cell(true, global.getTopLeftTemperature(), global, getNeighbors(j,i));
                    changedCells.append(CellColor.getJson(j,i,global.getHeatScale().getCellColor(global.getTopLeftTemperature()), global));
                } else if((i == (global.getAlloyHeight() - 1)) && (j == (global.getAlloyWidth() - 1)) && (global.getRightBorder() != 1)) {
                    // Set the bottom right cell
                    currentAlloy[i][j] = new Cell(true, global.getBottomRightTemperature(), global, getNeighbors(j,i));
                    changedCells.append(CellColor.getJson(j,i,global.getHeatScale().getCellColor(global.getBottomRightTemperature()), global));
                } else {
                    currentAlloy[i][j] = new Cell(global, getNeighbors(j,i));
                }
            }
        }

        // Create a copy of the heatMap
        oldAlloy = currentAlloy.clone();
    }

    private void swap() {
        Cell[][] tmp = oldAlloy;
        oldAlloy = currentAlloy;
        currentAlloy = tmp;
    }

    private Neighbor[] getNeighbors(int cellX, int cellY) {
        int x;
        int y;

        ArrayList<Neighbor> neighbors = new ArrayList<>();

        for(int i = 0; i < 4; i++) {
            if(i == 0) {
                // Get cell above
                x = cellX;
                y = cellY + 1;
            } else if(i == 1) {
                // Get cell below
                x = cellX;
                y = cellY - 1;
            } else if(i == 2) {
                // Get cell to the left
                x = cellX - 1;
                y = cellY;
            } else {
                // Get cell to the right
                x = cellX + 1;
                y = cellY;
            }

            // Make sure the neighbor is not off the screen
            if((x >= 0) && (y >= 0) && (x < currentAlloy[0].length) && (y < currentAlloy.length)) {
                neighbors.add(new Neighbor(x,y));
            }
        }

        // Return the array of neighbors
        return neighbors.toArray(new Neighbor[neighbors.size()]);
    }


    private void setLeftBorder(double[] leftBorder) {
        for(int i = 0; i < leftBorder.length; i++) {
            oldAlloy[i][0].setTemperature(leftBorder[i]);
        }
    }

    private void setRightBorder(double[] rightBorder) {
        for(int i = 0; i < rightBorder.length; i++) {
            oldAlloy[i][oldAlloy[0].length - 1].setTemperature(rightBorder[i]);
        }
    }

    @Override
    public void iterate(double[] leftBorder, double[] rightBorder) {
        changedCells.setLength(0);
        // Set the borders
        setLeftBorder(leftBorder);
        setRightBorder(rightBorder);

        // Reinitialize the regions
        regions.reinitialize();

        // Simulate the alloy
        temperatureDifference = forkJoinPool.invoke(regions);
        // Swap the alloy arrays
        swap();
    }

    @Override
    public double[] getLeftBorder() {
        for(int i = 0; i < leftBorder.length; i++) {
            leftBorder[i] = oldAlloy[i][1].getTemperature();
        }
        //System.out.println("LEFT::: " + Arrays.toString(leftBorder));
        return leftBorder;
    }

    @Override
    public double[] getRightBorder() {
        for(int i = 0; i < rightBorder.length; i++) {
            rightBorder[i] = oldAlloy[i][oldAlloy[0].length - 2].getTemperature();
        }
        //System.out.println("RIGHT::: " + Arrays.toString(rightBorder));
        return rightBorder;
    }

    private class RegionCalculator extends RecursiveTask<Double> {
        private RegionCalculator[] regionCalculators;

        private Region myRegion;

        public RegionCalculator(List<Region> regions) {
            this.myRegion = regions.get(0);

            if(regions.size() == 1) {
                // Compute region
                regionCalculators = new RegionCalculator[0];
            } else if(regions.size() == 2) {
                // Compute region and spawn one regionCalculator
                regionCalculators = new RegionCalculator[1];
                regionCalculators[0] = new RegionCalculator(regions.subList(1,2));
            } else {
                // Compute region and spawn two regionCalculators
                regionCalculators = new RegionCalculator[2];
                regionCalculators[0] = new RegionCalculator(regions.subList(1,(regions.size() / 2) + 1));
                regionCalculators[1] = new RegionCalculator(regions.subList((regions.size() / 2) + 1,regions.size()));
            }
        }

        @Override
        protected Double compute() {
            // Fork the regionCalculators
            for(RegionCalculator regionCalculator : regionCalculators) {
                regionCalculator.reinitialize();
                regionCalculator.fork();
            }
            // Perform own calculation
            double temperatureDifference = computeTemperatures(myRegion);

            // Join the regionCalculators
            for(RegionCalculator regionCalculator : regionCalculators) {
                temperatureDifference = Math.max(temperatureDifference, regionCalculator.join());
            }


            // Return the highest temperature difference
            return temperatureDifference;
        }

        // Calculate the temperature for a given region
        private double computeTemperatures(Region region) {
            double highestTemperatureDifference = 0;

            // Iterate through the region
            for(int i = region.getX1(); i <= region.getX2(); i++) {
                for(int j = region.getY1(); j <= region.getY2(); j++) {
                    // Calculate the cells new temperature
                    int cellColorId = currentAlloy[j][i].calculateTemperature(oldAlloy);
                    if(cellColorId != -1) {
                        // The cells color has changed, update the canvas
                        changedCells.append(CellColor.getJson(i, j, cellColorId, global));
                    }
                    // Calculate the highest temperature difference
                    highestTemperatureDifference = Math.max(highestTemperatureDifference, currentAlloy[j][i].getTemperatureDifference());
                }
            }

            // Return the highest temperature difference
            return highestTemperatureDifference;
        }
    }

    private static class CellColor {

        private CellColor() {
            // Prevent instantiation
        }

        public static String getJson(int x, int y, int colorId, Global global) {
            return "[" + ((x - global.getLeftBorder()) + global.getPixelOffset()) + "," + y + "," + colorId + "],";
        }
    }
}
