package com.threebars.worldclock2;


import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

public class SearchableActivity extends ListActivity {

	private ArrayAdapter<CityTimeZone> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		adapter = new ArrayAdapter<CityTimeZone>(this,
		        android.R.layout.simple_list_item_1);
		setListAdapter(adapter);
//		setContentView(R.layout.main_list);

	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      Log.d("SEARCH", "***********query is :  " + query);
//	      searchCities(query);
//	      new LoadCitiesTask().execute(query);
	    }
	    
		
	}
}
