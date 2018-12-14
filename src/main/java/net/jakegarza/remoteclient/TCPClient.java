package net.jakegarza.remoteclient;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient {
    private Socket socket;

    public TCPClient(Socket socket) {
        this.socket = socket;
    }

    public void print(String message) throws Exception {
        //Socket socket = new Socket(MainActivity.ip, 25566);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(message);
    }
}