package com.threebars.worldclock2;

import static com.threebars.worldclock2.WidgetSettingsActivity.PREF_PREFIX_KEY;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.threebars.worldclock2.WidgetSettingsActivity.FontItem;

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
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
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
			
//			Log.d(TAG,  " onUpdate: widgetId ::::::::::::::::::::::: " + widgetId + " city : " + (ctz == null ? "null " : ctz.city));
//			updateAppWidget(context, appWidgetManager, widgetId, "", ctz, use24Hours);
			
			if (ctzs != null) {
				AppWidgetProviderInfo appInfo = appWidgetManager.getAppWidgetInfo(widgetId);
				if (appInfo.initialLayout == R.layout.appwidget_layout) {
					updateAppWidget(context, appWidgetManager, widgetId, "", ctzs.get(0));
				} else if (appInfo.initialLayout == R.layout.widget_4x2_layout) {
					updateAppWidget(context, appWidgetManager, widgetId, "", ctzs);
				}
			}
			
		}
	}
	
	
	private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, String titlePrefix, CityTimeZone ctz) {
//        Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId + " city: " + (ctz == null ? "null" : ctz.city));

		boolean use24Hours = WidgetSettingsActivity.loadUse24HoursFromSharedPRefs(context, appWidgetId);
		int backgroundColor = WidgetSettingsActivity.loadWidgetBackgroundColor(context, appWidgetId);
		int textColor = WidgetSettingsActivity.loadWidgetTextColor(context, appWidgetId);
		FontItem fontItem = WidgetSettingsActivity.loadWidgetTextFont(context, appWidgetId);
		String font = fontItem == null ? "roboto-bold.ttf" : fontItem.fileName;
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

			//The formula is pixels = dps * (density / 160), 
			int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130 /*2 cells */, context.getResources().getDisplayMetrics());
			int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 26 /*1 cell */, context.getResources().getDisplayMetrics());
			Log.d(TAG, " width : " + width + " height : " +height);
			int fontSize = 100;
			
			int clockHeight= (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36 /*1 cell */, context.getResources().getDisplayMetrics());
			
			views.setImageViewBitmap(R.id.dateDate, buildUpdate(context, day + " " + mediumDate, textColor, width, height, 18, font, (height/2), Align.CENTER));
//			views.setTextViewText(R.id.dateDate, day + " " + mediumDate);
//			views.setTextViewText(R.id.dateTime, df.print(dt));
//			views.setTextViewText(R.id.dateCity, ctz.city);
			
			
			
			views.setImageViewBitmap(R.id.dateTime, buildUpdate(context, df.print(dt), textColor, width, clockHeight, 44, font, (height/2)+15, Align.CENTER));
			
			if (backgroundColor != Color.BLACK) {
				views.setInt(R.id.c_widget_layout, "setBackgroundColor", backgroundColor);
			}
			
			views.setImageViewBitmap(R.id.dateCity, buildUpdate(context, ctz.city, textColor, width, height, 18,  font, height/2, Align.CENTER));
			
//			views.setTextColor(R.id.dateTime, textColor);
//			views.setTextColor(R.id.dateCity, textColor);
//			views.setTextColor(R.id.dateDate, textColor);

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
	
	public static Bitmap buildUpdate(Context context, String time, int color, int width, int height, int fontSize, String fontFile, int startY, android.graphics.Paint.Align align ) 
	{
		Log.d(TAG, " font name is : " + fontFile);
	    Bitmap myBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    Canvas myCanvas = new Canvas(myBitmap);
	    Paint paint = new Paint();
//	    Typeface clock = Typeface.createFromAsset(context.getAssets(),"fonts/" + fontFile);
	    Typeface clock = Typefaces.get(context, "fonts/" + fontFile);
	    paint.setAntiAlias(true);
	    paint.setSubpixelText(true);
	    paint.setTypeface(clock);
	    paint.setStyle(Paint.Style.FILL);
	    paint.setColor(color);
	    paint.setTextSize(fontSize);
	    paint.setTextAlign(Align.CENTER);
	    myCanvas.drawText(time, (width / 2), startY, paint);
	    return myBitmap;
	}
	
	
	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, String titlePrefix, List<CityTimeZone> ctzs) {
//        Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId + " city: " + (ctz == null ? "null" : ctz.city));

		boolean use24Hours = WidgetSettingsActivity.loadUse24HoursFromSharedPRefs(context, appWidgetId);
		int backgroundColor = WidgetSettingsActivity.loadWidgetBackgroundColor(context, appWidgetId);
		int textColor = WidgetSettingsActivity.loadWidgetTextColor(context, appWidgetId);
		FontItem fontItem = WidgetSettingsActivity.loadWidgetTextFont(context, appWidgetId);
		String font = fontItem == null ? "roboto-bold.ttf" : fontItem.fileName;
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
				updateAppWidget(context, appWidgetManager, appWidgetId, titlePrefix, ctz);
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
				
				int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160 /*2 cells */, context.getResources().getDisplayMetrics());
				int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36 /*1 cell */, context.getResources().getDisplayMetrics());
				Log.d(TAG, " width : " + width + " height : " +height);
				
				
				views.setImageViewBitmap(R.id.dateTime1, buildUpdate(context, df.print(dt), textColor, width, height, 24, font, (height/2), Align.LEFT));
				views.setImageViewBitmap(R.id.dateCity1, buildUpdate(context, ctz.city, textColor, width, height, 24, font, (height/2), Align.RIGHT));
				

//				views.setTextViewText(R.id.dateTime1, df.print(dt));
//				views.setTextViewText(R.id.dateCity1, ctz.city);
				if (backgroundColor != Color.BLACK) {
					views.setInt(R.id.c_widget_layout2, "setBackgroundColor", backgroundColor);
				}
//				views.setTextColor(R.id.dateTime1, textColor);
//				views.setTextColor(R.id.dateTime2, textColor);
//				views.setTextColor(R.id.dateCity1, textColor);
//				views.setTextColor(R.id.dateCity2, textColor);
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

//					views.setTextViewText(R.id.dateTime2, df.print(dt2));
//					views.setTextViewText(R.id.dateCity2, ctz2.city);
					
					views.setImageViewBitmap(R.id.dateTime2, buildUpdate(context, df.print(dt2), textColor, width, height, 24, font, (height/2), Align.LEFT));
					views.setImageViewBitmap(R.id.dateCity2, buildUpdate(context, ctz2.city, textColor, width, height, 24, font, (height/2), Align.RIGHT));
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
