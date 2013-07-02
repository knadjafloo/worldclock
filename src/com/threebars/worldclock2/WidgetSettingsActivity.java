package com.threebars.worldclock2;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class WidgetSettingsActivity extends Activity {

	private static final String IDS_ARRAY_STR = "ids";
	private static final String USE24_HOURS = "use24Hours";
	private final static String TAG = "WidgetSettingsActivity";
	public static final int SEARCH_CODE = 1;
	private TextView cityName;
	private TextView city1Name;
	private TextView city2Name;
	private CheckBox use24CheckBox;
	private CityTimeZone cityTimeZone;
	private CityTimeZone city1TimeZone;
	private CityTimeZone city2TimeZone;
	private List<CityTimeZone> cityTimeZones;
	private boolean is_large_widget;
	
	private Button saveButton;
	private Button cancelButton;
	
//	public static final String PREF_PREFIX_KEY = "wc_widget_";
	public static final String PREF_PREFIX_KEY =  "ListOrderPreference";
	
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

		// Find the widget id from the intent.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			Log.w("TTTTTTTTTTTTTTTTTTTTTTTTTTTTT", mAppWidgetId + "");
		}
		
		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}
		
		cityTimeZones = loadCtzsFromSharedPrefs(this, mAppWidgetId);
		cityTimeZone = cityTimeZones != null && cityTimeZones.size() > 0 ? cityTimeZones.get(0) : null;
		
		setResult(RESULT_CANCELED);
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

		AppWidgetProviderInfo appInfo = appWidgetManager.getAppWidgetInfo(mAppWidgetId);
		if (appInfo.initialLayout == R.layout.appwidget_layout) {
			setContentView(R.layout.configure_layout);
			cityName = (TextView) findViewById(R.id.city_name);
			cityName.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent searchIntent = new Intent(WidgetSettingsActivity.this, SearchableActivity.class);
//					searchIntent.putExtra("widget_id", mAppWidgetId);
					startActivityForResult(searchIntent, SEARCH_CODE);
				}
			});
			
			if(cityTimeZone != null)
			{
				cityName.setText(cityTimeZone.city);
			}
			else {
				cityName.setText("Tap to configure the widget");
			}
			
		} else if (appInfo.initialLayout == R.layout.widget_4x2_layout) {
			is_large_widget = true;
			setContentView(R.layout.activity_main_setting);
//			cityTimeZones = loadCtzsFromSharedPrefs(this, mAppWidgetId);
			if (cityTimeZones != null && cityTimeZones.size() >= 2) {
				city1TimeZone = cityTimeZones.get(0);
				city2TimeZone = cityTimeZones.get(1);
			}
			city1Name = (TextView) findViewById(R.id.city_name1);
			city1Name.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent searchIntent = new Intent(WidgetSettingsActivity.this, SearchableActivity.class);
					searchIntent.putExtra("1", true);
					startActivityForResult(searchIntent, SEARCH_CODE);
				}
			});
			
			if(city1TimeZone != null)
			{
				city1Name.setText(city1TimeZone.city);
			}
			else {
				city1Name.setText("Tap to configure the widget");
			}
			
			city2Name = (TextView) findViewById(R.id.city_name2);
			city2Name.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent searchIntent = new Intent(WidgetSettingsActivity.this, SearchableActivity.class);
					searchIntent.putExtra("2", true);
					startActivityForResult(searchIntent, SEARCH_CODE);
				}
			});
			
			if(city2TimeZone != null)
			{
				city2Name.setText(city2TimeZone.city);
			}
			else {
				city2Name.setText("Tap to configure the widget");
			}
		}
		
		use24CheckBox = (CheckBox)findViewById(R.id.use_24hours);
		use24CheckBox.setChecked(loadUse24HoursFromSharedPRefs(this, mAppWidgetId));		
		
		
		
		saveButton = (Button) findViewById(R.id.save_button);
		cancelButton = (Button) findViewById(R.id.cancel_button);
		
		saveButton.setOnClickListener(saveClickListener);
		cancelButton.setOnClickListener(cancelClickListener);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SEARCH_CODE:
				boolean one = data.getExtras().getBoolean("1", false);
				boolean two = data.getExtras().getBoolean("2", false);
				if(one)
				{
					city1TimeZone = new CityTimeZone();
					city1TimeZone.id = data.getStringExtra("city_id");
					city1TimeZone.city = data.getStringExtra("cityName");
					city1TimeZone.timezone = data.getStringExtra("timezone");
					city1TimeZone.timezoneName = data.getStringExtra("timezone_name");
					city1Name.setText(city1TimeZone.city);
				}
				else if(two)
				{
					city2TimeZone = new CityTimeZone();
					city2TimeZone.id = data.getStringExtra("city_id");
					city2TimeZone.city = data.getStringExtra("cityName");
					city2TimeZone.timezone = data.getStringExtra("timezone");
					city2TimeZone.timezoneName = data.getStringExtra("timezone_name");
					city2Name.setText(city2TimeZone.city);
				}
				else
				{
					cityTimeZone = new CityTimeZone();
					cityTimeZone.id = data.getStringExtra("city_id");
					cityTimeZone.city = data.getStringExtra("cityName");
					cityTimeZone.timezone = data.getStringExtra("timezone");
					cityTimeZone.timezoneName = data.getStringExtra("timezone_name");
					cityName.setText(cityTimeZone.city);	
				}
				
//				mAppWidgetId = data.getIntExtra("widget_id", -1);
				break;
			}
		}
	}
	
	// Write the prefix to the SharedPreferences object for this widget
//    public static void saveTitlePref(Context context, int appWidgetId, CityTimeZone ctz, boolean use24Hours) {
//        SharedPreferences.Editor prefs = context.getSharedPreferences(PREF_PREFIX_KEY + appWidgetId, 0).edit();
////        prefs.putInt("widget_id", appWidgetId);
//		prefs.putString("city", ctz.city);
//		prefs.putString("id", ctz.id);
//		prefs.putString("timezoneName", ctz.timezoneName);
//		prefs.putString("timezone", ctz.timezone);
//		prefs.putBoolean("use24hrs", use24Hours);
//        prefs.commit();
//    }
    
	
    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
//    @Deprecated
//    public static CityTimeZone loadCtzFromSharedPrefs(Context context, int appWidgetId) {
//    	CityTimeZone ctz = new CityTimeZone();
////        SharedPreferences prefs = context.getSharedPreferences(PREF_PREFIX_KEY + appWidgetId, 0);
//    	SharedPreferences prefs = context.getSharedPreferences(appWidgetId + PREF_PREFIX_KEY, 0);
//    	ctz.id = prefs.getString("id", null);
//    	ctz.city = prefs.getString("city", null);
//    	ctz.timezone = prefs.getString("timezone", null);
//    	ctz.timezoneName = prefs.getString("timezoneName", null);
//    	
//    	Log.d(TAG, "from preference id : "+ ctz.id);
//    	Log.d(TAG, "from preference city : " + ctz.city);
//        return ctz.id == null ? null : ctz;
//    }
    
    public static boolean loadUse24HoursFromSharedPRefs(Context context, int appWidgetId) {
    	SharedPreferences prefs = context.getSharedPreferences(appWidgetId + PREF_PREFIX_KEY , 0);
    	if(prefs != null) {
    		return prefs.getBoolean(USE24_HOURS, false);
    	}
    	else {
    		return false;
    	}
    	
    }
    
    public static void saveCtzsToSharedPRefs(Context context, int appWidgetId, List<CityTimeZone> ctzs, boolean use24Hours)
    {
    	SharedPreferences prefs = context.getSharedPreferences(appWidgetId + PREF_PREFIX_KEY, 0);
    	String ids = "";
    	for(int i = 0; i < ctzs.size() - 1; i++) {
    		ids = ctzs.get(i).id + ",";
    	}
    	ids += ctzs.get(ctzs.size() - 1).id;
    	prefs.edit().putString(IDS_ARRAY_STR, ids).commit();
    	prefs.edit().putBoolean(USE24_HOURS, use24Hours).commit();
    	
    }
    
    public static List<CityTimeZone> loadCtzsFromSharedPrefs(Context context, int appWidgetId) {
//        SharedPreferences prefs = context.getSharedPreferences(PREF_PREFIX_KEY + appWidgetId, 0);
    	SharedPreferences prefs = context.getSharedPreferences(appWidgetId + PREF_PREFIX_KEY, 0);
    	String ids = prefs.getString(IDS_ARRAY_STR, null);
    	if(ids != null && ids.length() > 0)
    	{
    		CitiesDatabase db = new CitiesDatabase(context);
    		return db.getCitiesById(ids);
    	}
    	return null;
    }
    
    
	private View.OnClickListener saveClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			final Context context = WidgetSettingsActivity.this;

			if (cityTimeZones == null) {
				cityTimeZones = new ArrayList<CityTimeZone>();
			}
			else {
				cityTimeZones.clear();
			}
			if(is_large_widget)
			{
				if(city1TimeZone != null && city2TimeZone !=null)
				{
					if(mAppWidgetId > 0)
					{
						cityTimeZones.add(city1TimeZone);
						cityTimeZones.add(city2TimeZone);
						saveCtzsToSharedPRefs(context, mAppWidgetId, cityTimeZones, use24CheckBox.isChecked());
					}	
				}
				else
				{
					Toast.makeText(context, "Please select a city", Toast.LENGTH_SHORT).show();
					return;
				}
			}
			else if(cityTimeZone != null)
			{
				// When the button is clicked, save the string in our prefs and
				// return that they clicked OK.
				if(mAppWidgetId > 0)
				{
					cityTimeZones.add(cityTimeZone);
					saveCtzsToSharedPRefs(context, mAppWidgetId, cityTimeZones, use24CheckBox.isChecked());
				}
			}
			else {
				Toast.makeText(context, "Please select a city", Toast.LENGTH_SHORT).show();
				return;
			}


			// Push widget update to surface with newly set prefix
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//			MyWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId, PREF_PREFIX_KEY, cityTimeZone, use24CheckBox.isChecked());
			MyWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId, PREF_PREFIX_KEY, cityTimeZones, use24CheckBox.isChecked());
			
			// Make sure we pass back the original appWidgetId
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
		}
	};
	
	private View.OnClickListener cancelClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			setResult(RESULT_CANCELED);
			finish();
		}
	};

}
