package androidlab.exercise4_1;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ItemSQLiteHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "items.db";
	public static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_NAME = "items";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DEADLINE = "deadline";
	public static final String COLUMN_DONE = "done";
	public static final String COLUMN_LONGITUDE = "longitude";
	public static final String COLUMN_LATITUDE = "latitude";
	
	public ItemSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME 
				+ " (" + COLUMN_ID + " INTEGER primary key, " 
				+ COLUMN_NAME + " TEXT NOT NULL, " + COLUMN_DEADLINE + " TEXT"
	            + ", " + COLUMN_DONE + " INTEGER, " + COLUMN_LONGITUDE 
	            + " REAL, " + COLUMN_LATITUDE + " REAL);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ItemSQLiteHelper.class.getName(),"Upgrading database from version " 
				+ oldVersion + " to " + newVersion + ", all data will be saved");
		
		//Save the old data
		Cursor cursor = db.query(ItemSQLiteHelper.TABLE_NAME,
		        new String[] {COLUMN_ID, COLUMN_NAME, COLUMN_DEADLINE, COLUMN_DONE, COLUMN_LONGITUDE, COLUMN_LATITUDE}
				, null, null, null, null, null);
		
		//deleta the table
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		
		//create a new table
		onCreate(db);
		
		//put the old data in the new table
		cursor.moveToFirst();
		while (cursor.moveToNext()) {
			long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
			String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
			String deadline = cursor.getString(cursor.getColumnIndex(COLUMN_DEADLINE));
			int done = cursor.getInt(cursor.getColumnIndex(COLUMN_DONE));
			double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
			double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
			db.execSQL("INSERT OR IGNORE INTO " + TABLE_NAME + "VALUES (" + id 
					+ ", " + name + ", " + deadline + ", " + done + ", " + longitude 
					+ ", " + latitude + ");");
		}
	}
}
