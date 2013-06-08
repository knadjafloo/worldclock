package com.threebars.worldclock2;

import java.util.List;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;



public class IconicAdapter extends ArrayAdapter<CityTimeZone> implements Filterable {
	
	private List<CityTimeZone> data;
	private Context context;

	public IconicAdapter(List<CityTimeZone> cities, Context context) {
		super(context, R.layout.row2, cities);
		this.data = cities;
		this.context = context;
	}
	
	
	public List<CityTimeZone> getItems() {
		return this.data;
	}

	public void setData(List<CityTimeZone> array) {
		data = array;
		System.err.println( "#### size of data : " + data.size());
	}
	
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
//			LayoutInflater inflater = getLayoutInflater();
			LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

			row = inflater.inflate(R.layout.row2, parent, false);
		}

		TextView label = (TextView) row.findViewById(R.id.label);
		CityTimeZone cityTimeZone = data.get(position);
		label.setText(cityTimeZone.city);
		
		CustomDigitalClock clock = (CustomDigitalClock) row.findViewById(R.id.clock);
		
//		clock.setTimeZone(cityTimeZone.getTimezone());
		clock.setTimeZone(TimeUtil.getDSTTimeZone(cityTimeZone));
		
		TextView country  = (TextView) row.findViewById(R.id.country);
		country.setText(cityTimeZone.country);
		
		TextView gmt = (TextView) row.findViewById(R.id.gmt);
		gmt.setText(cityTimeZone.timezone/* + " " + cityTimeZone.timezoneName*/);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean showSeconds = sharedPrefs.getBoolean(context.getString(R.string.show_second_ticker), true);
		clock.setShowSeconds(showSeconds);
//		label.setText(array.get(position));

		return (row);
	}
	
	
}