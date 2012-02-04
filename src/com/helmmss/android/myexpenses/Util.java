package com.helmmss.android.myexpenses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;
import android.widget.ListView;

public class Util {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdf_qif = new SimpleDateFormat("M.d.yyyy");
	
	////////////////////////////////////////////////////////////////////
	// DATE conversion
	
	public static String formatDateForView(Calendar d) {
		
		if (d == null) {
			return "";
		}
		
		return DateFormat.getDateInstance().format(d.getTime());
	}

	public static String formatDateForQif(Calendar d) {
		
		if (d == null) {
			return "";
		}
		
		return sdf_qif.format(d.getTime());
	}

	public static Calendar parseDateString(String s) {
		
		// expected format of s: "yyyy-MM-dd"
		
		if (s == null || s.length() == 0 ) {
			return null;
		}

		Calendar c = Calendar.getInstance();
		
		try {
			c.setTime(sdf.parse(s));
		} catch (ParseException e) {
			// just return c - see below
		}
		
		return c;
	}
	
	public static String formatDateForDB(Calendar d) {
		
		if (d == null) {
			return "";
		}
		
		return sdf.format(d.getTime());
	}

	public static String formatDateForView(String s) {

		// expected format of s: "yyyy-MM-dd"
		
		Date d = null;
		
		try {
			d = sdf.parse(s);
		} catch (ParseException e) {
			return "";
		}
		
		return DateFormat.getDateInstance().format(d);
	}

	////////////////////////////////////////////////////////////////////
	// AMOUNT conversion

	public static String formatAmountForView(double d) {
		
		// A.
		
		// get the local currency format (default locale) ...
		// with the currency symbol ...
//		NumberFormat nf = NumberFormat.getCurrencyInstance();
//		return nf.format(d);
		
		// B.
		
		// get the local currency format (default locale) ...
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		// but create an own decimal format without the currency symbol ...
		DecimalFormat df = new DecimalFormat();
		// but correct lengths 
		df.setMaximumFractionDigits(nf.getMaximumFractionDigits());
		df.setMaximumIntegerDigits(nf.getMaximumIntegerDigits());
		df.setMinimumFractionDigits(nf.getMinimumFractionDigits());
		df.setMinimumIntegerDigits(nf.getMinimumIntegerDigits());
		
		return df.format(d);
		
		// C.
		
		// to use an other currency - not the one matching the locale
		// use the ISO 4217 code of the currency
		// https://secure.wikimedia.org/wikipedia/de/wiki/ISO_4217
		
//		NumberFormat nf = NumberFormat.getCurrencyInstance();
//		nf.setCurrency(Currency.getInstance("EUR"));
//		return nf.format(d);
	}
		
	public static String formatAmountForView(String s) {
		
		double d = 0D;
				
		if (s != null && s.length() != 0) {
			
			try {
				d = Double.parseDouble(s);
			} catch (NumberFormatException e) {
				return "";
			}
		}
		
		return formatAmountForView(d);
	}
	
//	public static String formatMethodOfPaymentForView(Context context, String s) {
//		
//		String methodOfPayment = "";
//		
//		if (s.equals(ExpenseTable.MethodOfPayment.CASH)) {
//			methodOfPayment = context.getResources().getString(R.string.cash);
//		} else if (s.equals(ExpenseTable.MethodOfPayment.CARD)) {
//			methodOfPayment = context.getResources().getString(R.string.card);
//		}
//
//		return methodOfPayment;
//	}
	
	public static File convertListToHtmlFile(ListView lv){
        
        String applName = lv.getContext().getString(R.string.app_name);

        File file = null;
        
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalRoot = Environment.getExternalStorageDirectory();
            File appDir = new File(externalRoot, applName);
            File appTmpDir = new File(appDir, "tmp");
            appTmpDir.mkdirs(); // create the directories ...
            file = new File(appTmpDir, applName + ".html");
        }

        FileWriter filewriter = null; 
    	
        try {
			if (file.exists()) {
				file.delete();
			}
        	file.createNewFile();
        	filewriter = new FileWriter(file);
		} catch (IOException e1) {
			return null;
		}
        		
        try {
		
			filewriter.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\""); 
			filewriter.append("\"http://www.w3.org/TR/html4/strict.dtd\">");
			filewriter.append("<html>");
			filewriter.append("<head>");
			filewriter.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
			filewriter.append("<title>").append(applName).append("</title>");
			filewriter.append("</head>");
			filewriter.append("<body>");
			filewriter.append("<div style=\"font-size:80%; font-family:Verdana\">");
			
			filewriter.append("<table border=\"0\" cellpadding=\"2px\">");
			filewriter.append("<tbody>");
        	        	
        	for (int i = 0; i < lv.getCount(); i++) {
        		
        		Object o = lv.getItemAtPosition(i);
        		
        		if (o instanceof Expense) {
        			
        			Expense expense = (Expense) o;
        			filewriter.append(toHtmlTableRow(lv.getContext(), expense));
				}
			}
        	
        	filewriter.append("</tbody>");
        	filewriter.append("</table>");
        	filewriter.append("</div>");
        	filewriter.append("</body>");
        	filewriter.append("</html>");

        	filewriter.close();
            
            return file; 
            
        } catch (Exception e) {
			return null;
        } 
    }

	public static File convertListToQifFile(ListView lv){
		       
        String applName = lv.getContext().getString(R.string.app_name);
        File file = null;
        
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalRoot = Environment.getExternalStorageDirectory();
            File appDir = new File(externalRoot, applName);
            File appTmpDir = new File(appDir, "tmp");
            appTmpDir.mkdirs(); // create the directories ...
            file = new File(appTmpDir, applName + ".QIF");
        }

        OutputStreamWriter out = null;
    	
        try {
            
			if (file.exists()) {
				file.delete();
			}
        	
        	file.createNewFile();
			String path = file.getAbsolutePath();
			out = new OutputStreamWriter(new FileOutputStream(path),"ISO-8859-1");
		} catch (IOException e1) {
			return null;
		}
        
        try {
        	
        	out.append("!Type:Cash\n");
        	        	
        	for (int i = 0; i < lv.getCount(); i++) {
        		
        		Object o = lv.getItemAtPosition(i);
        		
        		if (o instanceof Expense) {
        			
        			Expense expense = (Expense) o;
        			
        			if (ExpenseTable.MethodOfPayment.CASH.equals(expense.type)) {
        				out.append(toQifEntry(lv.getContext(), expense));
        			}
				}
			}

        	out.close();
            
            return file; 
            
        } catch (Exception e) {
			return null;
        } 
    }
		
	public static String formatTypeForView(Context context, String type) {
		
		if (ExpenseTable.MethodOfPayment.CARD.equals(type)) {
			return context.getResources().getString(R.string.expense_type_card);
		}
		if (ExpenseTable.MethodOfPayment.CASH.equals(type)) {
			return context.getResources().getString(R.string.expense_type_cash);
		}
		else {
			return "";
		}
	}
		
	public static File convertListToXmlFile(ListView lv) {
		
        String applName = lv.getContext().getString(R.string.app_name);
        File file = null;
        
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalRoot = Environment.getExternalStorageDirectory();
            File appDir = new File(externalRoot, applName);
            File appTmpDir = new File(appDir, "tmp");
            appTmpDir.mkdirs(); // create the directories ...
            file = new File(appTmpDir, applName + ".xml");
        }

		FileWriter filewriter = null;
    	
        try {
            
			if (file.exists()) {
				file.delete();
			}
        	
        	file.createNewFile();
			filewriter = new FileWriter(file);
		} catch (IOException e1) {
			return null;
		}

		XmlSerializer serializer = Xml.newSerializer();

		try {

			serializer.setOutput(filewriter);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "expenses");
			serializer.attribute("", "count", String.valueOf(lv.getCount()));

        	for (int i = 0; i < lv.getCount(); i++) {
        		
        		Object o = lv.getItemAtPosition(i);
        		
        		if (o instanceof Expense) {
        			
        			Expense expense = (Expense) o;
        			toXmlNode(lv.getContext(), serializer, expense);
				}
			}
			
			serializer.endTag("", "expenses");
			serializer.endDocument();
			filewriter.close();

			return file; 

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static File convertListToCsvFile(ListView lv) {

        String applName = lv.getContext().getString(R.string.app_name);
        File file = null;
        
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalRoot = Environment.getExternalStorageDirectory();
            File appDir = new File(externalRoot, applName);
            File appTmpDir = new File(appDir, "tmp");
            appTmpDir.mkdirs(); // create the directories ...
            file = new File(appTmpDir, applName + ".csv");
        }

        OutputStreamWriter out = null;
    	
        try {
            
			if (file.exists()) {
				file.delete();
			}
        	
        	file.createNewFile();
			String path = file.getAbsolutePath();
			out = new OutputStreamWriter(new FileOutputStream(path),"UTF-8");
		} catch (IOException e1) {
			return null;
		}
        
        try {
        	
        	for (int i = 0; i < lv.getCount(); i++) {
        		
        		Object o = lv.getItemAtPosition(i);
        		
        		if (o instanceof Expense) {
        			
        			Expense expense = (Expense) o;
    				out.append(toCsvLine(lv.getContext(), expense));
				}
			}

        	out.close();
            
            return file; 
            
        } catch (Exception e) {
			return null;
        } 
    }
	
	private static String toHtmlTableRow(Context context, Expense expense) {
		
		StringBuilder b = new StringBuilder();

		b.append("<tr>");
		b.append("<td>").append(Util.formatDateForView(expense.date)).append("</td>");
		b.append("<td>").append(Util.formatTypeForView(context, expense.type)).append("</td>");
		b.append("<td>").append(Util.formatAmountForView(expense.amount)).append("</td>");
		b.append("<td>").append(expense.category).append("</td>");
		b.append("<td>").append(expense.description).append("</td>");
		b.append("</tr>");
		
		return b.toString();
	}	

	private static String toCsvLine(Context context, Expense expense) {
				
		StringBuilder b = new StringBuilder();
		
		String sep = ";";

		b.append(Util.formatDateForView(expense.date)).append(sep);
		b.append(Util.formatTypeForView(context, expense.type)).append(sep);
		b.append(Util.formatAmountForView(expense.amount)).append(sep);
		b.append(expense.category).append(sep);
		b.append(expense.description).append(sep);
		b.append("\n");
		
		return b.toString();
	}
	
	private static String toQifEntry(Context context, Expense expense) {
		
//		QIF = Quicken Interchange Format
//		
// 		Example (one expense in QIF format):
//
//		D12.17.11
//		PBerlin
//		T-7.50
//		LLebensmittel
//		^
		
		StringBuilder b = new StringBuilder();

		b.append("D").append(Util.formatDateForQif(expense.date)).append("\n");
		b.append("T-").append(expense.amount).append("\n");  // note the "-": means "expense"
		b.append("L").append(expense.category).append("\n");
		
		if (expense.description != null && expense.description.length() != 0) {
			b.append("P").append(expense.description).append("\n");
		}
		else {
			b.append("P").append(expense.category).append("\n");
		}
		b.append("^").append("\n");
		
		return b.toString();
	}

	private static void toXmlNode(Context context, XmlSerializer serializer, Expense expense) {
		
		try {
			serializer.startTag("", "expense");
		} catch (Exception e) {
			return;
		}	
		
		try {
			
			serializer.attribute("", "date", Util.formatDateForView(expense.date));
			serializer.attribute("", "methodOfPayment", Util.formatTypeForView(context, expense.type));
			serializer.attribute("", "amount", Util.formatAmountForView(expense.amount));
			serializer.attribute("", "category", expense.category);
			serializer.attribute("", "description", expense.description);
			
		} catch (Exception e) {
			// try to add the end tag (see below)
		}		

		try {
			serializer.endTag("", "expense");
		} catch (Exception e) {
			return;
		}	
	}	

}
