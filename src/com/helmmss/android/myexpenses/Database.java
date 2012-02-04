package com.helmmss.android.myexpenses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "myexpenses.db";
	private static final int DB_VERSION = 1;
	private static Context context = null;
	
	public Database(Context context) {
		super(context, DB_NAME, null, DB_VERSION);  // creates the database (postponed creation)
		Database.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase dbCon) {
		
		ExpenseTable.createTable(dbCon, context);
		CategoryTable.createTable(dbCon, context);
		DateIntervalTable.createTable(dbCon, context);
	}

	@Override
	public void onUpgrade(SQLiteDatabase dbCon, int oldVersion, int newVersion) {
		
	}
}
