package com.mateuszwiater.csc375.a3.alloy;

import com.mateuszwiater.csc375.a3.heatscale.HeatRange;
import com.mateuszwiater.csc375.a3.util.Global;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Layout of communication
 *
 *  -- Object Creation --
 *  1. Server starts accepting connections
 *  2. Clients initiate a connection
 *      a. Client sends 2 things
 *          I. Number of cores (int)
 *          II. Hostname (String)
 *  3. Using the 2 things the server creates a ServerClient object
 *
 *  -- Setup --
 *  1. Server runs the prepare() method.
 *      a. Server sends the configuration data to the client so that the client can setup its Alloy
 *      b. Server sends these 7 things to the client
 *          1. Alloy height (int)
 *          2. Alloy width (int)
 *          3. topLeftTemperature (int)
 *          4. bottomRightTemperature (int)
 *          5. thermalConstantsArraySize (int)
 *          6. thermalConstants (doubleArray)
 *          7. colorsArraySize (int)
 *          8. colors (stringArray)
 *          9. pixelOffset (int)
 *          10. leftBorder (int)
 *          11. rightBorder (int)
 *
 *  -- Iteration --
 *  1. Server runs the iteration() method
 *      a. The iteration method sends over the needed borders
 *          1. leftBorder (doubleArray)
 *          2 rightBorder (doubleArray)
 *  2. Server runs the update method
 *      a. This updates the borders and changed cells
 *          1. Update leftBorder
 *          2. Update rightBorder
 *          3. Update changedCells
 *          4. Update currentTemperatureDifference
 */
public class RemoteAlloy extends Alloy {
    private DataOutputStream out;
    private DataInputStream in;

    private boolean isUpdated;

    public RemoteAlloy(int id, int cores, String hostName, Socket socket) {
        super(id, cores, hostName);
        this.changedCells = new StringBuffer();
        this.temperatureDifference = 0;
        this.isUpdated = false;
        try {
            this.out    = new DataOutputStream(socket.getOutputStream());
            this.in     = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void update() {
        try {
            for (int i = 0; i < leftBorder.length; i++) {
                leftBorder[i] = in.readDouble();
            }

            for (int i = 0; i < rightBorder.length; i++) {
                rightBorder[i] = in.readDouble();
            }

            changedCells.append(in.readUTF());
            temperatureDifference = in.readDouble();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isUpdated = true;
    }

    @Override
    public void prepare(Global global) {
        this.leftBorder = new double[global.getLeftBorder() * global.getAlloyHeight()];
        this.rightBorder = new double[global.getRightBorder() * global.getAlloyHeight()];

        try {
            out.writeInt(global.getAlloyHeight());
            out.writeInt(global.getAlloyWidth());
            out.writeInt(global.getTopLeftTemperature());
            out.writeInt(global.getBottomRightTemperature());
            out.writeDouble(global.getConvergenceThreshold());
            out.writeInt(global.getThermalConstants().length);
            for(double d : global.getThermalConstants()) {
                out.writeDouble(d);
            }
            out.writeInt(global.getHeatScale().getScale().length);
            for(HeatRange h : global.getHeatScale().getScale()) {
                out.writeUTF(h.getColor());
            }
            out.writeDouble(global.getMaxTemperature());
            out.writeInt(global.getPixelOffset());
            out.writeInt(global.getLeftBorder());
            out.writeInt(global.getRightBorder());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void iterate(double[] leftBorder, double[] rightBorder) {
        isUpdated = false;
        changedCells.setLength(0);
        // Send the borders to the client
        try {
            for (double d : leftBorder) {
                out.writeDouble(d);
            }

            for (double d : rightBorder) {
                out.writeDouble(d);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public StringBuffer getChangedCells() {
        if(!isUpdated) {
            update();
        }
        return changedCells;
    }

    @Override
    public double[] getLeftBorder() {
        if(!isUpdated) {
            update();
        }
        return leftBorder;
    }

    @Override
    public double[] getRightBorder() {
        if(!isUpdated) {
            update();
        }
        return rightBorder;
    }
}
