package com.maxeoneo.antitheftprotector;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
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

    // Database connection and get saved state of lock
    dataSource = new CLDataSource(context);
    dataSource.open();
    boolean active = dataSource.isLockActive();
    dataSource.close();

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
      // cellphone lock is on

      dataSource.open();
      String pwd = dataSource.getPwd();
      dataSource.close();

      if (pwd != null)
      {

        // hide Button
        bSetPwd.setVisibility(View.GONE);

        // start cellphone lock
        // set running to database
        dataSource.open();
        dataSource.setLockActive(true);
        dataSource.close();

      } else
      {
        // old pwd is not right
        System.out.println("please set pin first");

        tOnOff.setChecked(false);

        // Show message
        Toast toast = Toast.makeText(context, R.string.emSetPwd,
            Toast.LENGTH_SHORT);
        toast.show();
      }

    } else
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
      dataSource.open();
      final String savedPwd = dataSource.getPwd();
      dataSource.close();

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
            dataSource.open();
            dataSource.setLockActive(false);
            dataSource.close();

          } else
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
}
