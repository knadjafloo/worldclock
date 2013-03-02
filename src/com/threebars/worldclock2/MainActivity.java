package com.threebars.worldclock2;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

public class MainActivity extends ListActivity {

	private IconicAdapter adapter = null;
	private CitiesDatabase db;
	private static final String TAG = "WorldClockWidgetActivity";

	public int dragStartMode = DragSortController.ON_DOWN;
	public int removeMode = DragSortController.FLING_REMOVE;
	private DragSortController mController;
	
	private static final int RESULT_SETTINGS = 1;
	
	// Declaring SearchView as an instance object
    private SearchView searchView;

    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    if (from != to) {
                        CityTimeZone item = adapter.getItem(from);
                        adapter.remove(item);
                        adapter.insert(item, to);
                    }
                }
            };

    private DragSortListView.RemoveListener onRemove = 
            new DragSortListView.RemoveListener() {
                @Override
                public void remove(int which) {
                	Log.d(TAG, "************ calling removing item : " + which);
                    adapter.remove(adapter.getItem(which));
                }
            };

            
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = new CitiesDatabase(getApplicationContext());
		
		initializeUi();
		
		handleIntent(getIntent());
	}
	
	@SuppressLint("NewApi")
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true);
        }

        return true;
    }
	
	private void initializeUi() {
		@SuppressWarnings("unchecked")
		List<CityTimeZone> cities = (List<CityTimeZone>) getLastNonConfigurationInstance();
		if (cities != null) {
			// check if there's an intent
		} else {
			SharedPreferences prefs = getSharedPreferences("ListOrderPreference", Context.MODE_PRIVATE);
			String listOrder = prefs.getString("listOrder", null);

			cities = new ArrayList<CityTimeZone>();

			
			
			if (listOrder == null) {
				cities = db.getDefaultCities();
				Log.d(TAG, " # of default cities : " + cities.size());
			} else {
				Log.d(TAG, " fetching cities in listOrder: " + listOrder.toString());
				// load the shared preferences and display that
				cities = db.getCitiesById(listOrder);
			}

			db.close();


		}
		setContentView(R.layout.activity_main);
		DragSortListView mDslv = (DragSortListView) getListView();
//		mDslv.setTextFilterEnabled(true);
		mDslv.setDropListener(onDrop);
        mDslv.setRemoveListener(onRemove);
        

//		mController = buildController(mDslv);
//        mDslv.setFloatViewManager(mController);
//        mDslv.setOnTouchListener(mController);
		 updateListPositionPreferences();

		adapter = new IconicAdapter(cities, this);
		setListAdapter(adapter);
	}

//	@Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
//            setContentView(R.layout.activity_main);
//        }
//        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            setContentView(R.layout.activity_main);         
//        }
//    }
//	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                onSearchRequested();
                return true;
            case R.id.menu_settings: {
                Intent i = new Intent(this, UserSettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;
            }
            default:
                return false;
        }
        return false;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      initializeUi();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        // Do not need to recreate menu
        /*if(Build.VERSION.SDK_INT >= 11)
            invalidateOptionsMenu();*/
        clearSearchWidget();
        if(adapter != null) {
        	adapter.notifyDataSetInvalidated();
        }
    }

	@SuppressLint("NewApi")
	private void clearSearchWidget() {
		if(Build.VERSION.SDK_INT >= 11) {
            // Calling twice: first empty text field, second iconify the view
            searchView.setIconified(true);
            searchView.setIconified(true);
        }
		
	}
    /**
     * Called in onCreateView. Override this to provide a custom
     * DragSortController.
     */
    public DragSortController buildController(DragSortListView dslv) {
        // defaults are dragStartMode = onDown  removeMode = flingRight
        DragSortController controller = new DragSortController(dslv);
//        controller.setDragHandleId(R.id.drag_handle);
        controller.setClickRemoveId(R.id.drag_handle);
        controller.setRemoveEnabled(true);
        
        controller.setDragInitMode(dragStartMode);
        controller.setRemoveMode(DragSortController.FLING_REMOVE);
        return controller;
    }
    
    
    @Override
    protected void onNewIntent(Intent intent) {
    	handleIntent(intent);
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
    	return adapter.getItems();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if(db != null) {
    		db.close();
    	}
    }

	private void updateListPositionPreferences() {
		SharedPreferences prefs = getSharedPreferences("ListOrderPreference", Context.MODE_PRIVATE);

		if(adapter != null) {
			// store the order
			String order = ""; 	
			int count = adapter.getCount();
			for(int i = 0; i < count - 1; i++) {
				CityTimeZone ctz = adapter.getItem(i);
				order += ctz.getId() + ",";
			}
			order += adapter.getItem(count - 1).getId();
			
			SharedPreferences.Editor ed = prefs.edit();
	    	ed.putString("listOrder", order);
	    	ed.commit();  //Commiting changes
		}
	
	}
    private void handleIntent(Intent intent) { 
 	   if (Intent.ACTION_SEARCH.equals(intent.getAction())) { 
 	      String query = intent.getStringExtra(SearchManager.QUERY);
 	     Log.d(TAG, " calling Intent.ACTION_SEARCH..!! : " + query);
// 	      doSearch(query);
 	     Uri detailUri = intent.getData(); 
	      String id = detailUri.getLastPathSegment();
	      //get this item and add it to list
	      CityTimeZone newCity = db.getCity(id);
	      if(newCity != null) {
	    	  adapter.add(newCity);
	    	  adapter.notifyDataSetChanged();
	      }
 	   } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
 		   Log.d(TAG, " calling Intent.ACTION_VIEW..!!");
 	      Uri detailUri = intent.getData(); 
 	      String id = detailUri.getLastPathSegment();
 	      //get this item and add it to list
 	      CityTimeZone newCity = db.getCity(id);
 	      if(newCity != null) {
 	    	  adapter.add(newCity);
 	    	  adapter.notifyDataSetChanged();
 	      }
 	      
 	      clearSearchWidget();
 	      
 	     updateListPositionPreferences();
 	   } 
 	   
 	} 

}
