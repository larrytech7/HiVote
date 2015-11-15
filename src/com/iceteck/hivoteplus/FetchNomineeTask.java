package com.iceteck.hivoteplus;

import iceteck.model.data.ORMModel;
import iceteck.model.data.ORMModel.Nominee;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class FetchNomineeTask extends AsyncTask<Integer,Void, Void> {
	private Context context;
	private Activity activity;
	private final String LOG = FetchNomineeTask.class.getName();
	
	public FetchNomineeTask(Context c, int id){
		context = c;
		activity = (Activity) c;
		this.execute(id);
	}
	/**
	 * Get the nominees for the particular id and save in the local database. 
	 * This task is run only when the user clicks refresh or when the application is newly  installed on a user's device
	 * Subsequent DB updates are handled by services and sync adapters
	 */

	@Override
	protected Void doInBackground(Integer... params) {

		URL url;
		HttpURLConnection conn;
		InputStream inStream;
		JSONObject mJson;
		JSONArray nJsonId, nJsonCid, nJsonName, nJsonDescription, nJsonVotes, nJsonUrl , nJsonBitmap;
		
		Uri uri = Uri.parse("http://icetech.webege.com/categories.php?_id="+params[0]+"&candidate=1");
		uri.buildUpon()
		.appendQueryParameter("candidate", "1")
		.appendQueryParameter("_id", ""+params[0])
		.build();
		//log the requested url so that we make sure we sending the correct request to the sever
		Log.d(LOG, uri.toString());
		try {
			url  = new URL(uri.toString());
			
			//connect to n/w category source and make request for JSON data
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			
			//parse the returned JSOn from the server
			inStream = conn.getInputStream();
			String data = convertToJson(inStream);
			mJson = new JSONObject(data);
			//retrieve data sets from the json returned from the server
			nJsonCid = mJson.getJSONArray("cat__nom_id"); //category id for this nominee
			nJsonId = mJson.getJSONArray("nominee_id"); //nominee id
			nJsonName = mJson.getJSONArray("nominee_name"); //nominee name
			nJsonBitmap = mJson.getJSONArray("nominee_bitmap");
			nJsonDescription = mJson.getJSONArray("nominee_portfolio"); //nominee portfolio
			nJsonVotes = mJson.getJSONArray("nominee_votes");
			nJsonUrl = mJson.getJSONArray("nominee_url"); //represent the image/profile photo of
		
			Vector<ContentValues> cVector = new Vector<ContentValues>();
			
			for(int i=0; i< nJsonId.length(); i++)
			{
				 ContentValues cvalues = new ContentValues();
				 
				 cvalues.put(ORMModel.Nominee.COLUM_ID_CAT, Integer.parseInt(nJsonCid.getString(i)));
				 cvalues.put(ORMModel.Nominee.COLUM_ID, Integer.parseInt(nJsonId.getString(i)));
				 cvalues.put(ORMModel.Nominee.COLUMN_NAME, nJsonName.getString(i));
				 cvalues.put(ORMModel.Nominee.COLUMN_BITMAP_LOCATION, nJsonBitmap.getString(i));
				 cvalues.put(ORMModel.Nominee.COLUMN_PORTFOLIO, nJsonDescription.getString(i));
				 cvalues.put(ORMModel.Nominee.COLUMN_VOTES, Integer.parseInt(nJsonVotes.getString(i)));
				 cvalues.put(ORMModel.Nominee.COLUMN_URL_LINK, nJsonUrl.getString(i));
				 cVector.add(cvalues);
			}	 
			if(cVector.size() > 0){
				ContentValues[] vcs = new ContentValues[cVector.size()];
				cVector.toArray(vcs);
				try {
					int id = this.context.getContentResolver().bulkInsert(ORMModel.Nominee.CONTENT_URI, vcs);
					if(id >0){ 
						Log.d(LOG, "New Values Inserted ");
					}else{ //updates the nominee vote counts as downloaded from the online servers
						int up_id = 0;
						for(ContentValues cvalues: vcs){
							 up_id = this.context.getContentResolver().update(ORMModel.Nominee.CONTENT_URI,
									 cvalues,
									 Nominee.COLUM_ID+" = ?", 
									 new String[]{Long.toString(cvalues.getAsInteger(Nominee.COLUM_ID))});
						}
						if(up_id > 0){
							Log.d(LOG, "Updated values "+ORMModel.Nominee.CONTENT_URI);
						}else{
							Log.d(LOG, "Failed to insert value at "+ORMModel.Nominee.CONTENT_URI);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		Log.d(LOG, "Task Completed successfully");
	
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
		
	}
	
	@Override
	protected void onPostExecute(Void v){
		activity.setProgressBarIndeterminateVisibility(false);
	}
	
	@SuppressWarnings("resource")
	private String convertToJson(InputStream ins)
	{
		Scanner s = new Scanner(ins).useDelimiter("\n");
		return s.hasNext()? s.next():"";
	}
	

}
