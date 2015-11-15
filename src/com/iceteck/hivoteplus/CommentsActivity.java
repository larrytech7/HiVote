package com.iceteck.hivoteplus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class CommentsActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_comments);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbarbg));
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.comments_container, new CommentsFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		return id == R.id.action_settings? true: super.onOptionsItemSelected(item);		
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public Intent getParentActivityIntent(){
		return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}
	/**
	 * A Comments fragment containing area for use comments on the given subject
	 */
	public static class CommentsFragment extends Fragment implements OnClickListener{
		private int category_id = 0;
		private EditText commentText;
		private ListView commentListView;
		private ArrayList<String[]> comments;
		private View rootView;
		private Timer mTimer;
		
		public CommentsFragment() {
			comments = new ArrayList<String[]>();
		/*	comments.add(new String[]{"larry Lite", Calendar.getInstance(Locale.getDefault()).getTime().toLocaleString(), "This is a comment to test the functionality"});
			comments.add(new String[]{"Ice Maniac", "March 20, 2015 10:10:01 AM", "How can these things be done for the very best"});
			comments.add(new String[]{"Youseff", "March 20, 2015 10:10:01 AM", "How do guys vote for this nigga at such a point"});
			comments.add(new String[]{"Ice Prince", "March 20, 2015 10:10:01 AM", "Hey dude, who the hell do you think you are. Shut the hel up and cool down nigga"});
			comments.add(new String[]{"Samuel Figuel", "March 20, 2015 10:10:01 AM", "Calmos amigos i don't need your damn choice"});
		*/}

		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			mTimer = new Timer();
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
			
			rootView = inflater.inflate(R.layout.fragment_comments,container, false);
			category_id = getActivity().getIntent().getExtras().getInt("CATEGORY"); //retrieve the category key for which to comment on
			Log.d("Comments", "Category id = "+category_id);
			commentText = (EditText) rootView.findViewById(R.id.comment_edittext);
			commentListView = (ListView) rootView.findViewById(R.id.listView1);
			CommentAdapter cAdapter = new CommentAdapter(getActivity(), comments);
			commentListView.setAdapter(cAdapter);
			commentListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> vAdapter, View view, int position,
					long id) {
				CommentAdapter sadapter = (CommentAdapter)vAdapter.getAdapter();
				String[] mdata = sadapter.getItem(position);
				Toast.makeText(getActivity(), mdata[2], Toast.LENGTH_LONG).show();
			}
		});
			
			Button postbutton = (Button) rootView.findViewById(R.id.post_comment);
			postbutton.setOnClickListener(this);
			return rootView;
		}
		
		@Override
		public void onResume(){
			super.onResume();
			new CommentManagerTask(commentListView, getActivity()).execute(""+category_id);
		}

		@Override
		public void onClick(View v) {
			//JsonObject jsonComment = new JsonObject();
			JSONObject commentJson = new JSONObject();
			
			try {
				commentJson.put("comment_category", category_id)
				.put("comment", commentText.getText().toString())
				.put("comment_author", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("contact", "Google User"))
				.put("comment_date", Calendar.getInstance(Locale.getDefault()).getTime().toLocaleString());
				
				CommentManagerTask cmManager = new CommentManagerTask(getActivity(), commentListView, commentText);
				cmManager.execute(commentJson.toString(), "newcomment");
				
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(getActivity(), getResources().getString(R.string.commentinvalid), Toast.LENGTH_LONG).show();
			}
		}
		
		private class CommentManagerTask extends AsyncTask<String, Void, String>{
			private final String posturl = "http://icetech.webege.com/categories.php";
			private ListView listv;
			private EditText lEditText;
			private int cat_idd = 0;
			private Context context;
			
			public CommentManagerTask(Context ctx, ListView lv, EditText ed){
				listv = lv;
				lEditText = ed;
				context = ctx;
			}
			public CommentManagerTask(ListView lv, Context ctx){
				listv = lv;
				context = ctx;
			}
			@Override
			protected void onPreExecute(){
			  	((Activity)context).setProgressBarIndeterminateVisibility(true);
			}
			@Override
			protected String doInBackground(String... params) {
				
				try {
					URL url = new URL(posturl);
					//prepare and initiate post request
					HttpURLConnection mconnection = (HttpURLConnection) url.openConnection();
					mconnection.setDoInput(true);
					mconnection.setDoOutput(true);
					mconnection.setChunkedStreamingMode(0);
					mconnection.setRequestMethod("POST");
					mconnection.connect();
		
					OutputStream outstream = mconnection.getOutputStream();
					DataOutputStream dos = new DataOutputStream(outstream);
					if(params.length > 1)
						dos.writeBytes("newcomment="+params[0]);
					else{
						dos.writeBytes("loadcomments="+Integer.parseInt(params[0]));
						cat_idd = Integer.parseInt(params[0]);
					}
						//dos.writeBytes("&comment="+params[1]);
					dos.flush();
					dos.close();
					//process server response to the post request
					InputStream instream = mconnection.getInputStream();
					BufferedReader breader = new BufferedReader(new InputStreamReader(instream));
					StringBuffer responseBuffer = new StringBuffer();
					String line = "";
					while((line = breader.readLine()) != null ){
						responseBuffer.append(line);
					}
					breader.close();
					return responseBuffer.toString();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				return "";
			}
			
			@Override
			protected void onCancelled(String message){
				//if(this.getStatus() == Status.FINISHED)
					this.execute(""+cat_idd);
				Toast.makeText(getActivity(), "Comment published", Toast.LENGTH_LONG).show();
				lEditText.setText("");
			}
			
			@Override
			protected void onPostExecute(String result){
				/**
				 * Structure of 'result': {
				 *  	'status': Enumerated integer value (0,1,2), 0 for false operation result and 1 for successful operation. 2  means request was for a read operation
				 *  	'message': String describing the message for operations other than reading
				 *  	'comments': { 'id':[array of comment_ids],
				 *  				  'comment_author':  [array of corresponding comment authors],
				 *  				  'comment': [array of corresponding comments],
				 *  				  'comment_date':[array of comment dates]	
				 *  				}
				 * }
				 */
				try {
					JSONObject resultJson = new JSONObject(result);
					int result_st = resultJson.getInt("status");
					
					switch(result_st){
					case 0:
					case 1:
						String msg = resultJson.getString("message");
						Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
						lEditText.setText("");
					    mTimer.schedule(new CommentUpdaterTimerTask(), 500);
						break;
					case 2:
						JSONObject comments_js = resultJson.getJSONObject("comments");
						JSONArray com_id = comments_js.getJSONArray("com_id");
						JSONArray com_authors = comments_js.getJSONArray("comment_author");
						JSONArray com_comm = comments_js.getJSONArray("comment");
						JSONArray com_dates = comments_js.getJSONArray("comment_date");
						
						for(int i=0; i< com_id.length(); i++){
							comments.add(new String[]{com_authors.getString(i), com_dates.getString(i), com_comm.getString(i)});
						}
						CommentAdapter cAdapter = new CommentAdapter(getActivity(), comments);
						listv.setAdapter(null);
						listv.setAdapter(cAdapter);
						listv.smoothScrollToPosition(comments.size() -1);
					  	((Activity)context).setProgressBarIndeterminateVisibility(false);
						break;
					}
					Log.d("Comments", ""+resultJson.toString(2));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		private class CommentUpdaterTimerTask extends TimerTask{

			@Override
			public void run() {
				new CommentManagerTask(commentListView, getActivity()).execute(""+category_id);
			}
			
		}
	}
}
