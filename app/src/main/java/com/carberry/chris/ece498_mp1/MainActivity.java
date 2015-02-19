package com.carberry.chris.ece498_mp1;

import android.hardware.Sensor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;


import android.widget.Button;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Textview

//for writing to csv file
import java.io.FileWriter;
import java.io.IOException;



public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


/********************************************************
 *              Begin sensor class
 *******************************************************/

public class MySensorActivity extends MainActivity implements SensorEventListener {

    /*
    private Sensor mGyroSensor;
    private Sensor mLightSensor;
    private Sensor mAccelerometer;
    private Sensor Magnetometer;
    */

    float Accel_x;
    float Accel_y;
    float Accel_z;
    float Gyro_x;
    float Gyro_y;
    float Gyro_z;
    float Mag_x;
    float Mag_y;
    float Mag_z;
    float Light_intensity;

    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;


    // first attempt at writing csv, this may need to take an existing file and append to it that way
    // depending on how timing works with the sensors
    public void generateCsvFile(String sFilename)
    {
        try
        {
            FileWriter writer = new FileWriter(sFileName);

            // Need to fix this while loop with a button?
            while(1) {
                writer.append(Float.toString(Accel_x));
                writer.append(',');
                writer.append(Float.toString(Accel_y));
                writer.append(',');
                writer.append(Float.toString(Accel_z));
                writer.append(',');
                writer.append(Float.toString(Gyro_x));
                writer.append(',');
                writer.append(Float.toString(Gyro_y));
                writer.append(',');
                writer.append(Float.toString(Gyro_z));
                writer.append(',');
                writer.append(Float.toString(Mag_x));
                writer.append(',');
                writer.append(Float.toString(Mag_y));
                writer.append(',');
                writer.append(Float.toString(Mag_z));
                writer.append(',');
                writer.append(Float.toString(Light_intensity));
                writer.append('\n');

            }

            //generate whatever data you want

            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         // Get an instance of the sensor service
        // GyroSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //mGyroSensor= mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        // This is what we found on the stack overflow, something is screwed up though, context might be incorrect
        //http://stackoverflow.com/questions/4343342/is-there-a-way-to-retrieve-multiple-sensor-data-in-android
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
            mSensorListener = new SensorEventListener() {

            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;
                if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    Accel_x = event.values[0];
                    Accel_y = event.values[1];
                    Accel_z = event.values[2];
                }else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    Gyro_x = event.values[0];
                    Gyro_y = event.values[1];
                    Gyro_z = event.values[2];
                }else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    Mag_x = event.values[0];
                    Mag_y = event.values[1];
                    Mag_z = event.values[2];
                }else if (sensor.getType() == Sensor.TYPE_LIGHT) {
                    Light_intensity = event.values[0];
                }
            }
        }

        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_GAME);

        /*
        // USE THIS FOR CHECKING IF PHONE HAS THE FEATURE
        PackageManager PM= this.getPackageManager();
        boolean gyro = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        boolean light = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT);
        */

    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // important to unregister the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

}
