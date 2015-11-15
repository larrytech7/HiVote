package com.iceteck.services;

import iceteck.model.data.ORMModel;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iceteck.hivoteplus.CategoryActivity;
import com.iceteck.hivoteplus.R;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class UpdateService extends IntentService {
	//fields
	private static final String TAG = "UpdateService";
	public static final String NEW_STATUS_INTENT = "com.iceteck.hivote.NEW_STATUS";
	public static final String NEW_STATUS_EXTRA_COUNT = "NEW_STATUS_EXTRA_COUNT";
	public static final String RECEIVE_TIMELINE_NOTIFICATIONS = "com.iceteck.hivote.TIMELINE";
	private NotificationManager notificationManager;
	private NotificationCompat.Builder notifBuilder;
	private TimerTask mTimerTask;
	private Timer mTimer;
	private final int UPDATE_INTERVAL = 60*1000; //1 minute interval 
	
	public UpdateService()
	{
		super("");
		mTimerTask = new MTaskTimer();
		Log.d(TAG, "Service Created");
	}
	
	public UpdateService(String name) {
		super(name);
		Log.d(TAG, "UpdateService Running");
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	//get the updates and trigger a notification
	@Override
	protected void onHandleIntent(Intent arg0) {
		Intent it;
		this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    notifBuilder = new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher1)
		.setContentTitle("New Voting event")
		.setWhen(System.currentTimeMillis())
		.setAutoCancel(true)
		.setContentText(getResources().getString(R.string.notif_content))
		.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioManager.STREAM_NOTIFICATION);
		
		Log.d(TAG, "Handling Intent");
		if(mTimer == null){
			mTimer = new Timer();
		}else{
			mTimer.cancel();
		}
		//making N/W requests and managing response
		it = new Intent(NEW_STATUS_INTENT);
		it.putExtra(NEW_STATUS_EXTRA_COUNT, "updates");
		sendBroadcast(it, RECEIVE_TIMELINE_NOTIFICATIONS);
		boolean d = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("autoupdate", false);
		if(d){
			
			try {
				mTimer.scheduleAtFixedRate(mTimerTask, 0, UPDATE_INTERVAL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			mTimer.cancel();
			int lid = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("LASTINSERTEDID", 0);
			fetchUpdate(lid);
		}
	}
/**
 * Notify the user about newly created events
 */
	private void sendNotification() {
		// TODO Auto-generated method stub
		Log.d(TAG, "Sending Notifications");
		PendingIntent pi = PendingIntent.getActivity(this, -1, 
				new Intent(this, CategoryActivity.class),
				PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT);
		notifBuilder.setContentIntent(pi);
		
		notificationManager.notify(0, notifBuilder.build());
		
		Log.d(TAG, "Notification sent");
	}
	
	public class MTaskTimer extends TimerTask{
		@Override
		public void run() {
			int lid = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("LASTINSERTEDID", 0);
			fetchUpdate(lid);
		}
	}
	/**
	 * Parse and return JSON String from the inputStream object
	 * @param ins The input stream to parse
	 * @return JSON String representation of the data from the server
	 */
	@SuppressWarnings("resource")
	private String convertToJson(InputStream ins)
	{
		Scanner
		s = new Scanner(ins).useDelimiter("\n");
		return s.hasNext()? s.next():"";
	}
	
	private void fetchUpdate(int lastinsertedid){
		URL url;
		HttpURLConnection conn;
		JSONObject mJson;
		JSONArray catJsonId, catJsonTitle, catJsonDescription, catJsonDate, catJsonUrl;
		int lastinsertedId = 0;
		
		try {
			url  = new URL("http://icetech.webege.com/categories.php?category=1&start="+lastinsertedid);
			
			//connect to n/w category source and make request for JSON data
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			
			//parse the returned JSOn from the server
			String data = convertToJson(conn.getInputStream());
			mJson = new JSONObject(data);
			
			//retrieve data sets from the json returned from the server
			catJsonId = mJson.getJSONArray("category_id");
			catJsonTitle = mJson.getJSONArray("category_title");
			catJsonDescription = mJson.getJSONArray("category_description");
			catJsonUrl = mJson.getJSONArray("category_url");
			catJsonDate = mJson.getJSONArray("category_date");
			JSONArray catjsonAuthor = mJson.getJSONArray("category_author");
			int col_id = 0;
			
			Vector<ContentValues> cVector = new Vector<ContentValues>();
			
			for(int i=0; i< catJsonId.length(); i++)
			{
				 ContentValues cvalues = new ContentValues();
				 col_id = Integer.parseInt(catJsonId.getString(i));
				 cvalues.put(ORMModel.Category.COLUM_ID, col_id);
				 cvalues.put(ORMModel.Category.COLUMN_BITMAP_LOCATION, catJsonUrl.getString(i));
				 cvalues.put(ORMModel.Category.COLUMN_DESCRIPTION, catJsonDescription.getString(i));
				 cvalues.put(ORMModel.Category.COLUMN_TITLE, catJsonTitle.getString(i));
				 cvalues.put(ORMModel.Category.COLUMN_DATE, catJsonDate.getString(i));
				 cvalues.put(ORMModel.Category.COLUMN_AUTHOR, catjsonAuthor.getString(i));
				 cVector.add(cvalues);
				 lastinsertedId = col_id;
			}	 
			if(cVector.size() > 0){
				ContentValues[] vcs = new ContentValues[cVector.size()];
				cVector.toArray(vcs);
				try {
					PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("LASTINSERTEDID", lastinsertedId).commit();
					int id = getContentResolver().bulkInsert(ORMModel.Category.CONTENT_URI, vcs);
					if(id > 0){
						//if there are new updates, send a notification prompting the user to check them out
						sendNotification();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();	
				}finally{
					System.gc();
				}
			}
			
		Log.d(TAG , "Task Completed successfully");
	
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			System.gc();
		}

	}
}
