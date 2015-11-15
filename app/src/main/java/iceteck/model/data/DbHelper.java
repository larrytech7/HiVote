package iceteck.model.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper{
	
//	public static final String DB_NAME = "Candidate.sqlite";
	public static final String DB_NAME = "ivote.db";
	private static final int DB_VERSION = 4;
	private final String CREATE_CATEGORY_SQL = "CREATE TABLE "+ORMModel.Category.TABLE_NAME+"("+
			ORMModel.Category.COLUM_ID+" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "+
			ORMModel.Category.COLUMN_TITLE+" TEXT NOT NULL,"+
			ORMModel.Category.COLUMN_DATE+" DATE NULL,"+
			ORMModel.Category.COLUMN_BITMAP_LOCATION+" TEXT NOT NULL,"+
			ORMModel.Category.COLUMN_DESCRIPTION+" TEXT NOT NULL,"+ 
			ORMModel.Category.COLUMN_AUTHOR+" TEXT NULL,UNIQUE("+ORMModel.Category.COLUM_ID+") );";
	private final String CREATE_NOMINEE_SQL = "CREATE TABLE "+ORMModel.Nominee.TABLE_NAME+"("+
			ORMModel.Nominee.COLUM_ID_CAT+" INTEGER NOT NULL,"+
			ORMModel.Nominee.COLUM_ID+" INTEGER NOT NULL PRIMARY KEY, "+
			ORMModel.Nominee.COLUMN_NAME+" TEXT NOT NULL,"+
			ORMModel.Nominee.COLUMN_BITMAP_LOCATION+" TEXT NOT NULL,"+
			ORMModel.Nominee.COLUMN_PORTFOLIO+" TEXT NOT NULL,"+
			ORMModel.Nominee.COLUMN_VOTES+" INTEGER NOT NULL,"+
			ORMModel.Nominee.COLUMN_URL_LINK+" TEXT NOT NULL, UNIQUE("+ORMModel.Nominee.COLUM_ID+"), "
					+ "FOREIGN KEY("+ORMModel.Nominee.COLUM_ID_CAT+") REFERENCES "+ORMModel.Category.TABLE_NAME+
					" ("+ORMModel.Category.COLUM_ID+") );";

	public DbHelper(Context context) {
		super(context, DB_NAME , null, DB_VERSION);		
	}
	
	
	@Override
	public void onCreate(SQLiteDatabase sqliteDb) {
		sqliteDb.execSQL(CREATE_CATEGORY_SQL);
		sqliteDb.execSQL(CREATE_NOMINEE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int arg1, int arg2) {
			database.execSQL("DROP TABLE IF EXISTS "+ORMModel.Category.TABLE_NAME);
			database.execSQL("DROP TABLE IF EXISTS "+ORMModel.Nominee.TABLE_NAME);
			onCreate(database);
	}
	
}



