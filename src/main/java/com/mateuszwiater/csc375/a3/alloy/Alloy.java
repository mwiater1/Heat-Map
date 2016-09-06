package com.mateuszwiater.csc375.a3.alloy;

import com.mateuszwiater.csc375.a3.util.Global;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Alloy implements Comparable<Alloy>{
    private int id;
    private int cores;

    protected double temperatureDifference;

    private String hostName;

    protected StringBuffer changedCells;

    protected double[] leftBorder;
    protected double[] rightBorder;

    protected Alloy(int id, int cores) {
        this(id, cores, "");
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    protected Alloy(int id, int cores, String hostName) {
        this.id = id;
        this.cores = cores;
        this.hostName = hostName;
        this.temperatureDifference = 0;
        this.changedCells = new StringBuffer();
    }

    public void prepare(Global global) {}

    public void iterate(double[] leftBorder, double[] rightBorder) {
        new Exception("CANNOT ITERATE BEFORE PREPARING").printStackTrace();
        System.exit(-1);
    }

    public double[] getLeftBorder() {
        return leftBorder;
    }

    public double[] getRightBorder() {
        return  rightBorder;
    }

    public StringBuffer getChangedCells() {
        if(changedCells.length() != 0) {
            changedCells.delete(changedCells.length() - 1, changedCells.length());
        }
        return changedCells;
    }

    public double getTemperatureDifference() {
        return temperatureDifference;
    }

    public int getCores() {
        return cores;
    }

    public String getHostName() {
        return hostName;
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(Alloy o) {
        if(o.getCores() == this.getCores()) {
            return 0;
        } else if(o.getCores() > this.getCores()) {
            return 1;
        } else {
            return -1;
        }
    }
}
