package androidlab.exercise4_1;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView; 


public class MainActivity extends ListActivity {

	private Button add;
	private Item[] items;
	private Item pressedItem;
	private ItemsDataSource ds;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//get a datasource
		ds = new ItemsDataSource(this);
		ds.open();
		
		//update the itemlist
		updateList();
				
		
		add = (Button) findViewById(R.id.bAdd);
		add.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, AddActivity.class));
			}
		});
		
	}
	
	
	/**
	 * show a dialog with edit and delete buttons for the clicked item
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		pressedItem = items[position];
		showDialog(0);
	}
	
	
	/**
	 * definition of the dialog. id isn't used because only one dialog is needed
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
	  
	    Builder builder = new AlertDialog.Builder(this);
	    builder.setCancelable(true);
	    builder.setMessage("Item");
	    builder.setPositiveButton("Edit", new EditOnClickListener());
	    builder.setNegativeButton("Delete", new DeleteOnClickListener());
	    AlertDialog dialog = builder.create();
	    dialog.show();
	   
	    return super.onCreateDialog(id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		ds.open();
		updateList();
		super.onResume();
	}

	@Override
	protected void onPause() {
	    ds.close();
	    super.onPause();
	}
	
	
	/**
	 * Method to update the itemlist
	 */
	public void updateList() {
		items = ds.getAllUndoneItems();
		setListAdapter(new ArrayAdapter<Item>(MainActivity.this, android.R.layout.simple_list_item_1, items));
	}
	
	
	/**
	 * OnClickListener for the Edit button of the dialog
	 */
	private final class EditOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			Intent intent = new Intent(MainActivity.this, EditActivity.class);
			intent.putExtra("itemId", pressedItem.getId());
			startActivity(intent);
		}
	}

	/**
	 * OnClickListener for the Delete button of the dialog
	 */
	private final class DeleteOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			ds.deleteItem(pressedItem);
			pressedItem = null;
			updateList();
		}
	}

}
