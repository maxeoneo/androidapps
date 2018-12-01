package androidlab.exercise3_1;

import java.util.GregorianCalendar;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ItemsDataSource {

	// Database
	private SQLiteDatabase db;
	private ItemSQLiteHelper dbHelper;

	public ItemsDataSource(Context context) {
		dbHelper = new ItemSQLiteHelper(context);
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
	public void saveItem(String name, boolean doneBool, String deadline) {
		int done;
		if (doneBool) {
			done = 1;
		} else {
			done = 0;
		}
		
		//get max itemId in database
		Cursor c = db.query(false, ItemSQLiteHelper.TABLE_NAME
				, new String[] {ItemSQLiteHelper.COLUMN_ID}, null, null, null
				, null, ItemSQLiteHelper.COLUMN_ID + " DESC", "1");
		long id;
		if (c.moveToFirst()) {
			id = c.getLong(0);
		} else {
			id = 0L;
		}
		
		//save item in database with max itemId+1 as primary key
		db.execSQL("INSERT OR IGNORE INTO " + ItemSQLiteHelper.TABLE_NAME
				+ " VALUES (" + (id + 1) + ", '" + name + "', '" + deadline + "', " 
				+ done + ");");
	}
	
	/**
	 * Methode to update an Item in the database
	 */
	public void updateItem(long id, String name, boolean doneBool, String deadline) {
		int done;
		if (doneBool) {
			done = 1;
		} else {
			done = 0;
		}
		
		//update item in database
		db.execSQL("UPDATE OR IGNORE " + ItemSQLiteHelper.TABLE_NAME
				+ " SET " + ItemSQLiteHelper.COLUMN_NAME + " = '" + name 
				+ "', " + ItemSQLiteHelper.COLUMN_DEADLINE + " = '" + deadline 
				+ "', " + ItemSQLiteHelper.COLUMN_DONE + " = " + done 
				+ " WHERE " + ItemSQLiteHelper.COLUMN_ID + " = " + id + ";");	
	}
	
	/**
	 * Method to delete Item in database
	 */
	public void deleteItem(Item item) {
		long id = item.getId();
		db.delete(ItemSQLiteHelper.TABLE_NAME, ItemSQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}

	/**
	 * Method to get all undone Items out of the database
	 */
	public Item[] getAllUndoneItems() {
		//define the columns wich we want to query
		String[] columns = new String[] { ItemSQLiteHelper.COLUMN_ID,
				ItemSQLiteHelper.COLUMN_NAME, ItemSQLiteHelper.COLUMN_DEADLINE,
				ItemSQLiteHelper.COLUMN_DONE };
		
		//Query in Database
		Cursor cursor = db.query(ItemSQLiteHelper.TABLE_NAME, columns
				, ItemSQLiteHelper.COLUMN_DONE + " = 0", null, null, null
				, ItemSQLiteHelper.COLUMN_DEADLINE + " ASC");
		
		//Transform the data into java objects
		Item[] returnValue = new Item[cursor.getCount()];
		for (int i = 0; i < returnValue.length; i++) {
			cursor.moveToNext();
			returnValue[i] = new Item(cursor.getLong(0), cursor.getString(1));
			
			String deadline = cursor.getString(2);
			int done = cursor.getInt(3);
			returnValue[i].setDone(done == 1);
			
			
			
			if (deadline != null && !deadline.equals("null")) {
				int year = Integer.parseInt(deadline.substring(0, 4));
				int month = Integer.parseInt(deadline.substring(5, 7));
				int day = Integer.parseInt(deadline.substring(8, 10));
				int hour = Integer.parseInt(deadline.substring(11, 13));
				int min = Integer.parseInt(deadline.substring(14, 16));
				GregorianCalendar gc = new GregorianCalendar(year, month,
						day, hour, min);
				returnValue[i].setDeadline(gc);
			}
			
		}
		return returnValue;
	}
	
	/**
	 * Method to get one item by the itemId
	 */
	public Item getItemById(long id) {
		//define the columns wich we want to query
		String[] columns = new String[] { ItemSQLiteHelper.COLUMN_ID,
				ItemSQLiteHelper.COLUMN_NAME, ItemSQLiteHelper.COLUMN_DEADLINE,
				ItemSQLiteHelper.COLUMN_DONE };
		
		//Query in Database
		Cursor cursor = db.query(ItemSQLiteHelper.TABLE_NAME, columns
				, ItemSQLiteHelper.COLUMN_ID + " = " + id, null, null
				, null, null);
		
		//Transform the data into java objects
		Item returnValue = null;
		if (cursor.moveToFirst()) {
			returnValue = new Item(cursor.getLong(0), cursor.getString(1));
			String deadline = cursor.getString(2);
			int done = cursor.getInt(3);
			
			returnValue.setDone(done == 1);
			if (deadline != null && !deadline.equals("null")) {
				int year = Integer.parseInt(deadline.substring(0, 4));
				int month = Integer.parseInt(deadline.substring(5, 7));
				int day = Integer.parseInt(deadline.substring(8, 10));
				int hour = Integer.parseInt(deadline.substring(11, 13));
				int min = Integer.parseInt(deadline.substring(14, 16));
				GregorianCalendar gc = new GregorianCalendar(year, month,
						day, hour, min);
				returnValue.setDeadline(gc);
			}
		}
		return returnValue;
	}
	
	/**
	 * Method to get one item by the itemId
	 */
	public Item getNextItem() {
		//define the columns wich we want to query
		String[] columns = new String[] { ItemSQLiteHelper.COLUMN_ID,
				ItemSQLiteHelper.COLUMN_NAME, ItemSQLiteHelper.COLUMN_DEADLINE,
				ItemSQLiteHelper.COLUMN_DONE };
		
		//Query in Database
		Cursor cursor = db.query(ItemSQLiteHelper.TABLE_NAME, columns
				, ItemSQLiteHelper.COLUMN_DONE + " = 0" , null, null
				, null, ItemSQLiteHelper.COLUMN_DEADLINE + " ASC");
		
		//Transform the data into java objects
		Item returnValue = null;
		while (cursor.moveToNext()) {
			String deadline = cursor.getString(2);
			if (deadline != null && !deadline.equals("null")) {
				int year = Integer.parseInt(deadline.substring(0, 4));
				int month = Integer.parseInt(deadline.substring(5, 7));
				int day = Integer.parseInt(deadline.substring(8, 10));
				int hour = Integer.parseInt(deadline.substring(11, 13));
				int min = Integer.parseInt(deadline.substring(14, 16));
				GregorianCalendar gc = new GregorianCalendar(year, month,
						day, hour, min);
				
				if (gc.getTimeInMillis() > System.currentTimeMillis()) {
					returnValue = new Item(cursor.getLong(0), cursor.getString(1));
					returnValue.setDone(cursor.getInt(3) == 1);
					returnValue.setDeadline(gc);
					break;
				}
			}
		}
		return returnValue;
	}
}
