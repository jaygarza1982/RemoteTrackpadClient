package net.jakegarza.remoteclient;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceiver extends Thread {
    protected MulticastSocket socket = null;
    protected byte[] buf = new byte[256];
    private LinearLayout linearLayout;

    public MulticastReceiver(LinearLayout linearLayout) {
        this.linearLayout = linearLayout;
    }

    public void run() {
        try {
            socket = new MulticastSocket(4446);
            InetAddress group = InetAddress.getByName("224.0.2.60");
            socket.joinGroup(group);
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                final String message = packet.getAddress()+""; // + " " + received;


                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        TextView tv = new TextView(server_discover.getContext());
                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.i("Clicked", message + " textview was clicked.");

                                //Start the new activity which will control the remote computer
                                MainActivity.ip = message.substring(1);
                                Context context = server_discover.getContext();
                                Intent intent = new Intent(context, MainActivity.class);
                                context.startActivity(intent);
                            }
                        });
                        tv.setLayoutParams(lparams);
                        tv.setText(message);
                        //Do not redisplay messages
                        if (!server_discover.getServers().contains(message)) {
                            server_discover.getServers().add(message);
                            linearLayout.addView(tv);
                        }
                    }
                });

                //this.m_vwJokeLayout.addView(tv);
                //linearLayout.addView(new View(server_discover.getContext()));
                Log.i("Multicast message", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}