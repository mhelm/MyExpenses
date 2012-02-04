package com.helmmss.android.myexpenses;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class CategoryListUI extends StandardListActivity {
	
	private Database db = null;
	private SQLiteDatabase dbCon = null;
	private Cursor cursor = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHeaderTextLeft(R.string.category_ui_title);
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

		cursor = CategoryTable.getCategoryCursor(dbCon); 
		
        ListAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, 
                cursor, 
                new String[] { CategoryTable.Column.NAME  },
                new int[] { android.R.id.text1 }); 
	                
        setListAdapter(adapter);
	        					
		super.displayItems();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (data != null) {
			data.putExtra(ITEM_ID_PARAM_NAME, CategoryUI.PARAM_CATEGORY_ID);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onClickAddButton(View v) {
		
		Intent intent = new Intent(CategoryListUI.this, CategoryUI.class);
		intent.putExtra(CategoryUI.PARAM_MODE, CategoryUI.MODE_ADD);
		startActivityForResult(intent, 0);
		
		super.onClickAddButton(v);
	}

	@Override
	protected void onClickItemEdit(long categoryId) {
		
		Intent intent = new Intent(CategoryListUI.this, CategoryUI.class);
		intent.putExtra(CategoryUI.PARAM_MODE, CategoryUI.MODE_UPDATE);
		intent.putExtra(CategoryUI.PARAM_CATEGORY_ID, categoryId);
		startActivityForResult(intent, 0);	
		
		super.onClickItemEdit(categoryId);
	}

	@Override
	protected void onClickItemDelete(long categoryId) {
		
		CategoryTable.deleteCategory(dbCon, categoryId);
		displayItems();
		
		super.onClickItemDelete(categoryId);
	}

	@Override
	protected boolean isDeleteAllowed(long categoryId) {

		// only user defined categories can be deleted
		// system defined categories must not be deleted
		Category category = CategoryTable.getCategory(dbCon, categoryId);		
		return category.userDefined;
	}

}
