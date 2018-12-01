package androidlab.exercise4_1;

import java.util.TreeMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class CheckLocationService extends Service{
	
	private LocationManager locationManager;
	private LocationListener locationListener;
	private ItemsDataSource ds;
	private NotificationManager nm;
	private static final int uniqueId = 987654321;
	
	/** if you stay in the range of an Item and don't delete it, then
 	 *  you will get a notification every minute (if you delete the notificaion every time)
	 *  so we save the itemId together with a timer.  
	 */ 
	private TreeMap<Long, Long> pauseNotification = new TreeMap<Long, Long>();
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
    public void onCreate() {
		System.out.println("JOB: onCreate");
		ds = new ItemsDataSource(getApplicationContext());
		
		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		//cancel uniqueId so that the notification will be hidden after it 
		//is clicked (it doesn't work, but the exercise didn't ask for that :)
		nm.cancel(uniqueId);
		
		locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
		locationListener = new LocationListener() {

	        public void onLocationChanged(Location location) {
	        	
	        	ds.open();
	        	Item[] items = ds.getAllUndoneItems();
	        	ds.close();
	        	
	        	for (Item i : items) {
	        		i.getGpsLocation().getLocality();
	        		Location loc = new Location(LocationManager.GPS_PROVIDER);
	        		loc.setLongitude(i.getGpsLocation().getLongitude());
	        		loc.setLatitude(i.getGpsLocation().getLatitude());
	        		double distance = location.distanceTo(loc);
	        		if (distance <= 100.0) {
	        			
	        			// show notification max every 30 min
	        			if (pauseNotification.containsKey(i.getId())) {
	        				if (pauseNotification.get(i.getId()) > System.currentTimeMillis() - (30 * 60 * 1000)) {
	        					continue;
	        				} else {
	        					pauseNotification.remove(i.getId());
	        				}
	        			}
	        				
	        			// show the notification
	        			showNotification("Location for ToDo-Item: " + i.getName() + " is in range of 100m");
	        			
	        			// save the item id in the tree map with the time of the last occurrence
	        			pauseNotification.put(i.getId(), System.currentTimeMillis());
	        		}
	        	}
	        }

	    public void onProviderEnabled(String provider) {
	    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 10, this);
	    }

	    public void onProviderDisabled(String provider) {
	    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 10, this);
	    }

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
	  };
	  
    }
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			System.out.println("GPS is enabled");
			//check every min
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 10, locationListener);
		} else {
			System.out.println("GPS is disabled");
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 10, locationListener);
		}
		
		// check if you are already in range of a location
		
		
		return Service.START_STICKY;
	}
	
	public void showNotification(String notificationName) {
		// In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.savedLocationIsNear);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.ic_launcher, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, notificationName,
                       text, contentIntent);

        // Send the notification.
        nm.notify(uniqueId, notification);
	}


}
