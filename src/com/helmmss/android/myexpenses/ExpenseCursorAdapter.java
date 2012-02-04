package com.helmmss.android.myexpenses;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ExpenseCursorAdapter extends SimpleCursorAdapter {
	
//	private boolean filtered = false;
	
	public ExpenseCursorAdapter(Context context, int layout, 
			Cursor c, String[] from, int[] to, boolean filtered) {

		super(context, layout, c, from, to);
//		this.filtered = filtered;
	}
		
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = super.getView(position, convertView, parent);
		
//		if (filtered) {
//			v.setBackgroundResource(R.color.wheat1);
//		}
		
		return v; 
	}
	
	@Override
	public void setViewImage (ImageView v, String value) {
		
		if (value.equals(ExpenseTable.MethodOfPayment.CARD)) {
			v.setImageDrawable(ExpenseListUI.cardDrawable);	
		}
		else if (value.equals(ExpenseTable.MethodOfPayment.CASH)) {
			v.setImageDrawable(ExpenseListUI.cashDrawable);
		}
	}

	@Override
	public void setViewText (TextView v, String text) {
		
		// format amount from string used in DB to localized string on screen
		if (v.getId() == R.id.standardListItem4_textUpperRight) {
			text = Util.formatAmountForView(text);
		}

		// convert date from yyyy-mm-dd to localized string on screen
		else if (v.getId() == R.id.standardListItem4_textLowerLeft) {
			text = Util.formatDateForView(text);
		}
		
		super.setViewText(v, text);
	}
	
	@Override
	public Object getItem(int position){
		
		Cursor c = getCursor();
		
		if (c.moveToPosition(position)) {
			return ExpenseTable.getExpense(c);
		} 
		else {
			return null;
		}
		
		// return super.getItem(position);
	}
}
