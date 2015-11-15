package com.iceteck.hivoteplus;

import iceteck.model.data.ORMModel;
import sync.HiVoteSyncAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class NomineeFragment extends Fragment implements LoaderCallbacks<Cursor>{

	public static final int NOMINEE_NAME = 1;
	public static final int NOMINEE_LOADER = 2;
	public static ListView candidateListView;
	public static NomineeAdapter nAdapter;
	public static int categoryId = 0;
	private String SELECTED_ITEM ="SELECTED";
	public int mPosition = 0;
	//nominee columns that point to the fields in the nominee table
	private static final String[] COLUMNS = {
			ORMModel.Nominee.COLUM_ID,
			ORMModel.Nominee.COLUM_ID_CAT,
			ORMModel.Nominee.COLUMN_BITMAP_LOCATION,
			ORMModel.Nominee.COLUMN_NAME,
			ORMModel.Nominee.COLUMN_PORTFOLIO,
			ORMModel.Nominee.COLUMN_URL_LINK,
			ORMModel.Nominee.COLUMN_VOTES
		};
		
	public interface Callback{
			public void onItemSelected( int item_id, String ... args);
		}
		
	public NomineeFragment() {
	}
	
	@Override	
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(NOMINEE_LOADER, null, this);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState){
		//getActivity().
		super.onCreate(savedInstanceState);
		//set so that this fragment can handle menu options
		setHasOptionsMenu(true);
		getActivity().getActionBar().setTitle(CategoryActivity.currentCategory);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_nominee,container, false);
		candidateListView = (ListView) rootView.findViewById(R.id.nominee_listview);
		nAdapter = new NomineeAdapter(getActivity(), null, 0);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		Intent rit = getActivity().getIntent();
		try {
			categoryId = rit.getExtras().getInt("c_id");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		candidateListView.setAdapter(nAdapter);
		final boolean d = sp.getBoolean("voted", false);
		final String c = sp.getString(CategoryActivity.currentCategory, "");
		
		candidateListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View listviewItem, int position,long arg3) {
				
				NomineeAdapter nAdapter = (NomineeAdapter) adapterView.getAdapter();
				Cursor cr = nAdapter.getCursor();
				
				if(d && c.equalsIgnoreCase(CategoryActivity.currentCategory))
				{
					Toast.makeText(getActivity(), "Sorry, You have already cast a vote for: "+CategoryActivity.currentCategory,  Toast.LENGTH_LONG).show();
				}
				else if(cr.moveToFirst() && cr.moveToPosition(position)){
					((Callback)getActivity()).onItemSelected(0,cr.getString(cr.getColumnIndex(COLUMNS[3])), //the nominee's name
							CategoryActivity.currentCategory,           //selected category
							""+cr.getInt(cr.getColumnIndex(COLUMNS[1])), //id of category the nominee belongs to.
							""+cr.getInt(cr.getColumnIndex(COLUMNS[0])), //the nominee's id in the DB
							cr.getString(cr.getColumnIndex(COLUMNS[4])), // the nominee's portfolio
							cr.getString(cr.getColumnIndex(COLUMNS[5])), // the nominees's link if available from the DB
							cr.getString(cr.getColumnIndex(COLUMNS[2]))); // the nominee's image path url
				}
				mPosition = position;
			}
		});
		
		if(rit == null){
			new FetchNomineeTask(getActivity(), categoryId);
		}
		if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_ITEM))
			mPosition = savedInstanceState.getInt(SELECTED_ITEM);
		
		nAdapter.notifyDataSetChanged();
		return rootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater mit) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.clear();
		mit.inflate(R.menu.nominee, menu);
		super.onCreateOptionsMenu(menu, mit);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem mit)
	{
		switch(mit.getItemId())
		{
		case R.id.refresh:
			//refresh nominees. Reloads nominee list from source into listView.
			getActivity().setProgressBarIndeterminateVisibility(true);
			if(categoryId > 0)
				new FetchNomineeTask(getActivity(),categoryId);
			
			String sortOrder = ORMModel.Nominee.COLUMN_VOTES+" DESC";
			Cursor nCursor = getActivity().getContentResolver().query(ORMModel.Nominee.buildNomineeUri(categoryId), null, null, null, sortOrder);
			nAdapter.swapCursor(nCursor);
			nAdapter.notifyDataSetChanged();
			return true;
		case R.id.comment:
			//launch comments activity here.
			Intent comments_intent = new Intent(getActivity(), CommentsActivity.class);
			comments_intent.putExtra("CATEGORY", categoryId); //insert the id of the category whose comments are to be loaded
			startActivity(comments_intent);
			getActivity().overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_slide_out_top);
			return true;
		default:
			return super.onOptionsItemSelected(mit);	
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String sortOrder = ORMModel.Nominee.COLUMN_VOTES+" DESC";
		return new CursorLoader(getActivity(),
				ORMModel.Nominee.buildNomineeUri(categoryId),
				COLUMNS,
				null,
				null,
				sortOrder );
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		// TODO Auto-generated method stub
		nAdapter.swapCursor(cursor);
		if(mPosition != ListView.INVALID_POSITION){
			candidateListView.setSelection(mPosition);
		}
		getActivity().setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		nAdapter.swapCursor(null);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Intent nintent = getActivity().getIntent();
		if( nintent != null)
			getLoaderManager().restartLoader(NOMINEE_LOADER, null, this);
		HiVoteSyncAdapter.synImmediately(getActivity());
		
		getActivity().setProgressBarIndeterminateVisibility(true);
		
		String sortOrder = ORMModel.Nominee.COLUMN_VOTES+" DESC";
		Cursor nCursor = getActivity().getContentResolver().query(ORMModel.Nominee.buildNomineeUri(categoryId), null, null, null, sortOrder);
		nAdapter.swapCursor(nCursor);
	//	if(null != getArguments() && !getArguments().getBoolean("ONEPANE"))
		if(NomineeActivity.isTwoPaneUi())
			candidateListView.performItemClick(candidateListView, mPosition, getId());
	
		if(categoryId > 0)
			new FetchNomineeTask(getActivity(),categoryId);
	}

	@Override
	public void onSaveInstanceState(Bundle state){
		super.onSaveInstanceState(state);
		int cId = categoryId;
		state.putInt("category_id", cId);
		state.putInt(SELECTED_ITEM, mPosition);
	}
	
}
