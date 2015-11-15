package com.iceteck.hivoteplus;

import java.util.ArrayList;

import com.koushikdutta.ion.Ion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentAdapter extends BaseAdapter {

	private Context ctx;
	private ArrayList<String[]> adapterData;
	private View comment_layout;
	
	public CommentAdapter(Context context, ArrayList<String[]> data){
		ctx= context;
		adapterData = data;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return adapterData.size();
	}

	@Override
	public String[] getItem(int position) {
		// TODO Auto-generated method stub
		return adapterData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		/*if(convertView != null){
			return convertView;
		}else{*/
		comment_layout = this.getCount()>0 ? LayoutInflater.from(ctx).inflate(R.layout.item_layout_comments, parent, false):
			                              LayoutInflater.from(ctx).inflate(R.layout.list_item_comments_err, parent, false);
		
		if(null != comment_layout.findViewById(R.id.errTextView)){
			
		}else{
		String[] mydata = getItem(position);
		ImageView imgThumb = (ImageView) comment_layout.findViewById(R.id.comment_thumbnail);
		
		Ion.with(imgThumb)
		.placeholder(R.drawable.loading)
		.error(android.R.drawable.ic_menu_report_image)
		.animateLoad(R.anim.abc_slide_in_bottom)
		.animateIn(R.anim.abc_slide_in_top)
		.load("http://ubconnect.webatu.com/images/user.png");
		
		TextView usernametextview = (TextView) comment_layout.findViewById(R.id.username_textview);
			usernametextview.setText(mydata[0]);
		TextView commenttextview = (TextView) comment_layout.findViewById(R.id.comment_textview);
			commenttextview.setText(mydata[2]);
		TextView datetextview = (TextView) comment_layout.findViewById(R.id.date_textview);
			datetextview.setHint(mydata[1]);
		}
			return comment_layout;
//		}
	}
	
	 static class ViewHolder {
		public final ImageView userThumbnail;
		public final TextView username, usercomment, commentdate;
	
		public ViewHolder(View view){
			userThumbnail = (ImageView) view.findViewById(R.id.imageView1);
			username = (TextView) view.findViewById(R.id.username_textview);
			usercomment = (TextView) view.findViewById(R.id.comment_textview);
			commentdate = (TextView) view.findViewById(R.id.date_textview);
		}
	}
}
