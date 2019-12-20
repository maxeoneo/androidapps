package com.maxeoneo.antitheftprotector;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class CLDataSource
{

  // Database
  private SQLiteDatabase db;
  private CLSQLiteHelper dbHelper;

  public CLDataSource(Context context)
  {
    dbHelper = new CLSQLiteHelper(context);
  }

  public void open() throws SQLException
  {
    db = dbHelper.getWritableDatabase();
  }

  public void close()
  {
    dbHelper.close();
  }

  public void savePassword(String pwd)
  {
    open();
    saveAll(pwd, isLockActive(), querySendLocation(), queryPhoneNumber());
    close();
  }

  public String getPassword()
  {
    open();
    final String pwd = queryPassword();
    close();

    return pwd;
  }

  private String queryPassword()
  {
    String[] columns = new String[]{CLSQLiteHelper.COLUMN_PWD};
    Cursor cursor = db.query(CLSQLiteHelper.TABLE_NAME, columns,
        CLSQLiteHelper.COLUMN_ID + " = 1", null, null, null, null);

    String pwd = "";

    if (cursor.moveToFirst())
    {
      pwd = cursor.getString(0);
    }
    return pwd;
  }

  public void setLockActive(boolean active)
  {
    open();
    saveAll(queryPassword(), active, querySendLocation(), queryPhoneNumber());
    close();
  }

  public boolean isLockActive()
  {
    open();
    final boolean active = queryLockActive();
    close();
    return active;
  }

  private boolean queryLockActive()
  {
    String[] columns = new String[]{CLSQLiteHelper.COLUMN_IS_ACTIVE};
    Cursor cursor = db.query(CLSQLiteHelper.TABLE_NAME, columns,
        CLSQLiteHelper.COLUMN_ID + " = 1", null, null, null, null);

    int active = 0;

    if (cursor.moveToFirst())
    {
      active = cursor.getInt(0);
    }
    if (active == 1)
    {
      return true;
    }
    return false;
  }

  public boolean getSendLocation()
  {
    open();
    final boolean sendLocation = querySendLocation();
    close();
    return sendLocation;
  }

  private boolean querySendLocation()
  {
    String[] columns = new String[]{CLSQLiteHelper.COLUMN_IS_SEND_LOCATION};
    Cursor cursor = db.query(CLSQLiteHelper.TABLE_NAME, columns,
        CLSQLiteHelper.COLUMN_ID + " = 1", null, null, null, null);


    // get value and convert it to boolean
    int active = 0;

    if (cursor.moveToFirst())
    {
      active = cursor.getInt(0);
    }
    if (active == 1)
    {
      return true;
    }
    return false;
  }

  public String getPhoneNumber()
  {
    open();
    final String phoneNumber = queryPhoneNumber();
    close();
    return phoneNumber;
  }

  private String queryPhoneNumber()
  {
    String[] columns = new String[]{CLSQLiteHelper.COLUMN_PHONENUMBER};
    Cursor cursor = db.query(CLSQLiteHelper.TABLE_NAME, columns,
        CLSQLiteHelper.COLUMN_ID + " = 1", null, null, null, null);

    String number = "";

    if (cursor.moveToFirst())
    {
      number = cursor.getString(0);
    }
    System.out.println("Get phonenumber: " + number);
    return number;
  }

  public void setSendLocation(boolean sendLoaction)
  {
    open();
    saveAll(queryPassword(), isLockActive(), sendLoaction, queryPhoneNumber());
    close();
  }

  public void setPhonenumber(String phonenumber)
  {
    open();
    saveAll(queryPassword(), isLockActive(), querySendLocation(), phonenumber);
    close();
  }

  public void saveOptions(String pwd, boolean sendLocation, String phoneNumber)
  {
    open();
    saveAll(pwd, isLockActive(), sendLocation, phoneNumber);
    close();
  }

  private void saveAll(String pwd, boolean active, boolean sendLocation,
                       String phoneNumber)
  {

    System.out.println("save Phonenumber: " + phoneNumber);

    // convert from boolean to int
    int activeInt = 0;
    if (active)
    {
      activeInt = 1;
    }
    int sendLocationInt = 0;
    if (sendLocation)
    {
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
