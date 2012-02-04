package com.helmmss.android.myexpenses;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class ExpenseUI extends Activity {
	
	private Database db = null;
	private SQLiteDatabase dbCon = null;
	
	private static final int ID_DATE_DIALOG = 0;
	private static final int ID_CATEGORY_DIALOG = 1;
	
	public static final int MODE_UPDATE = 0;
	public static final int MODE_ADD = 1;
	
	public static final String PARAM_MODE = "Mode";
	public static final String PARAM_EXPENSE_ID = "ExpenseID";
	public static final String PARAM_AFFECTED_ROWS = "AffectedRows";
	
	private static final String PREF_DEFAULT_DATE = "defaultDate";
		
	private TextView headerTextLeft = null; 

	private Calendar date = null;
	private EditText dateEditText = null;
	private EditText amountEditText = null;
	private EditText categoryEditText = null;
	private EditText descriptionEditText = null;
	private RadioButton cashRadioButton = null;
	private RadioButton cardRadioButton = null;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_expense);

        headerTextLeft = (TextView) findViewById(R.id.expenseUI_textHeader);
        dateEditText = (EditText)findViewById(R.id.expenseUI_textDate); 
        amountEditText = (EditText) findViewById(R.id.expenseUI_textAmount);
        categoryEditText = (EditText) findViewById(R.id.expenseUI_textCategory);
        descriptionEditText = (EditText) findViewById(R.id.expenseUI_textDescription);  
        cashRadioButton = (RadioButton) findViewById(R.id.expenseUI_radioCash);
        cardRadioButton = (RadioButton) findViewById(R.id.expenseUI_radioCard);
		
        Button okButton = (Button) findViewById(R.id.expenseUI_buttonOK);
		Button cancelButton = (Button) findViewById(R.id.expenseUI_buttonCancel);		
			
		dateEditText.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	// choose date
	            showDialog(ID_DATE_DIALOG);
	       }
	    });
	
		dateEditText.setOnLongClickListener(new OnLongClickListener() {
	        public boolean onLongClick(View v) {
	        	// set date to current date
	        	date = Calendar.getInstance();
				dateEditText.setText(Util.formatDateForView(date));
	            return true;
	       }
	    });
				
		categoryEditText.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	// choose category
	            showDialog(ID_CATEGORY_DIALOG);
	       }
	    });
		
		categoryEditText.setOnLongClickListener(new OnLongClickListener() {
	        public boolean onLongClick(View v) {
	            showDialog(ID_CATEGORY_DIALOG);
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
				
				switch (mode) {
				case MODE_UPDATE:
					
					// expense UI is left with OK button
					// mode is "update" 
					// so update expense with ID: expenseID_in
					
					long expenseID_in = ((Long)b.get(PARAM_EXPENSE_ID)).longValue();
					long affectedRows_out = ExpenseTable.updateExpense(
							dbCon,
							date, 
							amountEditText.getText().toString(),
							categoryEditText.getText().toString(),
							descriptionEditText.getText().toString(),
							cashRadioButton.isChecked() ? 
									ExpenseTable.MethodOfPayment.CASH : 
									ExpenseTable.MethodOfPayment.CARD,
							expenseID_in
					);

					intent.putExtra(PARAM_AFFECTED_ROWS, affectedRows_out);

					break;

				case MODE_ADD:
					
					// expense UI is left with OK button
					// mode is "add" 
					// so add new expense to database

					long expenseID_out = ExpenseTable.insertExpense( 
							dbCon,
							date, 
							amountEditText.getText().toString(),
							categoryEditText.getText().toString(),
							descriptionEditText.getText().toString(),
							cashRadioButton.isChecked() ? 
									ExpenseTable.MethodOfPayment.CASH : 
									ExpenseTable.MethodOfPayment.CARD
					);

					intent.putExtra(PARAM_EXPENSE_ID, expenseID_out);

					break;

				default:
					break;
				}
				
				// return result to caller
				setResult(Activity.RESULT_OK, intent);
				// close the UI (activity)
				finish();
			}
		});
		
		// handler for Cancel button clicks
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED, null);
				finish();
			}
		});
		
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		String defaultDate = settings.getString(PREF_DEFAULT_DATE, null);
		if (defaultDate != null) {
			date = Util.parseDateString(defaultDate);
		}
		else {
			date = Calendar.getInstance();
		}
		
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
			
			headerTextLeft.setText(R.string.expense_ui_title_edit);

			long expenseId = ((Long)b.get(PARAM_EXPENSE_ID)).longValue();
			Expense expense = ExpenseTable.getExpense(dbCon, expenseId);

			date = expense.date;
			dateEditText.setText(Util.formatDateForView(expense.date));
			amountEditText.setText(expense.amount);
			categoryEditText.setText(expense.category);
			descriptionEditText.setText(expense.description);

			if (expense.type == null) {
				// value not present - so just take the default
				cashRadioButton.setChecked(true);
			} else if (expense.type.equalsIgnoreCase(ExpenseTable.MethodOfPayment.CASH)) {
				cashRadioButton.setChecked(true);
			} else if (expense.type.equalsIgnoreCase(ExpenseTable.MethodOfPayment.CARD)) {
				cardRadioButton.setChecked(true);
			} else { // just take the default
				cashRadioButton.setChecked(true);
			}

			break;

		case MODE_ADD:
			
			headerTextLeft.setText(R.string.expense_ui_title_add);
			dateEditText.setText(Util.formatDateForView(date));
			amountEditText.setText("");
			categoryEditText.setText("");
			descriptionEditText.setText("");
			cashRadioButton.setChecked(true);
		
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
		
        // store for later re-use
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = settings.edit();
        prefEditor.putString(PREF_DEFAULT_DATE, Util.formatDateForDB(date));
        prefEditor.commit();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		
		Dialog d = null;
		switch (id) {
		
		// dialog to choose a date
		case ID_DATE_DIALOG:
			d = new DatePickerDialog(
					this,
					new DatePickerDialog.OnDateSetListener() {
						public void onDateSet(DatePicker view, int year,
								int month, int day) {
							date.set(year, month, day);
							dateEditText.setText(Util.formatDateForView(date));
							amountEditText.requestFocus();
						}
					}, 
					date.get(Calendar.YEAR), 
					date.get(Calendar.MONTH),
					date.get(Calendar.DAY_OF_MONTH));
			break;
			
		// dialog to choose a category
		case ID_CATEGORY_DIALOG:
			
			final Categories categories = CategoryTable.getCategories(dbCon);
			
			d = new AlertDialog.Builder(this)
			     // .setIcon(R.drawable.icon)
					.setTitle(R.string.category_ui_title)
					.setItems(categories.names,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
									int whichButton) {									
									categoryEditText.setText(categories.names[whichButton]);
									descriptionEditText.requestFocus();
									removeDialog(ID_CATEGORY_DIALOG);
								}
							})
		            .setPositiveButton(R.string.button_add, new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int whichButton) {
							// prepare data for hand over to update-activity
							Intent intent = new Intent(ExpenseUI.this, CategoryUI.class);
							intent.putExtra(CategoryUI.PARAM_MODE, CategoryUI.MODE_ADD);
							// start update-activity
							startActivityForResult(intent, 0);						
							removeDialog(ID_CATEGORY_DIALOG);
		                }
		            })
					.create();
			break;
		
		default:
			d = null;
			break;
		}

		return d;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (resultCode) {
		case Activity.RESULT_OK:
			// get rid of cached category dialog (onCreate)
			// to be able to display the newest list of categories
			// each time it is displayed			
			categoryEditText.setText(data.getExtras().getString(CategoryUI.PARAM_CATEGORY_NAME));
			descriptionEditText.requestFocus();
			break;
		case Activity.RESULT_CANCELED:
			break;
		default:
			break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
}
