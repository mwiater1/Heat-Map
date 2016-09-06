package com.mateuszwiater.csc375.a3.simulator;

import com.mateuszwiater.csc375.a3.alloy.LocalClientAlloy;

public class ClientSimulator implements Simulator {
    LocalClientAlloy localClientAlloy;

    public ClientSimulator(String remoteAddress, int port) {
        this.localClientAlloy = new LocalClientAlloy(remoteAddress, port);
    }

    @Override
    public void start() {
        localClientAlloy.iterate();
    }
}
