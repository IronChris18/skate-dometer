package com.carberry.chris.ece498_mp1;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

import com.opencsv.CSVWriter;



import java.io.IOException;

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
    private float previousY;
    private float currentY;
    private int numSteps;

    // SeekBar Fields
    private SeekBar seekbar;
    private int threshold;


    //values for csv file
    float timeStamp = 0;
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

        threshold = 10;
        previousY = 0;
        currentY = 0;
        numSteps = 0;



        //FileWriter writer = new FileWriter("sensor_data.csv");
        try {
            FileWriter writer = new FileWriter("sensor_data.csv");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }


        // This is what we found on the stack overflow, something is screwed up though, context might be incorrect
        //http://stackoverflow.com/questions/4343342/is-there-a-way-to-retrieve-multiple-sensor-data-in-android

        timeStamp = System.currentTimeMillis();     //time since system boot



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
                    currentY = Accel_y;

                    //Measure if a step is taken
                    if (Math.abs(currentY - previousY) > threshold){
                        numSteps++;
                    }

                    // store previous y
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

                for (int i = 0; i % 1 == 0; i++)
                {
                    float timeStamp_new = System.currentTimeMillis() - timeStamp;

                    //String csv = "data.csv";

                    File file = new File("data.csv");
                    String csv = file.getAbsolutePath();

                    CSVWriter writer = new CSVWriter(new FileWriter(csv, true));

                    String record = Float.toString(timeStamp_new)+"&"+Float.toString(Accel_x)+"&"+Float.toString(Accel_y)+"&"+
                            Float.toString(Accel_z)+"&"+Float.toString(Gyro_x)+"&"+Float.toString(Gyro_y)+"&"+Float.toString(Gyro_z)+"&"+
                            Float.toString(Mag_x)+"&"+Float.toString(Mag_y)+"&"+Float.toString(Mag_z)+"&"+Float.toString(Light_intensity);

                    writer.writeNext(record).split("&");

                    writer.close();


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

        /*
        // USE THIS FOR CHECKING IF PHONE HAS THE FEATURE
        PackageManager PM= this.getPackageManager();
        boolean gyro = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        boolean light = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT);
        */

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

    /*
    first attempt at writing csv, this may need to take an existing file and append to it that way
    depending on how timing works with the sensors
    */
    public void generateCsvFile(String sFilename)
    {           //call this function onClick (start)

        boolean alreadyExists = new File(sFilename).exists();

        try
        {
            if (!alreadyExists)
            {
                FileWriter writer = new FileWriter(sFilename);
            }

            boolean i = true;
            // Need to fix this while loop with a button?

                float timeStamp_new = System.currentTimeMillis() - timeStamp;


                writer.append(Float.toString(timeStamp_new));
                writer.append(',');
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

            //generate whatever data you want

            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

    }

    // Chris -> I think this has to do with calling our stuff. onCreate should start up
    // as soon as we start a new activity
    /*public void sendMessage(View view) {
        Intent intent = new Intent(this, MySensorActivity.class);
    }*/
}





//mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
//mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
//mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
//mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_GAME);





/********************************************************
 *              Begin sensor class
 *******************************************************/

/*
class MySensorActivity extends MainActivity {


    private Sensor mGyroSensor;
    private Sensor mLightSensor;
    private Sensor mAccelerometer;
    private Sensor Magnetometer;


    // pedometer tutorial
    // http://nebomusic.net/androidlessons/Pedometer_Project.pdf
    //values for PEDOMETER/STEPS
    private Button buttonReset;
    private float acceleration;

    //values to Calculate Number of Steps
    private float previousY;
    private float currentY;
    private int numSteps;

    // SeekBar Fields
    private SeekBar seekbar;
    private int threshold;


    //values for csv file
    float timeStamp;
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
*/
/*    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        threshold = 10;
        previousY = 0;
        currentY = 0;
        numSteps = 0;

        // This is what we found on the stack overflow, something is screwed up though, context might be incorrect
        //http://stackoverflow.com/questions/4343342/is-there-a-way-to-retrieve-multiple-sensor-data-in-android
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        timeStamp = uptimeMillis();     //time since system boot


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

                    //logic for pedometer -> # of steps

                    // fetch the current y
                    currentY = Accel_y;

                    //Measure if a step is taken
                    if (Math.abs(currentY - previousY) > threshold){
                           numSteps++;
                    }

                    // store previous y
                    previousY = currentY;

                } if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    Gyro_x = event.values[0];
                    Gyro_y = event.values[1];
                    Gyro_z = event.values[2];
                } if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    Mag_x = event.values[0];
                    Mag_y = event.values[1];
                    Mag_z = event.values[2];
                } if (sensor.getType() == Sensor.TYPE_LIGHT) {
                    Light_intensity = event.values[0];
                }
            }

            //@Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {
                //
            }
        };

        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_GAME);


        // USE THIS FOR CHECKING IF PHONE HAS THE FEATURE
        //PackageManager PM= this.getPackageManager();
        //boolean gyro = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        //boolean light = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT);


    }*/

/*    @Override
    protected void onStart() {
        // Register a listener for the sensor.
        super.onStart();
        mSensorManager.registerListener((SensorEventListener) this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // important to unregister the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener((SensorEventListener) this); //not sure if correct
    }

    //
    //first attempt at writing csv, this may need to take an existing file and append to it that way
    //depending on how timing works with the sensors

    public void generateCsvFile(String sFilename)
    {           //call this function onClick (start)
        try
        {
            FileWriter writer = new FileWriter(sFilename);

            // Need to fix this while loop with a button?
            while(1) { //while !onClick(Stop)    race condition?
                writer.append(Float.toString(timeStamp));
                writer.append(',');
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

}
*/