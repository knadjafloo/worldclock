package com.threebars.worldclock2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import static com.threebars.worldclock2.WidgetSettingsActivity.PREF_PREFIX_KEY;

public class MyWidgetProvider extends AppWidgetProvider {

	private static final String TAG = "AppWidgetProvider";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.d(TAG, "onReceive in AppWidgetProvider called.");
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		// Get all ids
		ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			Log.d(TAG,  " city name is ::::::::::::::::::::::: " + getResultExtras(true).getString("city"));
			
			SharedPreferences sharedPref = context.getSharedPreferences(PREF_PREFIX_KEY + widgetId, 0);
			
			//read the info from the shared preference and update it
			CityTimeZone ctz = new CityTimeZone();
			ctz.city = sharedPref.getString("city", null);
			ctz.id = sharedPref.getString("id", null);
			updateAppWidget(context, appWidgetManager, widgetId, "", ctz);
//			
//			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget_layout);
//			
////			// Set the text
////			remoteViews.setTextViewText(R.id.update, String.valueOf(number));
//
//			// Register an onClickListener
//			Intent intent = new Intent(context, WidgetSettingsActivity.class);
////			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
//
//			Log.d(TAG, " calling click listener for widgetid : " + widgetId);
//			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//			remoteViews.setOnClickPendingIntent(R.id.c_widget_layout, pendingIntent);
//			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}
	
	
	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, String titlePrefix, CityTimeZone ctz) {
        Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId + " titlePrefix=" + titlePrefix);

        // Construct the RemoteViews object.  It takes the package name (in our case, it's our
        // package, but it needs this because on the other side it's the widget host inflating
        // the layout from our package).
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_layout);
        views.setTextViewText(R.id.update, ctz.city);

     // Register an onClickListener
		Intent intent = new Intent(context, WidgetSettingsActivity.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		intent.putExtra("city", ctz.city);
		intent.putExtra("id", ctz.id);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		views.setOnClickPendingIntent(R.id.c_widget_layout, pendingIntent);
        
        // Tell the widget manager
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
