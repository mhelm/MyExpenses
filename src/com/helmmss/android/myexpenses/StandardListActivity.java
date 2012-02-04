/**
 * 
 */
package com.helmmss.android.myexpenses;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class StandardListActivity extends ListActivity {
	
    public static final String ITEM_ID = "ItemId";
    public static final String ITEM_ID_PARAM_NAME = "ItemIdParameterName";

    private static final int ID_ACTION_DIALOG = 0;
    private static final int ID_ACTION_CONFIRM_SINGLE = 1;
    private static final int ID_ACTION_CONFIRM_ALL = 2;

    private LinearLayout header = null;
    private TextView headerTextLeft = null; 
	private TextView headerTextRight = null; 
	private Button addButton = null; 
	
	private String[] actionDialog_items = null;
//	private String actionDialog_title = "";
	
	private Parcelable state = null;
	
	private long itemInFocusID = 0L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_standard_list);

		header = (LinearLayout)findViewById(R.id.standardList_header); 
		headerTextLeft = (TextView)findViewById(R.id.standardList_headerTextLeft); 
		headerTextRight = (TextView)findViewById(R.id.standardList_headerTextRight); 		
		addButton = (Button) findViewById(R.id.standardList_buttonsAddButton);
		
		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
				onClickListItem(parent, v, pos, id);
			}
		});
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {
				return onLongClickListItem(parent, v, pos, id);
			}
		});
        addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onClickAddButton(v);
			}
		});
        header.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	onClickHeader(v);
	       }
	    });
	}
	
	protected Dialog onCreateDialog(int id, final Bundle b) {
		
		// save ListView state
		state = getListView().onSaveInstanceState();
		
		// create the (alert) dialog covering a title and the created list of actions
		Dialog d = null;
		switch (id) {
		case ID_ACTION_DIALOG:
			
			// build list of action items the user might select (like EDIT, DELETE, ...)
			ArrayList<String> actionDialog_itemsList = new ArrayList<String>();
			// only add action items to the list that are applicable for the selected list item
			long itemId = b.getLong(ITEM_ID);
			if (isEditAllowed(itemId)) {
				actionDialog_itemsList.add(getResources().getString(R.string.standard_action_edit));
			}
			if (isDeleteAllowed(itemId)) {
				actionDialog_itemsList.add(getResources().getString(R.string.standard_action_delete));
			}
			if (isDeleteAllAllowed()) {
				actionDialog_itemsList.add(getResources().getString(R.string.standard_action_deleteAll));
			}
			
			// convert arrayList to array
			actionDialog_items = (String[])actionDialog_itemsList.toArray(new String[actionDialog_itemsList.size()]);

			d = new AlertDialog.Builder(this)
					//.setTitle(actionDialog_title)
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {
								public void onCancel(DialogInterface dialog) {
									removeDialog(ID_ACTION_DIALOG);
								}
							})	
					.setItems(actionDialog_items,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// check which action item has been clicked 
									String selected = actionDialog_items[whichButton];
									// since action dialog is build dynamically
									// we need to check the content at the clicked position -
									// no only the position itself - because the content at position x might
									// be different at each call
									if (selected.equals(getResources().getString(R.string.standard_action_edit))) {
										// the EDIT action item has been clicked
										onClickItemEdit(b.getLong(ITEM_ID));
									}  
									else if (selected.equals(getResources().getString(R.string.standard_action_delete))) {
										// the DELETE action item has been clicked
										showDialog(ID_ACTION_CONFIRM_SINGLE, b);
									}
									else if (selected.equals(getResources().getString(R.string.standard_action_deleteAll))) {
										// the DELETE action item has been clicked
										showDialog(ID_ACTION_CONFIRM_ALL);
									}
									// action dialog is build dynamically - it is not fixed and can not be cached
									// so it needs to be deleted after using it to get rebuild next time
									removeDialog(ID_ACTION_DIALOG);
								}
							}).create();	
			break;

		case ID_ACTION_CONFIRM_SINGLE:
			
            d = new AlertDialog.Builder(this)
            .setIcon(R.drawable.alert_dialog_icon)
            .setTitle(R.string.deleteElement_dialog_title)
            .setMessage(R.string.deleteElement_dialog_warning_single)
            .setOnCancelListener(
        		new DialogInterface.OnCancelListener() {
        			public void onCancel(DialogInterface dialog) {
                    	removeDialog(ID_ACTION_CONFIRM_SINGLE);
        			}
        		})
            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {            	
                	onClickItemDelete(b.getLong(ITEM_ID));
                	removeDialog(ID_ACTION_CONFIRM_SINGLE);
                }
            })
            .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	removeDialog(ID_ACTION_CONFIRM_SINGLE);
                }
            })
            .create();
			break;			
			
		case ID_ACTION_CONFIRM_ALL: 
			
			String title = null;
			int count = getListView().getCount();
			if (count == 1) {
				title = getString(R.string.deleteElement_dialog_warning_single);				
			} else {
				title = getString(R.string.deleteElement_dialog_warning_multiple);
				title = title.replace("$1", String.valueOf(count));
			}
			
            d = new AlertDialog.Builder(this)
            .setIcon(R.drawable.alert_dialog_icon)
            .setTitle(R.string.deleteElement_dialog_title)
            .setMessage(title)
            .setOnCancelListener(
        		new DialogInterface.OnCancelListener() {
        			public void onCancel(DialogInterface dialog) {
                    	removeDialog(ID_ACTION_CONFIRM_ALL);
        			}
        		})
            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {            	
                	onClickItemDeleteAll();
                	removeDialog(ID_ACTION_CONFIRM_ALL);
                }
            })
            .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	removeDialog(ID_ACTION_CONFIRM_ALL);
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
	
	protected void displayItems() {

		if (state != null) {
			// restore selected item index and scroll position 
			getListView().onRestoreInstanceState(state);
			state = null;
		}
		
		else if (itemInFocusID != 0L) {			
			for (int i = 0; i < getListView().getCount(); i++) {
				long id = getListView().getItemIdAtPosition(i);
				if (itemInFocusID == id) {
					getListView().setSelection(i);
					break;
				}
			}
			itemInFocusID = 0L;
		} 
	}
	
	// overwrite this function if item must not be deleted (return false)
	protected boolean isDeleteAllowed(long itemId) {
		return true;
	}
	
	// overwrite this function if all items (in list) can be deleted (return true)
	protected boolean isDeleteAllAllowed() {
		return false;
	}

	// overwrite this function if item must not be edited (return false)
	protected boolean isEditAllowed(long itemId) {
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (resultCode) {
		case Activity.RESULT_OK:
			itemInFocusID = getItemId(ITEM_ID_PARAM_NAME, data);
			break;
		case Activity.RESULT_CANCELED:
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	protected void onClickListItem(AdapterView<?> parent, View v, int pos, long id) {
		
		Bundle args = new Bundle();
		args.putLong(ITEM_ID, id);				
		showDialog(ID_ACTION_DIALOG, args);
	}

	protected boolean onLongClickListItem(AdapterView<?> parent, View v, int pos, long id) {
		return false;
	}

	protected void onClickItemEdit(long itemID) {

	}

	protected void onClickItemDelete(long itemID) {

	}
	
	protected void onClickItemDeleteAll() {
		
	}
	
	protected void onClickAddButton(View v) {
        
	}
		
	protected void onClickHeader(View v) {
        
	}

	protected void setHeaderTextLeft(int ressourceID){
		headerTextLeft.setText(ressourceID);
	}

	protected void setHeaderTextLeft(String text){
		headerTextLeft.setText(text);
	}
	
	protected void setHeaderTextLeftColor(int color){
		headerTextLeft.setTextColor(color);
	}

	protected void setHeaderTextRight(int ressourceID){
		headerTextRight.setText(ressourceID);
	}

	protected void setHeaderTextRight(String text){
		headerTextRight.setText(text);
	}

	protected void setActionDialogTitle(int ressourceID){
//		actionDialog_title = getResources().getString(ressourceID);
	}

	private long getItemId(String itemIdParameterName, Intent data) {
		
		if (data != null && data.hasExtra(itemIdParameterName)) {
			String name = data.getExtras().getString(itemIdParameterName);
			return data.getExtras().getLong(name);
		}
		
		return 0L;
	}
}

