package com.iceteck.hivoteplus;

import iceteck.model.data.ORMModel;
import sync.HiVoteSyncAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iceteck.services.UpdateService;

@SuppressLint("NewApi")
public class CategoryActivity extends Activity{

	public static ListView categoryListView;
	public static CategoryAdapter myAdapter;
	public static String currentCategory;
	public static ProgressDialog pd;
	EditText edittext;
	public LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback;
	private DataSetObserver dataObserver;
	
	public static final int CATEGORY_TITLE = 1;
	public static final int CATEGORY_THUMBNAIL = 3;
	public static final int CATEGORY_DATE = 2;
	public static final int CATEGORY_DESC = 4;
	public static final int CATEGORY_ID = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_category);
		try{
			
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbarbg));
		HiVoteSyncAdapter.synImmediately(this);
		categoryListView = (ListView) findViewById(R.id.categoryListView);
	   
		dataObserver = new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				Toast.makeText(CategoryActivity.this, "DataSetChanged", Toast.LENGTH_LONG).show();
			}
		};

		}
		catch(NullPointerException np)
		{
			np.printStackTrace();
			Log.e("Error ID: "+Log.ERROR, "Error Info: "+np.getLocalizedMessage());
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.category, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem mit)
	{
		switch(mit.getItemId())
		{
		case R.id.addCat:
			startActivity(new Intent(this, AddCategoryActivity.class));
			this.overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
			//this.overridePendingTransition(R.anim.wave_scale, R.anim.wave_scale);
			return true;
		/*case R.id.refreshcat:
			updateCategories();
			return true;*/
		case R.id.exitcat:
			System.exit(0);
			return true;
		case R.id.help:
			TextView edt = new TextView(this);
			edt.setText(Html.fromHtml(getResources().getString(R.string.helptext)));
			edt.setClickable(false);
			edt.setEnabled(false);
			edt.setTextSize(20);
			edt.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
			Dialog d = new Dialog(this);
			d.setTitle("Help");
			
			d.setCancelable(true);
			d.setContentView(edt);
			d.show();
			return true;
		case R.id.action_share:
			try {
				
				startActivityForResult(getShareIntent(), 0);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(this, "No App was found on this device that could be used to share this app", Toast.LENGTH_LONG).show();
			}
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, AppSettingsActivity.class));
			this.overridePendingTransition(android.R.anim.slide_out_right,0);
			return true;
		default:
			return super.onOptionsItemSelected(mit);	
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		Cursor cur = this.getContentResolver().query(ORMModel.Category.CONTENT_URI, null, null, null, ORMModel.Category.COLUM_ID+" DESC");
		myAdapter = new CategoryAdapter(this,cur,0);
		myAdapter.registerDataSetObserver(dataObserver);
		categoryListView.setAdapter(myAdapter);
		updateCategories();
		
	}
	@Override
	public void onPause(){
		super.onPause();
		myAdapter.unregisterDataSetObserver(dataObserver);
	}
	
	private Intent getShareIntent(){
		Intent share = new Intent(Intent.ACTION_SEND);
		 share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_text));
		share.putExtra(Intent.EXTRA_HTML_TEXT, Html.fromHtml(getResources().getString(R.string.share_text)));
		return share;
	}
	
	private void updateCategories(){
		startService(new Intent(this, UpdateService.class));
		HiVoteSyncAdapter.synImmediately(this);
	}
	
}