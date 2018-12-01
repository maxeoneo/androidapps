package androidlab.exercise4_1;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
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

public class AddActivity extends Activity {
	
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);
		
		//get a datasource
		ds = new ItemsDataSource(this);
		ds.open();
		
		//at first no deadlline and GpsLocation added
		deadlineAdded = false;
		gpsLocationAdded = false;
		
		//connect with items from layout-xml
		timePicker = (TimePicker) findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);
		datePicker = (DatePicker) findViewById(R.id.datePicker);
		etItemName = (EditText) findViewById(R.id.etItemName);
		cbDone = (CheckBox) findViewById(R.id.cbDone);
		
		
		// save button
		bSave = (Button) findViewById(R.id.bSave);
		bSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String name = etItemName.getText().toString();
				
				
				// id of item
				long id;
				
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
						id = ds.saveItem(name, done, deadline);
						
						//Start service so that the user gets an info when deadline is 
						//next reached
						startService(new Intent(getBaseContext(), TodoService.class));
					}
					else {
						id = ds.saveItem(name, done, null);
					}
					
					// add location
					if (gpsLocationAdded) {
						ds.updateLongitudeAndLatitude(id, longitude, latitude);
						
						// start gps service
						startService(new Intent(getBaseContext(), CheckLocationService.class));
					}
					
					AddActivity.this.finish();	
				}
			}
		});
		
		
		// button add deadline
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
		
		
		// things for gps Location
		
		bAddGpsLocation = (Button) findViewById(R.id.bAddGpsLocation);
		bChangeGpsLocation = (Button) findViewById(R.id.bChangeGpsLocation);
		
		tvLongitude = (TextView) findViewById(R.id.tvLongitude);
		tvLatitude = (TextView) findViewById(R.id.tvLatitude);
		
		bChangeGpsLocation.setVisibility(View.GONE);
		tvLongitude.setVisibility(View.GONE);
		tvLatitude.setVisibility(View.GONE);
		gpsLocationAdded = false;
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
		
		// get the results from Map
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
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.addLocationError), Toast.LENGTH_LONG).show();
			}
			
		// get Results from Contacts	
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
			Intent intent = new Intent(AddActivity.this, AddGpsWithMapActivity.class);
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