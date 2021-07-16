package com.maxeoneo.antitheftprotector;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.core.content.ContextCompat;

public class UsbReceiver extends BroadcastReceiver
{

  private Context context;
  private CLDataSource dataSource;

  // fields for sending location
  private String mailAddress = "";
  private LocationManager locationManager;
  private LocationListener locationListener;
  private Location oldLocation = null;

  @Override
  public void onReceive(Context context, Intent intent)
  {
    this.context = context;
    this.dataSource = new CLDataSource(context);

//    prepareForSendingLocation(context);

    // create and start thread
    AlarmThread at = new AlarmThread(context);
    at.start();
  }

//  private void prepareForSendingLocation(Context context) {
//    locationManager = (LocationManager) context
//        .getSystemService(Context.LOCATION_SERVICE);
//    locationListener = new LocationListener()
//    {
//
//      public void onLocationChanged(Location location)
//      {
//
//        // only send email when old location and new location has a
//        // big enough distance
//        if (oldLocation == null || oldLocation.distanceTo(location) > 100)
//        {
//          oldLocation = location;
//          sendLocation(location);
//        }
//
//      }
//
//      public void onProviderEnabled(String provider)
//      {
//        // register gps provider, updates max every min
//        requestLocationUpdates(LocationManager.GPS_PROVIDER, Manifest.permission.ACCESS_FINE_LOCATION);
//      }
//
//      public void onProviderDisabled(String provider)
//      {
//        // register network provider, updates max every min
//        requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Manifest.permission.ACCESS_COARSE_LOCATION);
//      }
//
//      @Override
//      public void onStatusChanged(String arg0, int arg1, Bundle arg2)
//      {
//      }
//    };
//
//    dataSource.open();
//    // only if lock is active and send location is switch
//    if (dataSource.isLockActive() && dataSource.getSendLocation())
//    {
//      // get phonenumber and number of seconds from database
//      mailAddress = dataSource.getEmailAddress();
//
//      // register provider, updates max every min)
//      if (locationManager
//          .isProviderEnabled(LocationManager.GPS_PROVIDER))
//      {
//        requestLocationUpdates(LocationManager.GPS_PROVIDER, Manifest.permission.ACCESS_FINE_LOCATION);
//      }
//      else
//      {
//        requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Manifest.permission.ACCESS_COARSE_LOCATION);
//      }
//    }
//    dataSource.close();
//  }
//
//  private void requestLocationUpdates(String locationProvider, String permission)
//  {
//    if (ContextCompat.checkSelfPermission(context, permission)
//        == PackageManager.PERMISSION_GRANTED)
//    {
//
//      locationManager.requestLocationUpdates(locationProvider, 60000, 50, locationListener);
//    }
//  }

  /**
   * Method to send mail with location.
   */
//  private void sendLocation(Location loc)
//  {
//    if (!mailAddress.equals(""))
//    {
//      // send email
//      String emailText = context.getResources().getString(R.string.EmailText)
//          + "\nLatitude: "
//          + loc.getLatitude()
//          + "\nLongitude: "
//          + loc.getLongitude();
//
//      System.out.println("Send email to " + mailAddress + ": ");
//      System.out.println(emailText);
//
//      Intent i = new Intent(Intent.ACTION_SEND);
//      i.setType("message/rfc822");
//      i.putExtra(Intent.EXTRA_EMAIL  , new String[]{mailAddress});
//      i.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name) + ": " + context.getResources().getString(R.string.EmailSubject));
//      i.putExtra(Intent.EXTRA_TEXT   , emailText);
//      try {
//        context.startActivity(Intent.createChooser(i, "Send mail..."));
//      } catch (android.content.ActivityNotFoundException ex) {
//        Toast.makeText(MyActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
//      }
//    }
//  }

  /**
   * Thread for the alarm
   */
  private class AlarmThread extends Thread
  {

    private MediaPlayer alarmSound;
    private AudioManager am;

    private AlarmThread(Context context)
    {
      super();
      alarmSound = MediaPlayer.create(context, R.raw.sirene);
      alarmSound.setLooping(true);
      am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void run()
    {
      // get volume
      int volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

      // if lock is active play sound
      dataSource.open();

      if (dataSource.isLockActive())
      {
        setVolumeToMax();
        alarmSound.start();
        System.out.println("ALARM");
      }

      // play until lock isn't active any more
      while (dataSource.isLockActive())
      {
        try
        {
          Thread.sleep(1000);

          // set volume to max every second so that the thief can't mute it
          setVolumeToMax();

        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }

      // stop updating location
//      locationManager.removeUpdates(locationListener);

      // stop alarm
      alarmSound.stop();
      alarmSound.release();

      // set volume back
      am.setStreamVolume(AudioManager.STREAM_MUSIC, volume,
          AudioManager.FLAG_PLAY_SOUND);

      // close source;
      dataSource.close();
    }

    private void setVolumeToMax()
    {
      am.setStreamVolume(AudioManager.STREAM_MUSIC,
          am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
          AudioManager.FLAG_PLAY_SOUND);
    }
  }
}