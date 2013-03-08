package com.threebars.worldclock2;

import static com.threebars.worldclock2.WidgetSettingsActivity.PREF_PREFIX_KEY;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

	private static final String TAG = "AppWidgetProvider";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.d(TAG, "onReceive in AppWidgetProvider called.");
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		
		ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		//delete the shared preference belonging to this widget
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			Log.d(TAG, " clearing pref : " + widgetId);
			Editor prefs = context.getSharedPreferences(PREF_PREFIX_KEY + widgetId, 0).edit();
			prefs.clear();
			prefs.commit();
		}

	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		// Get all ids
		ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			Log.d(TAG,  " city name is ::::::::::::::::::::::: " + getResultExtras(true).getString("city"));
			
			
			//read the info from the shared preference and update it
			CityTimeZone ctz = WidgetSettingsActivity.loadCtzFromSharedPrefs(context, widgetId);
			updateAppWidget(context, appWidgetManager, widgetId, "", ctz);
		}
	}
	
	
	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, String titlePrefix, CityTimeZone ctz) {
        Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId + " city: " + (ctz == null ? "null" : ctz.city));

        // Construct the RemoteViews object.  It takes the package name (in our case, it's our
        // package, but it needs this because on the other side it's the widget host inflating
        // the layout from our package).
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_layout);
        if(ctz != null) {
        	views.setTextViewText(R.id.update, ctz.city);
        }
        ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
     // Register an onClickListener
		Intent intent = new Intent(context, WidgetSettingsActivity.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		views.setOnClickPendingIntent(R.id.c_widget_layout, pendingIntent);
        
        // Tell the widget manager
		AppWidgetManager.getInstance( context ).updateAppWidget( thisWidget, views );
    }
}
