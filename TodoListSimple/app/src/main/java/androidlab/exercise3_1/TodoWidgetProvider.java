package androidlab.exercise3_1;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class TodoWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		//get the next item
		ItemsDataSource ds = new ItemsDataSource(context);
		ds.open();
		Item next = ds.getNextItem();
		ds.close();
		
		//show the name and the deadline in the widget
		String name;
		if (next != null) {
			name = next.getName() + ": " + next.getDeadlineAsString();
		} else {
			name = "";
		}
			
		// update all widgets with this widgetName
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), 
				R.layout.widget_layout);

		// Set the text
		remoteViews.setTextViewText(R.id.nextItem, String.valueOf(name));
	    
		
		// add an onClickListener to start activity
	    Intent configIntent = new Intent(context, MainActivity.class);
	    PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
	    remoteViews.setOnClickPendingIntent(R.id.widgetLayout, configPendingIntent);
	 
	    appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
  	}
} 