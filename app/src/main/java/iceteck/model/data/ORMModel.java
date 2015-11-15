package iceteck.model.data;

import android.content.ContentUris;
import android.net.Uri;

public class ORMModel {
	public static final String AUTHORITY = "com.icetech.provider.ivote";
	public static Uri BASE_CONTENT_URI = Uri.parse("content://"+ AUTHORITY );
	
	public static final class Category{
		///this uri fully specifies the data to search/query for. in this case we manipulate category data
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("category").build(); 
		
		public static final String CONTENT_TYPE ="vnd.android.cursor.dir/"+AUTHORITY+"/category";
		public static final String CONTENT_ITEM_TYPE ="vnd.android.cursor.item/"+AUTHORITY+"/category";
		
		public static final String TABLE_NAME = "category";
		public static final String COLUM_ID = "_id";
		public static final String COLUMN_TITLE = "title";
		public static final String COLUMN_BITMAP_LOCATION = "bitmap_location";
		public static final String COLUMN_DESCRIPTION = "description";
		public static final String COLUMN_DATE = "date";
		public static final String COLUMN_AUTHOR = "author";
		
		public static Uri buildCategoryUri(long id)
		{
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}
	}
	
	public static final class Nominee{
		//this uri fully specifies the data to search. In this case , we search for or manipulate nominees data
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("nominee").build();
		
		public static final String CONTENT_TYPE ="vnd.android.cursor.dir/"+AUTHORITY+"/nominee";
		public static final String CONTENT_ITEM_TYPE ="vnd.android.cursor.item/"+AUTHORITY+"/nominee";
		
		public static final String TABLE_NAME = "nominee";
		
		public static final String COLUM_ID_CAT = "nominee_category_id";
		public static final String COLUM_ID = "_id";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_BITMAP_LOCATION = "profile_url";
		public static final String COLUMN_PORTFOLIO = "portfolio";
		public static final String COLUMN_VOTES = "vote_count";
		public static final String COLUMN_URL_LINK = "link";
		
		public static Uri buildNomineeUri(long id)
		{
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}	
	}
}
