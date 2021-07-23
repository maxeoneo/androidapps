package com.maxeoneo.todolistsimple;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddActivity extends Activity
{

  private EditText etItemName;
  private TimePicker timePicker;
  private DatePicker datePicker;
  private Button bSave;
  private ItemsDataSource ds;
  private CheckBox cbDone;
  private Button bAddDeadline;

  private boolean deadlineAdded;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add);

    //get a datasource
    ds = new ItemsDataSource(this);
    ds.open();


    //at first no deadlline is added
    deadlineAdded = false;

    //connect with items from layout-xml
    timePicker = (TimePicker) findViewById(R.id.timePicker);
    timePicker.setIs24HourView(true);
    datePicker = (DatePicker) findViewById(R.id.datePicker);
    etItemName = (EditText) findViewById(R.id.etItemName);
    cbDone = (CheckBox) findViewById(R.id.cbDone);

    bSave = (Button) findViewById(R.id.bSave);
    bSave.setOnClickListener(new OnClickListener()
    {

      @Override
      public void onClick(View v)
      {
        String name = etItemName.getText().toString();
        if (name.equals(""))
        {
          //show error message
          CharSequence txt = "You must enter a name";
          Toast toast = Toast.makeText(getApplicationContext(), txt
              , Toast.LENGTH_SHORT);
          toast.show();
        } else
        {
          //save the new item in database
          boolean done = cbDone.isChecked();
          if (deadlineAdded)
          {
            int intHour = timePicker.getCurrentHour();
            int intMin = timePicker.getCurrentMinute();
            int intMonth = datePicker.getMonth();
            int intDay = datePicker.getDayOfMonth();

            String strHour = intHour + "";
            String strMin = intMin + "";
            String strMonth = intMonth + "";
            String strDay = intDay + "";

            if (intHour < 10)
            {
              strHour = "0" + intHour;
            }
            if (intMin < 10)
            {
              strMin = "0" + intMin;
            }
            if (intMonth < 10)
            {
              strMonth = "0" + intMonth;
            }
            if (intDay < 10)
            {
              strDay = "0" + intDay;
            }

            String deadline = datePicker.getYear() + "-" + strMonth
                + "-" + strDay + " " + strHour + ":" + strMin;
            ds.saveItem(name, done, deadline);

            //Start service so that the user gets an info when deadline is
            //next reached
            startService(new Intent(getBaseContext(), TodoService.class));
          } else
          {
            ds.saveItem(name, done, null);
          }
          AddActivity.this.finish();
        }
      }
    });

    bAddDeadline = (Button) findViewById(R.id.bAddDeadline);
    bAddDeadline.setOnClickListener(new OnClickListener()
    {

      public void onClick(View v)
      {
        deadlineAdded = !deadlineAdded;
        if (deadlineAdded)
        {
          bAddDeadline.setText(R.string.removeDeadline);
          timePicker.setVisibility(View.VISIBLE);
          datePicker.setVisibility(View.VISIBLE);
        } else
        {
          bAddDeadline.setText(R.string.addDeadline);
          timePicker.setVisibility(View.GONE);
          datePicker.setVisibility(View.GONE);
        }
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  protected void onResume()
  {
    ds.open();
    super.onResume();
  }

  @Override
  protected void onPause()
  {
    ds.close();
    super.onPause();
  }
}
