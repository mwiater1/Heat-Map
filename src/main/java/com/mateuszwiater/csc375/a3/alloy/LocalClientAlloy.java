package com.mateuszwiater.csc375.a3.alloy;

import com.mateuszwiater.csc375.a3.util.Global;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class LocalClientAlloy {
    LocalAlloy alloy;

    Global global;

    private DataOutputStream out;
    private DataInputStream in;

    double[] leftBorder;
    double[] rightBorder;

    public LocalClientAlloy(String remoteAddress, int port) {
        this.alloy = new LocalAlloy();
        try {
            Socket socket = new Socket(remoteAddress, port);
            this.out    = new DataOutputStream(socket.getOutputStream());
            this.in     = new DataInputStream(socket.getInputStream());
            out.writeInt(alloy.getCores());
            out.writeUTF(alloy.getHostName());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.global = prepare();
        this.alloy.prepare(global);
        this.leftBorder = new double[global.getLeftBorder() * global.getAlloyHeight()];
        this.rightBorder = new double[global.getRightBorder() * global.getAlloyHeight()];
    }

    public void iterate() {
        while(true) {
            alloy.iterate(leftBorder, rightBorder);
            send(alloy.getTemperatureDifference(), alloy.getLeftBorder(), alloy.getRightBorder(), alloy.getChangedCells());
            receive(leftBorder, rightBorder);
        }
    }

    private Global prepare() {
        try {
            int alloyHeight = in.readInt();
            int alloyWidth = in.readInt();
            int topLeftTemperature = in.readInt();
            int bottomRightTemperature = in.readInt();
            double convergenceThreshold = in.readDouble();
            double[] thermalConstants = new double[in.readInt()];
            for(int i = 0; i < thermalConstants.length; i++) {
                thermalConstants[i] = in.readDouble();
            }
            String[] colors = new String[in.readInt()];
            for(int i = 0; i < colors.length; i++) {
                colors[i] = in.readUTF();
            }
            double maxTemperature = in.readDouble();
            int pixelOffset = in.readInt();
            int leftBorder = in.readInt();
            int rightBorder = in.readInt();
            return new Global(0, alloyHeight, alloyWidth, topLeftTemperature, bottomRightTemperature, convergenceThreshold, thermalConstants, colors, maxTemperature, pixelOffset, leftBorder, rightBorder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void send(double temperatureDifference, double[] leftBorder, double[] rightBorder, StringBuffer changedCells) {

        try {
            for(double d : leftBorder) {
                out.writeDouble(d);
            }
            for(double d : rightBorder) {
                out.writeDouble(d);
            }

            out.writeUTF(changedCells.toString());
            out.writeDouble(temperatureDifference);
            out.flush();
        } catch (IOException e) {
            System.exit(-1);
        }
    }

    private void receive(double[] leftBorder, double[] rightBorder) {
        try {
            for(int i = 0; i < leftBorder.length; i++) {
                leftBorder[i] = in.readDouble();
            }

            for(int i = 0; i < rightBorder.length; i++) {
                rightBorder[i] = in.readDouble();
            }
        } catch (IOException e) {
            System.out.println("Simulation Complete!");
            System.exit(0);
        }
    }
}
