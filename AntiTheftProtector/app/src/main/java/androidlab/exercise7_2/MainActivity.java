package androidlab.exercise7_2;

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

public class MainActivity extends Activity {

	private final Context context = this;
	private ToggleButton tOnOff;
	private Button bSetPwd;
	private CLDataSource dataSource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
		if (active) {
			bSetPwd.setVisibility(View.GONE);
		}
		bSetPwd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// custom dialog
				final Dialog dialog = new Dialog(context);
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

				if (oldPwdString != null) {
					oldPwd.setVisibility(View.VISIBLE);
				} else {
					oldPwd.setVisibility(View.GONE);
				}

				// get options from database
				phoneNumber.setText(pNumber);
				tSendLoc.setChecked(sendLoc);
				if (sendLoc) {
					phoneNumber.setVisibility(View.VISIBLE);
				} else {
					phoneNumber.setVisibility(View.GONE);
				}

				// set method which is called when user clicks toggle button
				tSendLoc.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						if (isChecked) {
							phoneNumber.setVisibility(View.VISIBLE);
						} else {
							phoneNumber.setVisibility(View.GONE);
						}
					}
				});

				Button bSave = (Button) dialog.findViewById(R.id.bSave);

				// if button is clicked, close the custom dialog
				bSave.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						// oldPwd must be right
						if (oldPwdString == null
								|| oldPwdString.equals(oldPwd.getText()
										.toString())) {

							// min 4 numbers
							if (newPwd.getText().length() >= 4) {

								// newPwd and repeatNewPwd must be the same and
								if (newPwd
										.getText()
										.toString()
										.equals(repeatNewPwd.getText()
												.toString())) {

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

								} else {
									// pwds are not the same
									System.out.println("PWDS are not the same");
									// Show message
									Toast toast = Toast.makeText(context,
											R.string.emNewAndRepeatedEquals,
											Toast.LENGTH_SHORT);
									toast.show();
								}
							} else {

								// when new pwd and repeated new pwd are empty
								// save only the other two things
								if (newPwd.getText().toString().equals("")
										|| repeatNewPwd.getText().toString()
												.equals("")) {

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
								} else {

									// pwds are to short
									System.out.println("PWDS are to short");
									// Show message
									Toast toast = Toast.makeText(context,
											R.string.emPinToShort,
											Toast.LENGTH_SHORT);
									toast.show();
								}
							}
						} else {
							// old pwd is not right
							System.out
									.println("old PWD is not right - database: "
											+ oldPwdString
											+ " your entered: "
											+ oldPwd.getText().toString());
							// Show message
							Toast toast = Toast.makeText(context,
									R.string.emOldPinNotRight,
									Toast.LENGTH_SHORT);
							toast.show();
						}
					}
				});

				dialog.show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Method is called when toggle button is clicked
	 */
	public void onToggleClicked(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {
			// cellphone lock is on

			dataSource.open();
			String pwd = dataSource.getPwd();
			dataSource.close();

			if (pwd != null) {

				// hide Button
				bSetPwd.setVisibility(View.GONE);

				// start cellphone lock
				// set running to database
				dataSource.open();
				dataSource.setLockActive(true);
				dataSource.close();

			} else {
				// old pwd is not right
				System.out.println("please set pin first");

				tOnOff.setChecked(false);

				// Show message
				Toast toast = Toast.makeText(context, R.string.emSetPwd,
						Toast.LENGTH_SHORT);
				toast.show();
			}

		} else {
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
			submit.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (pwd.getText().toString().equals(savedPwd)) {
						// show Button
						bSetPwd.setVisibility(View.VISIBLE);

						// stop cellphone lock
						// save not running to data base
						dataSource.open();
						dataSource.setLockActive(false);
						dataSource.close();

					} else {
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
