package com.maxeoneo.rollingdice;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity implements SensorEventListener
{

  private DiceGLSurfaceView mGLView;

  // acceleration sensor
  private SensorManager sensorManager;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    // Create a GLSurfaceView instance and set it
    // as the ContentView for this Activity.
    mGLView = new DiceGLSurfaceView(this);
    setContentView(mGLView);

    // register sensorManager
    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
  }

  @Override
  protected void onResume()
  {
    super.onResume();

    // register as acceleromater
    sensorManager.registerListener(this,
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  protected void onPause()
  {
    super.onPause();

    // unregister
    sensorManager.unregisterListener(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // do not add a menu
    return true;
  }

  @Override
  public void onAccuracyChanged(Sensor arg0, int arg1)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSensorChanged(SensorEvent event)
  {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
    {

      // only when shaking device
      float[] movement = event.values;

      float acceleration = (movement[0] * movement[0] + movement[1]
          * movement[1] + movement[2] * movement[2])
          / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

      // you have to shake strong enough
      if (acceleration >= 4)
      {
        mGLView.getNewCastingThread().start();
      }
    }
  }
}
