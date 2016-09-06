package com.mateuszwiater.csc375.a3.util;

import com.mateuszwiater.csc375.a3.heatscale.HeatScale;
import com.mateuszwiater.csc375.a3.region.Region;

import java.util.Arrays;

public class Global {
    private int maxIterations;
    private int alloyHeight;
    private int alloyWidth;
    private int topLeftTemperature;
    private int bottomRightTemperature;
    private int pixelOffset;
    private int leftBorder;
    private int rightBorder;

    private double convergenceThreshold;
    private double maxTemperature;

    private double[] thermalConstants;

    private HeatScale heatScale;

    public Global(int maxIterations, int alloyHeight, int topLeftTemperature, int bottomRightTemperature, double convergenceThreshold, double[] thermalConstants, String[] colors, double maxTemperature) {
        this.maxIterations          = maxIterations;
        this.alloyHeight            = alloyHeight;
        this.alloyWidth             = alloyHeight * 2;
        this.topLeftTemperature     = topLeftTemperature;
        this.bottomRightTemperature = bottomRightTemperature;
        this.convergenceThreshold   = convergenceThreshold;
        this.thermalConstants       = thermalConstants;
        this.heatScale              = new HeatScale(colors, maxTemperature);
        this.pixelOffset            = 0;
        this.leftBorder             = 0;
        this.rightBorder            = 0;
        this.maxTemperature         = maxTemperature;
    }

    public Global(int maxIterations, int alloyHeight, int alloyWidth ,int topLeftTemperature, int bottomRightTemperature, double convergenceThreshold, double[] thermalConstants, String[] colors, double maxTemperature, int pixelOffset, int leftBorder, int rightBorder) {
        this(maxIterations, alloyHeight, topLeftTemperature, bottomRightTemperature, convergenceThreshold, thermalConstants, colors, maxTemperature);
        this.alloyWidth = alloyWidth;
        this.pixelOffset = pixelOffset;
        this.leftBorder = leftBorder;
        this.rightBorder = rightBorder;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getAlloyHeight() {
        return alloyHeight;
    }

    public int getAlloyWidth() {
        return alloyWidth;
    }

    public int getTopLeftTemperature() {
        return topLeftTemperature;
    }

    public int getBottomRightTemperature() {
        return bottomRightTemperature;
    }

    public double getConvergenceThreshold() {
        return convergenceThreshold;
    }

    public double[] getThermalConstants() {
        return thermalConstants;
    }

    public HeatScale getHeatScale() {
        return heatScale;
    }

    public int getPixelOffset() {
        return pixelOffset;
    }

    public int getLeftBorder() {
        return leftBorder;
    }

    public int getRightBorder() {
        return rightBorder;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public static Global toGlobal(Region region, Global global) {
        int topLeftTemperature      = 0;
        int bottomRightTemperature  = 0;
        int leftBorder              = 1; // Treat as a boolean
        int rightBorder             = 1; // Treat as a boolean

        if(region.getX1() == 0 && region.getY1() == 0) {
            topLeftTemperature = global.getTopLeftTemperature();
            leftBorder = 0;
        }
        if(region.getX2() == (global.getAlloyWidth() - 1) && region.getY2() == (global.getAlloyHeight() - 1)) {
            bottomRightTemperature = global.getBottomRightTemperature();
            rightBorder = 0;
        }

        String[] colors = new String[global.getHeatScale().getScale().length];
        for(int i = 0; i < global.getHeatScale().getScale().length; i++) {
            colors[i] = global.getHeatScale().getScale()[i].getColor();
        }

        return new Global(global.getMaxIterations(), global.getAlloyHeight(), region.getX2() - region.getX1() + 1, topLeftTemperature, bottomRightTemperature, global.getConvergenceThreshold(), global.getThermalConstants(), colors, global.getMaxTemperature(), region.getX1(), leftBorder, rightBorder);
    }
}