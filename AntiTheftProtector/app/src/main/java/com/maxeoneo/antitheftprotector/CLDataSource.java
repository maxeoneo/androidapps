package com.maxeoneo.antitheftprotector;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class CLDataSource {

	// Database
	private SQLiteDatabase db;
	private CLSQLiteHelper dbHelper;

	public CLDataSource(Context context) {
		dbHelper = new CLSQLiteHelper(context);
	}

	public void open() throws SQLException {
		db = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	/**
	 * Method to save PWD in the database
	 */
	public void savePwd(String pwd) {
		saveAll(pwd, isLockActive(), isSendLocation(), getPhonenumber());
	}

	/**
	 * Method to get PWD from database
	 */
	public String getPwd() {
		String[] columns = new String[] { CLSQLiteHelper.COLUMN_PWD };
		Cursor cursor = db.query(CLSQLiteHelper.TABLE_NAME, columns,
				CLSQLiteHelper.COLUMN_ID + " = 1", null, null, null, null);

		String pwd = null;

		if (cursor.moveToFirst()) {
			pwd = cursor.getString(0);
		}
		return pwd;
	}

	/**
	 * Method to save the lock state in Database
	 */
	public void setLockActive(boolean active) {
		saveAll(getPwd(), active, isSendLocation(), getPhonenumber());
	}

	/**
	 * Method to get the lock state from database
	 */
	public boolean isLockActive() {
		String[] columns = new String[] { CLSQLiteHelper.COLUMN_IS_ACTIVE };
		Cursor cursor = db.query(CLSQLiteHelper.TABLE_NAME, columns,
				CLSQLiteHelper.COLUMN_ID + " = 1", null, null, null, null);

		int active = 0;

		if (cursor.moveToFirst()) {
			active = cursor.getInt(0);
		}
		if (active == 1) {
			return true;
		}
		return false;
	}

	/**
	 *  get sendLocation from database
	 */
	public boolean isSendLocation() {
		String[] columns = new String[] { CLSQLiteHelper.COLUMN_IS_SEND_LOCATION };
		Cursor cursor = db.query(CLSQLiteHelper.TABLE_NAME, columns,
				CLSQLiteHelper.COLUMN_ID + " = 1", null, null, null, null);

		
		// get value and convert it to boolean
		int active = 0;

		if (cursor.moveToFirst()) {
			active = cursor.getInt(0);
		}
		if (active == 1) {
			return true;
		}
		return false;
	}

	/**
	 * get saved phonenumber from database
	 */
	public String getPhonenumber() {
		String[] columns = new String[] { CLSQLiteHelper.COLUMN_PHONENUMBER };
		Cursor cursor = db.query(CLSQLiteHelper.TABLE_NAME, columns,
				CLSQLiteHelper.COLUMN_ID + " = 1", null, null, null, null);

		String number = "";

		if (cursor.moveToFirst()) {
			number = cursor.getString(0);
		}
		System.out.println("Get phonenumber: " + number);
		return number;
	}
	
	/**
	 * save only location
	 */
	public void setSendLocation(boolean sendLoaction) {
		saveAll(getPwd(), isLockActive(), sendLoaction, getPhonenumber());
	}

	/**
	 * save only phone number
	 */
	public void setPhonenumber(String phonenumber) {
		saveAll(getPwd(), isLockActive(), isSendLocation(), phonenumber);
	}
	
	
	/**
	 * save all options 
	 */
	public void saveOptions(String pwd, boolean sendLocation, String phonenumber){
		saveAll(pwd, isLockActive(), sendLocation, phonenumber);
	}

	/**
	 * save everything
	 */
	private void saveAll(String pwd, boolean active, boolean sendLocation,
			String phoneNumber) {

		System.out.println("save Phonenumber: " + phoneNumber);
		
		// convert from boolean to int
		int activeInt = 0;
		if (active) {
			activeInt = 1;
		}
		int sendLocationInt = 0;
		if (sendLocation) {
			sendLocationInt = 1;
		}

		// save item in database with max itemId+1 as primary key
		String sqlCommand = "INSERT OR REPLACE INTO "
				+ CLSQLiteHelper.TABLE_NAME + "(" + CLSQLiteHelper.COLUMN_ID
				+ ", " + CLSQLiteHelper.COLUMN_PWD + ", "
				+ CLSQLiteHelper.COLUMN_IS_ACTIVE + ", "
				+ CLSQLiteHelper.COLUMN_IS_SEND_LOCATION + ", "
				+ CLSQLiteHelper.COLUMN_PHONENUMBER + ") VALUES (1, " + pwd
				+ ", " + activeInt + ", " + sendLocationInt + ", '"
				+ phoneNumber + "');";
		db.execSQL(sqlCommand);
	}

}
