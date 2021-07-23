package androidlab.exercise6_1;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TMDataSource {

	// Database
	private SQLiteDatabase db;
	private TMSQLiteHelper dbHelper;

	public TMDataSource(Context context) {
		dbHelper = new TMSQLiteHelper(context);
	}

	public void open() throws SQLException {
		db = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	/**
	 * Methode to save an Item in the database
	 */
	public void save(boolean checked, int callEventCode) {
		int checkedInt;
		if (checked) {
			checkedInt = 1;
		} else {
			checkedInt = 0;
		}

		// save item in database with max itemId+1 as primary key
		String sqlCommand = "INSERT OR REPLACE INTO "
				+ TMSQLiteHelper.TABLE_NAME + "(" + TMSQLiteHelper.COLUMN_ID
				+ ", " + TMSQLiteHelper.COLUMN_CHECKED + ", "
				+ TMSQLiteHelper.COLUMN_CALLCODE + ") VALUES (1, "
				+ checkedInt + ", " + callEventCode + ");";
		db.execSQL(sqlCommand);
	}

	/**
	 * Method to get the state of the toggleButton
	 */
	public boolean isChecked() {
		String[] columns = new String[] {TMSQLiteHelper.COLUMN_CHECKED};
		Cursor cursor = db.query(TMSQLiteHelper.TABLE_NAME, columns
				, TMSQLiteHelper.COLUMN_ID + " = 1", null, null, null
				, null);
		
		boolean checked = false;
		
		if (cursor.moveToFirst()) {
			if (cursor.getInt(0) == 1) {
				checked = true;
			}
		}
		
		return checked;
	}

	/**
	 * Method to get the state of the toggleButton
	 */
	public int getmissedCallNotificationCode() {
		String[] columns = new String[] {TMSQLiteHelper.COLUMN_CALLCODE};
		Cursor cursor = db.query(TMSQLiteHelper.TABLE_NAME, columns
				, TMSQLiteHelper.COLUMN_ID + " = 1", null, null, null
				, null);
		
		int missedCallNotificaionCode = 1;
		
		if (cursor.moveToFirst()) {
			missedCallNotificaionCode =  cursor.getInt(0);
		}
		
		return missedCallNotificaionCode;
	}
}
