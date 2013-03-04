package com.threebars.worldclock2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class WidgetSettingsActivity extends Activity {

	public static final int SEARCH_CODE = 1;
	private TextView cityName;

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
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SEARCH_CODE:
				String city = data.getExtras().getString("cityName");
				int id = data.getExtras().getInt("city_id");
				cityName.setText(city);
				break;
			}
		}
	}

}
