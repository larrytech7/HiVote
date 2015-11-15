package com.iceteck.hivoteplus;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Build;

public class OrganisationActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organisation);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new OrganisationFragment()).commit();
		}
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      Toast.makeText(this, "Query: "+query, Toast.LENGTH_LONG).show();
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.organisation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Fragment for managing a list of organisations or parties who create events.
	 */
	public static class OrganisationFragment extends Fragment implements LoaderCallbacks<Cursor> {

		public OrganisationFragment() {
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
			
			Intent intent = getActivity().getIntent();
			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
		      String query = intent.getStringExtra(SearchManager.QUERY);
		      doMySearch(query);
		      Toast.makeText(getActivity(), "Query: "+query, Toast.LENGTH_LONG).show();
		    }
		}

		private void doMySearch(String query) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_organisation,
					container, false);
			return rootView;
		}

		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			// TODO Auto-generated method stub
			
		}
	}

}
