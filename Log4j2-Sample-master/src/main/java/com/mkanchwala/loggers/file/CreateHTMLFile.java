package com.mkanchwala.loggers.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;

public class CreateHTMLFile {
	
	final static String COMMON_FILE = "/Library/WebServer/Documents/imagemap/layouthtml/commonlayout.html";
	final static String TEMP_FILE_PATH = "/Library/WebServer/Documents/imagemap/temp/";
	final static String RENAME_FILE_PATH = "/Library/WebServer/Documents/imagemap/layouthtml/";
	static String clubLayoutfileName;
	static ConcurrentHashMap<String, TableRowData> tableDataMap = new ConcurrentHashMap<String, TableRowData>();
	static String clubid;
	static String eventdate;
	
	public static void main(String[] args)
    {	
		
		try
	    {
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			
			String URL= "jdbc:mysql://192.168.43.64:3306/club?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=GMT&verifyServerCertificate=false&useSSL=false";
	        
//	        ConnectionFactory cf = new DriverManagerConnectionFactory(
//	        		URL,//"jdbc:mysql://172.20.10.8:3306/club?verifyServerCertificate=false&useSSL=false",
//	        		"root",
//	        		"vichi123");// local mac
	      // create our mysql database connection
	      //String myDriver = "org.gjt.mm.mysql.Driver";
	      //String myUrl = "jdbc:mysql://localhost/test";
	      //Class.forName(myDriver);
	      Connection conn = DriverManager.getConnection(URL, "root", "vichi123");
	      
	      // our SQL SELECT query. 
	      // if you only need a few columns, specify them by name instead of using "*"
	      String query = "SELECT * FROM tablesdata WHERE clubid = '99999' AND eventdate = '30/Aug/2018' ";

	      // create the java statement
	      Statement st = conn.createStatement();
	      
	      // execute the query, and get a java resultset
	      ResultSet rs = st.executeQuery(query);
	      
	      // iterate through the java resultset
	      while (rs.next())
	      {
	    	  	TableRowData trd = new TableRowData();
	        String clubidx = rs.getString("clubid");
	        clubid = clubidx;
	        trd.setClubid(clubidx);
	        String tablenumber = rs.getString("tablenumber");
	        trd.setTablenumber(tablenumber);
	        String tableid = rs.getString("tableid");
	        trd.setTableid(tableid);
	        String tabletype = rs.getString("tabletype");
	        trd.setTabletype(tabletype);
	        String coords = rs.getString("coords");
	        trd.setCoords(coords);
	        String eventdatex = rs.getString("eventdate");
	        eventdate = eventdatex;
	        trd.setEventdate(eventdatex);
	        String booked = rs.getString("booked");
	        trd.setBooked(booked);
	        String layoutURL = rs.getString("layoutURL");
	        trd.setLayoutURL(layoutURL);
	        tableDataMap.put(tableid, trd);
	        
	      }
	      st.close();
	    }
	    catch (Exception e)
	    {
	      System.err.println("Got an exception! ");
	      System.err.println(e.getMessage());
	    }
		
		createHTML();
	  }
	
	private static void createHTML() {
		BufferedReader reader = null;
        
        FileWriter writer = null;
		
	    	try{
			
			
			File fileToBeModified = new File(COMMON_FILE);
			
			reader = new BufferedReader(new FileReader(fileToBeModified));
            
            //Reading all the lines of input text file into oldContent
            String line = reader.readLine();
            StringBuilder sb = new StringBuilder();
             
            while (line != null ) 
            {
            	if(line.contains("table_layout.png")) {
            		line = line.replace("table_layout.png", clubid+".png");
            	}
            	
            	sb.append(line);
	            	if(line.contains("<map name=\"shapesMap\">") ){
	            		
	            		
	            		Iterator<TableRowData> itr = tableDataMap.values().iterator();
	            		
	            		while(itr.hasNext()) {
	            			TableRowData tableRowData = itr.next();
	            			// create area now
	            			sb.append("<area  id=\""+tableRowData.getBooked()+"\"");
	            			sb.append(" data-mapid=\""+tableRowData.getBooked()+"\"");
	            			if(tableRowData.getBooked().equalsIgnoreCase("booked")) {
	            				sb.append(" data-maphilight='{\"fillColor\":\"d70303\",\"alwaysOn\":true, \"fillOpacity\":0.2, \"strokeColor\":\"d70303\"}' ");
	            			}else if(tableRowData.getBooked().equalsIgnoreCase("available") && tableRowData.getTabletype().equalsIgnoreCase("VIP")) {
	            				sb.append(" data-maphilight='{\"fillColor\":\"027ce8\",\"alwaysOn\":true, \"fillOpacity\":0.5, \"strokeColor\":\"027ce8\"}' ");
	            			}else {
	            				sb.append(" data-maphilight='{\"fillColor\":\"2bc60b\",\"alwaysOn\":true, \"fillOpacity\":0.1, \"strokeColor\":\"2bc60b\"}' ");
	            			}
	            			sb.append(" href=\"#"+tableRowData.getTableid()+"\"");
	            			sb.append(" title=\""+tableRowData.getTableid()+"\"");
	            			sb.append(" alt=\""+tableRowData.getTableid()+"\"");
	            			sb.append(" coords=\""+tableRowData.getCoords()+"\"");
	            			sb.append(" shape=\"rect\"> ");
	            			//sb.append("\n");
	            		}
	            	}
	            
                line = reader.readLine();
            }
             
            long time = System.currentTimeMillis();
            String tempFileName = Long.toString(time);
            
            File tempfile = new File(TEMP_FILE_PATH+tempFileName+".html");
            if (tempfile.createNewFile())
            {
                System.out.println("File is created!");
            } else {
                System.out.println("File already exists.");
            }
             
            //Write Content
            FileWriter writerx = new FileWriter(tempfile);
            writerx.write(sb.toString());
            writerx.close();
            eventdate = eventdate.replaceAll("/", "");
            String destFileName = clubid+"-"+eventdate;
            
            File destfile = new File(RENAME_FILE_PATH+destFileName+".html");
			
			
	    	
			if(tempfile.renameTo(destfile)){
				System.out.println(tempfile.getName() + " is renamed!");
			}else{
				System.out.println("renamed operation is failed.");
			}
		   
		}catch(Exception e){
			
			e.printStackTrace();
			
		}
		
	}
    	
}


