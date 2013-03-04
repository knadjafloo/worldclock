package com.threebars.worldclock2;

import java.util.List;

import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
//import com.example.android.apis.R;

/**
 * The configuration screen for the ExampleAppWidgetProvider widget sample.
 */
public class ExampleAppWidgetConfigure extends ListActivity {
    static final String TAG = "ExampleAppWidgetConfigure";

    private static final String PREFS_NAME
            = "com.threebars.worldclock2.ExampleAppWidgetConfigure";
    private static final String PREF_PREFIX_KEY = "prefix_";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    EditText mAppWidgetPrefix;

    public ExampleAppWidgetConfigure() {
        super();
    }

    private EditText filterText = null;
    ArrayAdapter<CityTimeZone> adapter = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.filterable_listview);

		CitiesDatabase db = new CitiesDatabase(this);
		List<CityTimeZone> cities = db.getAllCities();
		
		
		filterText = (EditText) findViewById(R.building_list.search_box);
		filterText.addTextChangedListener(filterTextWatcher);

		adapter = new ArrayAdapter<CityTimeZone>(this,
                android.R.layout.simple_list_item_1, 
                cities);
        setListAdapter(adapter);
		
	}
	

	private TextWatcher filterTextWatcher = new TextWatcher() {

	    public void afterTextChanged(Editable s) {
	    }

	    public void beforeTextChanged(CharSequence s, int start, int count,
	            int after) {
	    }

	    public void onTextChanged(CharSequence s, int start, int before,
	            int count) {
	        adapter.getFilter().filter(s);
	    }

	};

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    filterText.removeTextChangedListener(filterTextWatcher);
	}

//    @Override
//    public void onCreate(Bundle icicle) {
//        super.onCreate(icicle);
//
//        // Set the result to CANCELED.  This will cause the widget host to cancel
//        // out of the widget placement if they press the back button.
//        setResult(RESULT_CANCELED);
//
//        // Set the view layout resource to use.
//        setContentView(R.layout.appwidget_configure);
//
//        // Find the EditText
//        mAppWidgetPrefix = (EditText)findViewById(R.id.appwidget_prefix);
//
//        // Bind the action for the save button.
//        findViewById(R.id.save_button).setOnClickListener(mOnClickListener);
//
//        // Find the widget id from the intent. 
//        Intent intent = getIntent();
//        Bundle extras = intent.getExtras();
//        if (extras != null) {
//            mAppWidgetId = extras.getInt(
//                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
//        }
//
//        // If they gave us an intent without the widget id, just bail.
//        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
//            finish();
//        }
//
//        mAppWidgetPrefix.setText(loadTitlePref(ExampleAppWidgetConfigure.this, mAppWidgetId));
//    }
//
//    View.OnClickListener mOnClickListener = new View.OnClickListener() {
//        public void onClick(View v) {
//            final Context context = ExampleAppWidgetConfigure.this;
//
//            // When the button is clicked, save the string in our prefs and return that they
//            // clicked OK.
//            String titlePrefix = mAppWidgetPrefix.getText().toString();
//            saveTitlePref(context, mAppWidgetId, titlePrefix);
//
//            // Push widget update to surface with newly set prefix
//            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//            ExampleAppWidgetProvider.updateAppWidget(context, appWidgetManager,
//                    mAppWidgetId, titlePrefix);
//
//            // Make sure we pass back the original appWidgetId
//            Intent resultValue = new Intent();
//            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
//            setResult(RESULT_OK, resultValue);
//            finish();
//        }
//    };
//
//    // Write the prefix to the SharedPreferences object for this widget
//    static void saveTitlePref(Context context, int appWidgetId, String text) {
//        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
//        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
//        prefs.commit();
//    }
//
//    // Read the prefix from the SharedPreferences object for this widget.
//    // If there is no preference saved, get the default from a resource
//    static String loadTitlePref(Context context, int appWidgetId) {
//        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
//        String prefix = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
//        if (prefix != null) {
//            return prefix;
//        } else {
//            return context.getString(R.string.appwidget_prefix_default);
//        }
//    }
//
//    static void deleteTitlePref(Context context, int appWidgetId) {
//    }
//
//    static void loadAllTitlePrefs(Context context, ArrayList<Integer> appWidgetIds,
//            ArrayList<String> texts) {
//    }
}



