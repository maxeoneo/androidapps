package androidlab.exercise3_1;

import java.util.GregorianCalendar;

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

public class EditActivity extends Activity {
	
	private EditText etItemName;
	private TimePicker timePicker;
	private DatePicker datePicker;
	private Button bSave;
	private ItemsDataSource ds;
	private CheckBox cbDone;
	private Button bAddDeadline;
	
	private boolean deadlineAdded;
	
	private Item item;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		
		//get a datasource
		ds = new ItemsDataSource(this);
		ds.open();
		
		//connect with items from louyout-xml
		timePicker = (TimePicker) findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);
		datePicker = (DatePicker) findViewById(R.id.datePicker);
		etItemName = (EditText) findViewById(R.id.etItemName);
		cbDone = (CheckBox) findViewById(R.id.cbDone);
		
		//get the item which the user wants to edit and fill
		//in the data in the components of the activity
		long id = getIntent().getExtras().getLong("itemId");
		item = ds.getItemById(id);
		if (item != null) {
			//set Text of edit text = name of item
			etItemName.setText(item.getName());
			
			//check checkbox if item is already done
			//(in this version you can't edit a item which is already done)
			cbDone.setChecked(item.getDone());
			
			//set the date- and timepicker to the deadline of the item
			//if a deadline is added
			GregorianCalendar dl = item.getDeadline();
			if (dl != null) {
				deadlineAdded = true;
				timePicker.setVisibility(View.VISIBLE);
				datePicker.setVisibility(View.VISIBLE);
				timePicker.setCurrentHour(dl.get(GregorianCalendar.HOUR_OF_DAY));
				timePicker.setCurrentMinute(dl.get(GregorianCalendar.MINUTE));
				int year = dl.get(GregorianCalendar.YEAR);
				int month = dl.get(GregorianCalendar.MONTH);
				int day = dl.get(GregorianCalendar.DAY_OF_MONTH);
				
				// set the date
				datePicker.init(year, month, day, null);
			} else {
				deadlineAdded = false;
			}
		}
		
		
		bSave = (Button) findViewById(R.id.bSave);
		bSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String name = etItemName.getText().toString();
				if (name.equals("")) { 
					//show error message
					CharSequence txt = "You must enter a name";
					Toast toast = Toast.makeText(getApplicationContext(), txt
							, Toast.LENGTH_SHORT);
					toast.show();
				}
				else {
					//save the new item in database
					boolean done = cbDone.isChecked();
					if (deadlineAdded) {	
						int intHour = timePicker.getCurrentHour();
						int intMin = timePicker.getCurrentMinute();
						int intMonth = datePicker.getMonth();
						int intDay = datePicker.getDayOfMonth();
						
						String strHour = intHour + "";
						String strMin = intMin + "";
						String strMonth = intMonth + "";
						String strDay = intDay + "";
						
						if (intHour < 10) {
							strHour = "0" + intHour;
						}
						if (intMin < 10) {
							strMin = "0" + intMin;
						}
						if (intMonth < 10) {
							strMonth = "0" + intMonth;
						}
						if (intDay < 10) {
							strDay = "0" + intDay;
						}
						
						String deadline = datePicker.getYear() + "-" + strMonth 
								+ "-" + strDay + " " + strHour + ":" + strMin;
						ds.updateItem(item.getId(), name, done, deadline);
						
						//Start service so that the user gets an info when deadline is 
						//next reached
						startService(new Intent(getBaseContext(), TodoService.class));
					}
					else {
						ds.updateItem(item.getId(), name, done, null);
					}
					EditActivity.this.finish();	
					
				}
			}
		});
		
		bAddDeadline = (Button) findViewById(R.id.bAddDeadline);
		bAddDeadline.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				deadlineAdded = !deadlineAdded;
				if(deadlineAdded) {
					bAddDeadline.setText(R.string.removeDeadline);
					timePicker.setVisibility(View.VISIBLE);
					datePicker.setVisibility(View.VISIBLE);
				} else {
					bAddDeadline.setText(R.string.addDeadline);
					timePicker.setVisibility(View.GONE);
					datePicker.setVisibility(View.GONE);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		ds.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
	    ds.close();
	    super.onPause();
	}
	
}
