package com.mkanchwala.loggers.main;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class Test {

	public static void main(String[] args) throws ParseException {
//		java.util.Date date = new java.util.Date();
//        java.sql.Timestamp sqlTimeStamp = new java.sql.Timestamp(date.getTime());
//        System.out.println("util-date:" + date);
//        System.out.println("sql-timestamp:" + sqlTimeStamp );
		
		
		String str = "1 couple  and 2 girl and 3 stag is allowed"; 
		HashMap<String, Integer> passMap = new HashMap<String, Integer>();
//		str = str.replaceAll("[^0-9]+", " ");
//		System.out.println(Arrays.asList(str.trim().split(" ")));
		
		String[] keys = {"couple","girl","stag" };
		for(int i= 0; i < keys.length; i++) {
			int index = str.indexOf(keys[i]);
			if(index != -1) {
				Integer value  = Integer.valueOf(Character.toString(str.charAt(index - 2)));
				passMap.put(keys[i], value);
				
			}
			System.out.println(str.charAt(index - 2));
			System.out.println(passMap);
		}

	  }

}
