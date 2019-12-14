package androidlab.exercise6_1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TMSQLiteHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "tm.db";
	public static final int DATABASE_VERSION = 1;

	public static final String TABLE_NAME = "state";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_CHECKED = "checked";
	public static final String COLUMN_CALLCODE = "callcode";

	public TMSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
				+ COLUMN_ID + " INTEGER primary key, " + COLUMN_CHECKED
				+ " INTEGER NOT NULL, " + COLUMN_CALLCODE
				+ " INTEGER NOT NULL);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TMSQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", no data will be saved");
		
		// deleta the table
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

		// create a new table
		onCreate(db);
	}
}
