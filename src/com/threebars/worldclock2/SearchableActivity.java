package com.threebars.worldclock2;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SearchableActivity extends ListActivity {

	private EditText filterText = null;
	private ArrayAdapter<CityTimeZone> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.filterable_listview);

		CitiesDatabase db = new CitiesDatabase(this);
		List<CityTimeZone> cities = db.getAllCities();

		filterText = (EditText) findViewById(R.building_list.search_box);
		filterText.addTextChangedListener(filterTextWatcher);

		adapter = new ArrayAdapter<CityTimeZone>(this, android.R.layout.simple_list_item_1, cities);
		setListAdapter(adapter);

		ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CityTimeZone item = adapter.getItem(position);
				Intent i = getIntent();
				i.putExtra("city_id", item.id);
				i.putExtra("cityName", item.getCity());
				i.putExtra("timezone", item.getTimezone());
				i.putExtra("timezone_name", item.getTimezoneName());
				
				setResult(RESULT_OK, i);
				finish();
			}
		});

	}

	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			adapter.getFilter().filter(s);
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		filterText.removeTextChangedListener(filterTextWatcher);
	}

}
