package androidlab.exercise4_1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TodoService extends Service {

	private Item next;
	private ItemsDataSource ds;
	private NotificationManager nm;
	private static final int uniqueId = 123456789;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
    public void onCreate() {
		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		//cancel uniqueId so that the notification will be hidden after it 
		//is clicked (it doesn't work, but the exercise didn't ask for that :)
		nm.cancel(uniqueId);
    }
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		updateNext();
		ServiceThread st = new ServiceThread();
		st.start();
		
	    return Service.START_STICKY;
	}
	
	public void showNotification() {
		// In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.reached_deadline);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.ic_launcher, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, next.getName(),
                       text, contentIntent);

        // Send the notification.
        nm.notify(uniqueId, notification);
	}
	
	/**
	 * get the item with the next deadline
	 */
	public void updateNext() {
		//get the item with next deadline
		ds = new ItemsDataSource(getApplicationContext());
		ds.open();
		next = ds.getNextItem();
		ds.close();
	}
	
	public class ServiceThread extends Thread {

		@Override
		public void run() {
			while (next != null) {
				if (next.getDeadline().getTimeInMillis() <= System.currentTimeMillis()) {
					showNotification();
					updateNext();
				} else {
					//wait 30 seconds
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}

}
