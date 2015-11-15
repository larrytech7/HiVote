package iceteck.model.data;

import iceteck.model.data.ORMModel.Category;
import iceteck.model.data.ORMModel.Nominee;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MContentProvider extends ContentProvider{
		
	private static final int NOMINEE  =  1;
	private static final int NOMINEE_ID  =  2;
	private static final int CATEGORY = 3;
	private static final int CATEGORY_ID = 4;
	private DbHelper database;
	private SQLiteDatabase sqliteDB;

	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static{
		uriMatcher.addURI(ORMModel.AUTHORITY, ORMModel.Nominee.TABLE_NAME, NOMINEE);
		uriMatcher.addURI(ORMModel.AUTHORITY, ORMModel.Nominee.TABLE_NAME+"/#",NOMINEE_ID );
		uriMatcher.addURI(ORMModel.AUTHORITY, ORMModel.Nominee.TABLE_NAME+"/*",NOMINEE_ID );
		uriMatcher.addURI(ORMModel.AUTHORITY, ORMModel.Category.TABLE_NAME, CATEGORY);
		uriMatcher.addURI(ORMModel.AUTHORITY, ORMModel.Category.TABLE_NAME+"/#", CATEGORY_ID);
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] arg2) {
		// TODO Auto-generated method stub
		sqliteDB = database.getWritableDatabase();
		switch(uriMatcher.match(uri)){
		case NOMINEE:
			return sqliteDB.delete(Nominee.TABLE_NAME, null, null);
		case NOMINEE_ID:
			return sqliteDB.delete(Nominee.TABLE_NAME, Nominee.COLUM_ID+"= ?", new String[]{uri.getLastPathSegment()});
		case CATEGORY:
			return sqliteDB.delete(Category.TABLE_NAME, null, null);
		case CATEGORY_ID:
			return sqliteDB.delete(Category.TABLE_NAME, Category.COLUM_ID+"= ?", new String[]{uri.getLastPathSegment()});
		default:
				return 0;
		}
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		sqliteDB = database.getWritableDatabase();
		long id = -1 ;
		switch(uriMatcher.match(uri)){
		case NOMINEE:
			id = sqliteDB.insert(ORMModel.Nominee.TABLE_NAME, null, values); 
			if(id > 0){
			return  ORMModel.Nominee.buildNomineeUri(id);
			}else{
				throw new android.database.SQLException("Failed to insert into: "+uri);
			}
			
		case CATEGORY:
			id = sqliteDB.insert(ORMModel.Category.TABLE_NAME, null, values);
			if(id>0){
			return ORMModel.Category.buildCategoryUri(id);
			}else{
				throw new android.database.SQLException("Failed to insert into: "+uri);
			}
			
		default:
			throw new UnsupportedOperationException("Unknown URI: "+uri);
		}	
	}

	@Override
	public boolean onCreate() {
		 database = new DbHelper(getContext());
		 return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder(); 
		//sqlBuilder.setTables(Candidate.TABLE_NAME);
		
		 int uriType = uriMatcher.match(uri);
		    switch (uriType) {
		    case NOMINEE: //get a list of all nominees in the database
		    	/*sqlBuilder.setTables(Category.TABLE_NAME+", "+Nominee.TABLE_NAME);
		    	sqlBuilder.setTables(ORMModel.Category.TABLE_NAME+" LEFT OUTER JOIN "+ORMModel.Nominee.TABLE_NAME+
		    			" ON ("+ORMModel.Category.TABLE_NAME+"."+ORMModel.Category.COLUM_ID+" = "+
		    			ORMModel.Nominee.COLUM_ID_CAT+")");*/
		    	sqlBuilder.setTables(ORMModel.Nominee.TABLE_NAME);
		      break;
		    case NOMINEE_ID: //gets all the nominees belonging to a certain category id
		      // adding the ID to the original query
		      sqlBuilder.setTables(ORMModel.Nominee.TABLE_NAME);
		      sqlBuilder.appendWhere(ORMModel.Nominee.COLUM_ID_CAT + "="
		          + uri.getLastPathSegment());
		      break;
		    case CATEGORY: //get a list of all categories
		    	sqlBuilder.setTables(ORMModel.Category.TABLE_NAME);
		    	break;
		    case CATEGORY_ID: //get the category from a given id
		    	sqlBuilder.setTables(ORMModel.Category.TABLE_NAME);
		    	sqlBuilder.appendWhere(ORMModel.Category.COLUM_ID +" = "
		    	+uri.getLastPathSegment());
		    	break;
		    default:
		      throw new IllegalArgumentException("Unknown URI: " + uri);
		    }
		
		SQLiteDatabase db = database.getWritableDatabase();
		Cursor  cursor  =  sqlBuilder.query(db, projection, selection, selectionArgs, null,
				null, sortOrder);
				cursor.setNotificationUri(getContext().getContentResolver(), uri); 
				return cursor;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = database.getWritableDatabase();
		final int match = uriMatcher.match(uri);
		int rowsUpdated = 0;
		switch(match){
		case NOMINEE_ID:
			rowsUpdated = db.update(ORMModel.Nominee.TABLE_NAME,
					values, selection, selectionArgs);
			break;
		case CATEGORY:
			rowsUpdated = db.update(ORMModel.Category.TABLE_NAME,
					values, selection, selectionArgs);
			break;
			default:
				throw new UnsupportedOperationException("unknown URI  "+uri);				
		}
		if(null == selection || 0 != rowsUpdated){
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowsUpdated;
	}
	//optimal way to do multiple insert into the database. Efficiency increases as size of the data to be inserted increases
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values){
		SQLiteDatabase db = database.getWritableDatabase();
		switch(uriMatcher.match(uri)){
		case CATEGORY:
			db.beginTransaction();
			int insertCount =  0;
			try{
			for(ContentValues contentValue: values){
				long id = db.insert(ORMModel.Category.TABLE_NAME, null, contentValue);
				if(-1 != id){
					insertCount++;
					}
				}
			db.setTransactionSuccessful();
			}finally{
				db.endTransaction();
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return insertCount;
		case NOMINEE:
			db.beginTransaction();
			int nCount =  0;
			try{
			for(ContentValues contentValue: values){
				long id = db.insert(ORMModel.Nominee.TABLE_NAME, null, contentValue);
				if(-1 != id){
					nCount++;
				}else{
					id = update(Nominee.buildNomineeUri(contentValue.getAsInteger(Nominee.COLUM_ID)), contentValue, Nominee.COLUM_ID
							, new String[]{Long.toString(contentValue.getAsInteger(Nominee.COLUM_ID))});
				}
			}
			db.setTransactionSuccessful();
			}finally{
				db.endTransaction();
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return nCount;
		default:
			return super.bulkInsert(uri, values);
		}
	}

}
