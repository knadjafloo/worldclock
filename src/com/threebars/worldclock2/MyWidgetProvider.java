package com.threebars.worldclock2;

import static com.threebars.worldclock2.WidgetSettingsActivity.PREF_PREFIX_KEY;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

	private static final String TAG = "AppWidgetProvider";
	
	
	public void onReceive(Context context, Intent intent) {
        // Protect against rogue update broadcasts (not really a security issue,
        // just filter bad broacasts out so subclasses are less likely to crash).
        String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                
                int widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        		Log.d(TAG, " received update request for widget_id : " + widgetID);
        		// If there is no single ID, call the super implementation.
        		if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID)
        		{
        			super.onReceive(context, intent);
        		}
        		// Otherwise call our onUpdate() passing a one element array, with the retrieved ID.
        		else
        			this.onUpdate(context, AppWidgetManager.getInstance(context), new int[] { widgetID });
            }
        }
        else if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                this.onDeleted(context, new int[] { appWidgetId });
            }
        }
        else if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)) {
            this.onEnabled(context);
        }
        else if (AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(action)) {
            this.onDisabled(context);
        }
    }

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		
		for (int widgetId : appWidgetIds) {
			Log.d(TAG, " xxxxxxxxxxxxxxxxxxxxxxx clearing pref : " + widgetId);
			Editor prefs = context.getSharedPreferences(PREF_PREFIX_KEY + widgetId, 0).edit();
			prefs.clear();
			prefs.commit();
		}

	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		// Get all ids
//		ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
//		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
//		for (int widgetId : appWidgetIds) {
//			
//			//read the info from the shared preference and update it
//			CityTimeZone ctz = WidgetSettingsActivity.loadCtzFromSharedPrefs(context, widgetId);
//			Log.d(TAG,  " onUpdate: widgetId ::::::::::::::::::::::: " + widgetId + " city : " + (ctz == null ? "null " : ctz.city));
//			updateAppWidget(context, appWidgetManager, widgetId, "", ctz);
//		}
	}
	
	
	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, String titlePrefix, CityTimeZone ctz) {
//        Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId + " city: " + (ctz == null ? "null" : ctz.city));

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
		Log.d(TAG, "~~~~~~~~~~~~~~~~~~~ updated widget id : " + appWidgetId + " with city : " + (ctz != null ? ctz.city : "no city ") + " ready to set its intent....");
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		views.setOnClickPendingIntent(R.id.c_widget_layout, pendingIntent);
        
        // Tell the widget manager
//		AppWidgetManager.getInstance( context ).updateAppWidget( thisWidget, views );
		appWidgetManager.updateAppWidget(appWidgetId, views);
		
    }
}
