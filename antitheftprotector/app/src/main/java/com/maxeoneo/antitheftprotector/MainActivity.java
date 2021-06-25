package com.maxeoneo.antitheftprotector;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

  private Dialog settingsDialog;
  private EditText oldPwd;
  private EditText newPwd;
  private EditText repeatNewPwd;
  private ToggleButton toggleSendLocation;
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

    createSettingsDialog();
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
        showSettingsDialog();
        return true;
    }
    return false;
  }

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
        showErrorMessage(R.string.emSetPwd, Toast.LENGTH_SHORT);
      }

    }
    else
    {
      final Dialog enterPasswordDialog = new Dialog(context);
      enterPasswordDialog.setContentView(R.layout.enter_pwd_dialog);
      enterPasswordDialog.setTitle(R.string.enterPwd);
      enterPasswordDialog.setCancelable(false);

      final EditText pwd = (EditText) enterPasswordDialog.findViewById(R.id.enterPwd);

      // open keyboard automatically
      InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

      // get old PWD from Database
      final String savedPwd = dataSource.getPassword();

      Button submit = (Button) enterPasswordDialog.findViewById(R.id.bSubmitPwd);
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
            showErrorMessage(R.string.emWrongPin, Toast.LENGTH_SHORT);
          }

          // hide keyboard again
          InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
          inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

          enterPasswordDialog.dismiss();
        }
      });
      enterPasswordDialog.show();
    }
  }

  private void createSettingsDialog()
  {
    initializeSettingsDialog();
    initializeSettingsDialogContent();
  }

  private void initializeSettingsDialogContent()
  {
    oldPwd = (EditText) settingsDialog.findViewById(R.id.oldPwd);
    newPwd = (EditText) settingsDialog.findViewById(R.id.newPwd);
    repeatNewPwd = (EditText) settingsDialog.findViewById(R.id.repeatNewPwd);
    phoneNumber = (EditText) settingsDialog.findViewById(R.id.phoneNumber);
    toggleSendLocation = (ToggleButton) settingsDialog.findViewById(R.id.tSendLoc);
  }

  private void initializeSettingsDialog()
  {
    settingsDialog = new Dialog(this);
    settingsDialog.setContentView(R.layout.settings_dialog);
    settingsDialog.setTitle(R.string.bSettings);
    settingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
    {
      @Override
      public void onDismiss(DialogInterface dialog)
      {
        oldPwd.getText().clear();
        newPwd.getText().clear();
        repeatNewPwd.getText().clear();
      }
    });
  }

  private void showSettingsDialog()
  {
    final String oldPwdString = initializeOldPasswordField();
    final boolean sendLocation = initialtizeToggleButtonSendLocation();
    initializePhoneNumberFiled(sendLocation);
    initializeSaveButton(oldPwdString);

    settingsDialog.show();
  }

  private String initializeOldPasswordField()
  {
    final String oldPwdString = dataSource.getPassword();
    setVisibilityOfOldPassword(oldPwdString);
    return oldPwdString;
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

  private boolean initialtizeToggleButtonSendLocation()
  {
    final boolean sendLoc = dataSource.getSendLocation();
    toggleSendLocation.setChecked(sendLoc);
    toggleSendLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
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
            toggleSendLocation.setChecked(false);
          }
        }
        else
        {
          setVisibilityOfPhoneNumber(false);
        }
      }
    });
    return sendLoc;
  }

  private void initializePhoneNumberFiled(boolean visible)
  {
    final String pNumber = dataSource.getPhoneNumber();
    phoneNumber.setText(pNumber);
    setVisibilityOfPhoneNumber(visible);
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

  private void initializeSaveButton(final String oldPwdString)
  {
    Button bSave = (Button) settingsDialog.findViewById(R.id.bSave);

    bSave.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        if (oldPwdString == ""
            || oldPwdString.equals(oldPwd.getText().toString()))
        {
          if (hasNewPasswordAtLeastFourDigits())
          {
            if (newPasswordIsRepeatedCorrectly())
            {
              saveNewSettings();
              settingsDialog.dismiss();
            }
            else
            {
              showErrorMessage(R.string.emNewAndRepeatedEquals, Toast.LENGTH_SHORT);
            }
          }
          else
          {
            if (isPasswordUnchanged())
            {
              String number = PhoneNumberUtils.formatNumber(phoneNumber.getText().toString());
              dataSource.setPhoneNumber(number);
              dataSource.setSendLocation(toggleSendLocation.isChecked());

              settingsDialog.dismiss();
            }
            else
            {
              showErrorMessage(R.string.emPinToShort, Toast.LENGTH_SHORT);
            }
          }
        }
        else
        {
          showErrorMessage(R.string.emOldPinNotRight, Toast.LENGTH_SHORT);
        }
      }
    });
  }

  private boolean isPasswordUnchanged()
  {
    return newPwd.getText().toString().isEmpty()
        && repeatNewPwd.getText().toString().isEmpty();
  }

  private void showErrorMessage(int translationId, int lengthShort)
  {
    Toast toast = Toast.makeText(context, translationId, lengthShort);
    toast.show();
  }

  private boolean newPasswordIsRepeatedCorrectly()
  {
    return newPwd.getText().toString().equals(repeatNewPwd.getText().toString());
  }

  private void saveNewSettings()
  {
    String number = PhoneNumberUtils.formatNumber(phoneNumber.getText().toString());
    dataSource.saveSettings(newPwd.getText().toString(), toggleSendLocation.isChecked(), number);
  }

  private boolean hasNewPasswordAtLeastFourDigits()
  {
    return newPwd.getText().length() >= 4;
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
        showErrorMessage(R.string.PermissionExplanation, Toast.LENGTH_LONG);
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
          toggleSendLocation.setChecked(true);
        }
        else
        {
          toggleSendLocation.setChecked(false);
        }
        return;
      }
    }
  }
}
