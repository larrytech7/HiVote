package com.iceteck.hivoteplus;


import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.iceteck.tasks.SignupTask;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener,
 ConnectionCallbacks, OnConnectionFailedListener, android.content.DialogInterface.OnClickListener{
    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress ;
	private Button anonButton;
	private final static String LOG_TAG = "MAinActivity";
	private final int RC_SIGN_IN = 0;
	private ProgressDialog sign_in_progress;
	public static boolean mSignInClicked = false;
	public static String signIn_userName = "";
	private ImageSwitcher imageSwitcher;
	private EditText pnumber; //name, email and number of client/voter
	private SharedPreferences settings;
	private AlertDialog.Builder signupDialogBuilder;
	AlertDialog signupDialog;

	private static final int DIALOG_GET_GOOGLE_PLAY_SERVICES = 1;

	private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 2;
	private TelephonyManager telmanager;
	public static ProgressDialog pb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.intro);
		
	    telmanager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(Plus.API)
        .addScope(Plus.SCOPE_PLUS_LOGIN)
        .addScope(Plus.SCOPE_PLUS_PROFILE)
        .build();
		
		pnumber = (EditText) findViewById(R.id.pnumber);
	
		try{
			findViewById(R.id.sign_in_button).setOnClickListener(this);
			anonButton  = (Button) findViewById(R.id.signUp);
			anonButton.setOnClickListener(this);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Toast.makeText(this, "Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
		}
	
	      /*imageSwitcher = (ImageSwitcher)findViewById(R.id.imageSwitcher);

	      imageSwitcher.setFactory(new ViewFactory() {

	   @Override
	   public View makeView() {
	      ImageView myView = new ImageView(getApplicationContext());
	      myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
	      myView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.
	      FILL_PARENT,LayoutParams.FILL_PARENT));
	      return myView;
	       }
	   });
	*/
	}

	public void next(){  
	      Animation in = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
	      Animation out = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
	      imageSwitcher.setInAnimation(in);
	      imageSwitcher.setOutAnimation(out);
	      imageSwitcher.setImageResource(R.drawable.powered_by_google_dark);
	   }
	
	public void previous(){
	      
	      Animation in = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
	      Animation out = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
	      imageSwitcher.setInAnimation(out);
	      imageSwitcher.setOutAnimation(in);
	      imageSwitcher.setImageResource(R.drawable.powered_by_google_light);
	   }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem mit)
	{
		switch(mit.getItemId())
		{
		case R.id.aboutkb:
			TextView edt = new TextView(this);
			edt.setText(R.string.about_text);
			edt.setTextSize(30);
			edt.setClickable(false);
			edt.setEnabled(false);
			edt.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
			Dialog d = new Dialog(this);
			d.setTitle("About ");
			d.setCancelable(true);
			d.setContentView(edt);
			d.show();
			return true;
		default:
				return super.onOptionsItemSelected(mit);
		}		
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		boolean firstRan = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("registered", false);
		if(!firstRan){
		//remain here
			String number = telmanager.getLine1Number();
					if(null != number)
						pnumber.setText(number);
						else
							 Toast.makeText(this, getResources().getString(R.string.phoneretrievalerror), Toast.LENGTH_LONG).show();
					pnumber.setEnabled(false);
					pnumber.setClickable(false);
					pnumber.setFocusable(false);
		}
		else
		{
			Intent go = new Intent(this, CategoryActivity.class);
			go.putExtra("usrname", PreferenceManager.getDefaultSharedPreferences(this).getString("contact", ""));
			startActivity(go);
			this.finish();
		}
		if(mSignInClicked)
		{
			startActivity(new Intent(getApplicationContext(), CategoryActivity.class));
		}
	
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		//finish();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		mGoogleApiClient.disconnect();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == RC_SIGN_IN) {
	        mIntentInProgress = false;
	        if (resultCode != RESULT_OK) {
	            mSignInClicked = false;
	          }
	        
	        if (!mGoogleApiClient.isConnecting()) {
	        	try{
	        //		mGoogleApiClient.connect();
	        		Toast.makeText(this, "Sign In Error. Please try again. ", Toast.LENGTH_LONG).show();
	        	}
	        	catch(Exception e)
	        	{
	        		e.printStackTrace();
	        		Log.d("Connection error", ""+e.getMessage());// sign_in_progress.
	        	}
	        }
	      }
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	//android.R.drawable.ic
	    try {
			EditText numText = (EditText) findViewById(R.id.pnumber);
			outState.putCharSequence("client_number", numText.getText().toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(LOG_TAG, " "+e.getLocalizedMessage());
		}
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState){
		EditText numberTextView = (EditText) findViewById(R.id.pnumber);
		numberTextView.setText(savedInstanceState.getCharSequence("client_number"));
		
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		 if (!mIntentInProgress && result.hasResolution()) {
			    try {
			      mIntentInProgress = true;
			      sign_in_progress.dismiss();
			      //launch an intent that will attempt to resolve connection issues
			      startIntentSenderForResult(result.getResolution().getIntentSender(),
			          RC_SIGN_IN, null, 0, 0, 0);
			    } catch (SendIntentException e) {
			      // The intent was canceled before it was sent.  Return to the default
			      // state and attempt to connect to get an updated ConnectionResult.
			      mIntentInProgress = false;
			     // mGoogleApiClient.connect();
			    }
			    catch(Exception e)
			    {
			    	e.printStackTrace();
			    	Toast.makeText(getApplicationContext(), "Exception: "+e.getMessage(), Toast.LENGTH_LONG).show();
			    }
			  }
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		sign_in_progress.dismiss();
		settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String name = "Welcome ", email, number;
		if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) 
		{
		    Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
		    String personName = currentPerson.getDisplayName();
		    name += personName;
		    number = pnumber.getText().toString();
		    email = currentPerson.getId();
		    settings.edit()
			.putString("contact", personName)
			.putString("cemail", email)
			.putFloat("cnumber", Integer.parseInt(number))
			.putBoolean("registered", true)
			.commit();
		}
		mSignInClicked = true;
		Intent go = new Intent(this, CategoryActivity.class);
		//go.putExtra("usrname", name);
		startActivity(go);
		Toast.makeText(this, name, Toast.LENGTH_LONG).show();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId())
		{
		case R.id.sign_in_button:
			//sign user in using the googleAuth object
			int available = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            if (available != ConnectionResult.SUCCESS) {
                showDialog(DIALOG_GET_GOOGLE_PLAY_SERVICES);
                return;
            }
            else{
            	if(pnumber.getText().toString().isEmpty() || pnumber.getText().toString().length() < 9){
            		Toast.makeText(getApplicationContext(), "Enter a valid Phone number to use g+ signin", Toast.LENGTH_LONG).show();
            	}else{
            		mGoogleApiClient.connect();
            		sign_in_progress = ProgressDialog.show(this, "Signing In", "Connecting to Google.\n Please Wait ...");
            	}
            }
			
			break;
		case R.id.signUp: /* Create signup Account */
			signupDialogBuilder = new AlertDialog.Builder(this);
			signupDialogBuilder.setIcon(R.drawable.ic_launcher1)
							   .setTitle("Hivote Plus Signup ")
							   .setView(LayoutInflater.from(this).inflate(R.layout.form, null))
							   .setCancelable(false)
							   .setPositiveButton("Signup", this)
							   .setNegativeButton("Cancel", this);
								
			signupDialog = signupDialogBuilder.create();
			signupDialog.show();
			break;
		default: 
			break;
		}
	}

	public void fbLogin(View v){
		//log in user using facebook's API or SDK
		Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onConnectionSuspended(int cause) {

		switch(cause){
		case GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST:
			Toast.makeText(this, "Connection Suspended. "+getResources().getString(R.string.common_google_play_services_network_error_text),
					Toast.LENGTH_LONG).show();
			break;
		case GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED:
			Toast.makeText(this, "ConnectionSuspended. "+getResources().getString(R.string.common_google_play_services_unknown_issue)+".\nService has disconnected",
					Toast.LENGTH_LONG).show();	
			break;
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
	        if (id != DIALOG_GET_GOOGLE_PLAY_SERVICES) {
	            return super.onCreateDialog(id);
	        }
	        //checks for the availability of google play services on user's device
	        //creates a dialog box that enables the user to be able to install play servies 
	        // on their devices.
	        int available = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	        if (available == ConnectionResult.SUCCESS) {
	            return null;
	        }
	        if (GooglePlayServicesUtil.isUserRecoverableError(available)) {
	            return GooglePlayServicesUtil.getErrorDialog(
	                    available, this, REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES);
	        }
	        return new AlertDialog.Builder(this)
	                .setMessage(R.string.plus_generic_error)
	                .setCancelable(true)
	                .create();
	    }

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if(which == DialogInterface.BUTTON_POSITIVE){
			//start signupTask
			pb = new ProgressDialog(this);
			pb.setCancelable(false);
			pb.setMessage("Signing up. Please wait ...");
			pb.show();
			EditText user = (EditText) signupDialog.findViewById(R.id.vname);
			EditText em = (EditText) signupDialog.findViewById(R.id.vemail);
			EditText phon = (EditText) signupDialog.findViewById(R.id.vnumber);
			String username = user.getText().toString();
			String email = em.getText().toString();
			String phone = phon.getText().toString();
			String id = telmanager.getDeviceId();
			JSONObject js = new JSONObject();
			if(username.length() < 4){
				Toast.makeText(getApplicationContext(), "Username must be greater than 4 characters", Toast.LENGTH_SHORT).show();
				signupDialog.show();
				user.requestFocus();
				user.setError("Invalid username");
			}else if(!email.contains("@")){
				Toast.makeText(getApplicationContext(), "Invalid email. Please try again", Toast.LENGTH_SHORT).show();
				signupDialog.show();
				em.requestFocus();
				em.setError("Invalid email");
			}else if(!TextUtils.isDigitsOnly(phone) || phone.length() < 8){
				Toast.makeText(getApplicationContext(), "Invalid phone number. Please try again", Toast.LENGTH_SHORT).show();
				signupDialog.show();
				phon.requestFocus();
				phon.setError("Invalid phone");
			}else
			try { //parameters used by the server side to register new user
				js.put("username", username);
				js.put("email", email);
				js.put("phone", phone);
				js.put("did", id);

				SignupTask signup = new SignupTask(getApplicationContext());
				signup.execute(js.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "Invalid input. Please try again", Toast.LENGTH_SHORT).show();
			}
					
		}else{
			dialog.dismiss();
		}
	}
}
