package com.helmmss.android.myexpenses;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CategoryTable {
	
	private static final String TABLE_NAME = "category";
	
	public class Column {
		
		static final String ID = "_id";
		static final String TYPE = "type";
		static final String NAME = "name";
	}
	
	private class ColumnPosition {
		
		static final int ID = 0;
		static final int TYPE = 1;
		static final int NAME = 2;
	}
	
	private class CategoryType {
		
		static final int USERDEFINED = 0;
	}
	
	public static void createTable(SQLiteDatabase dbCon, Context context) {
		
		createTable(dbCon);
		initTable(dbCon, context);
	}
	
	private static void createTable(SQLiteDatabase dbCon) {
		
		// create Table
		String stmt = 
			"CREATE TABLE category (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"type INTEGER, " +
			"name TEXT NOT NULL" +
			");";
		dbCon.execSQL(stmt);
	}
	
	private static void initTable(SQLiteDatabase dbCon, Context context) {
		
		// insert some sample "user defined" categories 
		// (they can be deleted/changed by user later) 
		
		Resources r = context.getResources();
		
		insertCategory(dbCon, r.getString(R.string.category_sample_health));
		insertCategory(dbCon, r.getString(R.string.category_sample_food));
		insertCategory(dbCon, r.getString(R.string.category_sample_presents));
		insertCategory(dbCon, r.getString(R.string.category_sample_furnishings));
		insertCategory(dbCon, r.getString(R.string.category_sample_doItYourself));
		insertCategory(dbCon, r.getString(R.string.category_sample_parkingFee));
		insertCategory(dbCon, r.getString(R.string.category_sample_children));
		insertCategory(dbCon, r.getString(R.string.category_sample_donation));
		insertCategory(dbCon, r.getString(R.string.category_sample_leisure));
		insertCategory(dbCon, r.getString(R.string.category_sample_restaurant));
		insertCategory(dbCon, r.getString(R.string.category_sample_miscellaneous));
		insertCategory(dbCon, r.getString(R.string.category_sample_meansForWork));
		insertCategory(dbCon, r.getString(R.string.category_sample_books));
		insertCategory(dbCon, r.getString(R.string.category_sample_icecream));
	}
	
	public static Cursor getCategoryCursor(SQLiteDatabase dbCon) {
		
		Cursor c = null;
		
		try {

			String stmt = "SELECT * FROM category ORDER BY name ASC";
			c = dbCon.rawQuery(stmt, new String[] { });
			
		} finally {
//			c.close();  // don't close because cursor gets returned!
		}
		
		return c;
	}
		
	private static long insertCategory(SQLiteDatabase dbCon, int type, String name) {

		ContentValues values = new ContentValues();	
		values.put(Column.TYPE, type);
		values.put(Column.NAME, name);
		return dbCon.insert(TABLE_NAME, null, values);
	}
	
	public static long insertCategory(SQLiteDatabase dbCon, String name) {
		
		return insertCategory(dbCon, CategoryType.USERDEFINED, name);
	}
	
	public static int updateCategory(SQLiteDatabase dbCon, long categoryID, String name) {

		ContentValues values = new ContentValues();
		values.put(Column.NAME, name);
		
		return dbCon.update(TABLE_NAME, values, 
				Column.ID + " = ?", // where
				new String[] { String.valueOf(categoryID) } // whereArgs
		);
	}
	
	public static int deleteCategory(SQLiteDatabase dbCon, long categoryID) {
			
		return dbCon.delete(TABLE_NAME, 
				Column.ID + " = ?", // where
				new String[] { String.valueOf(categoryID) } // whereArgs
		);
	}
	
	public static Categories getCategories(SQLiteDatabase dbCon) {
		 
		Cursor c = null;

		try {
			
			String stmt = "SELECT * FROM category ORDER BY name ASC";
			c = dbCon.rawQuery(stmt, new String[] { });

			int size = c.getCount();
			
			String[] names = new String[size];
			long[] ids = new long[size];
			
			c.moveToFirst();
			
			for (int i = 0; i < ids.length; i++, c.moveToNext()) {
				ids[i] = c.getLong(ColumnPosition.ID);
				names[i] = c.getString(ColumnPosition.NAME);
			}

			return new Categories(ids, names);

		} finally {
			c.close();
		}	
	}
	
	public static Category getCategory(SQLiteDatabase dbCon, long categoryId) {

		String stmt = "SELECT * FROM category WHERE _id = " + String.valueOf(categoryId);	
		return getCategory(dbCon, stmt);
	}
	
	private static Category getCategory(SQLiteDatabase dbCon, String stmt) {
		
		Cursor c = null;
		
		try {

			long id = -1;
			boolean userDefined = false;
			String name = null;
			
			c = dbCon.rawQuery(stmt, null);
			
			if (c.moveToFirst()) {
				id = c.getLong(ColumnPosition.ID);
				userDefined = c.getInt(ColumnPosition.TYPE) == CategoryType.USERDEFINED;
				name = c.getString(ColumnPosition.NAME);
			}
						
			return new Category(id, name, userDefined);

		} finally {
			c.close();
		}	
	}
}
