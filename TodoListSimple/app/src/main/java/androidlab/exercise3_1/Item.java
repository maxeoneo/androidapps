package androidlab.exercise3_1;

import java.util.GregorianCalendar;


public class Item {
	private long id;
	private String name;
	private GregorianCalendar deadline;
	private boolean done;
	
	public Item(long id, String name) {
		this.id = id;
		this.name = name;
		this.done = false;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public GregorianCalendar getDeadline() {
		return deadline;
	}
	
	public String getDeadlineAsString() {
		int intHour = deadline.get(GregorianCalendar.HOUR_OF_DAY);
		int intMin = deadline.get(GregorianCalendar.MINUTE);
		int intMonth = deadline.get(GregorianCalendar.MONTH) + 1;
		int intDay = deadline.get(GregorianCalendar.DAY_OF_MONTH);
		
		String strHour = intHour + "";
		String strMin = intMin + "";
		String strMonth = intMonth + "";
		String strDay = intDay + "";
		
		if (intHour < 10) {
			strHour = "0" + intHour;
		}
		if (intMin < 10) {
			strMin = "0" + intMin;
		}
		if (intMonth < 10) {
			strMonth = "0" + intMonth;
		}
		if (intDay < 10) {
			strDay = "0" + intDay;
		}
		
		return deadline.get(GregorianCalendar.YEAR) + "-" + strMonth 
				+ "-" + strDay + " " + strHour + ":" + strMin;	
	}
	
	public void setDeadline(GregorianCalendar deadline) {
		this.deadline = deadline;
	}
	public long getId() {
		return id;
	}
	public void setDone(boolean done) {
		this.done = done;
	}
	public boolean getDone() {
		return done;
	}
	
	public String toString() {
		return name;
	}
	
}
