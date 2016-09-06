package com.mateuszwiater.csc375.a3.heatscale;

public class HeatScale {
    HeatRange[] heatRanges;

    int rangeSize;

    public HeatScale(String[] colors, double maxTemperature) {
        this.heatRanges = new HeatRange[colors.length];
        rangeSize = (int) maxTemperature / colors.length;

        for(int i = 0; i < colors.length; i++) {
            if(i != (colors.length - 1)) {
                heatRanges[i] = new HeatRange(colors[i],(rangeSize * i), (rangeSize * i) + rangeSize - 1,i);
            } else {
                heatRanges[i] = new HeatRange(colors[i],(rangeSize * i), (rangeSize * i) + rangeSize + ((int) maxTemperature % colors.length),i);
            }
        }
    }

    public int getCellColor(double temperature) {
        int color = ((int)temperature) / rangeSize;
        if(color == heatRanges.length) {
            color -= 1;
        }
        return color;
    }

    public HeatRange[] getScale() {
        return heatRanges;
    }
}
