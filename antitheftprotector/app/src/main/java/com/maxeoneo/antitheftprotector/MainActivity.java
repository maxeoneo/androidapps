package com.maxeoneo.antitheftprotector;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity
{

  private final Context context = this;
  private ToggleButton tOnOff;
  private Button bSetPwd;
  private CLDataSource dataSource;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    dataSource = new CLDataSource(context);

    boolean active = isLockActive();

    // set toggleButton to state from database
    tOnOff = (ToggleButton) findViewById(R.id.tOnOff);
    tOnOff.setChecked(active);

    // button set pwd
    bSetPwd = (Button) findViewById(R.id.bSetPwd);
    // hide set pwd button when active
    if (active)
    {
      bSetPwd.setVisibility(View.GONE);
    }
    bSetPwd.setOnClickListener(new OptionsOnClickListener(this));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  /**
   * Method is called when toggle button is clicked
   */
  public void onToggleClicked(View view)
  {
    // Is the toggle on?
    boolean on = ((ToggleButton) view).isChecked();

    if (on)
    {
      String pwd = getPassword();

      if (pwd != "")
      {
        // cellphone lock is on

        dataSource.open();
        final boolean sendLocation = dataSource.isLockActive();
        dataSource.close();

        if (!sendLocation || locationPermissionsGranted())
        {
          activateLock();
        }
        else
        {
          tOnOff.setChecked(false);

          // Show message
          Toast toast = Toast.makeText(context, R.string.emNoPermissions, Toast.LENGTH_SHORT);
          toast.show();
        }
      }
      else
      {
        // old pwd is not right
        tOnOff.setChecked(false);

        // Show message
        Toast toast = Toast.makeText(context, R.string.emSetPwd, Toast.LENGTH_SHORT);
        toast.show();
      }

    }
    else
    {
      // cellphone off

      // custom dialog
      final Dialog dialog = new Dialog(context);
      dialog.setContentView(R.layout.enter_pwd_dialog);
      dialog.setTitle(R.string.enterPwd);
      dialog.setCancelable(false);

      // set the custom dialog components
      final EditText pwd = (EditText) dialog.findViewById(R.id.enterPwd);

      // get old PWD from Database
      final String savedPwd = getPassword();

      Button submit = (Button) dialog.findViewById(R.id.bSubmitPwd);
      submit.setOnClickListener(new OnClickListener()
      {

        @Override
        public void onClick(View v)
        {
          if (pwd.getText().toString().equals(savedPwd))
          {
            // show Button
            bSetPwd.setVisibility(View.VISIBLE);

            // stop cellphone lock
            // save not running to data base
            setLockActive(false);

          }
          else
          {
            // can't stop cellphone lock
            tOnOff.setChecked(true);

            System.out.println("Wrong old pin. The right one is "
                + savedPwd);

            // Show message
            Toast toast = Toast.makeText(context,
                R.string.emWrongPin, Toast.LENGTH_SHORT);
            toast.show();
          }
          // close dialog
          dialog.dismiss();
        }
      });
      dialog.show();
    }
  }

  private void activateLock()
  {
    // hide Button
    bSetPwd.setVisibility(View.GONE);

    // start cellphone lock
    // set running to database
    setLockActive(true);
  }

  private boolean locationPermissionsGranted()
  {
    boolean granted = false;

    final boolean fineLocationAccessDeclined = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    final boolean coarseLocationAccessDeclined = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    if (fineLocationAccessDeclined || coarseLocationAccessDeclined)
    {

      // Permission is not granted
      // Should we show an explanation?
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
          || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
      {
        // Show an explanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.
      }
      else
      {
        // No explanation needed; request the permission
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1
        );

        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
      }
    }
    else
    {
      granted = true;
    }
    return granted;
  }

  private boolean isLockActive()
  {
    // Database connection and get saved state of lock
    dataSource.open();
    boolean active = dataSource.isLockActive();
    dataSource.close();
    return active;
  }

  private void setLockActive(boolean active)
  {
    dataSource.open();
    dataSource.setLockActive(active);
    dataSource.close();
  }

  private String getPassword()
  {
    dataSource.open();
    String pwd = dataSource.getPwd();
    dataSource.close();

    return pwd;
  }
}
