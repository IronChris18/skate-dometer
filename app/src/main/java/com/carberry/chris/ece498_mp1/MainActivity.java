package com.carberry.chris.ece498_mp1;

import android.app.ActionBar;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.TextView;

import android.app.Application;
import com.google.android.gms.maps.model.LatLng;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

// http://developer.android.com/guide/topics/media/audio-capture.html
// Do they want this put in an mp3 file or something? What do they want in CSV?
// WIFI STUFF, what info do they want in the CSV?
// http://developer.android.com/reference/android/net/wifi/WifiManager.html
// pedometer tutorial
// http://nebomusic.net/androidlessons/Pedometer_Project.pdf

public class MainActivity extends ActionBarActivity {
    //sensor manager
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;




    //values to Calculate Number of Steps & jumps
    private float previousZ = 0;
    private float currentZ = 0;
    private int numSteps = 0;
    private int numPushes = 0;
    private int distance = 0;
    private int stepLength = 2;

    // thresholds
    private double push_threshold = 15;
    private double threshold = 11.5;

    //values for csv file
    long timeStamp = System.currentTimeMillis();
    float Accel_x = 0;
    float Accel_y = 0;
    float Accel_z = 0;
    float Gyro_x = 0;
    float Gyro_y = 0;
    float Gyro_z = 0;
    float Mag_x = 0;
    float Mag_y = 0;
    float Mag_z = 0;
    //float Light_intensity = 0;

    //sensor flags
    int accel, gyro, mag = 0;
    int pos_slope_flag = 0;
    int pos_push_flag = 0;

    //timing variables
    long cur_time = 0;
    long cur_time_push = 0;
    float time_duration = 0;
    long timer_start = 0;

    //compass variables
    float Rotation = 0;
    int rotate_Flag = 0;
    float azimuthInRadians = 0;
    float azimuthInDegrees = 0;
    ArrayList angular_velocity = new ArrayList();
    float avg_velocity = 0.0f;
    float sum_of_velocities = 0.0f;
    int sum = 0;

    //smoothing variables
    static final float ALPHA = 0.2f;
    float[] smooth_accel_vals;
    private float[] accel_last = {0,0,0};

    // Create a constant to convert nanoseconds to seconds.

    private float timestamp;

    public void button_send(View view) {
        Intent intent = new Intent(this, gps.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        // Register a listener for each sensor.
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        // important to unregister the sensor when the activity pauses.
        mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));

        super.onPause();
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSensorData();
        final globalVariables gv = (globalVariables) getApplicationContext();
        gv.setDegrees(azimuthInRadians);
        gv.setPushes(numPushes);
    }

    public void getSensorData() {

        //for sensors in general
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //for wifi
        //WifiManager mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        //private inner class
        mSensorListener = new SensorEventListener() {

            //for compass
            float[] mGravity;
            float[] mGeomagnetic;


            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;

                if (sensor.getType() == Sensor.TYPE_ACCELEROMETER && accel != 1) {

                    // Smooth out the data so we can better understand thresholds
                    //smooth_accel_vals = highPass(event.values);
                    //System.print.out(smooth_accel_vals);

                    //For the compass functionality
                    mGravity = event.values;

                    Accel_x = event.values[0];
                    Accel_y = event.values[1];
                    Accel_z = event.values[2];
                    accel = 1;

                    /* fetch the current Z */
                    currentZ = Math.abs(Accel_z);
                }
                if (sensor.getType() == Sensor.TYPE_GYROSCOPE && gyro != 1) {

                    Gyro_x = event.values[0];
                    Gyro_y = event.values[1];
                    Gyro_z = event.values[2];
                    gyro = 1;


                }






            if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && mag != 1) {

                    //for the compass functionality
                    mGeomagnetic = event.values;

                    Mag_x = event.values[0];
                    Mag_y = event.values[1];
                    Mag_z = event.values[2];
                    mag = 1;
                }


                /*
                 * COMPASS DATA COLLECTION
                 *      a north based azimuth gives the number of degrees from north, the degrees can be seen at
                 *      0 or 360 degrees = NORTH
                 *      90 degrees = EAST
                 *      180 " = SOUTH
                 *      270 " = WEST
                 */
                if (mGravity != null && mGeomagnetic != null) {
                    float R[] = new float[9];
                    float I[] = new float[9];
                    boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                    if (success) {
                        float orientation[] = new float[3];
                        SensorManager.getOrientation(R, orientation);
                        azimuthInRadians = orientation[0]; // orientation contains: azimuth, pitch and roll
                        azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);
                        if (azimuthInDegrees < 0.0f) {
                            azimuthInDegrees += 360.0f;
                        }
                    }

                }


                //Measure if a step is taken
                if ((mag == 1) && (gyro == 1) && (accel == 1)) {
                    // step logic
                    if (currentZ - previousZ > 0) {
                        pos_slope_flag = 1;     //set flag if the previous step has finished
                        pos_push_flag = 1;
                    }
                    if ((currentZ > threshold) && (pos_slope_flag == 1) && (currentZ - previousZ < 0) && (System.currentTimeMillis() - cur_time > 150)) {
                        cur_time = System.currentTimeMillis();  //reset timer
                        numSteps++;     //count total # of steps
                        pos_slope_flag = 0;
                    }
                    //push logic
                    if ((currentZ > push_threshold) && (pos_push_flag == 1) && (currentZ - previousZ < 0) && (System.currentTimeMillis() - cur_time_push > 500)) {
                        cur_time_push = System.currentTimeMillis();  //reset timer
                        numPushes++;     //count total # of pushes
                        pos_push_flag = 0;

                    }

                    previousZ = currentZ;

                    //set sensor flags back to 0
                    mag = 0;
                    gyro = 0;
                    accel = 0;

                    long timeStamp_new = System.currentTimeMillis() - timeStamp;

                    /*
                    try {
                        CSVWriter writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory().toString() + "/data.csv", true));

                        String[] record = new String[]{Float.toString(timeStamp_new), Float.toString(Accel_x), Float.toString(Accel_y),
                                Float.toString(Accel_z), Float.toString(Gyro_x), Float.toString(Gyro_y), Float.toString(Gyro_z),
                                Float.toString(Mag_x), Float.toString(Mag_y), Float.toString(Mag_z), Float.toString(Light_intensity), Float.toString(azimuthInDegrees),
                                Float.toString(level)};

                        writer.writeNext(record);
                        writer.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    */

                    /* ANGULAR DISPLACEMENT
                     *
                     * If we reach the threshold, then we begin our timer (timer_start), we keep adding
                     * to the dynamic array of gyro values as long as we stay above the threshold, if we
                     * dip below the threshold (aka: stop turning) then we
                     * go into the next "if" statement, Then we take the avg of the gyro values
                     * and multiply by the time duration and add to the total rotation
                     */

                    if ((Gyro_z > 1.1 || Gyro_z < -1.1))       //if we are turning
                    {
                        angular_velocity.add(Math.abs(Gyro_z)); //need absolute value
                        if (angular_velocity.size() == 1) {
                            timer_start = System.currentTimeMillis();   //begin turn
                        }
                        rotate_Flag = 1; //give ok to calculate rotation
                    }


                    if (rotate_Flag == 1 && (Gyro_z < 1.1 && Gyro_z > -1.1))      //if turn is finished
                    {
                        time_duration = System.currentTimeMillis() - timer_start; //calculate length of turn

                        if (angular_velocity.size() != 0) {
                            for (int i = 0; i < angular_velocity.size(); i++) {
                                sum_of_velocities += Math.abs((float) angular_velocity.get(i));
                            }
                            avg_velocity = sum_of_velocities / angular_velocity.size();
                            sum = angular_velocity.size();
                            angular_velocity.clear();
                        }
                        time_duration = time_duration / 1000.0f;
                        Rotation = Rotation + (float) Math.toDegrees((avg_velocity * time_duration));
                        sum_of_velocities = 0;
                        rotate_Flag = 0;
                    }

                    TextView gyro = (TextView) findViewById(R.id.textView);
                    gyro.setText("Time_Stamp: " + timeStamp_new + "\nAccel_x: " + Accel_x + "\nAccel_y: " + Accel_y + "\nAccel_z: "
                            + Accel_z + "\nGyro_x: " + Gyro_x + "\nGyro_y: " + Gyro_y + "\nGyro_z: " + Gyro_z + "\nMag_x: " + Mag_x + "\nMag_y: " + Mag_y +
                            "\nMag_z: " + Mag_z + "\nSteps: " + numSteps + "\nPushes: " + numPushes +
                            "\nRotation: " + Rotation + "\nCompass: " + azimuthInDegrees);

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                //do nothing
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


    /***********************************************************************
    *                                MATH
    ************************************************************************/
    // TODO: calibrate device, turn off landscape changes
    // http://stackoverflow.com/questions/10119479/calculating-lat-and-long-from-bearing-and-distance


/*
    public float[] calculate_distance( float[] input) {


    }
*/

    /*
    * High pass filter
    *   Inputs:
    *     acceleration -> input from the sensor
    *
    *   Outputs:
    *     result -> filtered result
    */
    /*
    public float[] highPass(float[] acceleration) {
        //ramp-speed - play with this value until satisfied, NO IDEA
        float kFilteringFactor = 0.1f;
        //result.x,.y,.z is the filtered result
        float[] result = {0,0,0};
        //high-pass filter to eliminate gravity
        //accel_last might be the actual force of gravity
        accel_last[0] = acceleration.x * kFilteringFactor + accel_last[0] * (1.0f - kFilteringFactor);
        accel_last[1] = acceleration.y * kFilteringFactor + accel_last[1] * (1.0f - kFilteringFactor);
        accel_last[2] = acceleration.z * kFilteringFactor + accel_last[2] * (1.0f - kFilteringFactor);
        result.x = acceleration.x - accel_last[0];
        result.y = acceleration.y - accel_last[1];
        result.z = acceleration.z - accel_last[2];

        return result;
    }
*/
    /*
     * http://stackoverflow.com/questions/10119479/calculating-lat-and-long-from-bearing-and-distance
     *
     * Inputs:
     *  latitude_old = initial latitude reading we will start from
     *  longitude_old = " " longitude
     *	bearing = current compass value (I think)
     *	distance = distance traveled since last latitude and longitude was gathered
     *
     *Returns:
     *	LatLng = new estimated latitude and longitude
     */
    float bearing_old = 0;
    float bearing_new = 0;

    public LatLng calc_LatLng_DR(float latitude_old, float longitude_old, float bearing_old, float bearing_new, float distance) {
        // I think the math needs the distance to be in kilometers
        double dist = distance / 1000;
        // I might need to change for positive negative values
        double brng = Math.toRadians(bearing_new - bearing_old);
        double lat1 = Math.toRadians(latitude_old);
        double lon1 = Math.toRadians(longitude_old);

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
        double a = Math.atan2(Math.sin(brng) * Math.sin(dist) * Math.cos(lat1), Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
        //System.out.println("a = " +  a);
        double lon2 = lon1 + a;

        // Not sure if this part is needed
        lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        System.out.println("Latitude = " + Math.toDegrees(lat2) + "\nLongitude = " + Math.toDegrees(lon2));

        LatLng new_LatLng = new LatLng(lat2, lon2);

        return new_LatLng;
    }


    /*
     *
     *  NOTE: we may want to also use an "avg" function to smooth data further between several data samples
     *
     *
     */
    // public float[] avg_data( float[] input){ }

}



