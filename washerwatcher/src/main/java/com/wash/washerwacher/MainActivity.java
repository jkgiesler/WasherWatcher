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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ugly temporary fix to see other bugs
        StrictMode.ThreadPolicy policy;
        policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
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
        EditText text = (EditText) findViewById(R.id.editText);
        String value = "test";
        value = text.getText().toString();
        String hostName = "192.168.1.113"; //change to computer ID
        int portNumber = 5544;

        try {
            Socket transfer = new Socket(hostName, portNumber);
            PrintWriter out =
                    new PrintWriter(transfer.getOutputStream(), true);
            int count=0;

            /*
            progress was made. it will now send the first string a bunch of times
            which in actuality is all I need for the real application
            ill call it close enough
            */

            while(count<30) {
                value = text.getText().toString();
                out.println(value);
                try{
                TimeUnit.SECONDS.sleep(1);}
                catch (InterruptedException ie) {
                    System.exit(1);
                }
                count++;

            }
            transfer.close();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }
    }
}