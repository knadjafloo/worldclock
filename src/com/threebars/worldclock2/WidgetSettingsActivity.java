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

		cityTimeZone = new CityTimeZone();
		
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
		else
		{
			Log.w("TTTTTTTTTTTTTTTTTTTTTTTTTTTTT", " it's null");
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
//			finish();
			Log.e("WidgetSettings", "have incorrect app widget id");
		}

		
		saveButton = (Button) findViewById(R.id.save_button);
		cancelButton = (Button) findViewById(R.id.cancel_button);
		
		saveButton.setOnClickListener(saveClickListener);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SEARCH_CODE:
				
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
    private void saveTitlePref(Context context, int appWidgetId, CityTimeZone ctz) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREF_PREFIX_KEY + mAppWidgetId, 0).edit();
		prefs.putString("city", ctz.city);
		prefs.putString("id", ctz.id);
        prefs.commit();
    }
	
	private View.OnClickListener saveClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			final Context context = WidgetSettingsActivity.this;

			
			// When the button is clicked, save the string in our prefs and
			// return that they clicked OK.
			saveTitlePref(context, mAppWidgetId, cityTimeZone);

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

}
