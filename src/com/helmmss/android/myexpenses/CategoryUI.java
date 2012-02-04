package com.helmmss.android.myexpenses;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CategoryUI extends Activity {
	
	private Database db = null;
	private SQLiteDatabase dbCon = null;
	
	public static final int MODE_UPDATE = 0;
	public static final int MODE_ADD = 1;
	
    public static final String PARAM_MODE = "Mode";
    public static final String PARAM_CATEGORY_ID = "CategoryID";
    public static final String PARAM_CATEGORY_NAME = "CategoryName";
	public static final String PARAM_AFFECTED_ROWS = "AffectedRows";
		
	private EditText categoryEditText = null;
	private TextView headerTextLeft = null; 
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category);

        categoryEditText = (EditText) findViewById(R.id.categoryUI_textName);
        headerTextLeft = (TextView) findViewById(R.id.categoryUI_textHeader);
   
        Button okButton = (Button) findViewById(R.id.categoryUI_buttonOK);
		Button cancelButton = (Button) findViewById(R.id.categoryUI_buttonCancel);		
						
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				// output to caller is put in an intent
				final Intent intent = new Intent();
				
				// input from caller is got from an intent's bundle
				Bundle b = getIntent().getExtras();
				int mode = b.getInt(PARAM_MODE);
				
				switch (mode) {
				case MODE_UPDATE:
					
					long categoryId_in = b.getLong(PARAM_CATEGORY_ID);
					long affectedRows_out = CategoryTable.updateCategory(
							dbCon,
							categoryId_in,
							categoryEditText.getText().toString()
					);

					intent.putExtra(PARAM_AFFECTED_ROWS, affectedRows_out);

					break;

				case MODE_ADD:
					
					long categoryID_out = CategoryTable.insertCategory(
							dbCon,
							categoryEditText.getText().toString()
					);
					
					intent.putExtra(PARAM_CATEGORY_ID, categoryID_out);
					intent.putExtra(PARAM_CATEGORY_NAME, categoryEditText.getText().toString());

					break;

				default:
					break;
				}
				
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED, null);
				finish();
			}
		});

		// assign default data
		initialize();
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
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if (dbCon != null) {
			dbCon.close();
			dbCon = null;
		}

		if (db != null) {
			db.close();
			db = null;
		}
	}

	private void initialize() {
		
		// called onCreate() - so db and dbCon are still null and need to be initialized
		db = new Database(this);
		dbCon = db.getReadableDatabase();
		
		Bundle b = getIntent().getExtras();
		int mode = ((Integer)b.get(PARAM_MODE)).intValue();
		
		switch (mode) {
		case MODE_UPDATE:
			
			headerTextLeft.setText(R.string.category_ui_title_edit);			
			long catgeoryId = ((Long)b.get(PARAM_CATEGORY_ID)).longValue();
			Category category = CategoryTable.getCategory(dbCon, catgeoryId);
			categoryEditText.setText(category.name);
			
			break;

		case MODE_ADD:
			
			headerTextLeft.setText(R.string.category_ui_title_add);
			categoryEditText.setText("");

			break;

		default:

			break;
		}
	}
}
