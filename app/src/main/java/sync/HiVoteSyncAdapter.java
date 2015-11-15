package sync;

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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class HiVoteSyncAdapter extends AbstractThreadedSyncAdapter {

	private static final String TAG = "HiSyncAdapter";
	public static final String NEW_STATUS_INTENT = "com.iceteck.hivote.NEW_STATUS";
	public static final String NEW_STATUS_EXTRA_COUNT = "NEW_STATUS_EXTRA_COUNT";
	public static final String RECEIVE_TIMELINE_NOTIFICATIONS = "com.iceteck.hivote.TIMELINE";
	private NotificationManager notificationManager;
	private NotificationCompat.Builder notifBuilder;
	private TimerTask mTimerTask;
	private Timer mTimer;
	private final int UPDATE_INTERVAL = 60*1000; //1 minute interval 
	
	public HiVoteSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		Intent it;
		this.notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
	    notifBuilder = new NotificationCompat.Builder(getContext())
		.setSmallIcon(R.drawable.ic_launcher1)
		.setContentTitle("New Event Voting event")
		.setWhen(System.currentTimeMillis())
		.setAutoCancel(true)
		.setContentText(getContext().getResources().getString(R.string.notif_content))
		.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioManager.STREAM_NOTIFICATION);
		
		Log.d(TAG, "Synchronizing");
		if(mTimer == null){
			mTimer = new Timer();
		}else{
			mTimer.cancel();
			mTimer = new Timer();
		}
		//making N/W requests and managing response
		it = new Intent(NEW_STATUS_INTENT);
		it.putExtra(NEW_STATUS_EXTRA_COUNT, "updates");
		boolean d = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("autoupdate", false);
		if(d){
			mTimerTask = new MTaskTimer();
			try {
				mTimer.scheduleAtFixedRate(mTimerTask, 0, UPDATE_INTERVAL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			mTimer.cancel();
			int lid = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("LASTINSERTEDID", 0);
			fetchUpdate(lid);
		}
		Log.d(TAG, "Sync Performed");
	}
	
	public static Account getSyncAccount(Context context){
		AccountManager accManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
		Account newAccount = new Account(
				context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
		if(null == accManager.getPassword(newAccount)){
			if(!accManager.addAccountExplicitly(newAccount, "", null)){
				return null;
			}
			
		}
		return newAccount;
	}
	
	public static void synImmediately(Context ctx){
		
		Bundle bundle = new Bundle();
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(getSyncAccount(ctx), ctx.getString(R.string.content_authority), bundle);
		
	}
	/**
	 * Notify the user about newly created events/categories
	 */
		private void sendNotification() {
			// TODO Auto-generated method stub
			Log.d(TAG, "Sending Notifications");
			PendingIntent pi = PendingIntent.getActivity(getContext(), -1, 
					new Intent(getContext(), CategoryActivity.class),
					PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT|Intent.FLAG_ACTIVITY_CLEAR_TASK);
			notifBuilder.setContentIntent(pi);
			
			notificationManager.notify(0, notifBuilder.build());
			
			Log.d(TAG, "Notification sent");
		}
		
		public class MTaskTimer extends TimerTask{
			@Override
			public void run() {
				int lid = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("LASTINSERTEDID", 0);
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
		//udpates the category in the database
		private void fetchUpdate(int lastinsertedid){
			URL url;
			HttpURLConnection conn;
			InputStream inStream;
			JSONObject mJson;
			JSONArray catJsonId, catJsonTitle, catJsonDescription, catJsonDate, catJsonUrl;
			int lastinsert = 0;
			try {
				url  = new URL("http://icetech.webege.com/categories.php?category=1&start="+lastinsertedid);
				
				//connect to n/w category source and make request for JSON data
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				conn.connect();
				
				//parse the returned JSOn from the server
				inStream = conn.getInputStream();
				String data = convertToJson(inStream);
				System.out.println(data);
				mJson = new JSONObject(data);
				
				//retrieve data sets from the json returned from the server
				catJsonId = mJson.getJSONArray("category_id");
				catJsonTitle = mJson.getJSONArray("category_title");
				catJsonDescription = mJson.getJSONArray("category_description");
				catJsonUrl = mJson.getJSONArray("category_url");
				catJsonDate = mJson.getJSONArray("category_date");
				JSONArray catjsonAuthor = mJson.getJSONArray("category_author");
				
				Vector<ContentValues> cVector = new Vector<ContentValues>();
				
				for(int i=0; i< catJsonId.length(); i++)
				{
					 ContentValues cvalues = new ContentValues();
					 int col_id = Integer.parseInt(catJsonId.getString(i));
					 cvalues.put(ORMModel.Category.COLUM_ID, col_id);
					 cvalues.put(ORMModel.Category.COLUMN_BITMAP_LOCATION, catJsonUrl.getString(i));
					 cvalues.put(ORMModel.Category.COLUMN_DESCRIPTION, catJsonDescription.getString(i));
					 cvalues.put(ORMModel.Category.COLUMN_TITLE, catJsonTitle.getString(i));
					 cvalues.put(ORMModel.Category.COLUMN_DATE, catJsonDate.getString(i));
					 cvalues.put(ORMModel.Category.COLUMN_AUTHOR, catjsonAuthor.getString(i));
					 cVector.add(cvalues);
					 lastinsert = col_id;
				}	 
				if(cVector.size() > 0){
					ContentValues[] vcs = new ContentValues[cVector.size()];
					cVector.toArray(vcs);
					try {
						PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("LASTINSERTEDID", lastinsert).commit();
						int id = getContext().getContentResolver().bulkInsert(ORMModel.Category.CONTENT_URI, vcs);
						if(id > 0){
							//if there are new updates, we send a notification prompting the user to check them out
							sendNotification();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();	
					}
				}
				
			Log.d(TAG , "Task Completed successfully");
			Log.d(TAG, data);
		
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
}
