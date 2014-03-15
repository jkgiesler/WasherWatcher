package com.wash.washerwacher;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


public class MainActivity extends ActionBarActivity {
    private Socket socket;

    private static final int portNumber = 5544;
    private static final String hostName = "192.168.1.113";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new ClientThread()).start();
        //new Thread(new AudioThread()).start();
        new Thread(new TwitterThread()).start();
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
    @Override
    public void onStop(){
        super.onStop();
        Thread.currentThread().interrupt();
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
            //System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            //System.exit(1);
        }
    }

    class ClientThread implements Runnable {
        //creates the socket in a separate thread
        @Override
        public void run(){
            try {
                socket = new Socket(hostName,portNumber);
            } catch (UnknownHostException e) {
                //trying this to get rid of fatal error
                //doesn't seem to work
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                //trying this to get rid of fatal error
                Thread.currentThread().interrupt();
            }
        }
    }
    class AudioThread implements Runnable {
        private static final int RECORDER_SAMPLERATE =44100;
        private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
        private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;


        @Override
        public void run(){
            int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                    RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,RECORDER_SAMPLERATE,
                    RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING,bufferSize);


            short data [] = new short [bufferSize];
            double average = 0;
            while(!Thread.interrupted())
            {
                average = 0;
                recorder.startRecording();

                recorder.read(data,0,bufferSize);

                recorder.stop();

                for(short s : data) {
                    average += Math.abs(s);

                    //http://stackoverflow.com/questions/10579184/android-audiorecord-amplitude-reading-from-mic
                }
                String avg = Double.toString(average);
                Log.i("Audio level",avg);
                try {
                    Thread.sleep(10);
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }



        }
    }

    class TwitterThread implements Runnable {

        @Override
        public void run(){
            //config setup
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey("**")
                    .setOAuthConsumerSecret("**")
                    .setOAuthAccessToken("**")
                    .setOAuthAccessTokenSecret("**")
                    .setUseSSL(true);
            TwitterFactory tf = new TwitterFactory(cb.build());
            Twitter twitter = tf.getInstance();
            //and tweet

            try {
                Status status = twitter.updateStatus("@jgiesler your laundry is done");
            } catch (TwitterException e) {
                e.printStackTrace();
            }


        }
    }
}