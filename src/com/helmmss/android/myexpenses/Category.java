package com.helmmss.android.myexpenses;

public class Category {
	
	public long id = -1;
	public String name = null;
    public boolean userDefined = false;
	
	public Category(long id, String name, boolean userDefined) {
		this.id = id;
		this.name = name; 
		this.userDefined = userDefined;
	}

}
