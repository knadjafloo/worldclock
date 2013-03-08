package com.threebars.worldclock2;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class WidgetSettingsActivity extends Activity {

	private final static String TAG = "WidgetSettingsActivity";
	public static final int SEARCH_CODE = 1;
	private TextView cityName;
	private CityTimeZone cityTimeZone;
	
	private Button saveButton;
	private Button cancelButton;
	
	public static final String PREF_PREFIX_KEY = "wc_widget_";
	
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configure_layout);

		cityName = (TextView) findViewById(R.id.city_name);
		
		cityName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent searchIntent = new Intent(WidgetSettingsActivity.this, SearchableActivity.class);
				startActivityForResult(searchIntent, SEARCH_CODE);
			}
		});

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

		
		cityTimeZone = loadCtzFromSharedPrefs(this, mAppWidgetId );	
			
		if(cityTimeZone != null)
		{
			cityName.setText(cityTimeZone.city);
		}
		else {
			cityName.setText("Tap to configure the widget");
		}
		
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
				cityTimeZone = new CityTimeZone();
				cityTimeZone.id = data.getStringExtra("city_id");
				cityTimeZone.city = data.getStringExtra("cityName");
				cityTimeZone.timezone = data.getStringExtra("timezone");
				cityTimeZone.timezoneName = data.getStringExtra("timezone_name");
				cityName.setText(cityTimeZone.city);
				break;
			}
		}
	}
	
	// Write the prefix to the SharedPreferences object for this widget
    public static void saveTitlePref(Context context, int appWidgetId, CityTimeZone ctz) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREF_PREFIX_KEY + appWidgetId, 0).edit();
        prefs.putInt("widget_id", appWidgetId);
		prefs.putString("city", ctz.city);
		prefs.putString("id", ctz.id);
        prefs.commit();
    }
	
    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    public static CityTimeZone loadCtzFromSharedPrefs(Context context, int appWidgetId) {
    	CityTimeZone ctz = new CityTimeZone();
        SharedPreferences prefs = context.getSharedPreferences(PREF_PREFIX_KEY + appWidgetId, 0);
    	ctz.id = prefs.getString("id", null);
    	ctz.city = prefs.getString("city", null);
    	
    	Log.d(TAG, "from preference id : "+ ctz.id);
    	Log.d(TAG, "from preference city : " + ctz.city);
        return ctz.id == null ? null : ctz;
    }
	private View.OnClickListener saveClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			final Context context = WidgetSettingsActivity.this;

			
			if(cityTimeZone != null)
			{
				// When the button is clicked, save the string in our prefs and
				// return that they clicked OK.
				saveTitlePref(context, mAppWidgetId, cityTimeZone);
			}

			// Push widget update to surface with newly set prefix
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			MyWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId, PREF_PREFIX_KEY, cityTimeZone);

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
			finish();
		}
	};

}
