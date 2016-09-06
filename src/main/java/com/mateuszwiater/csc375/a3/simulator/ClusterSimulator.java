package com.mateuszwiater.csc375.a3.simulator;

import com.mateuszwiater.csc375.a3.Main;
import com.mateuszwiater.csc375.a3.alloy.Alloy;
import com.mateuszwiater.csc375.a3.alloy.LocalAlloy;
import com.mateuszwiater.csc375.a3.net.Server;
import com.mateuszwiater.csc375.a3.region.Region;
import com.mateuszwiater.csc375.a3.util.Global;
import com.mateuszwiater.csc375.a3.websocket.WebSocketManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ClusterSimulator implements Simulator {
    Global global;

    double[][] leftBorder;
    double[][] rightBorder;

    ArrayList<Alloy> alloys;

    double temperatureDifference;

    StringBuffer changedCells;

    public ClusterSimulator(int desiredClients, int port, Global global) {
        this.alloys = Server.getServerClients(desiredClients, port);
        this.alloys.add(new LocalAlloy());
        this.global = global;
        this.leftBorder = new double[alloys.size()][];
        this.rightBorder = new double[alloys.size()][];
        this.temperatureDifference = 0;
    }

    @Override
    public void start() {
        changedCells = new StringBuffer();
        int localAlloy = 0;
        Collections.sort(alloys);
        ArrayList<Region> regions = Region.getRegions(alloys, global.getAlloyHeight(), global.getAlloyWidth());
        // Prepare all of the servers
        for(int i = 0; i < alloys.size(); i++) {
            Global g = Global.toGlobal(regions.get(i), global);
            leftBorder[i] = new double[g.getLeftBorder() * g.getAlloyHeight()];
            rightBorder[i] = new double[g.getRightBorder() * g.getAlloyHeight()];
            alloys.get(i).prepare(g);
        }


        for(int i = 0; i < global.getMaxIterations(); i++) {
            temperatureDifference = 0;
            // Start the Iterations
            for(int j = 0; j < alloys.size(); j++) {
                if(alloys.get(j).getId() != localAlloy) {
                    alloys.get(j).iterate(getLeftBorder(j), getRightBorder(j));
                }
            }

            alloys.get(localAlloy).iterate(getLeftBorder(localAlloy), getRightBorder(localAlloy));

            // Update values
            for(int j = 0; j < alloys.size(); j++) {
                temperatureDifference = Math.max(temperatureDifference, alloys.get(j).getTemperatureDifference());
                leftBorder[j] = alloys.get(j).getLeftBorder();
                rightBorder[j] = alloys.get(j).getRightBorder();
                changedCells.append(alloys.get(j).getChangedCells().toString());
                if(!(alloys.get(j).getChangedCells().length() == 0)) {
                    changedCells.append(",");
                }
            }
            if(changedCells.length() != 0) {
                changedCells.delete(changedCells.length() - 1, changedCells.length());
            }
            changedCells.append("],");
            changedCells.append(i);
            changedCells.append(",");
            changedCells.append(temperatureDifference);
            changedCells.append("]");
            // Send the Json
            WebSocketManager.sendMessage("[[" + changedCells.toString());

            // Check if the alloy converged
            if(temperatureDifference <= global.getConvergenceThreshold()) {
                System.out.println("METAL CONVERGED");
                System.exit(0);
            }

            // Clear the Json string
            changedCells.setLength(0);
        }
        System.out.println("RAN OUT OF ITERATIONS");
    }

    private double[] getLeftBorder(int i) {
        if(i == 0) {
            return leftBorder[0];
        } else {
            return rightBorder[i - 1];
        }
    }

    private double[] getRightBorder(int i) {
        if(i == (leftBorder.length - 1)) {
            return rightBorder[(leftBorder.length - 1)];
        } else {
            return leftBorder[i + 1];
        }
    }
}
