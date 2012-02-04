package com.helmmss.android.myexpenses;

import java.util.Calendar;

public class Expense {
	
	public long id = -1;
	public Calendar date = null;
	public String amount = null;
	public String category = null;
	public String description = null;
	public String type = null;
		
	public Expense() {
		init(id, Calendar.getInstance(), "", "", "", "");
	}

	public Expense(long id, Calendar date, String amount, String category, String description, String type) {
		init(id, date, amount, category, description, type);
	}
	
	private void init(long id, Calendar date, String amount, String category, String description, String type) {
		this.id = id;
		this.date = date; 
		this.amount = amount;
		this.category = category;
		this.description = description;
		this.type = type;
	}
}
