package com.mateuszwiater.csc375.a3.simulator;

import com.mateuszwiater.csc375.a3.alloy.LocalAlloy;
import com.mateuszwiater.csc375.a3.util.Global;
import com.mateuszwiater.csc375.a3.websocket.WebSocketManager;

public class SingleSimulator implements Simulator {
    Global global;

    double[] leftBorder;
    double[] rightBorder;

    public SingleSimulator(Global global) {
        this.global = global;
        this.leftBorder = new double[0];
        this.rightBorder = new double[0];
    }

    @Override
    public void start() {
        LocalAlloy alloy = new LocalAlloy();

        alloy.prepare(global);
        StringBuffer changedCells;

        for(int i = 0; i < global.getMaxIterations(); i++) {
            alloy.iterate(leftBorder, rightBorder);
            changedCells = alloy.getChangedCells();
            // append the Json with desired values
            changedCells.append("],");
            changedCells.append(i);
            changedCells.append(",");
            changedCells.append(alloy.getTemperatureDifference());
            changedCells.append("]");
            // Send the Json
            System.out.println("[[" + changedCells.toString());
            WebSocketManager.sendMessage("[[" + changedCells.toString());

            // Check if the alloy converged
            if(alloy.getTemperatureDifference() <= global.getConvergenceThreshold()) {
                System.out.println("METAL CONVERGED");
                System.exit(0);
            }

            // Clear the Json string
            changedCells.setLength(0);
            // Start the Json
            changedCells.append("[[");
        }

        System.out.println("RAN OUT OF ITERATIONS");
    }
}
