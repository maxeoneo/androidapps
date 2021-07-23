package androidlab.exercise6_1;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private final int ongoingNotificationCode = -1;
	private int missedCallNotificationCode = 1;
	private NotificationManager notificationManager;

	private ToggleButton toggleButton;

	private TMDataSource dataSource;

	private TelephonyManager telephonyManager;
	private MyPhoneStateListener phoneStateListener;
	private int oldCallState;;

	private AudioManager audioManager;
	private int audioModeBefore;

	private SmsManager smsManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// things to listen on incoming calls
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		oldCallState = TelephonyManager.CALL_STATE_IDLE;

		// audiomanager to mute the phone
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// sms manager
		smsManager = SmsManager.getDefault();

		// notification manager
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// connect to toggleButton
		toggleButton = (ToggleButton) findViewById(R.id.toggleButton);

		// create new datasource which can save states
		dataSource = new TMDataSource(getApplicationContext());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// save data
		dataSource.open();
		dataSource.save(toggleButton.isChecked(), missedCallNotificationCode);
		dataSource.close();

		super.onPause();
	}

	@Override
	protected void onResume() {
		// get data from data source
		System.out.println("Get data from datasource in onResume()");
		dataSource.open();
		toggleButton.setChecked(dataSource.isChecked());
		missedCallNotificationCode = dataSource.getmissedCallNotificationCode();
		dataSource.close();
		
		super.onResume();
	}

	/**
	 * Method is called when toggle button is clicked
	 */
	public void onToggleClicked(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {
			// Enable Telephone Manager
			phoneStateListener = new MyPhoneStateListener();
			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_CALL_STATE);

			// Show message
			Toast toast = Toast.makeText(getApplicationContext(),
					"Telephone Manager is ON!", Toast.LENGTH_SHORT);
			toast.show();

			// Show icon in status bar
			CharSequence msg = getText(R.string.app_name) + " is running!";
			showNotification(ongoingNotificationCode, msg, "", true);

		} else {
			// Disable Telephone Manager
			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_NONE);

			// Show message
			Toast toast = Toast.makeText(getApplicationContext(),
					"Telephone Manager is OFF!", Toast.LENGTH_SHORT);
			toast.show();

			// Remove icon in status bar
			notificationManager.cancel(ongoingNotificationCode);
		}
	}

	/**
	 * Method to show a notification.
	 */
	private void showNotification(int requestCode, CharSequence msg,
			CharSequence info, boolean ongoingEvent) {

		CharSequence title = getText(R.string.app_name);
		Context context = getApplicationContext();
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(context, requestCode,
				intent, 0);

		Notification notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent)
				.setContentTitle(title).setContentText(msg)
				.setContentInfo(info)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.build();

		if (ongoingEvent) {
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
		}
		notificationManager.notify(requestCode, notification);
	}

	private class MyPhoneStateListener extends PhoneStateListener {

		private boolean isBusy() {
			// look if there is an event in calendar
			long now = System.currentTimeMillis();

			Cursor cursor = getApplicationContext().getContentResolver().query(
					Uri.parse("content://com.android.calendar/events"),
					new String[] { "calendar_id" },
					"dtstart <= " + now + " AND dtend > " + now
							+ " AND availability = "
							+ CalendarContract.Events.AVAILABILITY_BUSY, null,
					null);

			// if there is an event now the user is busy
			if (cursor.moveToFirst()) {
				return true;
			}

			// user is not busy
			return false;
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			// sms manager
			smsManager = SmsManager.getDefault();

			// if telephon rings
			if (state == TelephonyManager.CALL_STATE_RINGING) {

				System.out.println(incomingNumber + " is calling");

				// save old audioMode
				audioModeBefore = audioManager.getRingerMode();

				// when busy mute the phone
				if (isBusy()) {
					audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					
					// set old state to ringing
					oldCallState = TelephonyManager.CALL_STATE_RINGING;
				}
			}

			// if user answers call
			if (state == TelephonyManager.CALL_STATE_OFFHOOK
					&& oldCallState == TelephonyManager.CALL_STATE_RINGING) {
				// set oldState to offhook so no sms is send after call
				oldCallState = TelephonyManager.CALL_STATE_OFFHOOK;

				// set back audio manager
				audioManager.setRingerMode(audioModeBefore);
			}

			// if user missed call
			if (state == TelephonyManager.CALL_STATE_IDLE
					&& oldCallState == TelephonyManager.CALL_STATE_RINGING) {

				// set back audio manager
				System.out.println("AudioModeBefore: " + audioModeBefore);
				audioManager.setRingerMode(audioModeBefore);

				// reset oldState
				oldCallState = TelephonyManager.CALL_STATE_IDLE;

				// send answer sms
				String SENT = "android.telephony.SmsManager.STATUS_ON_ICC_SENT";
				PendingIntent sentPI = PendingIntent.getBroadcast(
						MainActivity.this, 0, new Intent(SENT), 0);
				String smsText = "You called me but I'm busy.";
				smsManager.sendTextMessage(incomingNumber, null, smsText,
						sentPI, null);
				System.out.println("Send sms to " + incomingNumber);

				// show notification for each muted call
				CharSequence msg = "Missed call!";
				CharSequence info = "Nr: " + incomingNumber;
				showNotification(missedCallNotificationCode, msg, info, false);

				// if missedCallNotificationCode reaches limit of 2147483647 it
				// will go on
				// with -2147483648
				missedCallNotificationCode++;
			}
		}
	}
}
