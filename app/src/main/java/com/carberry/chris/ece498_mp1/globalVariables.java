package com.carberry.chris.ece498_mp1;

import android.app.Application;

/**
 * Created by Chris on 5/8/2015.
 */
public class globalVariables extends Application {
    private float degrees;
    private float pushes;

    public float getDegrees() {

        return degrees;
    }

    public void setDegrees(float azimuth) {

        degrees = azimuth;
    }

    public float getPushes() {

        return pushes;
    }

    public void setPushes(float new_pushes) {

        pushes = new_pushes;
    }

}
