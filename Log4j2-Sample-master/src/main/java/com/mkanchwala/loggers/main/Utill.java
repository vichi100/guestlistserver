package com.mkanchwala.loggers.main;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.UUID;

public class Utill {
	
	
	public static  String getTodayDate(){

        TimeZone tz = TimeZone.getTimeZone("Asia/Kolkata");

        //TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        Calendar c = Calendar.getInstance(tz);
        Date todayDate = c.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
        String formattedDate = df.format(todayDate);
        return formattedDate;

    }
	
	public static java.sql.Timestamp getCurrentTime() throws ParseException {
		java.util.Date date = new java.util.Date();
        java.sql.Timestamp sqlTimeStamp = new java.sql.Timestamp(date.getTime());
        return sqlTimeStamp;

		
	}
	
	
	public static String createNewCustomerId() {
		
		UUID idOne = UUID.randomUUID();
        String str=""+idOne;        
//        int uid=str.hashCode();
//        String filterStr=""+uid;
//        str=filterStr.replaceAll("-", "");
        return str;
		
		
	}
	
	
	public static HashMap<String, Integer> getPassMapFromTicketDetails(String ticketDetails){
		//String str = "1 couple  and 2 girl and 3 stag is allowed"; 
		String[] keys = {"couple","girl","stag" };
		HashMap<String, Integer> passMap = new HashMap<String, Integer>();
		for(int i= 0; i < keys.length; i++) {
			int index = ticketDetails.indexOf(keys[i]);
			if(index != -1) {
				Integer value  = Integer.valueOf(Character.toString(ticketDetails.charAt(index - 2)));
				passMap.put(keys[i], value);
				
			}
//			System.out.println(ticketDetails.charAt(index - 2));
//			System.out.println(passMap);
		}
		
		return passMap;
	}
	
	
	public static String changeDateFormate(String dateStr){
		DateFormat inDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat outDateFormat = new SimpleDateFormat("dd/MMM/yyyy");
        Date MyDate=null;
        String newDateFormat = null;
        try{
            MyDate = inDateFormat.parse(dateStr);
            newDateFormat = outDateFormat.format(MyDate);

        }catch (Exception ex){
        		ex.printStackTrace();
        }
        
       
        return newDateFormat;
    }
	
	public static void main(String[] arg) {
		System.out.println(createNewCustomerId());
	}

}
