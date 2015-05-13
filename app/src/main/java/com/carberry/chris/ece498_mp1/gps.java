package com.carberry.chris.ece498_mp1;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class gps extends FragmentActivity{

    //sensor manager
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;

    //values to Calculate Number of Steps & jumps
    private float previousZ = 0;
    private float currentZ = 0;
    private int numSteps = 0;
    private int numPushes = 0;
    private double distance = 0;
    private int stepLength = 2;

    // thresholds
    private double push_threshold = 15;
    private double threshold = 11.5;

    //values for csv file
    long timeStamp = System.currentTimeMillis();
    int lin = 0;
    float Lin_x = 0;
    float Lin_y = 0;
    float Lin_z = 0;
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

    int velocity_low_flag = 0;
    double velocity = 0.0;
    double sensorTimeStamp = 0.0;
    double sensorTimeStamp_old = 0.0;
    // Create a constant to convert nanoseconds to seconds.

    //Map stuff
    int timer_flag = 0;
    LatLng dr_loc = new LatLng(0,0);
    LatLng loc = new LatLng(0, 0);
    int if_dr = 0;
    double new_distance = 0;
    double new_dr_distance = 0;
    double dr_dist = 0;
    int dr_flag = 0;
    double latitude;
    double longitude;
    int flag = 0;
    LatLng prev = new LatLng(0,0);
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        // important to unregister the sensor when the activity pauses.
        mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        setUpMapIfNeeded();

        //turn on gps
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);

        //get sensor data
        getSensorData();
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
            criteria.setAltitudeRequired(true);
            criteria.setSpeedRequired(true);
            criteria.setCostAllowed(true);
            criteria.setBearingRequired(true);

            //API level 9 and up
            criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
            criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
            criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
            criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);

            //set latlng variables
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            //LatLng dr_loc = new LatLng(latitude,longitude);
            //LatLng loc = new LatLng(latitude, longitude);
            loc = new LatLng(latitude, longitude);
            //http://stackoverflow.com/questions/2741403/get-the-distance-between-two-geo-points
            Location loc1 = new Location("");
            Location dr_loc1 = new Location("");

            loc1.setLatitude(latitude);
            loc1.setLongitude(longitude);
            Location loc2 = new Location("");
            loc2.setLatitude(prev.latitude);
            loc2.setLongitude(prev.longitude);
            float distanceInMeters = loc2.distanceTo(loc1);

            if (distanceInMeters > 10 && dr_flag > 3 && timer_flag < 4){
                //use dead reckoning
                timer_flag++;
                dr_loc = calc_LatLng_DR( prev, azimuthInRadians, (float)distance);
                dr_loc1.setLatitude(dr_loc.latitude);
                dr_loc1.setLongitude(dr_loc.longitude);
                float dr_distance = loc2.distanceTo(dr_loc1);
                Log.d("DAWG", "dr_loc1: "+dr_loc1);
                Log.d("DAWG","prev: "+prev);
                if (mMap != null) {
                    if (flag == 0)  //when the first update comes, we have no previous points,hence this
                    {
                        prev = dr_loc;
                        flag = 1;
                    }
                    //Add values to map markers
                    Log.d("DAWG","dr_loc: "+dr_loc);
                    mMap.addMarker(new MarkerOptions().position(dr_loc).title("Dist_dr: " + new_dr_distance).snippet("dist_gps: " + new_distance));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dr_loc, 19.0f));
                    mMap.addPolyline((new PolylineOptions())
                            .add(prev, dr_loc).width(6).color(Color.BLUE)
                            .visible(true));
                    prev = dr_loc;


                    new_dr_distance += dr_distance;
                    if_dr = 1;
                }
            }
            else {
                if (mMap != null) {
                    if (flag == 0)  //when the first update comes, we have no previous points,hence this
                    {
                        prev = loc;
                        flag = 1;
                    }
                    if(if_dr == 1){
                        if_dr = 0;
                        loc = prev;
                    }
                    timer_flag = 0;
                    //Add values to map markers
                    mMap.addMarker(new MarkerOptions().position(loc).title("Dist: " + new_distance).snippet("dr: " + new_dr_distance));
                    //zoom camera to current location
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 19.0f));
                    //create line between points on map
                    mMap.addPolyline((new PolylineOptions())
                            .add(prev, loc).width(6).color(Color.BLUE)
                            .visible(true));
                    prev = loc;
                    if(dr_flag > 0) {
                        new_distance += distanceInMeters;
                        Log.d("DAMN","gps_dist: "+new_distance);
                        new_dr_distance += distanceInMeters;
                    }
                    if(dr_flag < 5){
                        dr_flag++;
                    }
                }
            }
            // reset dead reckoning
            distance = 0;
            //new_distance += distanceInMeters;
        }
    };

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }
    }

    public void getSensorData() {

        //start sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new SensorEventListener() {

            //for compass
            float[] mGravity;
            float[] mGeomagnetic;

            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;

                if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION && lin != 1) {

                    Lin_x = event.values[0];
                    Lin_y = event.values[1];
                    Lin_z = event.values[2];
                    lin = 1;
                    double linear_acceleration[] = {0,0,0};
                    linear_acceleration[0] = Lin_x;
                    linear_acceleration[1] = Lin_y;
                    linear_acceleration[2] = Lin_z;

                    sensorTimeStamp = event.timestamp;
                    sensorTimeStamp /= (1000000000.0);
                    calc_dist(linear_acceleration,sensorTimeStamp);
                    sensorTimeStamp_old =  sensorTimeStamp;
                }
                if (sensor.getType() == Sensor.TYPE_ACCELEROMETER && accel != 1) {


                    // In this example, alpha is calculated as t / (t + dT),
                    // where t is the low-pass filter's time-constant and
                    // dT is the event delivery rate.
                    /*float alpha = 0.8f;
                    double gravity[] = {0,0,0};
                    double linear_acceleration[] = {0,0,0};
                    // Isolate the force of gravity with the low-pass filter.
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                    // Remove the gravity contribution with the high-pass filter.
                    linear_acceleration[0] = event.values[0] - gravity[0];
                    linear_acceleration[1] = event.values[1] - gravity[1];
                    linear_acceleration[2] = event.values[2] - gravity[2];

                    sensorTimeStamp = event.timestamp;
                    sensorTimeStamp /= (1000000000.0);
                    calc_dist(linear_acceleration,sensorTimeStamp);
                    sensorTimeStamp_old =  sensorTimeStamp;
*/
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
                    lin = 0;
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
    public LatLng calc_LatLng_DR(LatLng prev,  float bearing, float distance) {
        double radius = 6371.f; //in km
        distance /= 1000.0;     //in km
        // angular distance
        double adist = Math.toRadians(distance/radius);
        Log.d("DAMN","calc_dist: "+adist);
        // I might need to change for positive negative values
        double brng = Math.toRadians(azimuthInDegrees);
        double lat1 = prev.latitude;
        double lon1 = prev.longitude;
        Log.d("DAMN","lat1: "+lat1);
        Log.d("DAMN","lon1: "+lon1);
        lat1 = Math.toRadians(prev.latitude);
        lon1 = Math.toRadians(prev.longitude);

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(adist) + Math.cos(lat1) * Math.sin(adist) * Math.cos(brng));
        double a = Math.atan2(Math.sin(brng) * Math.sin(adist) * Math.cos(lat1), Math.cos(adist) - Math.sin(lat1) * Math.sin(lat2));
        //System.out.println("a = " +  a);
        Log.d("DAMN","a: "+a);
        double lon2 = lon1 + a;

        // Not sure if this part is needed
        //lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;



        //System.out.println("Latitude = " + Math.toDegrees(lat2) + "\nLongitude = " + Math.toDegrees(lon2));
        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);
        Log.d("DAMN","lat2: "+lat2);
        Log.d("DAMN","lon2: "+lon2);
        LatLng new_LatLng = new LatLng(lat2, lon2);

        return new_LatLng;
    }

    // use integration to calculate the distance traveled from the accelerometer values
     public void calc_dist(double[] acceleration,double timestamp_new){
        double temp = Math.abs(acceleration[1]);
         temp *= 35;
        double velocity_threshold = 0.5;
        // we need a way to reset velocity if we get close to stopping, velocity will probably
        // be our biggest source of error
        if(temp < velocity_threshold){
            velocity_low_flag++;
        }
        if(velocity_low_flag == 10){
            velocity_low_flag = 0;
            velocity = 0;
        }

        double DT = timestamp_new - sensorTimeStamp_old;
        velocity = temp*DT + velocity;
        distance = distance + velocity*DT + .5*temp*DT*DT;
        Log.d("SHIT","vel: "+velocity);
         //Log.d("SHIT","dist: "+distance);
         //Log.d("SHIT","Time: "+DT);
         Log.d("SHIT","Acc_0: "+acceleration[0]);
         Log.d("SHIT","Acc_1: "+acceleration[1]);
         Log.d("SHIT","Acc_2: "+acceleration[2]);
     }

}



