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
import android.view.MenuItem;
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
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings:
        createOptionsDialog();
        return true;
    }
    return false;
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
        activateLock();
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

  private void createOptionsDialog()
  {
    // custom dialog
    final Dialog dialog = new Dialog(this);
    dialog.setContentView(R.layout.options_dialog);
    dialog.setTitle(R.string.bOptions);

    // set the custom dialog components
    final EditText oldPwd = (EditText) dialog
        .findViewById(R.id.oldPwd);

    final EditText newPwd = (EditText) dialog
        .findViewById(R.id.newPwd);
    final EditText repeatNewPwd = (EditText) dialog
        .findViewById(R.id.repeatNewPwd);

    final ToggleButton tSendLoc = (ToggleButton) dialog
        .findViewById(R.id.tSendLoc);
    final EditText phoneNumber = (EditText) dialog
        .findViewById(R.id.phoneNumber);

    // get old PWD from Database
    dataSource.open();
    final String oldPwdString = dataSource.getPwd();
    final String pNumber = dataSource.getPhonenumber();
    final boolean sendLoc = dataSource.isSendLocation();
    dataSource.close();

    if (oldPwdString != "")
    {
      oldPwd.setVisibility(View.VISIBLE);
    }
    else
    {
      oldPwd.setVisibility(View.GONE);
    }

    // get options from database
    phoneNumber.setText(pNumber);
    tSendLoc.setChecked(sendLoc);
    if (sendLoc)
    {
      phoneNumber.setVisibility(View.VISIBLE);
    }
    else
    {
      phoneNumber.setVisibility(View.GONE);
    }

    // set method which is called when user clicks toggle button
    tSendLoc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {

      public void onCheckedChanged(CompoundButton buttonView,
                                   boolean isChecked)
      {

        if (isChecked)
        {
          if (locationPermissionsGranted())
          {
            phoneNumber.setVisibility(View.VISIBLE);
          }
          else
          {
            tSendLoc.setChecked(false);
          }
        }
        else
        {
          phoneNumber.setVisibility(View.GONE);
        }
      }
    });

    Button bSave = (Button) dialog.findViewById(R.id.bSave);

    // if button is clicked, close the custom dialog
    bSave.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {

        // oldPwd must be right
        if (oldPwdString == null
            || oldPwdString.equals(oldPwd.getText()
            .toString()))
        {

          // min 4 numbers
          if (newPwd.getText().length() >= 4)
          {

            // newPwd and repeatNewPwd must be the same and
            if (newPwd
                .getText()
                .toString()
                .equals(repeatNewPwd.getText()
                    .toString()))
            {

              // save options
              dataSource.open();

              // save all options (also when phonenumber
              // is "")

              String number = PhoneNumberUtils
                  .formatNumber(phoneNumber.getText()
                      .toString());
              dataSource.saveOptions(newPwd.getText()
                      .toString(), tSendLoc.isChecked(),
                  number);

              dataSource.close();

              // close dialog
              dialog.dismiss();

            }
            else
            {
              // pwds are not the same
              System.out.println("PWDS are not the same");
              // Show message
              Toast toast = Toast.makeText(context,
                  R.string.emNewAndRepeatedEquals,
                  Toast.LENGTH_SHORT);
              toast.show();
            }
          }
          else
          {

            // when new pwd and repeated new pwd are empty
            // save only the other two things
            if (newPwd.getText().toString().equals("")
                || repeatNewPwd.getText().toString()
                .equals(""))
            {

              String number = PhoneNumberUtils
                  .formatNumber(phoneNumber.getText()
                      .toString());

              dataSource.open();
              dataSource.setSendLocation(tSendLoc
                  .isChecked());
              dataSource.setPhonenumber(number);
              dataSource.close();

              // close dialog
              dialog.dismiss();
            }
            else
            {

              // pwds are to short
              System.out.println("PWDS are to short");
              // Show message
              Toast toast = Toast.makeText(context,
                  R.string.emPinToShort,
                  Toast.LENGTH_SHORT);
              toast.show();
            }
          }
        }
        else
        {
          // old pwd is not right
          System.out
              .println("old PWD is not right - database: "
                  + oldPwdString
                  + " your entered: "
                  + oldPwd.getText().toString());
          // Show message
          Toast toast = Toast.makeText(context,
              R.string.emOldPinNotRight,
              Toast.LENGTH_SHORT);
          toast.show();
        }
      }
    });

    dialog.show();
  }

  private boolean locationPermissionsGranted()
  {
    boolean granted = false;

    final boolean fineLocationAccessDeclined = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    final boolean coarseLocationAccessDeclined = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
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
}
