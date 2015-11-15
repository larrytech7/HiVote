package com.iceteck.hivoteplus;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class DetailActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbarbg));
		if (savedInstanceState == null) {
			String[] details = getIntent().getStringArrayExtra("details");
			
			Bundle dBundle = new Bundle();
			dBundle.putStringArray("details", details);
			
			DetailFragment dfrag = new DetailFragment();
			dfrag.setArguments(dBundle);
			
			getSupportFragmentManager().beginTransaction()
					.add(R.id.detail_container, dfrag)
					.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		return super.onOptionsItemSelected(item);
	}

}
