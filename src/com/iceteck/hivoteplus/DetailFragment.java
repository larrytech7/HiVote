package com.iceteck.hivoteplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DetailFragment extends Fragment implements android.view.View.OnClickListener{
	
	private TextView name, nomineeInfo;
	public static ProgressDialog pd;
	private ImageButton voteButton;
	final int SHARE_ID = 1;
	private LinearLayout profile_ll;
	private String nName, category,bitmap_url,portfolio;
	private int cat_id,nom_id;
	private View rootView;
	
	public DetailFragment() {
	}
	
	public static DetailFragment newInstance(int index){
		DetailFragment f = new DetailFragment();
			Bundle bundle = new Bundle();
			bundle.putInt("index", index);
			f.setArguments(bundle);
		return f;
	}
	
	public int getShownIndex(){
		return getArguments().getInt("index");
	}
	
	@Override
	public void onCreate(Bundle b){
		super.onCreate(b);
		setHasOptionsMenu(true);
	}
	
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_detail,container, false);
		try {
			//hold required data fields
			name = (TextView) rootView.findViewById(R.id.nomineeName);
			name.setTextColor(Color.BLACK);
			profile_ll = (LinearLayout) rootView.findViewById(R.id.nomineeProfile);
			nomineeInfo  = (TextView) rootView.findViewById(R.id.nomineeInfo);
			voteButton  = (ImageButton) rootView.findViewById(R.id.voteNominee);
			voteButton.setMaxWidth(50);
			voteButton.setOnClickListener(this);
			
		} catch (Exception e) {
			e.printStackTrace();
		//	Toast.makeText(getActivity(), "Failure: "+e.getMessage(), Toast.LENGTH_LONG).show();
		}
		return rootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater mit) {
		// Inflate the menu; this adds items to the action bar if it is present.
		mit.inflate(R.menu.detail, menu);
		MenuItem  mitem = menu.findItem(R.id.share);
		ShareActionProvider lShareActionProvider;
		lShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mitem);
		if(lShareActionProvider !=null){
			
			lShareActionProvider.setShareIntent(getShareIntent("I voted for "+nName+
					". Can you?<a href='icetech.webege.com/hivoteplus.apk'>Download HERE</a>"));
		}else{
			Toast.makeText(getActivity(), "ShareProvider not ok", Toast.LENGTH_LONG).show();
		}
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@SuppressLint("InlinedApi")
	private Intent getShareIntent(String text){
		Intent share = new Intent(Intent.ACTION_SEND);
		 share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		share.setType("text/*");
		share.putExtra(Intent.EXTRA_TEXT, text);
		share.putExtra(Intent.EXTRA_HTML_TEXT, Html.fromHtml(text));
		return share;
	}
	
	@SuppressLint("NewApi")
	public void onResume()
	{
		super.onResume();
		//set the candidates's name and nomination
		if(getActivity().getIntent() != null)
		{
			//Bundle infoBundle = getActivity().getIntent().getExtras();//getArguments().getStringArray("details");
			try{
				
				String[] details = getArguments().getStringArray("details");//infoBundle.getStringArray("details");
				
				cat_id = Integer.parseInt(details[2]);//infoBundle.getInt("nomineeCategory");
				nom_id = Integer.parseInt(details[3]);//infoBundle.getInt("nomineeId");
				nName = details[0];//infoBundle.getString("nominee");
				category = details[1];//infoBundle.getString("category");
				bitmap_url = details[6];//infoBundle.getString("nominee_bitmap");
				portfolio = details[4];//infoBundle.getString("nominee_portfolio");
				
				name.setText(nName);
				nomineeInfo.setText(portfolio);
				 
				BitmapDrawable bmp = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.ic_nominee));
				bmp.setGravity(Gravity.CENTER);
				profile_ll.setBackground(bmp);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				//Toast.makeText(getActivity(), "DetailsFragment Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
			}catch(NoSuchMethodError ex){
				ex.printStackTrace();
			}
		}
		else
			startActivity(new Intent(getActivity(), CategoryActivity.class));	
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
	}
	
	@Override
	public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			SharedPreferences sp;
			if(id == R.id.voteNominee)
			{
				sp = PreferenceManager.getDefaultSharedPreferences(getActivity());				
				vote(sp);
			}
		}
	
	public void vote(SharedPreferences sp){ 
		//called by the button to submit the voted nominee to the voting database
		String c = sp.getString(CategoryActivity.currentCategory, "");
		 String fanName=sp.getString("username", "");
		 String fanEmail=sp.getString("email", "anonymous@icetech.com");
		 String fanNumber = ""+sp.getFloat("phone", 22222222); 
		 TelephonyManager telMgr = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		 String dId = telMgr.getDeviceId();
		
		boolean d =sp.getBoolean("voted", false);
		if(d && c.equalsIgnoreCase(category))
		 {
			 Toast.makeText(getActivity(), "Sorry, You have already cast a vote in: "+category,  Toast.LENGTH_LONG).show(); 
		 }
		 else{	
				try{
					DetailFragment.pd = ProgressDialog.show(getActivity(), "Voting", "Submitting vote. Please wait ....");
					votingTask.execute(""+cat_id, fanName, fanEmail, fanNumber, ""+nom_id);
					sp.edit()//.putBoolean("voted", true) //state of the vote, true = voted , false = not yet voted
					.putString("voteId", dId) //voter identified by the mobile id
					.putString(CategoryActivity.currentCategory, category)
					.commit();
				}
				catch(IllegalStateException e)
				{
					DetailFragment.pd.dismiss();
					e.printStackTrace();
					Toast.makeText(getActivity(), "Already voted here\n", Toast.LENGTH_SHORT).show();
				}	
		 	}
		}

	AsyncTask<String, Void, String> votingTask = new AsyncTask<String, Void, String>()
			{
						@Override
						protected String doInBackground(String... arg0) {
							// TODO Auto-generated method stub
							URL url;
							HttpURLConnection conn;
							BufferedReader reader;
							InputStream inStream;// www.ubconnect.webatu.com
							String cleanUrl = "http://icetech.webege.com/categories.php?cid="+arg0[0]+"&fname="+arg0[1]+"&femail="+arg0[2]+"&fnumber="+arg0[3]+"&nid="+arg0[4]; 
							cleanUrl = cleanUrl.replaceAll(" ", "%20");
							try {
							url = new URL(cleanUrl);
							conn = (HttpURLConnection) url.openConnection();
							conn.setDoInput(true);
							conn.setConnectTimeout(5000);
							HttpURLConnection.setFollowRedirects(true);
							conn.setRequestMethod("GET");
							conn.setUseCaches(true);
							conn.connect();
							
							inStream = conn.getInputStream();
							StringBuffer sBuffer = new StringBuffer();
							
							if(inStream == null)
							{
								return null;
							}
							reader = new BufferedReader(new InputStreamReader(inStream));
							String line;
							//read JSON store as String
							while((line = reader.readLine()) != null)
							{
								sBuffer.append(line+"\n");
							}
							return sBuffer.toString();
							
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								return "Sorry your vote was not counted.\n"+e.getMessage();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								return "Sorry your vote was not counted.\n"+e.getMessage();
							}
						}
						
						@Override
						protected void onPostExecute(String result)
						{
							DetailFragment.pd.dismiss();
							if(result.contains("success")){
								Toast.makeText(getActivity(), "Voted Successfully", Toast.LENGTH_SHORT).show();
								PreferenceManager.getDefaultSharedPreferences(getActivity())
								.edit()
								.putBoolean("voted", true)
								.commit();
							}else
								Toast.makeText(getActivity(), ""+getResources().getString(R.string.err_vote), Toast.LENGTH_SHORT).show();
						Log.d("Vote Result", result);	
						}
			};

}
