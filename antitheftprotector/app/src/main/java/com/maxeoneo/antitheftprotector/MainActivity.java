package com.maxeoneo.antitheftprotector;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity
{

  private static final int LOCATIONS_PERMISSIONS_REQUEST = 1;
  private final Context context = this;
  private ToggleButton tOnOff;
  private CLDataSource dataSource;

  private Dialog dialog;
  private EditText oldPwd;
  private EditText newPwd;
  private EditText repeatNewPwd;
  private ToggleButton tSendLoc;
  private EditText phoneNumber;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    dataSource = new CLDataSource(context);

    boolean active = dataSource.isLockActive();

    // set toggleButton to state from database
    tOnOff = (ToggleButton) findViewById(R.id.tOnOff);
    tOnOff.setChecked(active);
    tOnOff.setBackgroundColor(active ? Color.RED : Color.WHITE);

    createOptionsDialog();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case R.id.action_settings:
        showOptionsDialog();
        return true;
    }
    return false;
  }

  /**
   * Method is called when toggle button is clicked
   */
  public void onToggleClicked(View view)
  {
    if (((ToggleButton) view).isChecked())
    {
      String pwd = dataSource.getPassword();

      if (pwd != "")
      {
        // cellphone lock is on
        activateLock(true);
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
      // custom dialog
      final Dialog dialog = new Dialog(context);
      dialog.setContentView(R.layout.enter_pwd_dialog);
      dialog.setTitle(R.string.enterPwd);
      dialog.setCancelable(false);

      // set the custom dialog components
      final EditText pwd = (EditText) dialog.findViewById(R.id.enterPwd);

      // open keyboard automatically
      InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

      // get old PWD from Database
      final String savedPwd = dataSource.getPassword();

      Button submit = (Button) dialog.findViewById(R.id.bSubmitPwd);
      submit.setOnClickListener(new OnClickListener()
      {

        @Override
        public void onClick(View v)
        {
          if (pwd.getText().toString().equals(savedPwd))
          {
            activateLock(false);
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

          // hide keyboard again
          InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
          inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

          // close dialog
          dialog.dismiss();
        }
      });
      dialog.show();
    }
  }

  private void createOptionsDialog()
  {
    dialog = new Dialog(this);
    dialog.setContentView(R.layout.options_dialog);
    dialog.setTitle(R.string.bOptions);

    oldPwd = (EditText) dialog.findViewById(R.id.oldPwd);
    newPwd = (EditText) dialog.findViewById(R.id.newPwd);
    repeatNewPwd = (EditText) dialog.findViewById(R.id.repeatNewPwd);
    phoneNumber = (EditText) dialog.findViewById(R.id.phoneNumber);
    tSendLoc = (ToggleButton) dialog.findViewById(R.id.tSendLoc);
  }

  private void showOptionsDialog()
  {
    // get old PWD from Database
    dataSource.open();
    final String oldPwdString = dataSource.getPassword();
    final String pNumber = dataSource.getPhoneNumber();
    final boolean sendLoc = dataSource.getSendLocation();
    dataSource.close();

    setVisibilityOfOldPassword(oldPwdString);

    tSendLoc.setChecked(sendLoc);

    phoneNumber.setText(pNumber);
    setVisibilityOfPhoneNumber(sendLoc);

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
            setVisibilityOfPhoneNumber(true);
          }
          else
          {
            tSendLoc.setChecked(false);
          }
        }
        else
        {
          setVisibilityOfPhoneNumber(false);
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
        if (oldPwdString == ""
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

              // save all options (also when phonenumber
              // is "")
              String number = PhoneNumberUtils.formatNumber(phoneNumber.getText().toString());
              dataSource.saveOptions(newPwd.getText().toString(), tSendLoc.isChecked(), number);

              // close dialog
              dialog.dismiss();

            }
            else
            {
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

              dataSource.setSendLocation(tSendLoc.isChecked());
              dataSource.setPhonenumber(number);

              // close dialog
              dialog.dismiss();
            }
            else
            {
              Toast toast = Toast.makeText(context,
                  R.string.emPinToShort,
                  Toast.LENGTH_SHORT);
              toast.show();
            }
          }
        }
        else
        {
          Toast toast = Toast.makeText(context,
              R.string.emOldPinNotRight,
              Toast.LENGTH_SHORT);
          toast.show();
        }
      }
    });

    dialog.show();
  }

  private void setVisibilityOfPhoneNumber(boolean visible)
  {
    if (visible)
    {
      phoneNumber.setVisibility(View.VISIBLE);
    }
    else
    {
      phoneNumber.setVisibility(View.GONE);
    }
  }

  private void setVisibilityOfOldPassword(String oldPwdString)
  {
    if (oldPwdString != "")
    {
      oldPwd.setVisibility(View.VISIBLE);
    }
    else
    {
      oldPwd.setVisibility(View.GONE);
    }
  }

  void activateLock(boolean active)
  {
    dataSource.setLockActive(active);
    tOnOff.setBackgroundColor(active ? Color.RED : Color.GRAY);
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
        Toast toast = Toast.makeText(context, R.string.PermissionExplanation, Toast.LENGTH_LONG);
        toast.show();
      }
      else
      {
        // No explanation needed; request the permission
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATIONS_PERMISSIONS_REQUEST
        );
      }
    }
    else
    {
      granted = true;
    }
    return granted;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
  {
    switch (requestCode)
    {
      case LOCATIONS_PERMISSIONS_REQUEST:
      {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
          phoneNumber.setVisibility(View.VISIBLE);
          tSendLoc.setChecked(true);
        }
        else
        {
          tSendLoc.setChecked(false);
        }
        return;
      }
    }
  }
}
