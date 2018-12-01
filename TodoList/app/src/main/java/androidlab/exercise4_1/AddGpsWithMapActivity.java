package androidlab.exercise4_1;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

public class AddGpsWithMapActivity extends MapActivity {

	private MapView mapView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_gps_with_map);
		
		// connect to xml
		mapView = (MapView) findViewById(R.id.mvMap);
		mapView.setLongClickable(true);
	}
	
	@Override
	public void onBackPressed() {
		System.out.println("Backpressed");
		Intent intent = new Intent();
		intent.putExtra("Longitude", 0.0);
		intent.putExtra("Latitude", 1.1);
		setResult(RESULT_CANCELED, intent);
		super.onBackPressed();
	}
	
	
	@Override
	public boolean dispatchTouchEvent (MotionEvent ev) {
		if (ev.getAction () == MotionEvent.ACTION_UP) {
			Projection p = mapView.getProjection ();
			GeoPoint loc = p.fromPixels((int)ev.getX(), (int)ev.getY());
			double longitude = ((double)loc.getLongitudeE6()) / 1000000;
			double latitude = ((double)loc.getLatitudeE6 ()) /1000000;
			Intent intent = new Intent();
			intent.putExtra("Longitude", longitude);
			intent.putExtra("Latitude", latitude);
			setResult(RESULT_OK, intent);
			finish();
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}



}
