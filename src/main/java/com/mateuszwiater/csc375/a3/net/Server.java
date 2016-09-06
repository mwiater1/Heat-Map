package com.mateuszwiater.csc375.a3.net;

import com.mateuszwiater.csc375.a3.alloy.Alloy;
import com.mateuszwiater.csc375.a3.alloy.RemoteAlloy;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private Server() {
        // Private to prevent instantiation
    }

    public static ArrayList<Alloy> getServerClients(int desiredConnections, int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Alloy> clients = new ArrayList<>(desiredConnections);

        for(int i = 1; i <= desiredConnections; i++) {
            try {
                Socket socket = serverSocket.accept();
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                Alloy client = new RemoteAlloy(i, inputStream.readInt(), inputStream.readUTF(), socket);
                System.out.println("Client " + client.getHostName() + " Connected With " + client.getCores() + " Cores");
                clients.add(client);
                //inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return clients;
    }

}
