package com.helmmss.android.myexpenses;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class DateIntervalListUI extends StandardListActivity {

	private Database db = null;
	private SQLiteDatabase dbCon = null;
	private Cursor cursor = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHeaderTextLeft(R.string.dateInterval_ui_title);
		setHeaderTextRight(R.string.blank);
//		setActionDialogTitle("");
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (db == null) {
			db = new Database(this);
		}
		
		if (dbCon == null) {
			dbCon = db.getReadableDatabase();
		}

		displayItems();
	}
	
	@Override	
	protected void onStop() {
		super.onStop();
		
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}

		if (dbCon != null) {
			dbCon.close();
			dbCon = null;
		}

		if (db != null) {
			db.close();
			db = null;
		}
	}
	
	@Override	
	protected void displayItems() {
		
		// always re-query data; records might have been added or deleted
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
		
		cursor = DateIntervalTable.getDateIntervalCursor(dbCon);
		
        ListAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, 
                cursor, 
                new String[] { DateIntervalTable.Column.NAME  }, 
                new int[] { android.R.id.text1 }); 
        
        setListAdapter(adapter);
		
		super.displayItems();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (data != null) {
			data.putExtra(ITEM_ID_PARAM_NAME, DateIntervalUI.PARAM_DATE_INTERVAL_ID);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onClickAddButton(View v) {
		
		Intent intent = new Intent(DateIntervalListUI.this, DateIntervalUI.class);
		intent.putExtra(DateIntervalUI.PARAM_MODE, DateIntervalUI.MODE_ADD);
		startActivityForResult(intent, 0);
		
		super.onClickAddButton(v);
	}

	@Override
	protected void onClickItemEdit(long dateIntervalId) {
		
		Intent intent = new Intent(DateIntervalListUI.this, DateIntervalUI.class);
		intent.putExtra(DateIntervalUI.PARAM_MODE, DateIntervalUI.MODE_UPDATE);
		intent.putExtra(DateIntervalUI.PARAM_DATE_INTERVAL_ID, dateIntervalId);
		startActivityForResult(intent, 0);	
		
		super.onClickItemEdit(dateIntervalId);
	}

	@Override
	protected boolean isDeleteAllowed(long dateIntervalId) {

		// only user defined date intervals can be deleted
		// system defined date intervals must not be deleted
		DateInterval dateInterval = DateIntervalTable.getDateInterval(dbCon, dateIntervalId);		
		return dateInterval.userDefined;
	}

	@Override
	protected void onClickItemDelete(long dateIntervalId) {

		DateIntervalTable.deleteDateInterval(dbCon, dateIntervalId);
		displayItems();
		
		super.onClickItemDelete(dateIntervalId);
	}
}
