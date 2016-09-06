package com.mateuszwiater.csc375.a3.heatscale;

public class HeatRange {
    String color;

    int start;
    int end;
    int id;

    public HeatRange(String color, int start, int end, int id) {
        this.color = color;
        this.start = start;
        this.end = end;
        this.id = id;
    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }

    public int getId() {
        return id;
    }

    public String getColor() {
        return color;
    }
}
