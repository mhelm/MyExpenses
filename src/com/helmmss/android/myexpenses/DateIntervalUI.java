package com.helmmss.android.myexpenses;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class DateIntervalUI extends Activity {
	
	private Database db = null;
	private SQLiteDatabase dbCon = null;
	
	private static final int ID_DATE_FROM_DIALOG = 0;
	private static final int ID_DATE_TO_DIALOG = 1;
	
	public static final int MODE_UPDATE = 0;
	public static final int MODE_ADD = 1;
	
    public static final String PARAM_MODE = "Mode";
    public static final String PARAM_DATE_INTERVAL_ID = "DateIntervalID";
	public static final String PARAM_AFFECTED_ROWS = "AffectedRows";

	private EditText nameEditText = null;
	private EditText dateFromEditText = null;
	private EditText dateToEditText = null;
	private TextView headerTextLeft = null; 
	private Calendar dateFrom = Calendar.getInstance(); // default date = the current date
	private Calendar dateTo = Calendar.getInstance(); // default date = the current date
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_date_interval);
		
		nameEditText = (EditText) findViewById(R.id.dateIntervalUI_textName);
		dateFromEditText = (EditText) findViewById(R.id.dateIntervalUI_textDateFrom);
		dateToEditText = (EditText) findViewById(R.id.dateIntervalUI_textDateTo);
        headerTextLeft = (TextView) findViewById(R.id.dateIntervalUI_textHeader);

        Button okButton = (Button) findViewById(R.id.dateIntervalUI_buttonOK);
		Button cancelButton = (Button) findViewById(R.id.dateIntervalUI_buttonCancel);		

		dateFromEditText.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            showDialog(ID_DATE_FROM_DIALOG);
	       }
	    });
		
		dateFromEditText.setOnLongClickListener(new OnLongClickListener() {
	        public boolean onLongClick(View v) {
	            showDialog(ID_DATE_FROM_DIALOG);
	            return true;
	       }
	    });

		dateToEditText.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            showDialog(ID_DATE_TO_DIALOG);
	       }
	    });

		dateToEditText.setOnLongClickListener(new OnLongClickListener() {
	        public boolean onLongClick(View v) {
	            showDialog(ID_DATE_TO_DIALOG);
	            return true;
	       }
	    });

		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				// output to caller is put in an intent
				final Intent intent = new Intent();
				
				// input from caller is got from an intent's bundle
				Bundle b = getIntent().getExtras();
				int mode = ((Integer)b.get(PARAM_MODE)).intValue();
				
				String name = nameEditText.getText().toString();

				if (name.length() == 0) {
					// no name entered
					name = createName();
				}
				
				switch (mode) {
				case MODE_UPDATE:
					
					long dateIntervalID_in = b.getLong(PARAM_DATE_INTERVAL_ID);
					int affectedRows_out = DateIntervalTable.updateDateInterval(dbCon, dateIntervalID_in, name, dateFrom, dateTo);
					intent.putExtra(PARAM_AFFECTED_ROWS, affectedRows_out);
					break;

				case MODE_ADD:
										
					long dateIntervalID_out = DateIntervalTable.insertDateInterval(dbCon, name, dateFrom, dateTo);
					intent.putExtra(PARAM_DATE_INTERVAL_ID, dateIntervalID_out);
					break;

				default:
					break;
				}
				
				setResult(Activity.RESULT_OK, intent);
				finish();
			}

			private String createName() {

				String name = "";
				
				if (dateFrom.equals(dateTo)) {
					// same date
					name = Util.formatDateForView(dateFrom);
				}
				else {
					// different dates
					name = Util.formatDateForView(dateFrom) + " - " + Util.formatDateForView(dateTo);
				}
				
				return name;
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED, null);
				finish();
			}
		});
		
		initialize();
	}
	
	private void initialize() {
		
		// called onCreate() - so db and dbCon are still null and need to be initialized
		db = new Database(this);
		dbCon = db.getReadableDatabase();
		
		Bundle b = getIntent().getExtras();
		int mode = ((Integer)b.get(PARAM_MODE)).intValue();
		
		switch (mode) {
		case MODE_UPDATE:
			
			headerTextLeft.setText(R.string.dateInterval_ui_title_edit);
			
			long dateIntervalId = b.getLong(PARAM_DATE_INTERVAL_ID);
			DateInterval dateInterval = DateIntervalTable.getDateInterval(dbCon, dateIntervalId);
			
			if (dateInterval.userDefined) {
				nameEditText.setText(dateInterval.name);
				dateFromEditText.setText(Util.formatDateForView(dateInterval.from));
				dateFrom = dateInterval.from;
				dateToEditText.setText(Util.formatDateForView(dateInterval.to));
				dateTo = dateInterval.to;
			}
			else {
				// system defined data interval
				nameEditText.setText(dateInterval.name);
				dateFromEditText.setText("");
				dateFromEditText.setEnabled(false);
				dateFrom = dateInterval.from;
				dateToEditText.setEnabled(false);
				dateToEditText.setText("");
				dateTo = dateInterval.to;
			}
			
			break;

		case MODE_ADD:
			
			headerTextLeft.setText(R.string.dateInterval_ui_title_add);
			dateFromEditText.setText(Util.formatDateForView(dateFrom));
			dateToEditText.setText(Util.formatDateForView(dateTo));
			nameEditText.setText("");

			break;

		default:
			break;
		}
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

	@Override
	protected Dialog onCreateDialog(int id) {
			
		Dialog d = null;
		
		switch (id) {
		case ID_DATE_FROM_DIALOG:
			d = new DatePickerDialog(
					this,
					new DatePickerDialog.OnDateSetListener() {
						public void onDateSet(DatePicker view, int year,
								int month, int day) {
							dateFrom.set(year, month, day);
							dateFromEditText.setText(Util.formatDateForView(dateFrom));
						}
					}, 
					dateFrom.get(Calendar.YEAR), dateFrom.get(Calendar.MONTH),
					dateFrom.get(Calendar.DAY_OF_MONTH));
			break;
		
		case ID_DATE_TO_DIALOG:
			d = new DatePickerDialog(
					this,
					new DatePickerDialog.OnDateSetListener() {
						public void onDateSet(DatePicker view, int year,
								int month, int day) {
							dateTo.set(year, month, day);
							dateToEditText.setText(Util.formatDateForView(dateTo));
						}
					}, 
					dateTo.get(Calendar.YEAR), dateTo.get(Calendar.MONTH),
					dateTo.get(Calendar.DAY_OF_MONTH));
			break;

		default:
			break;
		}
		
		return d;
	}
}
