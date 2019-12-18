package com.maxeoneo.antitheftprotector;

import android.app.Activity;
import android.app.Dialog;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class OptionsOnClickListener implements View.OnClickListener
{
  private Activity activity;
  private CLDataSource dataSource;

  public OptionsOnClickListener(Activity activity)
  {
    super();
    this.activity = activity;
    dataSource = new CLDataSource(activity);
  }


  @Override
  public void onClick(View v)
  {
    // custom dialog
    final Dialog dialog = new Dialog(activity);
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

    if (oldPwdString != null)
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
          phoneNumber.setVisibility(View.VISIBLE);
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
              Toast toast = Toast.makeText(activity,
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
              Toast toast = Toast.makeText(activity,
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
          Toast toast = Toast.makeText(activity,
              R.string.emOldPinNotRight,
              Toast.LENGTH_SHORT);
          toast.show();
        }
      }
    });

    dialog.show();
  }
}
