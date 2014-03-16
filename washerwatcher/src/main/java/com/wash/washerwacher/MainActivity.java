package com.wash.washerwacher;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private Socket socket; //for network
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final float NOISE = (float).5; //for accelerometer
    private static final int portNumber = 5544; //for network
    private static final String hostName = "192.168.1.113"; //for network

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing the accelerometer
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);


        new Thread(new ClientThread()).start();
        new Thread(new AudioThread()).start();
        //new Thread(new TwitterThread()).start();
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
    @Override
    public void onResume(){
        super.onResume();
        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onPause(){
        super.onPause();
        //mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){
        //what do I do with this!
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

   public void onSensorChanged(SensorEvent event){
       //code to calculate change in acceleration in XYZ
       float x = event.values[0];
       float y = event.values[1];
       float z = event.values[2];
       float mLastX=0;
       float mLastY=0;
       float mLastZ=0;
       if (!mInitialized){
           mLastX = x;
           mLastY = y;
           mLastZ = z;

           mInitialized = true;
       } else {
           float deltaX=(mLastX-x);
           float deltaY=(mLastY-y);
           float deltaZ=(mLastZ-z);

           if (deltaX<NOISE) deltaX =(float)0.0;
           if (deltaY<NOISE) deltaY =(float)0.0;
           if (deltaZ<NOISE) deltaZ =(float)0.0;

           mLastX = x;
           mLastY = y;
           mLastZ = z;

           String logx=Float.toString(deltaX);
           String logy=Float.toString(deltaY);
           String logz=Float.toString(deltaZ);
           Log.i("X "+logx+" Y "+logy+" Z ",logz);
       }



   }





   //Classes Go Here
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
        //does this thread require the presence of an sd card?...
        //ugh. why doesn't this work on 4.0.3 and works on 4.4...

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
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                average = 0;
            }



        }
    }

    class TwitterThread implements Runnable {

        @Override
        public void run(){
            //config setup --keep keys secret
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey("")
                    .setOAuthConsumerSecret("")
                    .setOAuthAccessToken("")
                    .setOAuthAccessTokenSecret("")
                    .setUseSSL(true);
            TwitterFactory tf = new TwitterFactory(cb.build());
            Twitter twitter = tf.getInstance();
            //and tweet + getting around twitter not liking duplicates
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
            String formattedDate = sdf.format(date);
            String strUsr = "jgiesler";
            try {
                Status status = twitter.updateStatus("@"+strUsr+" your laundry is done "+formattedDate);
            } catch (TwitterException e) {
                e.printStackTrace();
            }


        }
    }
}