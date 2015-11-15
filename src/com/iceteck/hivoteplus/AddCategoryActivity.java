package com.iceteck.hivoteplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sync.HiVoteSyncAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class AddCategoryActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_category);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbarbg));
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new AddFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.add_category, menu);
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			this.overridePendingTransition(android.R.anim.fade_out,0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * A fragment containing the ui.
	 */
	public static class AddFragment extends Fragment implements OnClickListener{
		private static final int SELECT_THUMB = 250;
		View rootView;
		public int[][] nomineeIds;
		public String[][] nomineeInfo;
		private long gen_id = Math.round(Math.random());
		public static int id = 0; //the number of extra nominees
		public static ProgressDialog pb;
		ImageView thumbnail;
		Button thumbpick;
		
		public AddFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			 rootView = inflater.inflate(R.layout.fragment_add_category,
					container, false);
			 ImageView imagAddButton = (ImageView) rootView.findViewById(R.id.btnAddNominee);
			 imagAddButton.setOnClickListener(this);
			 thumbnail = (ImageView) rootView.findViewById(R.id.imageViewThumbnail);
			 thumbpick = (Button) rootView.findViewById(R.id.buttonThumbpick);
			 thumbpick.setOnClickListener(this);
			 Button addCategory = (Button) rootView.findViewById(R.id.buttonAddCategory);
			 addCategory.setOnClickListener(this);
			 nomineeIds = new int[10][2];
			 nomineeInfo = new String[10][2];
			return rootView;
		}
		
		private void addNomineeView(){
			
			//add the view fields  required to enter nominee info
			EditText namet = (EditText) rootView.findViewById(R.id.nominee1);
			EditText desct = (EditText) rootView.findViewById(R.id.nominee1description);
			LayoutParams nameLayout = namet.getLayoutParams();
			LayoutParams descLayout = desct.getLayoutParams();
			EditText nameText, descriptionText;
			gen_id = gen_id+1;
			nameText = createEditText(nameLayout);
			nameText.setHint(namet.getHint());
			nameText.setId((int)gen_id);
			nameText.setFocusable(true);nameText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
			nameText.setTextColor(Color.BLACK);
			nameText.setBackgroundColor(Color.WHITE);
			nameText.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.user), null);
			nomineeIds[id][0] = (int) gen_id; //save id of the first field
			gen_id = gen_id+2;
			descriptionText = createEditText(descLayout);
			descriptionText.setHint(desct.getHint());
			descriptionText.setId((int)gen_id);
			descriptionText.setMinLines(3);descriptionText.setTextColor(Color.BLACK);
			descriptionText.setBackgroundColor(Color.WHITE);
			descriptionText.setCompoundDrawables(null, null, getResources().getDrawable(android.R.drawable.btn_star), null);
			nomineeIds[id][1] = (int)gen_id; //save id of the second field
			System.out.println(nomineeIds[id][0]+" : "+nomineeIds[id][1]+" nominees inserted = "+id);
			id++; //increase the id counter
			try {
				((ViewGroup) rootView.findViewById(R.id.nomineeLayout)).addView(nameText, 4);//.addView(nameText,5);
				((ViewGroup) rootView.findViewById(R.id.nomineeLayout)).addView(descriptionText,5);
				nameText.requestFocus();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		/**
		 * Get data from the given ids and create a JSON compatible String
		 * @param ids the id field representing the id's of the save fields
		 * @return
		 * @throws JSONException 
		 */
		private int SaveCateogry(int[][] ids) throws JSONException, Exception{
		     
			final JSONObject myCategoryData = new JSONObject();
			JSONArray myNominee = new JSONArray();
			JSONArray ja = new JSONArray();
			nomineeInfo = new String[ids.length][2];
			String title = ((EditText)rootView.findViewById(R.id.editText1)).getText().toString();
			String desc = ((EditText)rootView.findViewById(R.id.editText2)).getText().toString();
			String nom1 = ((EditText)rootView.findViewById(R.id.nominee1)).getText().toString();
			String nom1des = ((EditText)rootView.findViewById(R.id.nominee1description)).getText().toString();
			String nom2 = ((EditText)rootView.findViewById(R.id.nominee2)).getText().toString();
			String nom2desc = ((EditText)rootView.findViewById(R.id.nominee2description)).getText().toString();
			
			title = title.replace(" " , "%20");desc = desc.replace(" " , "%20");
			nom1 = nom1.replace(" " , "%20");nom1des = nom1des.replace(" " , "%20");
			nom2 = nom2.replace(" " , "%20");nom2desc = nom2desc.replace(" " , "%20");
			nomineeInfo[0][0] = nom1;
			nomineeInfo[0][1] = nom1des;
			nomineeInfo[1][0] = nom2;
			nomineeInfo[1][1] = nom2desc;
			
			//get all the extra nominees and put them into an array. This will suffice rather than using an object
			for(int i= 0; i< id;i++){
				String n1 = ((EditText)rootView.findViewById(ids[i][0])).getText().toString();
				String n2 = ((EditText)rootView.findViewById(ids[i][1])).getText().toString();
				n1 = n1.replace(" " , "%20");
				n2 = n2.replace(" ", "%20");
				nomineeInfo[i+2][0] = n1;
				nomineeInfo[i+2][1] = n2;
			}
			for(int j =0; j< 2+id; j++){
				System.out.println(nomineeInfo[j][0] +" : "+nomineeInfo[j][1]);
				myNominee.put(j, nomineeInfo[j][0]);
				ja.put(j, nomineeInfo[j][1]);
			}
			
			myCategoryData.put("categoryAuthor", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("contact", "Google User"));
			myCategoryData.put("categoryname", title);
			myCategoryData.put("categorydescription", desc);
			myCategoryData.put("nomineenames", myNominee);
			myCategoryData.put("Nomineedescription", ja);
			AddFragment.pb = ProgressDialog.show(getActivity(), "Add Category", "Adding New Category, please wait");
			AsyncTask<Void, Void,Void> aTask = new AsyncTask<Void, Void, Void>(){ String resultStr = "";
				@Override
				protected Void doInBackground(Void... params) {
				
					URL url;
					
					String urlString = "http://icetech.webege.com/categories.php?add=1&data="+myCategoryData.toString();
					HttpURLConnection conn;
						try {
							url  = new URL(urlString);

							conn = (HttpURLConnection) url.openConnection();
							conn.setRequestMethod("GET");
							conn.setDoInput(true);
							conn.connect();
							
							StringBuffer sBuffer = new StringBuffer();
							BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
							String line;
							//read JSON store as String
							while((line = reader.readLine()) != null)
							{
								sBuffer.append(line+"\n");
							}
							resultStr = sBuffer.toString();
							if(sBuffer.length() == 0)
							{
								return null;
							}
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (ProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					Log.d(this.getClass().getName() , "Task Completed successfully\nURL="+urlString);
					return null;
				}
				@Override
				protected void onPostExecute(Void Void){
					
					try {
						if(resultStr.contains("success")){
							Toast.makeText(getActivity(), "Success", Toast.LENGTH_LONG).show();
							HiVoteSyncAdapter.synImmediately(getActivity());
							getActivity().finish();
						}else{
							Toast.makeText(getActivity(), 
						getActivity().getResources().getString(R.string.err_network), 
						Toast.LENGTH_LONG).show();
						}	
						AddFragment.pb.dismiss();
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};

			aTask.execute();
			
			return 0;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.btnAddNominee:
				addNomineeView();
				break;
			case R.id.buttonAddCategory:
				try {
					EditText title = ((EditText)rootView.findViewById(R.id.editText1));
					EditText desc = ((EditText)rootView.findViewById(R.id.editText2));
					EditText nom1 = ((EditText)rootView.findViewById(R.id.nominee1));
					EditText nom1des = ((EditText)rootView.findViewById(R.id.nominee1description));
					EditText nom2 = ((EditText)rootView.findViewById(R.id.nominee2));
					EditText nom2desc = ((EditText)rootView.findViewById(R.id.nominee2description));
					if(title.getText().toString().isEmpty()){title.setError(getActivity().getResources().getString(R.string.err_field_required));
					title.requestFocus();}
					else if(desc.getText().toString().isEmpty()){desc.setError(getActivity().getResources().getString(R.string.err_field_required));
					desc.requestFocus();}
					else if(nom1.getText().toString().isEmpty()){nom1.setError(getActivity().getResources().getString(R.string.err_field_required));
					nom1.requestFocus(); }
					else if(nom1des.getText().toString().isEmpty()){nom1des.setError(getActivity().getResources().getString(R.string.err_field_required));
					nom1des.requestFocus();}
					else if(nom2.getText().toString().isEmpty()){nom2.setError(getActivity().getResources().getString(R.string.err_field_required));
					nom2.requestFocus(); }
					else if(nom2desc.getText().toString().isEmpty()){nom2desc.setError(getActivity().getResources().getString(R.string.err_field_required));
					nom2desc.requestFocus();}
					else SaveCateogry(nomineeIds);
					
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case R.id.buttonThumbpick:
				Intent pickIntent = new Intent( Intent.ACTION_PICK);
				pickIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(Intent.createChooser(pickIntent, "Select a Thumbnail"), SELECT_THUMB);
				break;
			}
		}
		
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data){
			super.onActivityResult(requestCode, resultCode, data);
			if(requestCode == SELECT_THUMB && null != data){
				thumbnail.setImageURI(data.getData());
				Log.d("THUMB SELECT", ""+data.getExtras().getByteArray(MediaStore.EXTRA_OUTPUT));
			}
			
		}
		/**
		 * Create and return an EditText view object in which the messages of the week can be put
		 * @author Larry_Lite
		 * @param editTextlayoutparam as applied to other view
		 */
		private EditText createEditText( LayoutParams editTextlayoutparam)
		{
			EditText ed = new EditText(this.getActivity());
				ed.setLayoutParams(editTextlayoutparam);
				ed.setMinLines(2);
				ed.setMaxLines(4);
				ed.setFocusable(true);
				ed.setFocusableInTouchMode(true);
				ed.setFreezesText(true);
				
			return ed != null? ed: null;			
		}
	}

}
