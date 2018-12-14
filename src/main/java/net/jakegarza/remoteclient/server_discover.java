package net.jakegarza.remoteclient;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

public class server_discover extends AppCompatActivity {

    private static Context context;
    private static HashSet<String> servers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_discover);
        context = this;

        LinearLayout LLServers = findViewById(R.id.LLServers);
        servers = new HashSet<String>();

     
        //Start getting messages from servers on the network
        Thread multicastThread = new Thread(new MulticastReceiver(LLServers));
        multicastThread.start();
    }

    public static Context getContext() { return context; }
    public static HashSet<String> getServers() { return servers; }
}
