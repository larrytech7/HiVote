package com.iceteck.hivoteplus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

public class NomineeActivity extends ActionBarActivity implements NomineeFragment.Callback{

	static boolean mDoublePane;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nominee_);
	//	getActionBar().setBackgroundDrawable(getResources().getDrawabl(R.drawable.actionbarbg));
		if(findViewById(R.id.detail_container) != null){
			mDoublePane = true;
			if (savedInstanceState == null) {
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.detail_container, new DetailFragment())
						.commit();
			}
			findViewById(R.id.btn_add_float).setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent cintent = new Intent(getApplicationContext(), CommentsActivity.class);
					cintent.putExtra("CATEGORY", getIntent().getExtras().getInt("c_id"));//NomineeFragment.categoryId);
					startActivity(cintent);
					(NomineeActivity.this).overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
				}
			});
		}
		else{
			mDoublePane = false;
			if (savedInstanceState == null) {
				NomineeFragment nfrag = new NomineeFragment();
				Bundle b=new Bundle();
				b.putBoolean("ONEPANE", true);
				nfrag.setArguments(b);
				getSupportFragmentManager().beginTransaction()
						.add(R.id.container, nfrag)
						.commit();
			}
		}
	}

	public static boolean isTwoPaneUi(){
		return mDoublePane;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemSelected(int item_id, String... args) {
		if(mDoublePane){
			
			Bundle bundle = new Bundle();
			bundle.putStringArray("details", args);
			
			DetailFragment dfragment = new DetailFragment();
			dfragment.setArguments(bundle);
			
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.detail_container, dfragment)
			.commit();
			
		}else{
		
			Intent detailsIntent = new Intent(this, DetailActivity.class);//default activity to launch is ProfileActivity 
			String[] details = new String[]{
					args[0],
					args[1],
					args[2],
					args[3],
					args[4],
					args[5],
					args[6]
			};
			
			detailsIntent.putExtra("details", details);
			startActivity(detailsIntent);	
		}
	}

}
