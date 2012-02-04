package com.helmmss.android.myexpenses;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DateIntervalTable {
	
	private static final String TABLE_NAME = "dateInterval";
	
	public class Column {
		
		static final String ID = "_id";
		static final String TYPE = "type";
		static final String NAME = "name";
		static final String DATE_FROM = "dateFrom";
		static final String DATE_TO = "dateTo";
	}
	
	private class ColumnPosition {
		
		static final int ID = 0;
		static final int TYPE = 1;
		static final int NAME = 2;
		static final int DATE_FROM = 3;
		static final int DATE_TO = 4;
	}
	
	private class IntervalType {
		
		static final int UNDEFINED = -1;
		static final int USERDEFINED = 0;
		static final int NONE = 2;
		static final int TODAY = 3;
		static final int YESTERDAY = 4; 
		static final int LASTWEEK = 5; 
		static final int LASTMONTH = 6;
		static final int LASTYEAR = 8; 
		static final int THISWEEK = 9; 
		static final int THISMONTH = 10; 
		static final int THISYEAR = 11; 
	}
	
	public static void createTable(SQLiteDatabase dbCon, Context context) {
		
		createTable(dbCon);
		initTable(dbCon, context);
	}
	
	private static void createTable(SQLiteDatabase dbCon) {
		
		// create Table
		StringBuffer stmt = new StringBuffer();
		stmt.setLength(0);
		stmt.append("CREATE TABLE dateInterval ");
		stmt.append("(");
		stmt.append("_id INTEGER PRIMARY KEY AUTOINCREMENT, ");
		stmt.append("type INTEGER, ");
		stmt.append("name TEXT NOT NULL, ");
		stmt.append("dateFrom TEXT NOT NULL, ");
		stmt.append("dateTo TEXT NOT NULL ");
		stmt.append(");");
		dbCon.execSQL(stmt.toString());
	}
	
	private static void initTable(SQLiteDatabase dbCon, Context context) {
	
		// insert initial data	
		insertDateInterval(dbCon, context, IntervalType.NONE, R.string.dateInterval_sample_none, null, null);
		insertDateInterval(dbCon, context, IntervalType.TODAY, R.string.dateInterval_sample_today, null, null);
		insertDateInterval(dbCon, context, IntervalType.YESTERDAY, R.string.dateInterval_sample_yesterday, null, null);
		insertDateInterval(dbCon, context, IntervalType.LASTWEEK, R.string.dateInterval_sample_lastWeek, null, null);
		insertDateInterval(dbCon, context, IntervalType.LASTMONTH, R.string.dateInterval_sample_lastMonth, null, null);
		insertDateInterval(dbCon, context, IntervalType.LASTYEAR, R.string.dateInterval_sample_lastYear, null, null);
		insertDateInterval(dbCon, context, IntervalType.THISWEEK, R.string.dateInterval_sample_thisWeek, null, null);
		insertDateInterval(dbCon, context, IntervalType.THISMONTH, R.string.dateInterval_sample_thisMonth, null, null);
		insertDateInterval(dbCon, context, IntervalType.THISYEAR, R.string.dateInterval_sample_thisYear, null, null);
	}
	
	private static long insertDateInterval(SQLiteDatabase dbCon, int dateIntervalType, 
			String dateIntervalName, Calendar calendarFrom, Calendar calendarTo) {
				
		ContentValues values = new ContentValues();
		values.put(Column.TYPE, dateIntervalType);
		values.put(Column.NAME, dateIntervalName);
		values.put(Column.DATE_FROM, Util.formatDateForDB(calendarFrom));
		values.put(Column.DATE_TO, Util.formatDateForDB(calendarTo));
		
		return dbCon.insert(TABLE_NAME, null, values);
	}

	private static long insertDateInterval(SQLiteDatabase dbCon, Context context, int dateIntervalType, 
			int dateIntervalNameId, Calendar calendarFrom, Calendar calendarTo) {

		String dateIntervalName = context.getResources().getString(dateIntervalNameId);
		return insertDateInterval(dbCon, dateIntervalType, dateIntervalName, calendarFrom, calendarTo);
	}

	public static long insertDateInterval(SQLiteDatabase dbCon, 
			String dateIntervalName, Calendar calendarFrom, Calendar calendarTo) {
		
		int dateIntervalType = IntervalType.USERDEFINED;
		return insertDateInterval(dbCon, dateIntervalType, dateIntervalName, calendarFrom, calendarTo);
	}
		
	public static int updateDateInterval(SQLiteDatabase dbCon, 
			long dateIntervalId, String dateIntervalName, Calendar calendarFrom, Calendar calendarTo) {
			
		ContentValues values = new ContentValues();		
		values.put(Column.NAME, dateIntervalName);
		values.put(Column.DATE_FROM, Util.formatDateForDB(calendarFrom));
		values.put(Column.DATE_TO, Util.formatDateForDB(calendarTo));
		
		return dbCon.update(TABLE_NAME, values, 
				Column.ID + " = ?", // where 
				new String[] { String.valueOf(dateIntervalId) } // whereArgs
		);  
	}
	
	public static int deleteDateInterval(SQLiteDatabase dbCon, long dateIntervalId) {

		return dbCon.delete(TABLE_NAME,
				Column.ID + " = ?", // where
				new String[] { String.valueOf(dateIntervalId) }	// whereArgs
		);
	}
		
	public static Cursor getDateIntervalCursor(SQLiteDatabase dbCon) {
	
		Cursor c = null;
		
		try {

			String stmt = "SELECT * FROM dateInterval ORDER BY name ASC";
			c = dbCon.rawQuery(stmt, new String[] { });
			
		} finally {
//			c.close();  // don't close because c gets returned!
		}
		
		return c;
	}
	
	public static DateInterval getDateInterval(SQLiteDatabase dbCon, long intervalId) {
		
		String stmt = "SELECT * FROM dateInterval WHERE _id = " + String.valueOf(intervalId);	
		return getDateInterval(dbCon, stmt); 	
	}
		
	public static DateInterval getDefaultDateInterval(SQLiteDatabase dbCon) {
		
		String stmt = "SELECT * FROM dateInterval WHERE type = " + String.valueOf(IntervalType.NONE);		
		return getDateInterval(dbCon, stmt); 
	}
		
	public static boolean isFullDateInterval(SQLiteDatabase dbCon, long intervalId) {
		
		String stmt = "SELECT * FROM dateInterval WHERE type = " + String.valueOf(IntervalType.NONE);	
		return getDateInterval(dbCon, stmt).id == intervalId;
	}

	public static boolean isUserDefinedDateInterval(SQLiteDatabase dbCon, long intervalId) {
		
		String stmt = "SELECT * FROM dateInterval WHERE _id = " + String.valueOf(intervalId);	
		return getDateInterval(dbCon, stmt).userDefined;
	}

	public static DateIntervals getDateIntervals(SQLiteDatabase dbCon) {
				 
		Cursor c = null;

		try {
			
			String stmt = "SELECT * FROM dateInterval ORDER BY name ASC";
			c = dbCon.rawQuery(stmt, new String[] { });

			int size = c.getCount(); // - 1;
			
			String[] names = new String[size];
			long[] ids = new long[size];
			
			c.moveToFirst();
			
			for (int i = 0; i < ids.length; i++, c.moveToNext()) {
				ids[i] = c.getLong(ColumnPosition.ID);
				names[i] = c.getString(ColumnPosition.NAME);
			}
			
			return new DateIntervals(ids, names);
			
		} finally {
			
			c.close();
		}	
	}
	
	private static DateInterval getDateInterval(SQLiteDatabase dbCon, String stmt) {
		
		Cursor c = null;
		
		try {

			long id = -1;
			int type = IntervalType.UNDEFINED;
			String intervalName = null;
			Calendar dateFrom = null;
			Calendar dateTo = null;
			
			c = dbCon.rawQuery(stmt, null);
			
			if (c.moveToFirst()) {
				id = c.getLong(ColumnPosition.ID);
				type = c.getInt(ColumnPosition.TYPE);
				intervalName = c.getString(ColumnPosition.NAME);
				dateFrom = Util.parseDateString(c.getString(ColumnPosition.DATE_FROM));
				dateTo = Util.parseDateString(c.getString(ColumnPosition.DATE_TO));
			}
						
			if (type == IntervalType.USERDEFINED) {
				return getUserDefinedDateInterval(dbCon, id, intervalName, dateFrom, dateTo);
			}
			else {
				return getSystemDefinedDateInterval(dbCon, id, intervalName, type);
			}

		} finally {
			c.close();
		}	
	}
		
	private static DateInterval getUserDefinedDateInterval(SQLiteDatabase dbCon, long id, String intervalName, Calendar dateFrom, Calendar dateTo  /*, int type*/) {
		
		return new DateInterval(id, intervalName, dateFrom, dateTo, true);
	}
	
	private static DateInterval getSystemDefinedDateInterval(SQLiteDatabase dbCon, long id, String intervalName, int type) {
		
		Calendar date = null;
		Calendar dateFrom = null;
		Calendar dateTo = null;
		
		int day = 0;
		int month = 0; 
		int year = 0;
		
		switch (type) {
			
		case IntervalType.NONE:
			
			// from eldest to newest
			String dateEldest = ExpenseTable.getDateOfEldestExpense(dbCon);
			String dateYoungest = ExpenseTable.getDateOfYoungestExpense(dbCon);
			dateFrom = Util.parseDateString(dateEldest);
			dateTo = Util.parseDateString(dateYoungest);			
			return new DateInterval(id, intervalName, dateFrom, dateTo, false);
			// break;

		case IntervalType.TODAY:
			
			date = Calendar.getInstance(); 
			return new DateInterval (id, intervalName, date, date, false);
			// break;

		case IntervalType.YESTERDAY:
			
			date = Calendar.getInstance();
			long now = date.getTimeInMillis();
			long yesterday = now -  24 * 60 * 60 * 1000;
			date.setTimeInMillis(yesterday);
			return new DateInterval (id, intervalName, date, date, false);
			// break;

		case IntervalType.LASTWEEK:
			
			dateFrom = Calendar.getInstance();
			dateTo = Calendar.getInstance();
			
			dateTo.setLenient(true);
			dateFrom.setLenient(true);
			
			// push dateFrom to first day of this week
			dateFrom.set(Calendar.DAY_OF_WEEK, dateFrom.getFirstDayOfWeek());
			
			// push dateFrom to first day of the last week
			dateFrom.add(Calendar.DAY_OF_MONTH, -7);

			// push dateTo to first day of this week 
			dateTo.set(Calendar.DAY_OF_WEEK, dateTo.getFirstDayOfWeek());
			
			// push dateTo to last day of last week 
			dateTo.add(Calendar.DAY_OF_MONTH, -1);
			
			return new DateInterval (id, intervalName, dateFrom, dateTo, false);
			// break;

		case IntervalType.THISWEEK:
			
			dateFrom = Calendar.getInstance();
			dateTo = Calendar.getInstance();
			
			dateTo.setLenient(true);
			dateFrom.setLenient(true);
			
			// push dateFrom to first day of this week
			dateFrom.set(Calendar.DAY_OF_WEEK, dateFrom.getFirstDayOfWeek());
			
			// push dateTo to first day of this week 
			dateTo.set(Calendar.DAY_OF_WEEK, dateTo.getFirstDayOfWeek());
			
			// push dateTo to last day of this week 
			dateTo.add(Calendar.DAY_OF_MONTH, 6);
			
			return new DateInterval (id, intervalName, dateFrom, dateTo, false);
			// break;

		case IntervalType.LASTMONTH:
			
			dateFrom = Calendar.getInstance();
			dateTo = Calendar.getInstance();
			
			dateTo.setLenient(true);
			dateFrom.setLenient(true);
			
			day = 1;
			month = dateFrom.get(Calendar.MONTH) - 1;
			year = dateFrom.get(Calendar.YEAR);
			
			dateFrom.set(year, month, day);

			day = dateFrom.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			dateTo.set(year, month, day);
			
			return new DateInterval (id, intervalName, dateFrom, dateTo, false);
			// break;

		case IntervalType.THISMONTH:
			
			dateFrom = Calendar.getInstance();
			dateTo = Calendar.getInstance();
			
			dateTo.setLenient(true);
			dateFrom.setLenient(true);
			
			day = 1;
			month = dateFrom.get(Calendar.MONTH);
			year = dateFrom.get(Calendar.YEAR);
			
			dateFrom.set(year, month, day);

			day = dateFrom.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			dateTo.set(year, month, day);
			
			return new DateInterval (id, intervalName, dateFrom, dateTo, false);
			// break;

		case IntervalType.LASTYEAR:
			
			dateFrom = Calendar.getInstance();
			dateTo = Calendar.getInstance();
			
			dateTo.setLenient(true);
			dateFrom.setLenient(true);
			
			day = 1;
			month = Calendar.JANUARY;
			year = dateFrom.get(Calendar.YEAR) - 1;

			dateFrom.set(year, month, day);

			day = 31;
			month = Calendar.DECEMBER; 
						
			dateTo.set(year, month, day);
			
			return new DateInterval (id, intervalName, dateFrom, dateTo, false);
			// break;

		case IntervalType.THISYEAR:
			
			dateFrom = Calendar.getInstance();
			dateTo = Calendar.getInstance();
			
			dateTo.setLenient(true);
			dateFrom.setLenient(true);
			
			day = 1;
			month = Calendar.JANUARY;
			year = dateFrom.get(Calendar.YEAR);

			dateFrom.set(year, month, day);

			day = 31;
			month = Calendar.DECEMBER; 
						
			dateTo.set(year, month, day);
			
			return new DateInterval (id, intervalName, dateFrom, dateTo, false);
			// break;

		default:
			
			return null;
			// break;
		}
	}
}
