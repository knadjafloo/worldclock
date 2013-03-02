package com.threebars.worldclock2;


import java.util.List;

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
		
		setContentView(R.layout.filterable_listview);

		CitiesDatabase db = new CitiesDatabase(this);
		List<CityTimeZone> cities = db.getAllCities();
        setListAdapter(new ArrayAdapter<CityTimeZone>(this,
                       android.R.layout.simple_list_item_1, 
                       cities));
		
	}
}
