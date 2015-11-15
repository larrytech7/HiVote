package com.iceteck.hivoteplus;

import iceteck.model.data.ORMModel.Category;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Get data from content provider or database and bind it to our ui
 * @author Larry Akah N
 */
public class CategoryAdapter extends CursorAdapter{
	
	public CategoryAdapter(Context context, Cursor cursor,int flags) {
		super(context, cursor, flags);
	}
	
	// Read from the cursor and bind data to view in our layout
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		//retrieve holder from view passed
		ViewHolder vHolder = (ViewHolder) view.getTag();
		
		final Context ctx = context;
		//temporally use image placeholder 
		//get the id of the category
		final int category_id = cursor.getInt(CategoryActivity.CATEGORY_ID);
		//read category title from cursor
		final String categoryTitle = cursor.getString(CategoryActivity.CATEGORY_TITLE);
			vHolder.titleView.setText(categoryTitle);
		String cdate = cursor.getString(CategoryActivity.CATEGORY_DATE);
			vHolder.dateTextview.setText("Date: "+cdate);
		final String description = cursor.getString(CategoryActivity.CATEGORY_DESC);
		String author = cursor.getString(cursor.getColumnIndex(Category.COLUMN_AUTHOR));
		vHolder.authorTextview.setText(context.getResources().getString(R.string.author)+" "+author);
		//find out more about the given category
			vHolder.btnLearnmore.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					showMore(ctx, categoryTitle, description);
				}
			});
			//select the given category to view and choose a nominee
			vHolder.btnView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int _id = category_id;
					CategoryActivity.currentCategory = categoryTitle;
					/*
					if(c.moveToFirst() && c.moveToPosition(pos)){
						_id = c.getInt(0);
					}*/
					//we could launch the respective category here
					Intent it = new Intent(ctx, NomineeActivity.class)
								.putExtra("category", CategoryActivity.currentCategory)
								.putExtra("c_id", _id);
					ctx.startActivity(it);
					((Activity) ctx).overridePendingTransition(android.R.anim.slide_in_left,0);
				}
			});
			//delete the category from the database
			vHolder.iconDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					alertDeleteAction(ctx, categoryTitle, category_id);
				}
			});
			//allow admin to close the election such that no one can vote any longer
			vHolder.toggleStatus.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					if(!isChecked){
						buttonView.setButtonDrawable(android.R.drawable.presence_offline);
//						buttonView.setCompoundDrawablesRelativeWithIntrinsicBounds(android.R.drawable.presence_offline, 0, 0, 0);
						//TODO
						//deactivateVoting(eventID);
					}else{
						buttonView.setButtonDrawable(android.R.drawable.presence_online);
//						buttonView.setCompoundDrawablesRelativeWithIntrinsicBounds(android.R.drawable.presence_online, 0, 0, 0);
					}
				}
			});
	}

	private void showMore(Context ctx, String title, String info){
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
		.setIcon(ctx.getResources().getDrawable(R.drawable.ic_launcher1))
		.setTitle(title)
		.setMessage(info);
		builder.create().show();
	}
	
	private void alertDeleteAction(final Context ctx, final String itemname, final long category_id){
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
		.setIcon(ctx.getResources().getDrawable(R.drawable.ic_launcher1))
		.setTitle("Confirmation")
		.setMessage("Are you sure you really want to delete: "+itemname+" ?\n Note! This election is deleted only on your device.")
		.setPositiveButton("YES", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int deleted = ctx.getContentResolver().delete(Category.buildCategoryUri(category_id), null, null);
				if(deleted > 0){
					ctx.getContentResolver().notifyChange(Category.CONTENT_URI, null);
					//registerDataSetObserver(dos);
					Toast.makeText(ctx, "Sucessfully Deleted "+itemname, Toast.LENGTH_LONG).show();
				}
			}
		})
		.setNegativeButton("NO", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(ctx, "Cancelled ", Toast.LENGTH_SHORT).show();
			}
		});
		builder.create().show();
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		
		View newView = LayoutInflater.from(context).inflate(R.layout.list_item_categories, parent, false);
		ViewHolder vHolder = new ViewHolder(newView);
		newView.setTag(vHolder);
		return newView;
	}

	public static class ViewHolder{
		public final ImageView iconView;
		public final TextView titleView;
		public final TextView dateTextview,authorTextview;
		public final Button btnLearnmore, btnView, iconDelete;
		public final ToggleButton toggleStatus;
		
		public ViewHolder(View view){
			iconView = (ImageView) view.findViewById(R.id.category_icon);
			titleView = (TextView) view.findViewById(R.id.category_title_textview);
			dateTextview = (TextView) view.findViewById(R.id.category_date);
			authorTextview = (TextView) view.findViewById(R.id.textViewAuthor);
			btnLearnmore = (Button) view.findViewById(R.id.btn_learnmore);
			btnView = (Button) view.findViewById(R.id.btn_enter);
			iconDelete = (Button) view.findViewById(R.id.buttonCategoryDelete);
			toggleStatus = (ToggleButton) view.findViewById(R.id.toggleElectionState);
		}
	}

}
