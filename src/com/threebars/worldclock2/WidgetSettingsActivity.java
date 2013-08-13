package com.threebars.worldclock2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.margaritov.preference.colorpicker.AlphaPatternDrawable;
import net.margaritov.preference.colorpicker.ColorPickerDialog;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WidgetSettingsActivity extends Activity implements ColorPickerDialog.OnColorChangedListener {

	private static final String IDS_ARRAY_STR = "ids";
	private static final String USE24_HOURS = "use24Hours";
	private static final String BACKGROUND_COLOR = "background";
	private static final String TEXT_COLOR = "textColor";
	private static final String FONT = "font";
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
	
	private ImageView previewImgView;
	private int mValue = R.integer.COLOR_BLACK;
	private ImageView previewTextColorView;
	private TextView fontView;
	private int mTextColor = Color.LTGRAY;
	private float mDensity = 0;
	private FontItem mFontItem = null;
	private boolean mAlphaSliderEnabled = false;
	private boolean mHexValueEnabled = false;
	ColorPickerDialog mDialog;
	ColorPickerDialog mDialog2;
	Dialog mFontDialog;
	
	private Button saveButton;
	private Button cancelButton;
	
	private ListView mFontListView;
	private ArrayList<FontItem> fontsList;
	
//	public static final String PREF_PREFIX_KEY = "wc_widget_";
	public static final String PREF_PREFIX_KEY =  "ListOrderPreference";
	
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

		mDensity = this.getResources().getDisplayMetrics().density;
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
		
		mValue = loadWidgetBackgroundColor(this, mAppWidgetId);
		mTextColor = loadWidgetTextColor(this, mAppWidgetId);
		mFontItem = loadWidgetTextFont(this, mAppWidgetId);
		
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
		
		fontView = (TextView)findViewById(R.id.fontType);
		fontView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showFontPopUp();
			}
		});
		
		
		previewImgView = (ImageView)findViewById(R.id.previewImg);
		previewImgView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
					mDialog = new ColorPickerDialog(WidgetSettingsActivity.this, R.integer.COLOR_BLACK);
					mDialog.setType(BACKGROUND_COLOR);
					mDialog.setOnColorChangedListener(WidgetSettingsActivity.this);
					if (mAlphaSliderEnabled) {
						mDialog.setAlphaSliderVisible(true);
					}
					if (mHexValueEnabled) {
						mDialog.setHexValueEnabled(true);
					}
					mDialog.show();
									
			}
		});
		
		if (mValue != R.integer.COLOR_BLACK) {
			setPreviewColor(BACKGROUND_COLOR);
		}
		previewTextColorView = (ImageView)findViewById(R.id.previewImgText);
		previewTextColorView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
					mDialog2 = new ColorPickerDialog(WidgetSettingsActivity.this, Color.LTGRAY);
					mDialog2.setType(TEXT_COLOR);
					mDialog2.setOnColorChangedListener(WidgetSettingsActivity.this);
					if (mAlphaSliderEnabled) {
						mDialog2.setAlphaSliderVisible(true);
					}
					if (mHexValueEnabled) {
						mDialog2.setHexValueEnabled(true);
					}
					mDialog2.show();
									
			}
		});
		
		
			setPreviewColor(TEXT_COLOR);
		
		use24CheckBox = (CheckBox)findViewById(R.id.use_24hours);
		use24CheckBox.setChecked(loadUse24HoursFromSharedPRefs(this, mAppWidgetId));		
		
		if(mFontItem != null) {
			setFontItem(mFontItem);
		}
		
		
		saveButton = (Button) findViewById(R.id.save_button);
		cancelButton = (Button) findViewById(R.id.cancel_button);
		
		saveButton.setOnClickListener(saveClickListener);
		cancelButton.setOnClickListener(cancelClickListener);
	}
	
	private void showFontPopUp() {
		fontsList = new ArrayList<FontItem>();

		AssetManager assetManager = getAssets();
		// To get names of all files inside the "Fonts" folder
		try {
			String[] files = assetManager.list("fonts");
			String timeFormat = "hh:mm a";

			DateTimeFormatter df = DateTimeFormat.forPattern(timeFormat);
			String currentTime = "Sample " + df.print(new DateTime());
			for (int i = 0; i < files.length; i++) {
				FontItem item = new FontItem();
				item.fileName = files[i];
				item.displayValue = currentTime;
				fontsList.add(item);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (mFontDialog == null) {
			mFontDialog = new Dialog(this);
			mFontDialog.setContentView(R.layout.font_list_layout);
			final ArrayAdapter<FontItem> adapter = new FontArrayAdapter(this, fontsList);
			mFontListView = (ListView) mFontDialog.findViewById(R.id.font_list);
			mFontListView.setAdapter(adapter);
			mFontListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					mFontItem = adapter.getItem(position);
					setFontItem(mFontItem);
					mFontDialog.dismiss();
				}

			});
		}
		mFontDialog.setCancelable(true);
		mFontDialog.setTitle("Fonts");
		mFontDialog.show();

	}
	
	private void setFontItem(FontItem mFontItem) {
		fontView.setText(mFontItem.displayValue);
//		Typeface face=Typeface.createFromAsset(getAssets(), "fonts/" + mFontItem.fileName);
		Typeface face = Typefaces.get(this, "fonts/" + mFontItem.fileName);
		fontView.setTypeface(face);
	}
	
	public static class FontItem {
		
		public FontItem() {
			
		}
		
		String fileName;
		String displayValue;
	}
	
	private class FontArrayAdapter extends ArrayAdapter<FontItem> {
		  private final Context context;
		  private final List<FontItem> values;

		  public FontArrayAdapter(Context context, List<FontItem> values) {
		    super(context, R.layout.font_list_layout, values);
		    this.context = context;
		    this.values = values;
		  }

		  @Override
		  public View getView(int position, View convertView, ViewGroup parent) {
		    LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    View rowView = inflater.inflate(R.layout.font_list_item, parent, false);
		    TextView textView = (TextView) rowView.findViewById(R.id.fontItem);
		    FontItem font = values.get(position);
		    textView.setText(font.displayValue);
		    
//		    Typeface face=Typeface.createFromAsset(getAssets(), "fonts/" + font.fileName);
		    Typeface face = Typefaces.get(context, "fonts/" + font.fileName);
		    textView.setTypeface(face); 
		    return rowView;
		  }
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
    
    public static void saveCtzsToSharedPRefs(Context context, int appWidgetId, List<CityTimeZone> ctzs, boolean use24Hours, int color, int textColor, FontItem fontItem)
    {
    	SharedPreferences prefs = context.getSharedPreferences(appWidgetId + PREF_PREFIX_KEY, 0);
    	String ids = "";
    	for(int i = 0; i < ctzs.size() - 1; i++) {
    		ids = ctzs.get(i).id + ",";
    	}
    	ids += ctzs.get(ctzs.size() - 1).id;
    	prefs.edit().putString(IDS_ARRAY_STR, ids).commit();
    	prefs.edit().putBoolean(USE24_HOURS, use24Hours).commit();
    	prefs.edit().putInt(BACKGROUND_COLOR, color).commit();
    	prefs.edit().putInt(TEXT_COLOR, textColor).commit();
    	if(fontItem != null) {
    		prefs.edit().putString(FONT, fontItem.fileName).commit();
    	}
    	
    }
    
    public static List<CityTimeZone> loadCtzsFromSharedPrefs(Context context, int appWidgetId) {
//        SharedPreferences prefs = context.getSharedPreferences(PREF_PREFIX_KEY + appWidgetId, 0);
    	SharedPreferences prefs = context.getSharedPreferences(appWidgetId + PREF_PREFIX_KEY, 0);
    	String ids = prefs.getString(IDS_ARRAY_STR, null);
    	if(ids != null && ids.length() > 0)
    	{
    		CitiesDatabase db = new CitiesDatabase(context);
    		List<CityTimeZone> cities = db.getCitiesById(ids);
    		db.close();
    		return cities;
    	}
    	return null;
    }
    
    public static int loadWidgetBackgroundColor(Context context, int appWidgetId) {
    	SharedPreferences prefs = context.getSharedPreferences(appWidgetId + PREF_PREFIX_KEY, 0);
    	return prefs.getInt(BACKGROUND_COLOR, R.integer.COLOR_BLACK);
    }
    
    public static int loadWidgetTextColor(Context context, int appWidgetId) {
    	SharedPreferences prefs = context.getSharedPreferences(appWidgetId + PREF_PREFIX_KEY, 0);
    	return prefs.getInt(TEXT_COLOR, Color.LTGRAY);
    }
    
    public static FontItem loadWidgetTextFont(Context context, int appWidgetId) {
    	SharedPreferences prefs = context.getSharedPreferences(appWidgetId + PREF_PREFIX_KEY, 0);
    	
    	String fontFile =  prefs.getString(FONT, null);
    	if(fontFile == null) {
    		return null;
    	}
    	else {
    		FontItem item = new FontItem();
    		item.fileName = fontFile;
    		item.displayValue = "Sample";
    		return item;
    	}
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
						saveCtzsToSharedPRefs(context, mAppWidgetId, cityTimeZones, use24CheckBox.isChecked(), mValue, mTextColor, mFontItem);
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
					saveCtzsToSharedPRefs(context, mAppWidgetId, cityTimeZones, use24CheckBox.isChecked(), mValue, mTextColor, mFontItem);
				}
			}
			else {
				Toast.makeText(context, "Please select a city", Toast.LENGTH_SHORT).show();
				return;
			}


			// Push widget update to surface with newly set prefix
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//			MyWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId, PREF_PREFIX_KEY, cityTimeZone, use24CheckBox.isChecked());
			MyWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId, PREF_PREFIX_KEY, cityTimeZones/*, use24CheckBox.isChecked(), mValue*/);
			
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
	
	

	@Override
	public void onColorChanged(int color, String type) {
		// TODO Auto-generated method stub
		if(TEXT_COLOR.equals(type)) {
			mTextColor = color;
			setPreviewColor(TEXT_COLOR);
		}
		else if( BACKGROUND_COLOR.equals(type)) {
			mValue = color;
			setPreviewColor(BACKGROUND_COLOR);	
		}
		
	}
	
	private void setPreviewColor(String type) {
		
		if (BACKGROUND_COLOR.equals(type)) {
			previewImgView.setBackgroundDrawable(new AlphaPatternDrawable((int) (5 * mDensity)));
			previewImgView.setImageBitmap(getPreviewBitmap(mValue));
		}

		else if (TEXT_COLOR.equals(type)) {
			previewTextColorView.setBackgroundDrawable(new AlphaPatternDrawable((int) (5 * mDensity)));
			previewTextColorView.setImageBitmap(getPreviewBitmap(mTextColor));
		}
	}
	
	
	private Bitmap getPreviewBitmap(int color) {
		int d = (int) (mDensity * 31); //30dip
		Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
		int w = bm.getWidth();
		int h = bm.getHeight();
		int c = color;
		for (int i = 0; i < w; i++) {
			for (int j = i; j < h; j++) {
				c = (i <= 1 || j <= 1 || i >= w-2 || j >= h-2) ? Color.GRAY : color;
				bm.setPixel(i, j, c);
				if (i != j) {
					bm.setPixel(j, i, c);
				}
			}
		}

		return bm;
	}
	

    protected void showDialog(Bundle state) {
		mDialog = new ColorPickerDialog(this, mValue);
		mDialog.setOnColorChangedListener(this);
		if (mAlphaSliderEnabled) {
			mDialog.setAlphaSliderVisible(true);
		}
		if (mHexValueEnabled) {
			mDialog.setHexValueEnabled(true);
		}
		if (state != null) {
			mDialog.onRestoreInstanceState(state);
		}
		mDialog.show();
	}
	
	
}
