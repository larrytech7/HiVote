package com.iceteck.tasks;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.iceteck.hivoteplus.CategoryActivity;
import com.iceteck.hivoteplus.MainActivity;

/**
 * Takes a json encoded signup parameters an submits to an online server for registration
 * @author Larry AKah
 *
 */
public class SignupTask extends AsyncTask<String, Void, String> {

	private Context context;
	
	public SignupTask(Context c){
		context = c;
	}
	
	@Override
	protected String doInBackground(String... params) {
		
		try {
			//should contain json structure of login parameters (username, phone, email, deviceid)
			JSONObject param = new JSONObject(params[0]);
			
			URL murl = new URL("http://www.iceteck.com/hivote/index.php");
			HttpURLConnection conn = (HttpURLConnection) murl.openConnection();
			conn.setConnectTimeout(60000);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "text/plain");
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes("newuser="+param.toString());
			wr.flush();
			wr.close();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String input;
			while((input = br.readLine()) != null){
				sb.append(input);
			}
			
			br.close();
			return sb.toString();
			
		} catch (JSONException e) {
			
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(String result){
		MainActivity.pb.dismiss();
		if(null != result){
			//parse results
			try {
				JSONObject jresult = new JSONObject(result);
				int status = jresult.getInt("status");
				String message = jresult.getString("message");
				String name = jresult.getString("username");
				String email = jresult.getString("email");
				Float number = (float) jresult.getInt("phone");
				String device = jresult.getString("deviceid");
				if(status ==1){
					//success
					Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
					sp.edit()
					.putString("contact", name)
					.putString("cemail", email)
					.putFloat("cnumber", number)
					.putString("device", device)
					.putBoolean("registered", true)
					.apply();
					context.startActivity(new Intent(context, CategoryActivity.class));
				}else{
					Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			Toast.makeText(context, "Connection error. Check your internet", Toast.LENGTH_SHORT).show();
		}
		
	}

}
