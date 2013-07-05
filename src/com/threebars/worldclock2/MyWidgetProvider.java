package com.threebars.worldclock2;

import static com.threebars.worldclock2.WidgetSettingsActivity.PREF_PREFIX_KEY;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

	private static final String TAG = "AppWidgetProvider";
	public static final String UPDATE_ALL_WIDGETS = "com.threebars.worldclock2.UPDATE_ALL_WIDGETS";
	
	private static AlarmManager alarmManager;
	private static PendingIntent pendingIntentAlarm;

	
	@Override
	public void onEnabled(Context context) {
		//initialize alarm Manager
		if (alarmManager == null) {
			Log.d("onEnabled", "$$$$$$$$$$$ calling on enabled and starting alarm manager...");
			alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			Intent intentUpdate = new Intent(context, MyWidgetProvider.class);
		      intentUpdate.setAction(MyWidgetProvider.UPDATE_ALL_WIDGETS);//Set an action anyway to filter it in onReceive()
		      //We will need the exact instance to identify the intent.
		      pendingIntentAlarm = PendingIntent.getBroadcast(context,
	                                                            0,
	                                                            intentUpdate,
	                                                            PendingIntent.FLAG_UPDATE_CURRENT);
		    GregorianCalendar nextMinute = (GregorianCalendar)Calendar.getInstance();
		    nextMinute.add(Calendar.MINUTE, 1);
		    nextMinute.set(Calendar.SECOND, 0);
			
			alarmManager.setRepeating(AlarmManager.RTC, nextMinute.getTimeInMillis() , DateUtils.MINUTE_IN_MILLIS, pendingIntentAlarm);
		}
	}
	
	public static void updateAllWidgets(final Context context, final int layoutResourceId, final Class<? extends AppWidgetProvider> appWidgetClass) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutResourceId);

		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context, appWidgetClass));

		for (int i = 0; i < appWidgetIds.length; ++i) {
			manager.updateAppWidget(appWidgetIds[i], remoteViews);
		}
	}
	
	@Override
	public void onDisabled(Context context) {
		Log.d("onDisabled", "calling onDisabled............................");
		// cancel the alarm manager since last instance is deleted
//	      Intent intentUpdate = new Intent(context, MyWidgetProvider.class);
//	      //AlarmManager are identified with Intent's Action and Uri.
//	      intentUpdate.setAction(MyWidgetProvider.UPDATE_ALL_WIDGETS);
//	      //For a global AlarmManager, don't put the uri to cancel
//	      //all the AlarmManager with action UPDATE_ONE.
//	      PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(context,
//	                                                                    0,
//	                                                                    intentUpdate,
//	                                              PendingIntent.FLAG_UPDATE_CURRENT);
	      if(alarmManager != null)
	      {
	    	  alarmManager.cancel(pendingIntentAlarm);
	      }
	      Log.d("cancelAlarmManager", "Cancelled Alarm. Action = " + MyWidgetProvider.UPDATE_ALL_WIDGETS);
	}
	
	public void onReceive(Context context, Intent intent) {
		
	    String action = intent.getAction();
	    if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) { 
	        Bundle extras = intent.getExtras();
	        if (extras != null) {
	            int[] appWidgetIds = extras
	                    .getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);             
	            if (appWidgetIds.length > 0) {
	                this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);//here you can call onUpdate method, and update your views as you wish
	            }
	        }
	        
	    } else if(action.equals(UPDATE_ALL_WIDGETS)) {
	    	//get all widgetIds currently active
	    	Log.d(TAG, "updating allwidgets..........");
	    	
	    	 AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
	         ComponentName appWidgetName = 
	             new ComponentName(context, MyWidgetProvider.class);    
	         int[] appWidgetIds = widgetManager.getAppWidgetIds(appWidgetName);
	         
	         ComponentName appWidgetName2 = 
		             new ComponentName(context, LargeAppWidgetProvider.class);    
		         int[] appWidgetIds2 = widgetManager.getAppWidgetIds(appWidgetName2);
	         
	         onUpdate(context, widgetManager, appWidgetIds);
	         onUpdate(context, widgetManager, appWidgetIds2);
	    	
//	    	AppWidgetManager manager = AppWidgetManager.getInstance(context);
//	    	final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context, MyWidgetProvider.class));
//	    	this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);//here you can call onUpdate method, and update your views as you wish
	    } else if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
	        Bundle extras = intent.getExtras();
	        if (extras != null
	                && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
	            final int appWidgetId = extras
	                    .getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
	            this.onDeleted(context, new int[] { appWidgetId });
	        }
	    } else if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)) {
	        this.onEnabled(context);
	    } else if (AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(action)) {
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
			prefs.remove(PREF_PREFIX_KEY + widgetId);
			prefs.commit();
		}

	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		// Get all ids
		ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
//		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : appWidgetIds) {
			
			//read the info from the shared preference and update it
//			CityTimeZone ctz = WidgetSettingsActivity.loadCtzFromSharedPrefs(context, widgetId);
			List<CityTimeZone> ctzs = WidgetSettingsActivity.loadCtzsFromSharedPrefs(context, widgetId);
			boolean use24Hours = WidgetSettingsActivity.loadUse24HoursFromSharedPRefs(context, widgetId);
			int backgroundColor = WidgetSettingsActivity.loadWidgetBackgroundColor(context, widgetId);
//			Log.d(TAG,  " onUpdate: widgetId ::::::::::::::::::::::: " + widgetId + " city : " + (ctz == null ? "null " : ctz.city));
//			updateAppWidget(context, appWidgetManager, widgetId, "", ctz, use24Hours);
			
			if (ctzs != null) {
				AppWidgetProviderInfo appInfo = appWidgetManager.getAppWidgetInfo(widgetId);
				if (appInfo.initialLayout == R.layout.appwidget_layout) {
					updateAppWidget(context, appWidgetManager, widgetId, "", ctzs.get(0), false, backgroundColor);
				} else if (appInfo.initialLayout == R.layout.widget_4x2_layout) {
					updateAppWidget(context, appWidgetManager, widgetId, "", ctzs, use24Hours, backgroundColor);
				}
			}
			
		}
	}
	
	
	private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, String titlePrefix, CityTimeZone ctz, boolean use24Hours, int bgColor) {
//        Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId + " city: " + (ctz == null ? "null" : ctz.city));

        // Construct the RemoteViews object.  It takes the package name (in our case, it's our
        // package, but it needs this because on the other side it's the widget host inflating
        // the layout from our package).
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_layout);
		if (ctz != null) {
			String timeFormat = "hh:mm a";
			if (use24Hours) {
				timeFormat = "HH:mm";
			}
			DateTimeFormatter df = DateTimeFormat.forPattern(timeFormat);

			DateTime dt = new DateTime(DateTimeZone.forID(TimeUtil.getTimeZone(ctz.getTimezoneName())));

			DateTimeFormatter fmt = DateTimeFormat.mediumDate();
			String mediumDate = fmt.print(dt);
			fmt = DateTimeFormat.forPattern("EE"); // get day of the week
			String day = fmt.print(dt);

			/*android.graphics.Color.TRANSPARENT */
//			views.setInt(R.id.c_widget_layout, "setBackgroundResource", android.graphics.Color.YELLOW); // this must be set first

			views.setTextViewText(R.id.dateDate, day + " " + mediumDate);
			views.setTextViewText(R.id.dateTime, df.print(dt));
			views.setTextViewText(R.id.dateCity, ctz.city);
			
			if (bgColor != Color.BLACK) {
				views.setInt(R.id.c_widget_layout, "setBackgroundColor", bgColor);
			}

		}
        ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
     // Register an onClickListener
		Intent intent = new Intent(context, WidgetSettingsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP );	//need these flags so they don't get reused
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		Log.d(TAG, "~~~~~~~~~~~~~~~~~~~ updated widget id : " + appWidgetId + " with city : " + (ctz != null ? ctz.city : "no city ") + " ready to set its intent....");
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		//android reuses intents so make sure this intent is unique by providing appWidgetId
		PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		views.setOnClickPendingIntent(R.id.c_widget_layout, pendingIntent);
        
        // Tell the widget manager
		appWidgetManager.updateAppWidget(appWidgetId, views);
    }
	
	
	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, String titlePrefix, List<CityTimeZone> ctzs, boolean use24Hours, int bgColor) {
//        Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId + " city: " + (ctz == null ? "null" : ctz.city));

        // Construct the RemoteViews object.  It takes the package name (in our case, it's our
        // package, but it needs this because on the other side it's the widget host inflating
        // the layout from our package).
		RemoteViews views = null;
		if (ctzs != null) 
		{
			AppWidgetProviderInfo appInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
			int layout = appInfo.initialLayout;
			
			if (layout == R.layout.appwidget_layout) {
				CityTimeZone ctz = ctzs.get(0);
				updateAppWidget(context, appWidgetManager, appWidgetId, titlePrefix, ctz, use24Hours, bgColor);
				return;
			} else if (layout == R.layout.widget_4x2_layout) {
				views = new RemoteViews(context.getPackageName(), R.layout.widget_4x2_layout);
				String timeFormat = "hh:mm a";
				 if (use24Hours) {
				 timeFormat = "HH:mm";
				 }
				DateTimeFormatter df = DateTimeFormat.forPattern(timeFormat);

				// update first one
				CityTimeZone ctz = ctzs.get(0);
				DateTime dt = new DateTime(DateTimeZone.forID(TimeUtil.getTimeZone(ctz.getTimezoneName())));

				DateTimeFormatter fmt = DateTimeFormat.mediumDate();
				String mediumDate = fmt.print(dt);
				fmt = DateTimeFormat.forPattern("EE"); // get day of the week
				String day = fmt.print(dt);

				/* android.graphics.Color.TRANSPARENT */
				// views.setInt(R.id.c_widget_layout, "setBackgroundResource",
				// android.graphics.Color.YELLOW); // this must be set first

				views.setTextViewText(R.id.dateTime1, df.print(dt));
				views.setTextViewText(R.id.dateCity1, ctz.city);
				if (bgColor != Color.BLACK) {
					views.setInt(R.id.c_widget_layout2, "setBackgroundColor", bgColor);
				}
				// update second one (if exists)
				if (ctzs.size() > 1) {
					CityTimeZone ctz2 = ctzs.get(1);
					DateTime dt2 = new DateTime(DateTimeZone.forID(TimeUtil.getTimeZone(ctz2.getTimezoneName())));

					String mediumDate2 = fmt.print(dt2);
					DateTimeFormatter fmt2 = DateTimeFormat.forPattern("EE"); // get day of the week
					String day2 = fmt.print(dt2);

					/* android.graphics.Color.TRANSPARENT */
					// views.setInt(R.id.c_widget_layout, "setBackgroundResource",
					// android.graphics.Color.YELLOW); // this must be set first

					views.setTextViewText(R.id.dateTime2, df.print(dt2));
					views.setTextViewText(R.id.dateCity2, ctz2.city);
				}

				 ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
			     // Register an onClickListener
					Intent intent = new Intent(context, WidgetSettingsActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP );	//need these flags so they don't get reused
					intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
					
					intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

					//android reuses intents so make sure this intent is unique by providing appWidgetId
					PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					
					views.setOnClickPendingIntent(R.id.c_widget_layout2, pendingIntent);
			        
			        // Tell the widget manager
					appWidgetManager.updateAppWidget(appWidgetId, views);
			}
		}
       
    }

	
	public static Bitmap getBackground (int bgcolor)
	{
	try
	    {
	        Bitmap.Config config = Bitmap.Config.ARGB_8888; // Bitmap.Config.ARGB_8888 Bitmap.Config.ARGB_4444 to be used as these two config constant supports transparency
	        Bitmap bitmap = Bitmap.createBitmap(2, 2, config); // Create a Bitmap
	 
	        Canvas canvas =  new Canvas(bitmap); // Load the Bitmap to the Canvas
	        canvas.drawColor(bgcolor); //Set the color
	 
	        return bitmap;
	    }
	    catch (Exception e)
	    {
	        return null;
	    }
	}
}
