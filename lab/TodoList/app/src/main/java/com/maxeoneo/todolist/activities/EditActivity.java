package com.maxeoneo.todolist.activities;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.maxeoneo.todolist.AddGpsWithMapActivity;
import com.maxeoneo.todolist.CheckLocationService;
import com.maxeoneo.todolist.Item;
import com.maxeoneo.todolist.ItemsDataSource;
import com.maxeoneo.todolist.TodoService;

import androidlab.todolist.R;

public class EditActivity extends MapActivity {
	
	private static final int PICK_GPS_FROM_MAP = 1;
	private static final int PICK_GPS_FROM_CONTACT = 2;
	
	private EditText etItemName;
	private TimePicker timePicker;
	private DatePicker datePicker;
	private Button bSave;
	private ItemsDataSource ds;
	private CheckBox cbDone;
	private Button bAddDeadline;
	private Button bAddGpsLocation;
	private Button bChangeGpsLocation;
	private TextView tvLongitude;
	private TextView tvLatitude;
	
	private boolean deadlineAdded;
	private boolean gpsLocationAdded;
	
	private double longitude;
	private double latitude;
	
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
		
		// Deadline
		bAddDeadline = (Button) findViewById(R.id.bAddDeadline);
		
		// GPS things
		bAddGpsLocation = (Button) findViewById(R.id.bAddGpsLocation);
		bChangeGpsLocation = (Button) findViewById(R.id.bChangeGpsLocation);
		tvLongitude = (TextView) findViewById(R.id.tvLongitude);
		tvLatitude = (TextView) findViewById(R.id.tvLatitude);
		
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
				bAddDeadline.setText(R.string.removeDeadline);
				// set the date
				datePicker.init(year, month, day, null);
			} else {
				deadlineAdded = false;
			}
			
			// set gps location
			if (item.getGpsLocation() != null) {
				gpsLocationAdded = true;
				longitude = item.getGpsLocation().getLongitude();
				latitude = item.getGpsLocation().getLatitude();
				tvLongitude.setText(getString(R.string.longitude) + ": " + longitude);
				tvLatitude.setText(getString(R.string.latitude) + ": " + latitude);
				bAddGpsLocation.setText(R.string.removeGpsLocation);
				bChangeGpsLocation.setVisibility(View.VISIBLE);
				tvLongitude.setVisibility(View.VISIBLE);
				tvLatitude.setVisibility(View.VISIBLE);
			} else {
				bAddGpsLocation.setText(R.string.addGpsLocation);
				tvLongitude.setVisibility(View.GONE);
				tvLatitude.setVisibility(View.GONE);
				bChangeGpsLocation.setVisibility(View.GONE);
				gpsLocationAdded = false;
			}
		}
		
		// save button
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
					
					// add location 
					if (gpsLocationAdded) {
						ds.updateLongitudeAndLatitude(item.getId(), longitude, latitude);
						
						// start gps service
						startService(new Intent(getBaseContext(), CheckLocationService.class));
					}
					
					EditActivity.this.finish();	
					
				}
			}
		});
		
		// button add deadline
		
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
		
		
		// things for gps Location
		bAddGpsLocation.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				gpsLocationAdded = !gpsLocationAdded;
				if(gpsLocationAdded) {

					showDialog(0);
					
				} else {
					bAddGpsLocation.setText(R.string.addGpsLocation);
					tvLongitude.setVisibility(View.GONE);
					tvLatitude.setVisibility(View.GONE);
					bChangeGpsLocation.setVisibility(View.GONE);
					gpsLocationAdded = false;
				}
			}
		});
		
		bChangeGpsLocation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(0);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		super.onActivityResult(requestCode, resultCode, intent);
		
		
		// get results from map
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.addLocation), Toast.LENGTH_LONG).show();
				longitude = intent.getExtras().getDouble("Longitude");
				latitude = intent.getExtras().getDouble("Latitude");
				tvLongitude.setText(getString(R.string.longitude) + ": " + longitude);
				tvLatitude.setText(getString(R.string.latitude) + ": " + latitude);				
				bAddGpsLocation.setText(R.string.removeGpsLocation);
				bChangeGpsLocation.setVisibility(View.VISIBLE);
				tvLongitude.setVisibility(View.VISIBLE);
				tvLatitude.setVisibility(View.VISIBLE);
				gpsLocationAdded = true;
			}
		
			
		// get results from contact
		} else if (requestCode == 2) {
			
			// add location by contact 
			if (resultCode == RESULT_OK) {
				
				// get contact address
				Cursor c = getContentResolver().query(intent.getData(), null, null, null, null);
				if (c.moveToFirst()) {
				    String address = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
				    
				    Geocoder g = new Geocoder(this, Locale.GERMANY);
				    try {
						// convert string into address object
				    	List<Address> adds = g.getFromLocationName(address, 1);
						
				    	// get longitude and latitude
						longitude = adds.get(0).getLongitude();
						latitude = adds.get(0).getLatitude();
						
						tvLongitude.setText(getString(R.string.longitude) + ": " + longitude);
						tvLatitude.setText(getString(R.string.latitude) + ": " + latitude);
						
						Toast.makeText(getApplicationContext(),
								getString(R.string.addLocation), Toast.LENGTH_LONG).show();
						
						bAddGpsLocation.setText(R.string.removeGpsLocation);
						bChangeGpsLocation.setVisibility(View.VISIBLE);
						tvLongitude.setVisibility(View.VISIBLE);
						tvLatitude.setVisibility(View.VISIBLE);
						gpsLocationAdded = true;
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(getApplicationContext(),
								getString(R.string.addLocationError), Toast.LENGTH_LONG).show();
					}
				}
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.addLocationError), Toast.LENGTH_LONG).show();
			}
		}
		
	}
	
	@Override
	protected void onPause() {
	    ds.close();
	    super.onPause();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
	  
	    Builder builder = new AlertDialog.Builder(this);
	    builder.setCancelable(true);
	    builder.setMessage("Adding Location");
	    builder.setPositiveButton("By Map", new MapOnClickListener());
	    builder.setNegativeButton("By Contact Address", new ContactOnClickListener());
	    AlertDialog dialog = builder.create();
	    dialog.show();
	   
	    return super.onCreateDialog(id);
	}
	
	
	/**
	 * OnClickListener for getting Location by Map
	 */
	private final class MapOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			Intent intent = new Intent(EditActivity.this, AddGpsWithMapActivity.class);
			startActivityForResult(intent, PICK_GPS_FROM_MAP);
		}
	}

	/**
	 * OnClickListener for getting Location by Contact address
	 */
	private final class ContactOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
		    
		    Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
		    
		    // Show user only contacts with address
		    pickContactIntent.setType(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_TYPE); 
		    startActivityForResult(pickContactIntent, PICK_GPS_FROM_CONTACT);
		}
	}	
}