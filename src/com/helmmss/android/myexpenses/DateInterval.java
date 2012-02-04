package com.helmmss.android.myexpenses;

import java.util.Calendar;

public class DateInterval {
	
	public long id = -1;
	public String name = null;
	public Calendar from = null;
	public Calendar to = null;
	public boolean userDefined = false;
	
	public DateInterval() {}

	public DateInterval(long id, String name, Calendar dateFrom, Calendar dateTo, boolean userDefined) {
		this.id = id;
		this.name = name; 
		this.from = dateFrom;
		this.to = dateTo;
		this.userDefined = userDefined;
	}
}
