package com.helmmss.android.myexpenses;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;

public class ExpenseListUI extends StandardListActivity {
	
	private Database db = null;
	private SQLiteDatabase dbCon = null;
	private Cursor cursor = null;

    private static final int DIALOG_ID_FILTER_SELECTION = 10;
	private static final int DIALOG_ID_DATE_INTERVAL_SELECTION = 11;
	private static final int DIALOG_ID_SEND_MAIL = 12;
	
	private static final int REQUEST_CODE_ADD_ITEM = 10;
	private static final int REQUEST_CODE_UPDATE_ITEM = 11;
	private static final int REQUEST_CODE_SEND_MAIL = 12;
	private static final int REQUEST_CODE_ADD_DATE_INTERVAL = 13;
	
	private static final String PREF_MAIL_ADDRESS = "mailAddress";
	private static final String PREF_MAIL_FORMAT = "mailFormat";
	private static final String PREF_DATE_RANGE_ID = "dateRangeId";
	private static final String PREF_TYPE_SELECTOR = "typeSelector";
	private static final String PREF_CATEGORY_SELECTOR = "categorySelector";
	private static final String PREF_DESCRIPTION_SELECTOR = "descriptionSelector";
	private static final String PREF_DATE_SELECTOR = "dateSelector";

	private static final String HTML = "HTML";
	private static final String QIF = "QIF";
	private static final String CSV = "CSV";
	private static final String XML = "XML";
	
	private File convertedList = null;
	
	// used to cache the current filter settings
	private DateInterval dateRange = null;
	private String typeSelector = null;
	private String categorySelector = null;
	private String descriptionSelector = null;
	private Calendar dateSelector = null; // overruled by dateRange
	
	public static Drawable cardDrawable = null;
	public static Drawable cashDrawable = null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (cardDrawable == null) {
			cardDrawable = getResources().getDrawable(R.drawable.cards);
		}
		if (cashDrawable == null) {
			cashDrawable = getResources().getDrawable(R.drawable.cash);
		}
		
		setHeaderTextLeft(R.string.expense_ui_title);
		setHeaderTextRight(R.string.blank);
//		setActionDialogTitle("");

		dateRange = new DateInterval();
		
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		dateRange.id = settings.getLong(PREF_DATE_RANGE_ID, -1);
		typeSelector = settings.getString(PREF_TYPE_SELECTOR, null);
		categorySelector = settings.getString(PREF_CATEGORY_SELECTOR, null);
		descriptionSelector = settings.getString(PREF_DESCRIPTION_SELECTOR, null);
		dateSelector = Util.parseDateString(settings.getString(PREF_DATE_SELECTOR, null));
	}

	@Override
	protected void onResume() {
		super.onResume();		
		refresh();
	}
	
	private void refresh() {
		
		if (db == null) {
			db = new Database(this);
		}
		
		if (dbCon == null) {
			dbCon = db.getReadableDatabase();
		}
			
		displayDateInterval();
		displayAmount();
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
		
        // store for later re-use
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = settings.edit();
        prefEditor.putLong(PREF_DATE_RANGE_ID, dateRange.id);
        prefEditor.putString(PREF_TYPE_SELECTOR, typeSelector);
        prefEditor.putString(PREF_CATEGORY_SELECTOR, categorySelector);
        prefEditor.putString(PREF_DESCRIPTION_SELECTOR, descriptionSelector);
        prefEditor.putString(PREF_DATE_SELECTOR, Util.formatDateForDB(dateSelector));
        prefEditor.commit();
	}
	
	private void displayDateInterval() {
		
		if (dateSelector != null) {
			setHeaderTextLeft(Util.formatDateForView(dateSelector));
//			setHeaderTextLeftColor(getBaseContext().getResources().getColor(R.color.wheat1));
			return;
		}

		if (dateRange != null) {
			// due to adding or removing items (which might influence the displayed 
			// date range) always re-query the current range (using id)
			dateRange = DateIntervalTable.getDateInterval(dbCon, dateRange.id);
		}
		
		// dateRange might be null here, e.g. when a certain date interval is
		// currently used in the expense list UI, but gets deleted while being 
		// in use; or it has not yet been set at all 
		if (dateRange == null) {
			dateRange = DateIntervalTable.getDefaultDateInterval(dbCon);
		}
		
		boolean notFiltered = DateIntervalTable.isFullDateInterval(dbCon, dateRange.id);
		int color = 0;
		
		if (notFiltered) {
			// no date filter is active
			// display the eldest and the newest date
			color = getBaseContext().getResources().getColor(R.color.white);
		}
		else {
			color = getBaseContext().getResources().getColor(R.color.white);
		}
		
		String interval = null; 
		
		if (dateRange.from != null && dateRange.to != null && dateRange.from.equals(dateRange.to)) {
			// same dates --> display only one
			interval = Util.formatDateForView(dateRange.from);
		}
		else if (dateRange.from == null || dateRange.to == null) {
			// one or both dates are null --> display only the given one (if any)
			interval = Util.formatDateForView(dateRange.from) + Util.formatDateForView(dateRange.to);
		}
		else {
			interval = Util.formatDateForView(dateRange.from) + " - " + Util.formatDateForView(dateRange.to);
		}
		
		setHeaderTextLeft(interval);
		setHeaderTextLeftColor(color);
	}	
	
	private void displayAmount() {
		
		double amount = ExpenseTable.getSumOfExpenses(dbCon, dateRange, typeSelector, categorySelector, descriptionSelector, dateSelector);
		setHeaderTextRight(Util.formatAmountForView(amount));
	}

	@Override
	protected void displayItems() {
		
		// always re-query data; records might have been added or deleted
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
		
		cursor = ExpenseTable.getExpenseCursor(dbCon, dateRange, typeSelector, categorySelector, descriptionSelector, dateSelector); 
		
		SimpleCursorAdapter adapter = new ExpenseCursorAdapter(this,
        		R.layout.list_item_expense, 
                cursor, 
        		new String[] { // column names
					ExpenseTable.Column.TYPE,
					ExpenseTable.Column.CATEGORY,
					ExpenseTable.Column.AMOUNT,
					ExpenseTable.Column.DATE,
					ExpenseTable.Column.DESCRIPTION,
				},  
        		new int [] { // corresponding text views 
					R.id.standardListItem4_image,
					R.id.standardListItem4_textUpperLeft,
					R.id.standardListItem4_textUpperRight,
					R.id.standardListItem4_textLowerLeft,
					R.id.standardListItem4_textLowerRight
				}, 
				! (typeSelector == null && categorySelector == null && 
				   descriptionSelector == null && dateSelector == null) // filtered
		);  

        setListAdapter(adapter);
        		
		super.displayItems();		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {
		
			case REQUEST_CODE_SEND_MAIL:
				
				break;
	
			case REQUEST_CODE_ADD_ITEM:
				
				if (resultCode == Activity.RESULT_OK) {
					data.putExtra(ITEM_ID_PARAM_NAME, ExpenseUI.PARAM_EXPENSE_ID);
				}
				break;
	
			case REQUEST_CODE_UPDATE_ITEM:
				
				if (resultCode == Activity.RESULT_OK) {
					data.putExtra(ITEM_ID_PARAM_NAME, ExpenseUI.PARAM_EXPENSE_ID);
				}
				break;
	
			case REQUEST_CODE_ADD_DATE_INTERVAL:
				
				switch (resultCode) {
				case Activity.RESULT_OK:
					dateRange.id = data.getExtras().getLong(DateIntervalUI.PARAM_DATE_INTERVAL_ID);
					// date range was set - so reset dateSelector (if set)
					dateSelector = null;
					refresh();
					break;
				case Activity.RESULT_CANCELED:
					break;
				default:
					break;
				}
				
				break;
	
			default:
				
				break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}		

	@Override
	protected void onClickAddButton(View v) {
		
		Intent intent = new Intent(ExpenseListUI.this, ExpenseUI.class);
		intent.putExtra(ExpenseUI.PARAM_MODE, ExpenseUI.MODE_ADD);
		startActivityForResult(intent, REQUEST_CODE_ADD_ITEM);
		
		super.onClickAddButton(v);
	}

	@Override
	protected void onClickItemEdit(long id) {
		
		Intent intent = new Intent(ExpenseListUI.this, ExpenseUI.class);
		intent.putExtra(ExpenseUI.PARAM_MODE, ExpenseUI.MODE_UPDATE);
		intent.putExtra(ExpenseUI.PARAM_EXPENSE_ID, id);
		startActivityForResult(intent, REQUEST_CODE_UPDATE_ITEM);	
		
		super.onClickItemEdit(id);
	}

	@Override
	protected void onClickItemDelete(long id) {
		
		if (getListView().getCount() == 1) {
			// delete one item = delete all items = delete list
			onClickItemDeleteAll();
			return;
		}
		
		ExpenseTable.deleteExpense(dbCon, id);
		displayDateInterval();
		displayAmount();
		displayItems();
		
		super.onClickItemDelete(id);
	}
	
	@Override
	protected void onClickItemDeleteAll() {
		
		ExpenseTable.deleteExpense(dbCon, dateRange, typeSelector, categorySelector, descriptionSelector, dateSelector);
		
		typeSelector = null;
		categorySelector = null;
		descriptionSelector = null;
		dateSelector = null;
		
		displayDateInterval();
		displayAmount();
		displayItems();
		
		super.onClickItemDeleteAll();
	}
	
	// added for the item filter dialog
	
	@Override
	protected boolean onLongClickListItem(AdapterView<?> parent, View v, int pos, long id) {
		
		Bundle args = new Bundle();
		args.putLong(ITEM_ID, id);				
		showDialog(DIALOG_ID_FILTER_SELECTION, args);
		return true;
	}
	
	// added for date-interval-dialog
	
	@Override
	protected void onClickHeader(View v) {
		showDialog(DIALOG_ID_DATE_INTERVAL_SELECTION);
    }
	
	@Override
	protected Dialog onCreateDialog(int id, final Bundle b) {
		
		// dialog might need to be re-created - e.g. when screen is turned
		// from portrait to landscape - then db and dbCon can be null and 
		// need to be initialized
		
		if (db == null) {
			db = new Database(this);
		}
		
		if (dbCon == null) {
			dbCon = db.getReadableDatabase();
		}

		Dialog d = null;
    	
        switch (id) {
        
		case DIALOG_ID_FILTER_SELECTION:
			
			final Expense expense = ExpenseTable.getExpense(dbCon, b.getLong(ITEM_ID));
			
			String categoryFilter = expense.category;
			String descriptionFilter = expense.description;
			String typeFilter = Util.formatTypeForView(getListView().getContext(), expense.type);
			String dateFilter = Util.formatDateForView(expense.date);
			
			ArrayList<String> filterDialogItems_list = new ArrayList<String>();
			ArrayList<Boolean> filterDialogCheckmarks_list = new ArrayList<Boolean>();

			final int[] position = new int[] { -1, -1, -1, -1 };
			int pos = -1;
			
			if (typeFilter != null && typeFilter.length() != 0) {
				filterDialogItems_list.add(typeFilter);
				filterDialogCheckmarks_list.add(typeSelector != null);
				position[0] = ++pos;
			}
			if (dateFilter != null && dateFilter.length() != 0) {
				filterDialogItems_list.add(dateFilter);
				filterDialogCheckmarks_list.add(dateSelector != null);
				position[1] = ++pos;
			}
			if (categoryFilter != null && categoryFilter.length() != 0) {
				filterDialogItems_list.add(categoryFilter);
				filterDialogCheckmarks_list.add(categorySelector != null);
				position[2] = ++pos;
			}
			if (descriptionFilter != null && descriptionFilter.length() != 0) {
				filterDialogItems_list.add(descriptionFilter);
				filterDialogCheckmarks_list.add(descriptionSelector != null);
				position[3] = ++pos;
			}
			
			final String[] filterDialogItems = filterDialogItems_list.toArray(new String[]{});
			final boolean[] filterDialogCheckmarks = new boolean[filterDialogCheckmarks_list.size()];
			
			for (int i = 0; i < filterDialogCheckmarks_list.size(); i++) {
				filterDialogCheckmarks[i] = filterDialogCheckmarks_list.get(i).booleanValue();
			}

			d = new AlertDialog.Builder(this)
                .setTitle(R.string.expense_filterDialog_title)
                .setOnCancelListener(
                		new DialogInterface.OnCancelListener() {
                			public void onCancel(DialogInterface dialog) {
                				removeDialog(DIALOG_ID_FILTER_SELECTION);
                			}
                		})
                .setMultiChoiceItems(filterDialogItems,
                		filterDialogCheckmarks,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton,
                                    boolean isChecked) {
                            	if (whichButton == position[2]) { // category
                            		if (isChecked) {
                            			categorySelector = expense.category;
                            		} else {
                            			categorySelector = null;
                            		}
                            	}
                            	else if (whichButton == position[0]) { // type
                            		if (isChecked) {
                            			typeSelector = expense.type;
                            		} else {
                            			typeSelector = null;
                            		}
                            	}
                            	else if (whichButton == position[3]) { // description
                            		if (isChecked) {
                            			descriptionSelector = expense.description;
                            		} else {
                            			descriptionSelector = null;
                            		}
                            	}
                            	else if (whichButton == position[1]) { // date
                            		if (isChecked) {
                            			dateSelector = expense.date;
                            		} else {
                            			dateSelector = null;
                            		}
                            	}
                            }
                        })
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	refresh();
                    	removeDialog(DIALOG_ID_FILTER_SELECTION);
                    }
                })
               .create();
			
			break;
                            	            	            
            case DIALOG_ID_DATE_INTERVAL_SELECTION:
        
            	final DateIntervals dateIntervals = DateIntervalTable.getDateIntervals(dbCon);
                d = new AlertDialog.Builder(this)
                .setTitle(R.string.dateInterval_filterDialog_title)
                .setOnCancelListener(
                		new DialogInterface.OnCancelListener() {
                			public void onCancel(DialogInterface dialog) {
                				removeDialog(DIALOG_ID_DATE_INTERVAL_SELECTION);
                			}
                		})
				.setItems(dateIntervals.names,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichPosition) {			
								dateRange.id = dateIntervals.ids[whichPosition];
								// date range was set - so reset dateSelector (if set)
								dateSelector = null;
								refresh();
								removeDialog(DIALOG_ID_DATE_INTERVAL_SELECTION);
							}
						})
                .setPositiveButton(R.string.button_add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
						// prepare data for hand-over to update-activity
						Intent intent = new Intent(ExpenseListUI.this, DateIntervalUI.class);
						intent.putExtra(DateIntervalUI.PARAM_MODE, DateIntervalUI.MODE_ADD);
						// start update-activity
						startActivityForResult(intent, REQUEST_CODE_ADD_DATE_INTERVAL);		
						removeDialog(DIALOG_ID_DATE_INTERVAL_SELECTION);
                    }
                })
				.create();
                break;
                
            case DIALOG_ID_SEND_MAIL:
                
                LayoutInflater factory = LayoutInflater.from(this);
                final View textEntryView = factory.inflate(R.layout.dialog_send_list, null);
                                
                d = new AlertDialog.Builder(this)
//					.setIcon(R.drawable.alert_dialog_icon)
	                .setTitle(R.string.sendList_dialog_title)
	                .setOnCancelListener(
                		new DialogInterface.OnCancelListener() {
                			public void onCancel(DialogInterface dialog) {
                            	removeDialog(DIALOG_ID_SEND_MAIL);
                			}
                		})
	                .setView(textEntryView)
	                .setPositiveButton(R.string.sendList_dialog_button_send, 
	                	new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int whichButton) {
                        	
	                        // get mail address from UI
                        	EditText adr = (EditText)textEntryView.findViewById(R.id.username_edit);
                            RadioButton qifRadioButton = (RadioButton)textEntryView.findViewById(R.id.mail_format_qif);
                            RadioButton htmlRadioButton = (RadioButton)textEntryView.findViewById(R.id.mail_format_html);
                            RadioButton xmlRadioButton = (RadioButton)textEntryView.findViewById(R.id.mail_format_xml);
                            RadioButton csvRadioButton = (RadioButton)textEntryView.findViewById(R.id.mail_format_csv);

                            String mailAddress = adr.getEditableText().toString();
                            
                            String mailFormat = null;
                            if (qifRadioButton.isChecked()) {
                            	mailFormat = QIF;
                            }
                            else if (htmlRadioButton.isChecked()) {
                            	mailFormat = HTML;
                            }
                            else if (xmlRadioButton.isChecked()) {
                            	mailFormat = XML;
                            }
                            else if (csvRadioButton.isChecked()) {
                            	mailFormat = CSV;
                            }
                            
                            // store address (private mode) for later re-use
                            SharedPreferences settings = getPreferences(MODE_PRIVATE);
                            SharedPreferences.Editor prefEditor = settings.edit();
                            prefEditor.putString(PREF_MAIL_ADDRESS, mailAddress);
                            prefEditor.putString(PREF_MAIL_FORMAT, mailFormat);
                            prefEditor.commit();

                            String mimeType = "";
                            
                            if (qifRadioButton.isChecked()) {
             					convertedList = Util.convertListToQifFile(getListView());
             					mimeType = "text/plain; charset=ISO-8859-1";
                            }
                            else if (htmlRadioButton.isChecked()) {
                            	convertedList = Util.convertListToHtmlFile(getListView());
                            	mimeType = "text/html; charset=UTF-8";
                            }
                            else if (xmlRadioButton.isChecked()) {
                            	convertedList = Util.convertListToXmlFile(getListView());
                            	mimeType = "text/xml; charset=UTF-8";
                            }
                            else if (csvRadioButton.isChecked()) {
                            	convertedList = Util.convertListToCsvFile(getListView());
                            	mimeType = "text/plain; charset=UTF-8";
                            }
                            else {
                            	// should not happen
                            	convertedList = null;
                            }
                        	
    		            	if (convertedList != null) {

	    	                    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	    		                emailIntent.setType(mimeType);             
	    		                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ mailAddress });
	    		                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
	    		                emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.sendList_eMail_subject));
	    		                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + convertedList.getAbsolutePath()));
	
	       		                startActivityForResult(Intent.createChooser(emailIntent, getResources().getString(R.string.sendList_dialog_title)), REQUEST_CODE_SEND_MAIL);	                
    		            	}
    		            	else {
    		            		// present toast: file conversion failed
    		            	}
    		            	
                        	removeDialog(DIALOG_ID_SEND_MAIL);
                        }
                    })
                    .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	removeDialog(DIALOG_ID_SEND_MAIL);
                        }
                    })
                    .create();
                
                EditText adr = (EditText)textEntryView.findViewById(R.id.username_edit);
                RadioButton qifRadioButton = (RadioButton)textEntryView.findViewById(R.id.mail_format_qif);
                RadioButton htmlRadioButton = (RadioButton)textEntryView.findViewById(R.id.mail_format_html);
                RadioButton csvRadioButton = (RadioButton)textEntryView.findViewById(R.id.mail_format_csv);
                RadioButton xmlRadioButton = (RadioButton)textEntryView.findViewById(R.id.mail_format_xml);

                SharedPreferences settings = getPreferences(MODE_PRIVATE);
                
                // check if mail address already had been entered before
                // if yes, re-use the known address
                String mailAddress = settings.getString(PREF_MAIL_ADDRESS, "");
                adr.setText(mailAddress);	

                // check if mail format already had been entered before
                // if yes, re-use the known format
                String mailFormat = settings.getString(PREF_MAIL_FORMAT, HTML);
                if (HTML.equals(mailFormat)) {
                	htmlRadioButton.setChecked(true);
                }
                else if (XML.equals(mailFormat)) {
                	xmlRadioButton.setChecked(true);
                }
                else if (CSV.equals(mailFormat)) {
                	csvRadioButton.setChecked(true);
                }
                else if (QIF.equals(mailFormat)) {
                	qifRadioButton.setChecked(true);
                }
            
                break;
                
    		default:
        	   
    			d = super.onCreateDialog(id, b);
    			break;
        }
        return d;
	}

	// added for option menu
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the selected menu XML resource.
		getMenuInflater().inflate(R.menu.menu_expense_list_ui, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case R.id.submenu_categories:
				
				Intent categoryIntent = new Intent(ExpenseListUI.this, CategoryListUI.class);
				startActivityForResult(categoryIntent, 0);
                return true;  // menu item was consumed
				
			case R.id.submenu_dateFilters:

				Intent dateFilterIntent = new Intent(ExpenseListUI.this, DateIntervalListUI.class);
				startActivityForResult(dateFilterIntent, 0);
                return true;  // menu item was consumed
				                    
			case R.id.submenu_mail:
				
				showDialog(DIALOG_ID_SEND_MAIL);           		            	
                return true;  // menu item was consumed
                
            default:
            	break;

		}
	        
		return false;
	}
	
	@Override
	protected boolean isDeleteAllAllowed() {
		// all expenses can be deleted
		return true;
	}
}
