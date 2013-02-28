/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.threebars.worldclock2;

import static com.threebars.worldclock2.CitiesDatabase.COLUMN_NAMES.COL_LATITUDE;
import static com.threebars.worldclock2.CitiesDatabase.COLUMN_NAMES.COL_LONGITUDE;
import static com.threebars.worldclock2.CitiesDatabase.COLUMN_NAMES.COL_TIMEZONE;
import static com.threebars.worldclock2.CitiesDatabase.COLUMN_NAMES.COL_TIMEZONE_NAME;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;


/**
 * Contains logic to return specific words from the dictionary, and
 * load the dictionary table when it needs to be created.
 */
public class CitiesDatabase {
    private static final String TAG = "DictionaryDatabase";

    public static final String TABLE_CITIES_FTS = "cities_fts";
    
    public static enum COLUMN_NAMES {

		COL_ID("_id", 0),
		COL_KEY_CITY( "CITY", 1),
		COL_KEY_COUNTRY("COUNTRY", 2),
		COL_TIMEZONE("TIMEZONE", 3),
		COL_TIMEZONE_NAME("TIMEZONE_NAME", 4),
		COL_LATITUDE("LATITUDE", 5),
		COL_LONGITUDE("LONGITUDE", 6);
//		COL_CITY_PREF("_pref_name", 7);
		
		private String columnName;
		private int columnIndex;

		COLUMN_NAMES(String columnName, int columnIndex) {
			this.columnName = columnName;
			this.columnIndex = columnIndex;
		}
		
		public int getIndex() {
			return this.columnIndex;
		}
		
		public String getName() {
			return this.columnName;
		}
		
		public String toString() {
			return this.columnName;
		}

	};
	
	private static final String DATABASE_NAME = "cities";
    private static final int DATABASE_VERSION = 2;

	
	
    //The columns we'll include in the dictionary table
    public static final String KEY_CITY = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String KEY_COUNTRY = SearchManager.SUGGEST_COLUMN_TEXT_2;


    private final DatabaseOpenHelper mDatabaseOpenHelper;

	private Context context;
    private static final HashMap<String,String> mColumnMap = buildColumnMap();

    /**
     * Constructor
     * @param context The Context within which to work, used to create the DB
     */
    public CitiesDatabase(Context context) {
    	mDatabaseOpenHelper = new DatabaseOpenHelper(context);
    	this.context = context;
    }

    public void close() {
    	if(mDatabaseOpenHelper != null ) {
    		mDatabaseOpenHelper.close();
    	}
    }
    
    public void populateDb() {
    	new LoadCitiesTask().execute();
    }
    /**
     * Builds a map for all columns that may be requested, which will be given to the 
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include 
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(KEY_CITY, KEY_CITY);
        map.put(KEY_COUNTRY, KEY_COUNTRY);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }

    /**
     * Returns a Cursor positioned at the word specified by rowId
     *
     * @param rowId id of word to retrieve
     * @param columns The columns to include, if null then all are included
     * @return Cursor positioned to matching word, or null if not found.
     */
    public Cursor getCity(String rowId, String[] columns) {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] {rowId};

        return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
         */
    }
    
    public CityTimeZone getCity(String rowId) {
    	SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
    	
        Cursor cursor = db.rawQuery("select rowid, * from " + TABLE_CITIES_FTS + " where rowid = ?", new String[] {rowId});
        cursor.moveToFirst();
        if(cursor.getCount() > 0) {
        	CityTimeZone ctz = cursorToCityTimeZone(cursor);
        	cursor.close();
        	return ctz;
        }
        cursor.close();
        return null;
    }
    
    public List<CityTimeZone> getDefaultCities()
    {
    	List<CityTimeZone> cities = new ArrayList<CityTimeZone>();
    	SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(" select rowid, * from " + TABLE_CITIES_FTS + " where " + KEY_CITY + " like '%Vancouver%' OR " + KEY_CITY + " like '%Tokyo%' OR " + KEY_CITY + " like '%New York%'", null);
		cursor.moveToFirst();
		
		while (!cursor.isAfterLast()) {
			CityTimeZone comment = cursorToCityTimeZone(cursor);
			Log.d(TAG, " got default city : " + comment);
			cities.add(comment);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return cities;
    }
    
    public List<CityTimeZone> getCitiesById(String ids) {
    	List<CityTimeZone> cities = new ArrayList<CityTimeZone>();
    	SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_CITIES_FTS,	//db name 
		new String[] {"rowid",KEY_CITY, KEY_COUNTRY, COL_TIMEZONE.getName(), COL_TIMEZONE_NAME.getName(), COL_LATITUDE.getName(), COL_LONGITUDE.getName()},	//columns
		"rowid IN ( " + ids + " ) ",	//selection,
		null,		 //selectionArgs, 
		null,//groupBy, 
		null,//having, 
		null);//orderBy)		
		
		cursor.moveToFirst();
		
		while (!cursor.isAfterLast()) {
			CityTimeZone comment = cursorToCityTimeZone(cursor);
			cities.add(comment);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		
		String[] idsArr = ids.split(",");
		final Map<String, Integer> orderMap = new HashMap<String, Integer>();
		int count = 0;
		for(String id : idsArr) {
			orderMap.put(id, count++);
		}
		//order cities
		Collections.sort(cities, new Comparator<CityTimeZone>() {

			@Override
			public int compare(CityTimeZone lhs, CityTimeZone rhs) {
				return orderMap.get(lhs.getId()).compareTo(orderMap.get(rhs.getId()));
			}
			
		});
		
		Log.d("DAO", "# of cities : " + cities.size());
		return cities;

    }
    
	private CityTimeZone cursorToCityTimeZone(Cursor cursor) {
		CityTimeZone ctz = new CityTimeZone();
		
		ctz.setId(cursor.getString(0));
		ctz.setCity(cursor.getString(1));
		ctz.setCountry(cursor.getString(2));
		ctz.setTimezone(cursor.getString(3));
		ctz.setTimezoneName(cursor.getString(4));
		ctz.setLatitude(cursor.getDouble(5));
		ctz.setLongitude(cursor.getDouble(6));
		
		return ctz;
	}

    /**
     * Returns a Cursor over all words that match the given query
     *
     * @param query The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all words that match, or null if none found.
     */
    public Cursor getCityMatches(String query, String[] columns) {
        String selection = TABLE_CITIES_FTS + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE <KEY_WORD> MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the word column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.
         *   These aliases are defined in the DictionaryProvider when queries are made.
         * - This can be revised to also search the definition text with FTS3 by changing
         *   the selection clause to use FTS_VIRTUAL_TABLE instead of KEY_WORD (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
    }

    /**
     * Performs a database query.
     * @param selection The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns The columns to return
     * @return A Cursor over all rows matching the query
     */
    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_CITIES_FTS);
        builder.setProjectionMap(mColumnMap);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }
    
    private static ProgressDialog progressBar;
    
    /**
     * This creates/opens the database.
     */
    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        /* Note that FTS3 does not support column constraints and thus, you cannot
         * declare a primary key. However, "rowid" is automatically used as a unique
         * identifier, so when making requests, we will use "_id" as an alias for "rowid"
         */
        
    	private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE " + TABLE_CITIES_FTS + " USING fts3(" + KEY_CITY + ", " + KEY_COUNTRY + ", "
                + COL_TIMEZONE + ", "
                + COL_TIMEZONE_NAME + ", "
                + COL_LATITUDE + ", "
                + COL_LONGITUDE + ");";

        DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);
            
            	Log.d(TAG, "LOADING CITIES FROM FILE ");
            	try {
					loadWords();
				} catch (IOException e) {
					e.printStackTrace();
				}
            	
        }
        
        /**
         * Starts a thread to load the database table with words
         */
        private void loadDictionary() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadWords();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        private void loadWords() throws IOException {
            Log.d(TAG, "Loading words...");
            final Resources resources = mHelperContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.cities);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            mDatabase.beginTransaction();
            try {
                String line = null;
    			while ((line = reader.readLine()) != null) {

    				CityTimeZone cityTimeZone = new CityTimeZone();
    				
    				String otherThanQuote = " [^\"] ";
    		        String quotedString = String.format(" \" %s* \" ", otherThanQuote);
    		        String regex = String.format("(?x) "+ // enable comments, ignore white spaces
    		                ",                         "+ // match a comma
    		                "(?=                       "+ // start positive look ahead
    		                "  (                       "+ //   start group 1
    		                "    %s*                   "+ //     match 'otherThanQuote' zero or more times
    		                "    %s                    "+ //     match 'quotedString'
    		                "  )*                      "+ //   end group 1 and repeat it zero or more times
    		                "  %s*                     "+ //   match 'otherThanQuote'
    		                "  $                       "+ // match the end of the string
    		                ")                         ", // stop positive look ahead
    		                otherThanQuote, quotedString, otherThanQuote);

    		        String[] tokens = line.split(regex);

    				
    				cityTimeZone.city = tokens[0].replaceAll("\"", "");
    				cityTimeZone.country = tokens[2].replaceAll("\"", "");
    				int startBracketIndex = tokens[1].indexOf("(") + 1;
    				int closeBracketIndex = tokens[1].indexOf(")");
    				cityTimeZone.timezone = tokens[1].substring(startBracketIndex, closeBracketIndex).replaceAll("\"", "");
    				cityTimeZone.latitude = Double.parseDouble(tokens[3]);
    				cityTimeZone.longitude = Double.parseDouble(tokens[4]);
    				cityTimeZone.timezoneName = tokens[5].replaceAll("\"", "");
    				
    				long id = addCityTimeZone(cityTimeZone);
//    					cityTimeZoneList .add(cityTimeZone);
//    				System.out.println(cityTimeZone.toString());

    			}
    			mDatabase.setTransactionSuccessful();
            } finally {
                reader.close();
                mDatabase.endTransaction();
            }
            Log.d(TAG, "DONE loading words.");
            
            
        }

        /**
         * Add a word to the dictionary.
         * @return rowId or -1 if failed
         */
        public long addCityTimeZone(CityTimeZone ctz) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_CITY, ctz.city);
            initialValues.put(KEY_COUNTRY, ctz.country);
            initialValues.put(COLUMN_NAMES.COL_LATITUDE.getName(), ctz.latitude);
            initialValues.put(COLUMN_NAMES.COL_LONGITUDE.getName(),ctz.longitude);
            initialValues.put(COLUMN_NAMES.COL_TIMEZONE.getName(), ctz.timezone);
            initialValues.put(COLUMN_NAMES.COL_TIMEZONE_NAME.getName(), ctz.timezoneName);
            
            return mDatabase.insert(TABLE_CITIES_FTS, null, initialValues);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITIES_FTS);
            onCreate(db);
        }
    }
    
    public class LoadCitiesTask extends AsyncTask<Void, Integer,Void> {
        
    	private int maxLineNumber = 100;
		
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		try {
	    		final Resources resources = context.getResources();
				InputStream inputStream = resources.openRawResource(R.raw.cities);
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				
	    		LineNumberReader  lnr = new LineNumberReader(reader);
				lnr.skip(Long.MAX_VALUE);
				maxLineNumber = lnr.getLineNumber();
	
				progressBar = new ProgressDialog(context);
	        	progressBar.setMessage("Loading Cities for first time...");
	        	progressBar.setCancelable(false);
	        	progressBar.setIndeterminate(false);
	        	progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				
				progressBar.setMax(maxLineNumber);
	    		progressBar.show();
    		
				reader.close();
				inputStream.close();
			} catch (IOException e) {
			}
    	}
		@Override
		protected Void doInBackground(Void... params) {
			Log.d(TAG, "Loading cities...");
			final Resources resources = context.getResources();
			InputStream inputStream = resources.openRawResource(R.raw.cities);
			
			
			
			SQLiteDatabase mDatabase = mDatabaseOpenHelper.getWritableDatabase();

			long start = System.currentTimeMillis();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			try {
				
				mDatabase.beginTransaction();

	            String line = null;
	            int count = 0;
				try {
					while ((line = reader.readLine()) != null) {
						
						CityTimeZone cityTimeZone = new CityTimeZone();
						
						String otherThanQuote = " [^\"] ";
					    String quotedString = String.format(" \" %s* \" ", otherThanQuote);
					    String regex = String.format("(?x) "+ // enable comments, ignore white spaces
					            ",                         "+ // match a comma
					            "(?=                       "+ // start positive look ahead
					            "  (                       "+ //   start group 1
					            "    %s*                   "+ //     match 'otherThanQuote' zero or more times
					            "    %s                    "+ //     match 'quotedString'
					            "  )*                      "+ //   end group 1 and repeat it zero or more times
					            "  %s*                     "+ //   match 'otherThanQuote'
					            "  $                       "+ // match the end of the string
					            ")                         ", // stop positive look ahead
					            otherThanQuote, quotedString, otherThanQuote);

						String[] tokens = line.split(regex);
	
						cityTimeZone.city = tokens[0];
						cityTimeZone.country = tokens[2];
						cityTimeZone.timezone = tokens[1].substring(2, 11);
						cityTimeZone.latitude = Double.parseDouble(tokens[3]);
						cityTimeZone.longitude = Double.parseDouble(tokens[4]);
						cityTimeZone.timezoneName = tokens[5];
						// cityTimeZone.preferredName = "";
	
						long id = mDatabaseOpenHelper.addCityTimeZone(cityTimeZone);
						if (id < 0) {
							Log.e(TAG, "unable to add city: " + cityTimeZone);
						}
						count++;
						publishProgress((int) ((count / (float) maxLineNumber) * 100));
					}
				} catch (NumberFormatException e) {
					Log.d(TAG, "error adding " + e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					Log.d(TAG, "error adding2 " + e.getMessage());
					e.printStackTrace();
				}

				mDatabase.setTransactionSuccessful();
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
				}
				mDatabase.endTransaction();
				mDatabase.close();
			}
			
			
			long end = System.currentTimeMillis();

			Log.d(TAG, "TIME IT TOOK : " + (end - start) / 1000d + " seconds...");
			Log.d(TAG, "DONE loading words.");
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			progressBar.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			progressBar.dismiss();
			
			SharedPreferences prefs = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
			SharedPreferences.Editor ed = prefs.edit();
        	ed.putBoolean("hasLoadedCities", true);
        	ed.commit();  //Commiting changes
		}
	     
    	
    }
}
