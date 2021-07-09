package com.maxeoneo.antitheftprotector;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CLSQLiteHelper extends SQLiteOpenHelper
{

  public static final String DATABASE_NAME = "tm.db";
  public static final int DATABASE_VERSION = 2;

  public static final String TABLE_NAME = "pwd";
  public static final String COLUMN_ID = "id";
  public static final String COLUMN_PWD = "pwd";
  public static final String COLUMN_IS_ACTIVE = "isActive";
  public static final String COLUMN_IS_SEND_LOCATION = "isSendLocation";
  public static final String COLUMN_EMAIL_ADDRESS = "emailAddress";

  public CLSQLiteHelper(Context context)
  {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db)
  {
    db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
        + COLUMN_ID + " INTEGER primary key, " + COLUMN_PWD
        + " VARCHAR(255), " + COLUMN_IS_ACTIVE
        + " INTEGER NOT NULL DEFAULT 0, " + COLUMN_IS_SEND_LOCATION
        + " INTEGER NOT NULL DEFAULT 0, " + COLUMN_EMAIL_ADDRESS
        + " VARCHAR(255));");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {
    Log.w(CLSQLiteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", no data will be saved");

    // delete the table
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

    // create a new table
    onCreate(db);
  }
}
