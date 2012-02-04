package com.helmmss.android.myexpenses;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ExpenseTable {
	
	private static final String TABLE_NAME = "expense";
	
	public class Column {
		static final String ID = "_id";
		static final String DATE = "date";
		static final String AMOUNT = "amount";
		static final String CATEGORY = "category";
		static final String DESCRIPTION = "description";
		static final String TYPE = "type";
	}

	private class ColumnPosition {
		static final int ID = 0;
		static final int DATE = 1;
		static final int AMOUNT = 2;
		static final int CATEGORY = 3;
		static final int DESCRIPTION = 4;
		static final int TYPE = 5;
	}
	
	public class MethodOfPayment {
		static final String CASH = "cash";
		static final String CARD = "card";
	}

	public static void createTable(SQLiteDatabase dbCon, Context context) {
		
		createTable(dbCon);
		initTable(dbCon, context);
	}
	
	private static void createTable(SQLiteDatabase dbCon) {
		
		// create Table
		String stmt = 
			"CREATE TABLE expense (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
			"date TEXT NOT NULL," +
			"amount NUMERIC NOT NULL," +
			"category TEXT," +
			"description TEXT," +
			"type TEXT" +
			");";
		dbCon.execSQL(stmt);
	}
	
	private static void initTable(SQLiteDatabase dbCon, Context context) { 
		
	}

	public static long insertExpense(SQLiteDatabase dbCon, Calendar date, String amout, String category, String description, String type) {
			
		ContentValues values = new ContentValues();	
		values.put(Column.DATE, Util.formatDateForDB(date));
		values.put(Column.AMOUNT, amout);
		values.put(Column.CATEGORY, category);
		values.put(Column.DESCRIPTION, description);
		values.put(Column.TYPE, type);
		
		return dbCon.insert(TABLE_NAME, null, values);
	}
	
	public static long insertExpense(SQLiteDatabase dbCon, Expense expense) {
		
		ContentValues values = new ContentValues();
		values.put(Column.DATE, Util.formatDateForDB(expense.date));
		values.put(Column.AMOUNT, expense.amount);
		values.put(Column.CATEGORY, expense.category);
		values.put(Column.DESCRIPTION, expense.description);
		values.put(Column.TYPE, expense.type);
		
		return dbCon.insert(TABLE_NAME, null, values);
	}

	public static long updateExpense(SQLiteDatabase dbCon, Calendar date, String amout, String category, String description, String type, long expenseID) {
	
		ContentValues values = new ContentValues();
		values.put(Column.DATE, Util.formatDateForDB(date));
		values.put(Column.AMOUNT, amout);
		values.put(Column.CATEGORY, category);
		values.put(Column.DESCRIPTION, description);
		values.put(Column.TYPE, type);
		
		return dbCon.update(TABLE_NAME, values, 
				Column.ID + " = ?", 
				new String[] { String.valueOf(expenseID) } 
		);
	}

	public static long updateExpense(SQLiteDatabase dbCon, Expense expense) {
		
		ContentValues values = new ContentValues();
		values.put(Column.DATE, Util.formatDateForDB(expense.date));
		values.put(Column.AMOUNT, expense.amount);
		values.put(Column.CATEGORY, expense.category);
		values.put(Column.DESCRIPTION, expense.description);
		values.put(Column.TYPE, expense.type);
		
		return dbCon.update(TABLE_NAME, values, 
				Column.ID + " = ?", 
				new String[] { String.valueOf(expense.id) } 
		);
	}

	public static int deleteExpense(SQLiteDatabase dbCon, long expenseID) {
		
		return dbCon.delete(TABLE_NAME, 
				Column.ID + " = ?", 
				new String[] { String.valueOf(expenseID) } 
		);
	}
	
	public static int deleteExpense(SQLiteDatabase dbCon, 
			DateInterval dateRange, 
			String typeSelector, String categorySelector, String descriptionSelector, Calendar dateSelector) {
		
		typeSelector = typeSelector != null ? typeSelector : "%"; 
		categorySelector = categorySelector != null ? categorySelector : "%"; 
		descriptionSelector = descriptionSelector != null ? descriptionSelector : "%"; 

		String from = null;
		String to = null;

		if (dateSelector == null) {
			from = Util.formatDateForDB(dateRange.from);
			to = Util.formatDateForDB(dateRange.to);
		}
		else {
			from = Util.formatDateForDB(dateSelector);
			to = Util.formatDateForDB(dateSelector);
		}

		return dbCon.delete(TABLE_NAME, 
				"date >= ? AND date <= ? AND type LIKE ? AND category LIKE ? AND description LIKE ? ",
				new String[] {from, to, typeSelector, categorySelector, descriptionSelector } 
		);
	}
		
	public static Expense getExpense(SQLiteDatabase dbCon, long expenseId) {
		
		Cursor c = null;
		
		try {
			
			String stmt = "SELECT * FROM expense WHERE _id = " + String.valueOf(expenseId);	
			c = dbCon.rawQuery(stmt, null);
			
			if (c.moveToFirst()) {
			
				return getExpense(c);
			}
			else {

				return null;
			}
		}
			
		finally {
			c.close();
		}
	}
	
	public static Expense getExpense(Cursor c) {
		
		return new Expense(
			c.getLong(ColumnPosition.ID),
			Util.parseDateString(c.getString(ColumnPosition.DATE)),
			c.getString(ColumnPosition.AMOUNT),
			c.getString(ColumnPosition.CATEGORY),
			c.getString(ColumnPosition.DESCRIPTION),
			c.getString(ColumnPosition.TYPE)
		);
	}

	public static Cursor getExpenseCursor(SQLiteDatabase dbCon, 
			DateInterval dateRange, 
			String typeSelector, String categorySelector, String descriptionSelector, Calendar dateSelector) {
		
		Cursor c = null;
		
		typeSelector = typeSelector != null ? typeSelector : "%"; 
		categorySelector = categorySelector != null ? categorySelector : "%"; 
		descriptionSelector = descriptionSelector != null ? descriptionSelector : "%"; 
		
		try {
			
			String from = null;
			String to = null;

			if (dateSelector == null) {
				from = Util.formatDateForDB(dateRange.from);
				to = Util.formatDateForDB(dateRange.to);
			}
			else {
				from = Util.formatDateForDB(dateSelector);
				to = Util.formatDateForDB(dateSelector);
			}

			String stmt = "SELECT * FROM expense WHERE date >= ? AND date <= ? AND type LIKE ? AND category LIKE ? AND description LIKE ? ORDER BY date DESC, _id DESC ";
						
			c = dbCon.rawQuery(stmt, new String[] {from, to, typeSelector, categorySelector, descriptionSelector});
						
		} finally {
//			c.close();  // don't close because c gets returned!
		}
		
		return c;
	}

	public static double getSumOfExpenses(SQLiteDatabase dbCon, 
			DateInterval dateRange, 
			String typeSelector, String categorySelector, String descriptionSelector, Calendar dateSelector) {

		Cursor c = null;
		
		typeSelector = typeSelector != null ? typeSelector : "%"; 
		categorySelector = categorySelector != null ? categorySelector : "%"; 
		descriptionSelector = descriptionSelector != null ? descriptionSelector : "%"; 

		try {
			
			String from = null;
			String to = null;

			if (dateSelector == null) {
				from = Util.formatDateForDB(dateRange.from);
				to = Util.formatDateForDB(dateRange.to);
			}
			else {
				from = Util.formatDateForDB(dateSelector);
				to = Util.formatDateForDB(dateSelector);
			}
			
			String stmt = "SELECT total(amount) FROM expense WHERE date >= ? AND date <= ? AND type LIKE ? AND category LIKE ? AND description LIKE ? ";
			
			c = dbCon.rawQuery(stmt, new String[] {from, to, typeSelector, categorySelector, descriptionSelector });
			
			if (c.moveToFirst()) {
				return c.getDouble(0);
			}
			else {
				return 0D;
			}
		}

		finally {
			c.close();
		}
	}
	
	public static final String getDateOfEldestExpense(SQLiteDatabase dbCon) {
	
		Cursor c = null;
		
		try {

			c = dbCon.rawQuery("SELECT min(date) FROM expense", null);
						
			if (c.moveToFirst()) {
				return c.getString(0);
			}
			else {
				return null;
			}
		
		} finally {
			c.close();
		}
	}

	public static final String getDateOfYoungestExpense(SQLiteDatabase dbCon) {
		
		Cursor c = null;

		try {

			c = dbCon.rawQuery("SELECT max(date) FROM expense", null);
						
			if (c.moveToFirst()) {
				return c.getString(0);
			}
			else {
				return null;
			}
		
		} finally {
			c.close();
		}
	}
}
