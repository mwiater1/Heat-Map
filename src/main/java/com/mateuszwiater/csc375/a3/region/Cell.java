package com.mateuszwiater.csc375.a3.region;

import com.mateuszwiater.csc375.a3.util.Global;

import java.util.concurrent.ThreadLocalRandom;

public class Cell {
    Global global;

    private double[] metalPercentage;

    private boolean constant;

    private int colorId;

    private Neighbor[] neighbors;

    private double temperature;
    private double temperatureDifference;

    // General use constructor
    public Cell(Global global, Neighbor[] neighbors) {
        this.global                 = global;
        this.neighbors              = neighbors;
        this.metalPercentage        = new double[3];
        this.temperature            = 0;
        this.temperatureDifference  = 0;
        this.constant               = false;
        this.colorId                = global.getHeatScale().getCellColor(temperature);

        // Generate the metal percentages
        generateMetalPercentage();
    }

    // Special constructor for corner cells
    public Cell(boolean constant, double temperature, Global global, Neighbor[] neighbors) {
        this(global, neighbors);
        this.constant = constant;
        this.temperature = temperature;
        this.colorId = global.getHeatScale().getCellColor(temperature);
    }

    private void generateMetalPercentage() {
        metalPercentage[0] = ThreadLocalRandom.current().nextInt(1,101);
        metalPercentage[1] = ThreadLocalRandom.current().nextInt(1,101);
        metalPercentage[2] = ThreadLocalRandom.current().nextInt(1,101);

        double sum = metalPercentage[0] + metalPercentage[1] + metalPercentage[2];

        metalPercentage[0] /= sum;
        metalPercentage[1] /= sum;
        metalPercentage[2] /= sum;
    }

    // Calculate the temperature and return a color id
    public int calculateTemperature(Cell[][] oldHeatMap) {
        if(!constant) {
            double tmp2;
            double tmp1 = 0;

            // Calculate the new temperature
            for(int i = 0; i < global.getThermalConstants().length; i++) {
                tmp2 = 0;
                for(int j = 0; j < neighbors.length; j++) {
                    tmp2 += oldHeatMap[neighbors[j].getY()][neighbors[j].getX()].getTemperature() * oldHeatMap[neighbors[j].getY()][neighbors[j].getX()].getMetalPercentage()[i];
                }
                tmp1 += global.getThermalConstants()[i] * tmp2;
            }
            tmp1 /= neighbors.length;

            tmp2 = global.getMaxTemperature();
            if(tmp1 > tmp2) {
                tmp1 = tmp2;
            }
            temperatureDifference = Math.abs(tmp1 - temperature);
            temperature = tmp1;
        }

        // Get thew new cell color
        int tColorId = global.getHeatScale().getCellColor(temperature);
        if(tColorId != colorId) {
            colorId = tColorId;
            return tColorId;
        } else {
            return -1;
        }
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getTemperatureDifference() {
        return temperatureDifference;
    }

    public double[] getMetalPercentage() {
        return metalPercentage;
    }
}
