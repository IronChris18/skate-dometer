package com.carberry.chris.ece498_mp1;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.*;
import java.io.IOException;

import com.opencsv.CSVWriter;

import static android.os.SystemClock.uptimeMillis;

/* for writing to csv file */

public class MainActivity extends ActionBarActivity {

    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;

    // pedometer tutorial
    // http://nebomusic.net/androidlessons/Pedometer_Project.pdf
    //values for PEDOMETER/STEPS
    private Button buttonReset;
    private float acceleration;

    //values to Calculate Number of Steps
    private float previousZ;
    private float currentZ;
    private float previousY;
    private float currentY;
    private int numSteps;

    // SeekBar Fields
    private SeekBar seekbar;
    private double threshold;


    //values for csv file
    long timeStamp = System.currentTimeMillis();;
    float Accel_x = 0;
    float Accel_y = 0;
    float Accel_z = 0;
    float Gyro_x = 0;
    float Gyro_y = 0;
    float Gyro_z = 0;
    float Mag_x = 0;
    float Mag_y = 0;
    float Mag_z = 0;
    float Light_intensity = 0;



    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        threshold = 10.5;
        previousZ = 0;
        currentZ = 0;
        numSteps = 0;

        //timeStamp = System.currentTimeMillis();     //time since system boot

        //private inner class
        mSensorListener = new SensorEventListener()
        {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;
                if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    Accel_x = event.values[0];
                    Accel_y = event.values[1];
                    Accel_z = event.values[2];

                    /*logic for pedometer -> # of steps */

                    /* fetch the current y */
                    currentZ = Accel_z;
                    currentY = Accel_y;

                    //Measure if a step is taken
                    if (((currentZ - previousZ) > threshold)|| (currentY - previousY) > threshold){
                        numSteps++;
                    }

                    // store previous y
                    previousZ = currentZ;
                    previousY = currentY;

                }
                if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    Gyro_x = event.values[0];
                    Gyro_y = event.values[1];
                    Gyro_z = event.values[2];
                }
                if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    Mag_x = event.values[0];
                    Mag_y = event.values[1];
                    Mag_z = event.values[2];
                }
                if (sensor.getType() == Sensor.TYPE_LIGHT) {
                    Light_intensity = event.values[0];
                }

                long timeStamp_new = System.currentTimeMillis() - timeStamp;

                //System.out.println("Path: "+Environment.getExternalStorageDirectory().toString()+"/data.csv");

                try
                {
                    CSVWriter writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory().toString()+"/data.csv", true));

                    String[] record = new String [] { Float.toString(timeStamp_new), Float.toString(Accel_x), Float.toString(Accel_y),
                        Float.toString(Accel_z), Float.toString(Gyro_x), Float.toString(Gyro_y), Float.toString(Gyro_z),
                        Float.toString(Mag_x), Float.toString(Mag_y), Float.toString(Mag_z), Float.toString(Light_intensity) };

                    writer.writeNext(record);
                    writer.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                TextView gyro = (TextView) findViewById(R.id.textView);
                gyro.setText("Accel_x: " + Accel_x + "\nAccel_y: " + Accel_y + "\nAccel_z: " + Accel_z
                        + "\nGyro_x: " + Gyro_x + "\nGyro_y: " + Gyro_y + "\nGyro_z: " + Gyro_z + "\nMag_x: " + Mag_x + "\nMag_y: " + Mag_y +
                        "\nMag_z: " + Mag_z + "\nLight: " + Light_intensity);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {


            }

        };


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
            Handle action bar item clicks here. The action bar will
            automatically handle clicks on the Home/Up button, so long
            as you specify a parent activity in AndroidManifest.xml.
        */
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        // Register a listener for each sensor.
        super.onResume();

        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        // important to unregister the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener((SensorEventListener) this);
    }


}

