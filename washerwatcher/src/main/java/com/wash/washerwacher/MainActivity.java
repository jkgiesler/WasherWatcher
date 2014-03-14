package com.wash.washerwacher;

import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ActionBarActivity {
    private Socket socket;

    private static final int portNumber = 5544;
    private static final String hostName = "192.168.1.113";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new ClientThread()).start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void onClick(View v) {
        try {
            EditText text = (EditText) findViewById(R.id.editText);
            String value = text.getText().toString();
            if(value == null) value = "nothing";

            PrintWriter out;
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(value);

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }
    }

    class ClientThread implements Runnable {
        //creates the socket in a separate thread
        @Override
        public void run(){
            try {
                socket = new Socket(hostName,portNumber);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}