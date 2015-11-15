package com.iceteck.hivoteplus;

import iceteck.model.data.ORMModel;
import iceteck.model.data.ORMModel.Nominee;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class NomineeAdapter extends CursorAdapter {

	public NomineeAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, flags);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		//retrieve holder from view passed
		ViewHolder vHolder = (ViewHolder) view.getTag();		
		//temporally use image placeholder 
		vHolder.iconView.setImageResource(R.drawable.ic_launcher5);
		//read category title from cursor
		String nomineeTitle = cursor.getString(cursor.getColumnIndex(ORMModel.Nominee.COLUMN_NAME));
		int nomineevotes = cursor.getInt(cursor.getColumnIndex(Nominee.COLUMN_VOTES));
		vHolder.titleView.setText(nomineeTitle);
		String voteresult = nomineevotes != 1 ? nomineevotes+" votes": nomineevotes+" vote";
		vHolder.votecountText.setText(voteresult);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		View newView = LayoutInflater.from(context).inflate(R.layout.list_item_nominee, parent, false);
		ViewHolder vHolder = new ViewHolder(newView);
		newView.setTag(vHolder);
		return newView;
	}
	
	public static class ViewHolder{
		public final ImageView iconView;
		public final TextView titleView, votecountText;
		
		public ViewHolder(View view){
			iconView = (ImageView) view.findViewById(R.id.imageView1);
			titleView = (TextView) view.findViewById(R.id.nominee_name_textview);
			votecountText = (TextView) view.findViewById(R.id.textViewResults);
		}
	}
	
}
