package com.example.esp32camapp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpListener extends Thread {

    private boolean listening = true;
    private OnIpReceivedListener listener;

    public interface OnIpReceivedListener {
        void onIpReceived(String ip);
    }

    public void setOnIpReceivedListener(OnIpReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(4210);
            byte[] buffer = new byte[1024];
            while (listening) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.startsWith("ESP32_IP:")) {
                    String ip = message.replace("ESP32_IP:", "").trim();
                    if (listener != null) {
                        listener.onIpReceived(ip);
                    }
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopListening() {
        listening = false;
    }
}