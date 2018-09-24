package com.mkanchwala.loggers.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mkanchwala.loggers.file.TableRowData;
import com.mkanchwala.loggers.gmap.ApartmentClient;
import com.mkanchwala.loggers.gmap.DirectionResponse;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;

public class DBHelper {
	
	static Logger log = LogManager.getLogger(ClubServer.class.getName());

	/**
	 * @param args
	 * 
	 *            https://www.linode.com/docs/databases/mysql/how-to-install-
	 *            mysql-on-centos-6
	 * 
	 */
	
	private GenericObjectPool connectionPool = null;
	

	

	public DataSource setUp() throws Exception{
	

		
			Class.forName("com.mysql.jdbc.Driver").newInstance();;
		

			log.info("MySQL JDBC Driver Registered!");

		
			
			connectionPool = new GenericObjectPool();
	        connectionPool.setMaxActive(100);
	        
	        ConnectionFactory cf = new DriverManagerConnectionFactory(
	        		"jdbc:mysql://199.180.133.121:3306/club",
	        		"root",
	        		"vichi123");// on server
//	        
//	        String URL= "jdbc:mysql://192.168.0.5:3306/club?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=GMT&verifyServerCertificate=false&useSSL=false";
//	        
//	        ConnectionFactory cf = new DriverManagerConnectionFactory(
//	        		URL,//"jdbc:mysql://172.20.10.8:3306/club?verifyServerCertificate=false&useSSL=false",
//	        		"root",
//	        		"vichi123");// local mac
	        
	        PoolableConnectionFactory pcf =
	                new PoolableConnectionFactory(cf, connectionPool,
	                        null, null, false, true);
	        log.info("You made it, take control your database now!");
	        return new PoolingDataSource(connectionPool);
			
//			connection = DriverManager.getConnection(
//					"jdbc:mysql://199.180.133.121:3306/club", "root",
//					"vichi123");

		

		
	}
	
	public GenericObjectPool getConnectionPool() {
        return connectionPool;
    }

	public List<JSONObject> loadClubListFromDatabase(JSONObject jObj, DataSource ds) throws SQLException {

		log.info("start executing loadClubListFromDatabase method");
		log.info("Request parameter: "+jObj.toString());
		
		StringBuffer latlongsb = new StringBuffer();
		
		Connection connection = ds.getConnection();
		
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		List<JSONObject> clubList = new ArrayList<JSONObject>();

		String cityx = "mumbai";//jObj.getString(Constants.CITY);//"mumbai";// jObj.getString("productType");
		String userLatLong = jObj.getString(Constants.LAT_LONG);//"19.247720,72.850071";//
		// String productOrder = jObj.getString("productType");
		try {
			preparedStatement = connection
					.prepareStatement("SELECT * FROM clubdetails WHERE city = ?");
			preparedStatement.setString(1, cityx);
			resultSet = preparedStatement.executeQuery();
			
			int addCount = 0;

			while (resultSet.next()) {
				JSONObject clubObj = new JSONObject();
				
				if(addCount != 0) {
					latlongsb.append("|");
				}
				
				String clubid = resultSet.getString(Constants.CLUB_ID);
				clubObj.put(Constants.CLUB_ID, clubid);
				String clubname = resultSet.getString(Constants.CLUB_NAME);
				clubObj.put(Constants.CLUB_NAME, clubname);
				String city = resultSet.getString(Constants.CITY);
				clubObj.put(Constants.CITY, city);
				String location = resultSet.getString(Constants.LOACTION);
				clubObj.put(Constants.LOACTION, location);
				String address = resultSet.getString(Constants.ADDRESS);
				clubObj.put(Constants.ADDRESS, address);
				String imageURL = resultSet.getString(Constants.IMAGE_URL);
				clubObj.put(Constants.IMAGE_URL, imageURL);
				String videoURL = resultSet.getString(Constants.VIDEO_URL);
				clubObj.put(Constants.VIDEO_URL, videoURL);
				String latlong = resultSet.getString(Constants.LAT_LONG);
				latlongsb.append(latlong);
				clubObj.put(Constants.LAT_LONG, latlong);
				String rating = resultSet.getString(Constants.RATING);
				clubObj.put(Constants.RATING, rating);
				
				clubList.add(clubObj);
				addCount++;
			}
			// find distance from current location
			ApartmentClient wc = new ApartmentClient();
			DirectionResponse direction = wc.getDirectionInfo(userLatLong,latlongsb.toString());
			
			for(int i=0; i< addCount-1; i++) {
				JSONObject clubObj = clubList.get(i);
				clubObj.put(Constants.DISTANCE, direction.getRows().get(0).getElements().get(i).getDistance().getValue());
				//System.out.println("Distance  in metr : "+direction.getRows().get(0).getElements().get(i).getDistance().getValue());
			}
			
			
			clubList.sort(new Comparator<JSONObject>() {
				
				@Override
				public int compare(JSONObject clubObj1, JSONObject clubObj2) {
					if(Float.valueOf(clubObj1.getString(Constants.DISTANCE)) == Float.valueOf(clubObj2.getString(Constants.DISTANCE))) {
						return 0;
					}
					return (int) (Float.valueOf(clubObj1.getString(Constants.DISTANCE)) - Float.valueOf(clubObj2.getString(Constants.DISTANCE))) ;
				}
			});
			

			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e.getMessage());
		}finally {
            if (preparedStatement != null) {
            	preparedStatement.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		
		log.info("end executing loadClubListFromDatabase method, sent data");
		return clubList;

	}
	
	
	public List<JSONObject> getTicketdetailsFromDatabase(JSONObject jObj, DataSource ds) throws SQLException {

		log.info("start executing getTicketdetailsFromDatabase method");
		log.info("Request parameter: "+jObj.toString());

		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Connection connection = ds.getConnection();

		List<JSONObject> ticketDetailsList = new ArrayList<JSONObject>();

		String clubidx = jObj.getString(Constants.CLUB_ID);// jObj.getString("productType");
		// String productOrder = jObj.getString("productType");
		try {
			preparedStatement = connection
					.prepareStatement("SELECT * FROM ticketdetails WHERE clubid = ?");
			preparedStatement.setString(1, clubidx);
			resultSet = preparedStatement.executeQuery();
			
			
			while (resultSet.next()) {
				JSONObject ticketDetailsObj = new JSONObject();
				
				String clubid = resultSet.getString(Constants.CLUB_ID);
				ticketDetailsObj.put(Constants.CLUB_ID, clubid);
				String clubname = resultSet.getString(Constants.CLUB_NAME);
				ticketDetailsObj.put(Constants.CLUB_NAME, clubname);
				String type = resultSet.getString(Constants.TICKET_TYPE);
				ticketDetailsObj.put(Constants.TICKET_TYPE, type);
				String size = resultSet.getString(Constants.SIZE);
				ticketDetailsObj.put(Constants.SIZE, size);
				String category = resultSet.getString(Constants.CATEGORY);
				ticketDetailsObj.put(Constants.CATEGORY, category);
				String cost = resultSet.getString(Constants.COST);
				ticketDetailsObj.put(Constants.COST, cost);
				String details = resultSet.getString(Constants.DETAILS);
				ticketDetailsObj.put(Constants.DETAILS, details);
				String Day = resultSet.getString(Constants.DAY);
				ticketDetailsObj.put(Constants.DAY, Day);
				String date = resultSet.getString(Constants.DATE);
				ticketDetailsObj.put(Constants.DATE, date);
				String totaltickets = resultSet.getString(Constants.TOTAL_TICKETS);
				ticketDetailsObj.put(Constants.TOTAL_TICKETS, totaltickets);
				String availbletickets = resultSet.getString(Constants.AVAILBLE_TICKETS);
				ticketDetailsObj.put(Constants.AVAILBLE_TICKETS, availbletickets);
				ticketDetailsList.add(ticketDetailsObj);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e.getMessage());
		}finally {
            if (preparedStatement != null) {
            	preparedStatement.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		
		log.info("end executing getTicketdetailsFromDatabase method, sent data");
		return ticketDetailsList;

	}
	
	
	
	public List<JSONObject> getTicketdetailsByDateFromDatabase(JSONObject jObj, DataSource ds) throws SQLException {

		log.info("start executing getTicketdetailsFromDatabase method");
		log.info("Request parameter: "+jObj.toString());

		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Connection connection = ds.getConnection();

		List<JSONObject> ticketDetailsList = new ArrayList<JSONObject>();

		String clubidx = jObj.getString(Constants.CLUB_ID);// jObj.getString("productType");
		String datex = jObj.getString(Constants.DATE);
		try {
			preparedStatement = connection
					.prepareStatement("SELECT * FROM ticketdetails WHERE clubid = ? and date = ?");
			preparedStatement.setString(1, clubidx);
			preparedStatement.setString(2, datex);
			resultSet = preparedStatement.executeQuery();
			
			
			while (resultSet.next()) {
				JSONObject ticketDetailsObj = new JSONObject();
				
				String clubid = resultSet.getString(Constants.CLUB_ID);
				ticketDetailsObj.put(Constants.CLUB_ID, clubid);
				String clubname = resultSet.getString(Constants.CLUB_NAME);
				ticketDetailsObj.put(Constants.CLUB_NAME, clubname);
				String type = resultSet.getString(Constants.TICKET_TYPE);
				ticketDetailsObj.put(Constants.TICKET_TYPE, type);
				String size = resultSet.getString(Constants.SIZE);
				ticketDetailsObj.put(Constants.SIZE, size);
				String category = resultSet.getString(Constants.CATEGORY);
				ticketDetailsObj.put(Constants.CATEGORY, category);
				String cost = resultSet.getString(Constants.COST);
				ticketDetailsObj.put(Constants.COST, cost);
				String details = resultSet.getString(Constants.DETAILS);
				ticketDetailsObj.put(Constants.DETAILS, details);
				String Day = resultSet.getString(Constants.DAY);
				ticketDetailsObj.put(Constants.DAY, Day);
				String date = resultSet.getString(Constants.DATE);
				ticketDetailsObj.put(Constants.DATE, date);
				String totaltickets = resultSet.getString(Constants.TOTAL_TICKETS);
				ticketDetailsObj.put(Constants.TOTAL_TICKETS, totaltickets);
				String availbletickets = resultSet.getString(Constants.AVAILBLE_TICKETS);
				ticketDetailsObj.put(Constants.AVAILBLE_TICKETS, availbletickets);
				ticketDetailsList.add(ticketDetailsObj);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e.getMessage());
		}finally {
            if (preparedStatement != null) {
            	preparedStatement.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		
		log.info("end executing getTicketdetailsFromDatabase method, sent data");
		return ticketDetailsList;

	}
	
	public List<JSONObject> getbookedTicketFromDatabaseForPro(JSONObject jObj, DataSource ds) throws SQLException {

		log.info("start executing getbookedTicketFromDatabase method");
		log.info("Request parameter: "+jObj.toString());

		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Connection connection = ds.getConnection();

		List<JSONObject> bookedTicketDetailsList = new ArrayList<JSONObject>();
		String customerId = jObj.getString(Constants.CUSTOMERID);
		String eventDate = jObj.getString(Constants.EVENTDATE);
		
		try { 
//			preparedStatement = connection
//					.prepareStatement("SELECT * FROM bookingdetails WHERE customerId = ? order by bookingtime DESC ");
			
			preparedStatement = connection
					.prepareStatement("SELECT * FROM bookingdetails where customerId = ? and eventDate = ? order by bookingtime DESC ");
			preparedStatement.setString(1, customerId);
			preparedStatement.setString(2, eventDate);
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next()) {
				JSONObject bookeTticketDetailsObj = new JSONObject();
				
				String cutomername = resultSet.getString(Constants.CUSTOMERNAME);
				bookeTticketDetailsObj.put(Constants.CUSTOMERNAME, cutomername);
				String cutomerId = resultSet.getString(Constants.CUSTOMERID);
				bookeTticketDetailsObj.put(Constants.CUSTOMERID, cutomerId);
				
				String mobile = resultSet.getString(Constants.MOBILE);
				bookeTticketDetailsObj.put(Constants.MOBILE, mobile);
				String clubname = resultSet.getString(Constants.CLUB_NAME);
				bookeTticketDetailsObj.put(Constants.CLUB_NAME, clubname);
				String clubid = resultSet.getString(Constants.CLUB_ID);
				bookeTticketDetailsObj.put(Constants.CLUB_ID, clubid);
				String QRnumber = resultSet.getString(Constants.QRNUMBER);
				bookeTticketDetailsObj.put(Constants.QRNUMBER, QRnumber);
				String tickettype = resultSet.getString(Constants.TICKETTYPE);
				bookeTticketDetailsObj.put(Constants.TICKETTYPE, tickettype);
				String eventDatex = resultSet.getString(Constants.EVENTDATE);
				bookeTticketDetailsObj.put(Constants.EVENTDATE, eventDatex);
				String cost = resultSet.getString(Constants.COST);
				bookeTticketDetailsObj.put(Constants.COST, cost);
				String costAfterDiscount = resultSet.getString(Constants.COSTAFTERDISCOUNT);
				bookeTticketDetailsObj.put(Constants.COSTAFTERDISCOUNT, costAfterDiscount);
				String remainingAmt = resultSet.getString(Constants.REMAINING_AMOUNT);
				bookeTticketDetailsObj.put(Constants.REMAINING_AMOUNT, remainingAmt);
				String qrNumber = resultSet.getString(Constants.QRNUMBER);
				bookeTticketDetailsObj.put(Constants.QRNUMBER, qrNumber);
				
				String ticketDetails = resultSet.getString(Constants.TICKET_DETAILS);
				bookeTticketDetailsObj.put(Constants.TICKET_DETAILS, ticketDetails);
				
				bookedTicketDetailsList.add(bookeTticketDetailsObj);
				
				
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e.getMessage());
		}finally {
            if (preparedStatement != null) {
            	preparedStatement.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		
		log.info("end executing getbookedTicketFromDatabase method, sent data");
		return bookedTicketDetailsList;

	}
	
	
	
	
	public List<JSONObject> getbookedTicketFromDatabase(JSONObject jObj, DataSource ds) throws SQLException {

		log.info("start executing getbookedTicketFromDatabase method");
		log.info("Request parameter: "+jObj.toString());

		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Connection connection = ds.getConnection();

		List<JSONObject> bookedTicketDetailsList = new ArrayList<JSONObject>();
		String customerId = jObj.getString(Constants.CUSTOMERID);
		
		try { 
			preparedStatement = connection
					.prepareStatement("SELECT * FROM bookingdetails WHERE customerId = ? order by bookingtime DESC ");
			
			
			preparedStatement.setString(1, customerId);
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next()) {
				JSONObject bookeTticketDetailsObj = new JSONObject();
				
				String cutomername = resultSet.getString(Constants.CUSTOMERNAME);
				bookeTticketDetailsObj.put(Constants.CUSTOMERNAME, cutomername);
				String cutomerId = resultSet.getString(Constants.CUSTOMERID);
				bookeTticketDetailsObj.put(Constants.CUSTOMERID, cutomerId);
				
				String mobile = resultSet.getString(Constants.MOBILE);
				bookeTticketDetailsObj.put(Constants.MOBILE, mobile);
				String clubname = resultSet.getString(Constants.CLUB_NAME);
				bookeTticketDetailsObj.put(Constants.CLUB_NAME, clubname);
				String clubid = resultSet.getString(Constants.CLUB_ID);
				bookeTticketDetailsObj.put(Constants.CLUB_ID, clubid);
				String QRnumber = resultSet.getString(Constants.QRNUMBER);
				bookeTticketDetailsObj.put(Constants.QRNUMBER, QRnumber);
				String tickettype = resultSet.getString(Constants.TICKETTYPE);
				bookeTticketDetailsObj.put(Constants.TICKETTYPE, tickettype);
				String eventDatex = resultSet.getString(Constants.EVENTDATE);
				bookeTticketDetailsObj.put(Constants.EVENTDATE, eventDatex);
				String cost = resultSet.getString(Constants.COST);
				bookeTticketDetailsObj.put(Constants.COST, cost);
				String costAfterDiscount = resultSet.getString(Constants.COSTAFTERDISCOUNT);
				bookeTticketDetailsObj.put(Constants.COSTAFTERDISCOUNT, costAfterDiscount);
				String remainingAmt = resultSet.getString(Constants.REMAINING_AMOUNT);
				bookeTticketDetailsObj.put(Constants.REMAINING_AMOUNT, remainingAmt);
				String qrNumber = resultSet.getString(Constants.QRNUMBER);
				bookeTticketDetailsObj.put(Constants.QRNUMBER, qrNumber);
				
				String ticketDetails = resultSet.getString(Constants.TICKET_DETAILS);
				bookeTticketDetailsObj.put(Constants.TICKET_DETAILS, ticketDetails);
				
				bookedTicketDetailsList.add(bookeTticketDetailsObj);
				
				
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e.getMessage());
		}finally {
            if (preparedStatement != null) {
            	preparedStatement.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		
		log.info("end executing getbookedTicketFromDatabase method, sent data");
		return bookedTicketDetailsList;

	}
	
	public List<JSONObject> getEventDetailsFromDatabase(JSONObject jObj, DataSource ds) throws SQLException {

		log.info("start executing getEventDetailsFromDatabase method");
		log.info("Request parameter: "+jObj.toString());

		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Connection connection = ds.getConnection();

		List<JSONObject> eventDetailsList = new ArrayList<JSONObject>();
		
		String clubidx = jObj.getString(Constants.CLUB_ID);

		//String clubidx = "99999";
		try {
			preparedStatement = connection
					.prepareStatement("SELECT * FROM eventdetails WHERE clubid = ? order by STR_TO_DATE(date,'%d/%b/%Y')  ASC ");
			preparedStatement.setString(1, clubidx);
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next()) {
				JSONObject eventDetailsObj = new JSONObject();
				
				String clubid = resultSet.getString(Constants.CLUB_ID);
				eventDetailsObj.put(Constants.CLUB_ID, clubid);
				String clubname = resultSet.getString(Constants.CLUB_NAME);
				eventDetailsObj.put(Constants.CLUB_NAME, clubname);
				String djname = resultSet.getString(Constants.DJ_NAME);
				eventDetailsObj.put(Constants.DJ_NAME, djname);
				String music = resultSet.getString(Constants.MUSIC_TYPE);
				eventDetailsObj.put(Constants.MUSIC_TYPE, music);
				String date = resultSet.getString(Constants.DATE);
				eventDetailsObj.put(Constants.DATE, date);
				String imageURL = resultSet.getString(Constants.IMAGE_URL);
				eventDetailsObj.put(Constants.IMAGE_URL, imageURL);
				
				eventDetailsList.add(eventDetailsObj);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e.getMessage());
		}finally {
            if (preparedStatement != null) {
            	preparedStatement.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		
		log.info("end executing getEventDetailsFromDatabase method, sent data");
		return eventDetailsList;

	}
	
	
	public List<JSONObject> getEventDetailsForOfferFromDatabase(JSONObject jObj, DataSource ds) throws SQLException {

		log.info("start executing getEventDetailsForOfferFromDatabase method");
		log.info("Request parameter: "+jObj.toString());

		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try {
			connection = ds.getConnection();
		}catch (Exception e) {
			e.printStackTrace();
		}
		

		List<JSONObject> eventDetailsList = new ArrayList<JSONObject>();

		String clubidx = jObj.getString(Constants.CLUB_ID);// jObj.getString("productType");
		String datex = jObj.getString(Constants.DATE);
		// String productOrder = jObj.getString("productType");
		try {
			preparedStatement = connection
					.prepareStatement("SELECT * FROM eventdetails WHERE clubid = ? and date=?");
			preparedStatement.setString(1, clubidx);
			preparedStatement.setString(2, datex);
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next()) {
				JSONObject eventDetailsObj = new JSONObject();
				
				String clubid = resultSet.getString(Constants.CLUB_ID);
				eventDetailsObj.put(Constants.CLUB_ID, clubid);
				String clubname = resultSet.getString(Constants.CLUB_NAME);
				eventDetailsObj.put(Constants.CLUB_NAME, clubname);
				String djname = resultSet.getString(Constants.DJ_NAME);
				eventDetailsObj.put(Constants.DJ_NAME, djname);
				String music = resultSet.getString(Constants.MUSIC_TYPE);
				eventDetailsObj.put(Constants.MUSIC_TYPE, music);
				String date = resultSet.getString(Constants.DATE);
				eventDetailsObj.put(Constants.DATE, date);
				String imageURL = resultSet.getString(Constants.IMAGE_URL);
				eventDetailsObj.put(Constants.IMAGE_URL, imageURL);
				
				eventDetailsList.add(eventDetailsObj);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e.getMessage());
		}finally {
            if (preparedStatement != null) {
            	preparedStatement.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		
		log.info("end executing getEventDetailsForOfferFromDatabase method, sent data");
		return eventDetailsList;

	}
	
	public List<JSONObject> getOffersFromDatabase(JSONObject jObj, DataSource ds) throws SQLException {
		
		log.info("start executing getOffersFromDatabase method");
		log.info("Request parameter: "+jObj.toString());
		
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Connection connection = ds.getConnection();
		
		List<JSONObject> offersDetailsList = new ArrayList<JSONObject>();
		String city = jObj.getString(Constants.CITY);
		
		

		
		try {
			preparedStatement = connection
					.prepareStatement("SELECT * FROM offers WHERE city = ?");
			preparedStatement.setString(1, city);
			resultSet = preparedStatement.executeQuery();
			
			
			

			
			while (resultSet.next()) {
				JSONObject offerDetailsObj = new JSONObject();
				
				String clubidx = resultSet.getString(Constants.CLUB_ID);
				offerDetailsObj.put(Constants.CLUB_ID, clubidx);
				String clubname = resultSet.getString(Constants.CLUBNME);
				offerDetailsObj.put(Constants.CLUBNME, clubname);
				String cityx = resultSet.getString(Constants.CITY);
				offerDetailsObj.put(Constants.CITY, cityx);
				String location = resultSet.getString(Constants.LOACTION);
				offerDetailsObj.put(Constants.LOACTION, location);
				
				String offerid = resultSet.getString(Constants.OFFERID);
				offerDetailsObj.put(Constants.OFFERID, offerid);
				
				String offerName = resultSet.getString(Constants.OFFERNAME);
				offerDetailsObj.put(Constants.OFFERNAME, offerName);
				
				String offerForTable = resultSet.getString(Constants.OFFERFORTABLE);
				offerDetailsObj.put(Constants.OFFERFORTABLE, offerForTable);
				
				String offerForPass = resultSet.getString(Constants.OFFERFORPASS);
				offerDetailsObj.put(Constants.OFFERFORPASS, offerForPass);
				String eventName = resultSet.getString(Constants.EVENTNAME);
				offerDetailsObj.put(Constants.EVENTNAME, eventName);
				
				
				
				String djName = resultSet.getString(Constants.DJNAME);
				offerDetailsObj.put(Constants.DJNAME, djName);
				
				String music = resultSet.getString(Constants.MUSIC);
				offerDetailsObj.put(Constants.MUSIC, music);
				
				
				String eventDatex = resultSet.getString(Constants.DATE);
				offerDetailsObj.put(Constants.DATE, eventDatex);
				
				
				String starttime = resultSet.getString(Constants.STARTTIME);
				offerDetailsObj.put(Constants.STARTTIME, starttime);
				
				String imageURL = resultSet.getString(Constants.IMAGE_URL);
				offerDetailsObj.put(Constants.IMAGE_URL, imageURL);
				
				String timetoexpire = resultSet.getString(Constants.TIME_TO_EXPIRE);
				offerDetailsObj.put(Constants.TIME_TO_EXPIRE, timetoexpire);
				
				
				
				
				
				offersDetailsList.add(offerDetailsObj);
				
				
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e.getMessage());
		}finally {
            if (preparedStatement != null) {
            	preparedStatement.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		
		log.info("end executing getOffersFromDatabase method, sent data");
		return offersDetailsList;
		
	}
	
	
	public  List<JSONObject> getTabledetailsByDateForClubFromDatabase(JSONObject jObj, DataSource ds) throws SQLException {
		log.info("start executing getTabledetailsByDateForClubFromDatabase method");
		log.info("Request parameter: "+jObj.toString());
		
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Connection connection = ds.getConnection();
		
		List<JSONObject> bookedTableDetailsList = new ArrayList<JSONObject>();
		String clubid = jObj.getString(Constants.CLUB_ID);
		//String eventdate = jObj.getString(Constants.DATE);
		
		try {
			
			preparedStatement = connection
					.prepareStatement("SELECT * FROM tablesdata where clubid = ?");
			//preparedStatement.setString(1, eventdate);
			preparedStatement.setString(1, clubid);
			
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next()) {
				JSONObject bookeTableDetailsObj = new JSONObject();
				
				
				
				String clubidx = resultSet.getString(Constants.CLUB_ID);
				bookeTableDetailsObj.put(Constants.CLUB_ID, clubidx);
				String clubname = resultSet.getString(Constants.CLUBNME);
				bookeTableDetailsObj.put(Constants.CLUBNME, clubname);
				String tableid = resultSet.getString(Constants.TABLE_ID);
				bookeTableDetailsObj.put(Constants.TABLE_ID, tableid);
				String tablenumber = resultSet.getString(Constants.TABLE_NUMBER);
				bookeTableDetailsObj.put(Constants.TABLE_NUMBER, tablenumber);
				String details = resultSet.getString(Constants.DETAILS);
				bookeTableDetailsObj.put(Constants.DETAILS, details);
				String tabletype = resultSet.getString(Constants.TABLE_TYPE);
				bookeTableDetailsObj.put(Constants.TABLE_TYPE, tabletype);
				String size = resultSet.getString(Constants.SIZE);
				bookeTableDetailsObj.put(Constants.SIZE, size);
				String cost = resultSet.getString(Constants.COST);
				bookeTableDetailsObj.put(Constants.COST, cost);
				
				
				String coords = resultSet.getString(Constants.COORDS);
				bookeTableDetailsObj.put(Constants.COORDS, coords);
				
				String eventdate = resultSet.getString(Constants.EVENTDATE);
				eventdate = eventdate.replaceAll("/", "");
		        String destFileName = clubidx+"-"+eventdate+".html";

		        String layoutURL = Constants.TABLE_LAYOUT_URL+destFileName;
		        
				bookeTableDetailsObj.put(Constants.LAYOUT_URL, layoutURL);
				
				
				String eventdatex = resultSet.getString(Constants.EVENTDATE);
				bookeTableDetailsObj.put(Constants.EVENTDATE, eventdatex);
				
				
				String isBooked = resultSet.getString(Constants.ISBOOKED);
				bookeTableDetailsObj.put(Constants.ISBOOKED, isBooked);
				
				bookedTableDetailsList.add(bookeTableDetailsObj);
				
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			log.info(e.getMessage());
		}
		
		
		
		
		return bookedTableDetailsList;
		
	}
	
	public  List<JSONObject> getTabledetailsByDateFromDatabase(JSONObject jObj, DataSource ds) throws SQLException {
		log.info("start executing getTabledetailsByDateFromDatabase method");
		log.info("Request parameter: "+jObj.toString());
		
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Connection connection = ds.getConnection();
		
		List<JSONObject> bookedTableDetailsList = new ArrayList<JSONObject>();
		String clubid = jObj.getString(Constants.CLUB_ID);
		String eventdate = jObj.getString(Constants.DATE);
		
		try {
			
			preparedStatement = connection
					.prepareStatement("SELECT * FROM tablesdata where eventdate = ? and clubid = ?");
			preparedStatement.setString(1, eventdate);
			preparedStatement.setString(2, clubid);
			
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next()) {
				JSONObject bookeTableDetailsObj = new JSONObject();
				
				
				
				String clubidx = resultSet.getString(Constants.CLUB_ID);
				bookeTableDetailsObj.put(Constants.CLUB_ID, clubidx);
				String clubname = resultSet.getString(Constants.CLUBNME);
				bookeTableDetailsObj.put(Constants.CLUBNME, clubname);
				String tableid = resultSet.getString(Constants.TABLE_ID);
				bookeTableDetailsObj.put(Constants.TABLE_ID, tableid);
				String tablenumber = resultSet.getString(Constants.TABLE_NUMBER);
				bookeTableDetailsObj.put(Constants.TABLE_NUMBER, tablenumber);
				String details = resultSet.getString(Constants.DETAILS);
				bookeTableDetailsObj.put(Constants.DETAILS, details);
				String tabletype = resultSet.getString(Constants.TABLE_TYPE);
				bookeTableDetailsObj.put(Constants.TABLE_TYPE, tabletype);
				String size = resultSet.getString(Constants.SIZE);
				bookeTableDetailsObj.put(Constants.SIZE, size);
				String cost = resultSet.getString(Constants.COST);
				bookeTableDetailsObj.put(Constants.COST, cost);
				
				
				String coords = resultSet.getString(Constants.COORDS);
				bookeTableDetailsObj.put(Constants.COORDS, coords);
				
				String eventdatex = resultSet.getString(Constants.EVENTDATE);
				bookeTableDetailsObj.put(Constants.EVENTDATE, eventdatex);
				
				eventdatex = eventdatex.replaceAll("/", "");
		        String destFileName = clubidx+"-"+eventdatex+".html";

		        String layoutURL = Constants.TABLE_LAYOUT_URL+destFileName;
		        
				bookeTableDetailsObj.put(Constants.LAYOUT_URL, layoutURL);
				
				
				
				String isBooked = resultSet.getString(Constants.ISBOOKED);
				bookeTableDetailsObj.put(Constants.ISBOOKED, isBooked);
				
				bookedTableDetailsList.add(bookeTableDetailsObj);
				
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			log.info(e.getMessage());
		}
		
		
		
		
		return bookedTableDetailsList;
		
	}
	
	
	
	
	public List<JSONObject> getPassNguestListDefaultData(JSONObject jObj, DataSource ds) throws SQLException {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Connection connection = ds.getConnection();
		
		String clubid = jObj.getString(Constants.CLUB_ID);
		String eventDate = jObj.getString(Constants.EVENTDATE);
		String day = "tuesday";//Utill.getDayFromDate(eventDate);
		
		String getPassNguestListDefaultDataSQL = "SELECT * FROM ticketdefaultdatadetails WHERE clubid = ? AND day = ?";
		//JSONObject passNguestlistDataDetailsMAP = new JSONObject();
		List<JSONObject>  passNguestlistDataDetailsList = new ArrayList<JSONObject>();
		
		try {
			
			preparedStatement = connection
					.prepareStatement(getPassNguestListDefaultDataSQL);
			preparedStatement.setString(1, clubid);
			preparedStatement.setString(2, day.toLowerCase());
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				JSONObject passNguestlistDataDetailsObj = new JSONObject();
				
				String clubidx = resultSet.getString(Constants.CLUB_ID);
				String type = resultSet.getString(Constants.TICKET_TYPE);
				String category = resultSet.getString(Constants.CATEGORY);
				String cost = resultSet.getString(Constants.COST);
				String dayx = resultSet.getString(Constants.DAY);
				String totaltickets = resultSet.getString(Constants.TOTAL_TICKETS);
				
				passNguestlistDataDetailsObj.put(Constants.CLUB_ID, clubidx);
				passNguestlistDataDetailsObj.put(Constants.TICKET_TYPE, type);
				passNguestlistDataDetailsObj.put(Constants.CATEGORY, category);
				passNguestlistDataDetailsObj.put(Constants.COST, cost);
				passNguestlistDataDetailsObj.put(Constants.DAY, dayx);
				passNguestlistDataDetailsObj.put(Constants.TOTAL_TICKETS, totaltickets);
				
				passNguestlistDataDetailsList.add(passNguestlistDataDetailsObj);
				
				
				
			}
			
			
		}catch (Exception ex) {
			ex.printStackTrace();
		}finally {
            if (preparedStatement != null) {
            	preparedStatement.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		log.info("end executing getPassNguestListDefaultData method, sent data");
		return passNguestlistDataDetailsList;
	}
	
	public List<JSONObject> getTablesDefaultData(JSONObject jObj, DataSource ds) throws SQLException {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Connection connection = ds.getConnection();
		
		
		String clubid = jObj.getString(Constants.CLUB_ID);
		String eventDate = jObj.getString(Constants.EVENTDATE);
		String day = "monday";//Utill.getDayFromDate(eventDate);
		
		String getTableDefalutDataSQL = "SELECT * FROM defaulttablesdata WHERE clubid = ? AND day = ? ORDER BY tablenumber ASC";
		List<JSONObject> ticketDetailsList = new ArrayList<JSONObject>();
		
		try {
			preparedStatement = connection
					.prepareStatement(getTableDefalutDataSQL);
			preparedStatement.setString(1, clubid);
			preparedStatement.setString(2, day.toLowerCase());
			resultSet = preparedStatement.executeQuery();
			
			String insretTableDataDetailsSQL = "INSERT INTO tablesdata ( clubid, clubname, tableid, tablenumber, details, tabletype, size, cost, coords, layoutURL, eventdate, booked) " + 
					"VALUES (?, ? , ?, ?, ? , ?, ?, ? , ?, ?, ? , ? )";
			
			while (resultSet.next()) {
				JSONObject tableDefaultDataDetailsObj = new JSONObject();
				String clubidx = resultSet.getString(Constants.CLUB_ID);
				String tableId = resultSet.getString(Constants.TABLE_ID);
				String tableNumber = resultSet.getString(Constants.TABLE_NUMBER);
				String details = resultSet.getString(Constants.DETAILS);
				String tableType = resultSet.getString(Constants.TABLE_TYPE);
				String size = resultSet.getString(Constants.SIZE);
				String cost = resultSet.getString(Constants.COST);
				String coords = resultSet.getString(Constants.COORDS);
				String layoutURL = resultSet.getString(Constants.LAYOUT_URL);
				String dayx = resultSet.getString(Constants.DAY);
				
				tableDefaultDataDetailsObj.put(Constants.CLUB_ID, clubidx);
				tableDefaultDataDetailsObj.put(Constants.TABLE_ID, tableId);
				tableDefaultDataDetailsObj.put(Constants.TABLE_NUMBER, tableNumber);
				tableDefaultDataDetailsObj.put(Constants.DETAILS, details);
				tableDefaultDataDetailsObj.put(Constants.TABLE_TYPE, tableType);
				tableDefaultDataDetailsObj.put(Constants.SIZE, size);
				tableDefaultDataDetailsObj.put(Constants.COST, cost);
				tableDefaultDataDetailsObj.put(Constants.COORDS, coords);
				tableDefaultDataDetailsObj.put(Constants.LAYOUT_URL, layoutURL);
				tableDefaultDataDetailsObj.put(Constants.DAY, dayx);
				//tableDefaultDataDetailsMAP.put(Constants.TABLE_NUMBER, tableDefaultDataDetailsObj);
				ticketDetailsList.add(tableDefaultDataDetailsObj);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
            if (preparedStatement != null) {
            	preparedStatement.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		log.info("end executing getTablesDefaultData method, sent data");
		return ticketDetailsList;
	}
	
	
	public List<JSONObject> getOffersForClubFromDatabase(JSONObject jObj, DataSource ds) throws SQLException {
		
		log.info("start executing getOffersForClubFromDatabase method");
		log.info("Request parameter: "+jObj.toString());
		
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Connection connection = ds.getConnection();
		
		List<JSONObject> bookedTicketDetailsList = new ArrayList<JSONObject>();
		String clubid = jObj.getString(Constants.CLUB_ID);
		String eventDate = jObj.getString(Constants.EVENTDATE);
		
		

		//String customerMob = "99999";// jObj.getString("productType");
		// String productOrder = jObj.getString("productType");
		try {
			preparedStatement = connection
					.prepareStatement("SELECT * FROM offers WHERE clubid = ? and eventDate=?");
			preparedStatement.setString(1, clubid);
			preparedStatement.setString(2, eventDate);
			resultSet = preparedStatement.executeQuery();
			
			
			
	
			
			while (resultSet.next()) {
				JSONObject bookeTticketDetailsObj = new JSONObject();
				
				String clubidx = resultSet.getString(Constants.CLUB_ID);
				bookeTticketDetailsObj.put(Constants.CLUB_ID, clubidx);
				String clubname = resultSet.getString(Constants.CLUBNME);
				bookeTticketDetailsObj.put(Constants.CLUBNME, clubname);
				String eventDatex = resultSet.getString(Constants.DATE);
				bookeTticketDetailsObj.put(Constants.EVENTDATE, eventDatex);
				String offerName = resultSet.getString(Constants.OFFERNAME);
				bookeTticketDetailsObj.put(Constants.OFFERNAME, offerName);
				String offerValue = resultSet.getString(Constants.OFFERVALUE);
				bookeTticketDetailsObj.put(Constants.OFFERVALUE, offerValue);
				
				String offerFor = resultSet.getString(Constants.OFFERFOR);
				bookeTticketDetailsObj.put(Constants.OFFERFOR, offerFor);
				String imageURL = resultSet.getString(Constants.IMAGE_URL);
				bookeTticketDetailsObj.put(Constants.IMAGE_URL, imageURL);
				String starttime = resultSet.getString(Constants.STARTTIME);
				bookeTticketDetailsObj.put(Constants.STARTTIME, starttime);
				String timetoexpire = resultSet.getString(Constants.TIME_TO_EXPIRE);
				bookeTticketDetailsObj.put(Constants.TIME_TO_EXPIRE, timetoexpire);
				
				
				
				bookedTicketDetailsList.add(bookeTticketDetailsObj);
				
				
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e.getMessage());
		}finally {
            if (preparedStatement != null) {
            	preparedStatement.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		log.info("end executing getOffersForClubFromDatabase method, sent data");
		return bookedTicketDetailsList;
		
	}
	
	
	public JSONObject clubLogindDataAndValidation(JSONObject clubDataDetails, DataSource ds) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStmt = null;
		JSONObject eventDetailsObj = new JSONObject();
		String passwordCheckingDone = "firsttime";
		
		try {
			log.info("start executing clubLogindDataAndValidation method");
			log.info("Request parameter: "+clubDataDetails.toString());
			connection = ds.getConnection();
			preparedStmt = null;
			String userId = clubDataDetails.getString(Constants.USER_ID);
			String clubid = clubDataDetails.getString(Constants.CLUB_ID);
			String password = clubDataDetails.getString(Constants.PASSWORD);
			String date = clubDataDetails.getString(Constants.EVENTDATE);//Utill.getTodayDate();
			
			
			String insertClubLoginDetailsSQL = "INSERT INTO clubdatalogin ( clubname, clubid,  password) "
					+ " VALUES  ( ?, ? , ? )";
			String updatePasswordSQL = "UPDATE clubdatalogin SET password = ? WHERE clubid = ? ";
			String verifyClubLoginDetailsSQL = "SELECT * FROM  clubdatalogin WHERE clubid = ? ";
			String getClubNameSQL = "SELECT clubname FROM clubdetails where clubid = ? ";
			
			//verify first if club is already in data base
			preparedStmt = connection
					.prepareStatement(verifyClubLoginDetailsSQL);
			preparedStmt.setString(1, clubid);
			ResultSet resultSet = preparedStmt.executeQuery();
			
			while (resultSet.next()) {
				
				String passwordx = resultSet.getString(Constants.PASSWORD);
				//check if password need to change
				if(passwordx == null || passwordx.length() == 0) {
					preparedStmt = connection
							.prepareStatement(updatePasswordSQL);
					preparedStmt.setString(1, password);
					preparedStmt.setString(2, clubid);
					preparedStmt.execute();
					passwordCheckingDone = "success";
				}
				
				// check if password match
				if(password != null && passwordx != null && password.equals(passwordx)) {
					passwordCheckingDone = "match";
				}else {
					passwordCheckingDone =  "fail";
				}
				
			}
			
			if(passwordCheckingDone.equals("firsttime")) {
				// club details not is db logging first time
				preparedStmt = connection
						.prepareStatement(insertClubLoginDetailsSQL);
				preparedStmt.setString(1, userId);
				preparedStmt.setString(2, clubid);
				preparedStmt.setString(3, password);
				preparedStmt.execute();
			}
			
			
			
			
			preparedStmt = connection
					.prepareStatement("SELECT * FROM eventdetails WHERE clubid = ? and date = ?");
			preparedStmt.setString(1, clubid);
			preparedStmt.setString(2, date);
			resultSet = preparedStmt.executeQuery();
			boolean isEventForToday = false;
			while (resultSet.next()) {
				isEventForToday = true;
				
				String clubidn = resultSet.getString(Constants.CLUB_ID);
				eventDetailsObj.put(Constants.CLUB_ID, clubidn);
				String clubname = resultSet.getString(Constants.CLUB_NAME);
				eventDetailsObj.put(Constants.CLUB_NAME, clubname);
				String djname = resultSet.getString(Constants.DJ_NAME);
				eventDetailsObj.put(Constants.DJ_NAME, djname);
				String music = resultSet.getString(Constants.MUSIC_TYPE);
				eventDetailsObj.put(Constants.MUSIC_TYPE, music);
				String datex = resultSet.getString(Constants.DATE);
				eventDetailsObj.put(Constants.DATE, datex);
				String imageURL = resultSet.getString(Constants.IMAGE_URL);
				eventDetailsObj.put(Constants.IMAGE_URL, imageURL);
				eventDetailsObj.put(Constants.IF_EVENT_EXIST, "yes");
			}
			
			if(!isEventForToday) {
				preparedStmt = connection
						.prepareStatement(getClubNameSQL);
				preparedStmt.setString(1, clubid);
				resultSet = preparedStmt.executeQuery();
				while (resultSet.next()) {
					String clubname = resultSet.getString(Constants.CLUB_NAME);
					eventDetailsObj.put(Constants.CLUB_NAME, clubname);
					eventDetailsObj.put(Constants.IF_EVENT_EXIST, "no");
				}
				
			}
			
			
			
		}catch (Exception e) {
			e.printStackTrace();
			passwordCheckingDone = "fail";
		}finally {
            if (preparedStmt != null) {
            	preparedStmt.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		
		JSONObject jObj = new JSONObject();
		jObj.put("eventDetailsObj", eventDetailsObj);
		jObj.put("passwordCheckingDone", passwordCheckingDone);
		
		log.info("end executing clubLogindDataAndValidation method, sent data");
		
		return jObj;
		
	}
	
	
	public String createNewCustomer(JSONObject customerDetails, DataSource ds) throws SQLException {
		log.info("start executing createNewCustomer method");
		log.info("Request parameter: "+customerDetails.toString());
		Connection connection = ds.getConnection();
		PreparedStatement preparedStmt = null;
		
		String cutomername = customerDetails.getString(Constants.CUSTOMERNAME);
		String mobile = customerDetails.getString(Constants.MOBILE);
		String customerId = Utill.createNewCustomerId();
		
		String createNewCustomerSQL = "INSERT INTO customer ( cutomername, mobile,  customerId) "
				+ " VALUES  ( ?, ? , ? )";
		
		
		try {
			
			preparedStmt = connection
					.prepareStatement(createNewCustomerSQL);
			preparedStmt.setString(1, cutomername);
			preparedStmt.setString(2, mobile);
			preparedStmt.setString(3, customerId);
			
			preparedStmt.execute();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
            if (preparedStmt != null) {
            	preparedStmt.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		
		log.info("end executing createNewCustomer method, sent data");
		
		return customerId;
		
	}
	
	
	public String insertNewEventDetails(JSONObject orderJobj, DataSource ds) throws SQLException {
		
		PreparedStatement preparedStmtx = null;
		Connection connection = ds.getConnection();
		
		ResultSet resultSetx;
		
		String reply="success";
		
		String verifyEventDetailSQL = "SELECT * FROM eventdetails WHERE clubid = ? AND date = ?";
		
		String deleteEventDetailSQL = "DELETE FROM eventdetails WHERE clubid = ? AND date = ?";
		
		String insertEventDetailSQL = "INSERT INTO eventdetails (clubid,  clubname, djname, music, date, imageURL ) "
				+ " VALUES  ( ?, ? , ?, ?, ? , ?)";
		
		
		String deleteTicketDetailsSQL = "DELETE FROM ticketdetails WHERE clubid = ? AND date = ?";
		
		String insertTicketDetailsSQL = "INSERT INTO ticketdetails ( clubid, clubname, type, size, category, cost, details, Day, date, totaltickets, availbletickets)" + 
				" VALUES (?, ? , ?, ?, ? , ?, ?, ? , ?, ?, ? )";
		
		String insretTableDataDetailsSQL = "INSERT INTO tablesdata ( clubid, clubname, tableid, tablenumber, details, tabletype, size, cost, coords, layoutURL, eventdate, booked) " + 
				"VALUES (?, ? , ?, ?, ? , ?, ?, ? , ?, ?, ? , ? )";
		
		String deleteTableDataDetailsSQL = "DELETE FROM tablesdata WHERE clubid = ? AND eventdate = ?";
		
		
		
		try {
			connection.setAutoCommit(false);
			
			String clubid = orderJobj.getString(Constants.CLUB_ID);
			String clubname = orderJobj.getString(Constants.CLUB_NAME);
			String djname = orderJobj.getString(Constants.DJ_NAME);
			String music = orderJobj.getString(Constants.MUSIC);
			String eventDate = orderJobj.getString(Constants.EVENTDATE);
			String day = Utill.getDayFromDate(eventDate);
			//eventDate = Utill.changeDateFormate(eventDate);
			String imageURL = orderJobj.getString(Constants.IMAGE_URL);
			String tableDefaultDetailsDataListStr = orderJobj.getString("tableDefaultDetailsDataList");
			JSONArray tableDefaultDetailsDataList = new JSONArray(tableDefaultDetailsDataListStr);
			String passNguestListDefaultDataStr = orderJobj.getString("passNguestListDefaultData");
			JSONArray passNguestListDefaultDataList = new JSONArray(passNguestListDefaultDataStr);
			
			
			//Insert into event Details
			preparedStmtx = connection.prepareStatement(deleteEventDetailSQL);
			preparedStmtx.setString(1, clubid);
			preparedStmtx.setString(2, eventDate);
			preparedStmtx.execute();
//			while(resultSetx.next()) {//if this true means already EVENT exist for date
//				reply = "exist";
//			}
			
			
			
			if(true) {
				preparedStmtx = connection.prepareStatement(insertEventDetailSQL);
				preparedStmtx.setString(1, clubid);
				preparedStmtx.setString(2, clubname);
				preparedStmtx.setString(3, djname);
				preparedStmtx.setString(4, music);
				preparedStmtx.setString(5, eventDate);
				preparedStmtx.setString(6, imageURL);
				boolean isInserted = preparedStmtx.execute();
				
			}
			
			
			// insert into ticketdetails table
			
			preparedStmtx = connection.prepareStatement(deleteTicketDetailsSQL);// deleting any old details
			preparedStmtx.setString(1, clubid);
			preparedStmtx.setString(2, eventDate);
			preparedStmtx.execute();
			
			preparedStmtx = connection.prepareStatement(insertTicketDetailsSQL);
			for(int i=0; i < passNguestListDefaultDataList.length(); i++) {
				JSONObject jobj = passNguestListDefaultDataList.getJSONObject(i);
				preparedStmtx.setString(1, clubid);
				preparedStmtx.setString(2, clubname);
				preparedStmtx.setString(3, jobj.getString(Constants.TICKET_TYPE));
				preparedStmtx.setString(4, "0");
				preparedStmtx.setString(5, jobj.getString(Constants.CATEGORY));
				preparedStmtx.setString(6, jobj.getString(Constants.COST));
				preparedStmtx.setString(7, "");
				preparedStmtx.setString(8, day.toLowerCase());
				preparedStmtx.setString(9, eventDate);
				preparedStmtx.setString(10, jobj.getString(Constants.TOTAL_TICKETS));
				preparedStmtx.setString(11, jobj.getString(Constants.TOTAL_TICKETS));
				preparedStmtx.addBatch();
			}
			
			preparedStmtx.executeBatch();
			
			
			// insert into tablesdata table
			String checkIfAnyTableIsBooked = "Select * from tablesdata where clubid = ? AND eventdate = ? and booked = 'booked'";
			
			boolean isTableAlreadyBooked = false;
			preparedStmtx = connection.prepareStatement(checkIfAnyTableIsBooked);
			preparedStmtx.setString(1, clubid);
			preparedStmtx.setString(2, eventDate);
			ResultSet resultSet =preparedStmtx.executeQuery();
			while(resultSet.next()) {//if this true means already EVENT exist for date
				isTableAlreadyBooked = true;
				reply = "Partialy Success !!! since some table is already booked with your privious data which you had provided, Please contact support now";
			}
			
			if(!isTableAlreadyBooked) {
				
				preparedStmtx = connection.prepareStatement(deleteTableDataDetailsSQL);// deleting any old details
				preparedStmtx.setString(1, clubid);
				preparedStmtx.setString(2, eventDate);
				preparedStmtx.execute();
				
				
				
				
				preparedStmtx = connection.prepareStatement(insretTableDataDetailsSQL);
				for(int i=0; i < tableDefaultDetailsDataList.length(); i++) {
					JSONObject jobj = tableDefaultDetailsDataList.getJSONObject(i);
					preparedStmtx.setString(1, clubid);
					preparedStmtx.setString(2, clubname);
					preparedStmtx.setString(3, jobj.getString(Constants.TABLE_ID));
					preparedStmtx.setString(4, jobj.getString(Constants.TABLE_NUMBER));
					preparedStmtx.setString(5, jobj.getString(Constants.DETAILS));
					preparedStmtx.setString(6, jobj.getString(Constants.TABLE_TYPE));
					preparedStmtx.setString(7, jobj.getString(Constants.SIZE));
					preparedStmtx.setString(8, jobj.getString(Constants.COST));
					preparedStmtx.setString(9, jobj.getString(Constants.COORDS));
					preparedStmtx.setString(10, jobj.getString(Constants.LAYOUT_URL));
					preparedStmtx.setString(11, eventDate);
					preparedStmtx.setString(12, "available");
					preparedStmtx.addBatch();
				}
				preparedStmtx.executeBatch();
				
				// create HTML page to layout display
				ConcurrentHashMap<String, TableRowData> tableDataMap = new ConcurrentHashMap<String, TableRowData>();
				String verifyTableAvailable = "SELECT * FROM tablesdata WHERE clubid = ? AND eventdate = ? ";
				preparedStmtx = connection.prepareStatement(verifyTableAvailable);
				preparedStmtx.setString(1, clubid);
				//preparedStmt.setString(2, tableId);
				preparedStmtx.setString(2, eventDate);
				resultSetx = preparedStmtx.executeQuery();
				while(resultSetx.next()) {
					TableRowData trd = new TableRowData();
			        String clubidx = resultSetx.getString("clubid");
			        trd.setClubid(clubidx);
			        String tablenumber = resultSetx.getString("tablenumber");
			        trd.setTablenumber(tablenumber);
			        String tableid = resultSetx.getString("tableid");
			        trd.setTableid(tableid);
			        String isBooked = resultSetx.getString(Constants.ISBOOKED);
			        String booked = resultSetx.getString("booked");
			        trd.setBooked(booked);
			        
			        String tabletype = resultSetx.getString("tabletype");
			        trd.setTabletype(tabletype);
			        String coords = resultSetx.getString("coords");
			        trd.setCoords(coords);
			        
			        String eventdate = eventDate.replaceAll("/", "");
			        String destFileName = clubidx+"-"+eventdate+".html";

			        String layoutURL = Constants.TABLE_LAYOUT_URL+destFileName;
			        trd.setLayoutURL(layoutURL);
			        tableDataMap.put(tableid, trd);

				}
				createHTML(clubid, tableDataMap, eventDate);
				
			}
			
			connection.commit();
			
			
		}catch (Exception e) {
			e.printStackTrace();
			reply = "fail";
		}finally {
			connection.setAutoCommit(true);
			if (preparedStmtx != null) {
				preparedStmtx.close();
            }
            if (connection != null) {
            	connection.close();
            }
		}
		
		
		return reply;
		
	}
	
	
	public JSONObject getDataForReportChartFromDatabase(JSONObject orderJobj, DataSource ds) throws SQLException {
		log.info("start executing inserOrderDetails method");
		log.info("Request parameter: "+orderJobj.toString());
		
		PreparedStatement preparedStmt = null;
		Connection connection = ds.getConnection();
		ResultSet resultSetx;
		
		List<JSONObject> bookedTableDetailsList = new ArrayList<JSONObject>();
		List<JSONObject> bookedPassDetailsList = new ArrayList<JSONObject>();
		List<JSONObject> bookedGuestListDetailsList = new ArrayList<JSONObject>();
		
		JSONObject allBookingDetailsObj = new JSONObject();
		
		try {
			String customerId = orderJobj.getString(Constants.CLUB_ID);
			String eventDate = orderJobj.getString(Constants.EVENTDATE);
			
			String reportDataSQL = "SELECT * FROM bookingdetails WHERE clubid = ? and eventDate = ? ";
			PreparedStatement preparedStmtx = connection.prepareStatement(reportDataSQL);
			preparedStmtx.setString(1, customerId);
			preparedStmtx.setString(2, eventDate);
			resultSetx = preparedStmtx.executeQuery();
			
			while(resultSetx.next()) {//if this true means already booked a guestlist
				String cutomername = resultSetx.getString("cutomername");
				String customerIdx = resultSetx.getString("customerId");
				String QRnumber = resultSetx.getString("QRnumber");
				String tickettype = resultSetx.getString("tickettype");
				String costafterdiscount = resultSetx.getString("costafterdiscount");
				String ticketDetails = resultSetx.getString("ticketDetails");
				String bookingDate = resultSetx.getString("bookingdate");
				
				String eventDatey = resultSetx.getString("eventDate");
				String paidamount = resultSetx.getString("paidamount");
				String remainingamount = resultSetx.getString("remainingamount");
				
				
				JSONObject eventDetailsObj = new JSONObject();
				eventDetailsObj.put(Constants.CUSTOMERNAME, cutomername);
				eventDetailsObj.put(Constants.CUSTOMERID, customerIdx);
				eventDetailsObj.put(Constants.QRNUMBER, QRnumber);
				eventDetailsObj.put(Constants.TABLE_TYPE, tickettype);
				eventDetailsObj.put(Constants.COSTAFTERDISCOUNT, costafterdiscount);
				eventDetailsObj.put(Constants.TICKET_DETAILS, ticketDetails);
				eventDetailsObj.put(Constants.BOOKINGDATE, bookingDate);
				eventDetailsObj.put(Constants.EVENTDATE, eventDatey);
				eventDetailsObj.put(Constants.PAID_AMOUNT, paidamount);
				eventDetailsObj.put(Constants.REMAINING_AMOUNT, remainingamount);
				
				if(tickettype.equalsIgnoreCase("table")) {
					bookedTableDetailsList.add(eventDetailsObj);
				}else if(tickettype.equalsIgnoreCase("pass")) {
					bookedPassDetailsList.add(eventDetailsObj);
				}else if(tickettype.equalsIgnoreCase("guest list")) {
					bookedGuestListDetailsList.add(eventDetailsObj);
				}
				
			}
			
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (preparedStmt != null) {
            	preparedStmt.close();
            }
            if (connection != null) {
            	connection.close();
            }
		}
		allBookingDetailsObj.put("bookedTableDetailsList", bookedTableDetailsList);
		allBookingDetailsObj.put("bookedPassDetailsList", bookedPassDetailsList);
		allBookingDetailsObj.put("bookedGuestListDetailsList", bookedGuestListDetailsList);
		
		return allBookingDetailsObj;
	}

	public String inserOrderDetails(JSONObject orderJobj, DataSource ds) throws SQLException {
		log.info("start executing inserOrderDetails method");
		log.info("Request parameter: "+orderJobj.toString());
		PreparedStatement preparedStmt = null;
		Connection connection = ds.getConnection();
		ResultSet resultSetx;

		try {
			

			log.info("inserOrderDetails");
			
			// order String to order json obj
			//String inserOrderDetails = jObj.getString("inserOrderDetails");
			//JSONObject orderJobj = new JSONObject(inserOrderDetails);
			String cutomername = orderJobj.getString(Constants.CUSTOMERNAME);
			String customerId = orderJobj.getString(Constants.CUSTOMERID);
			String mobile = orderJobj.getString(Constants.MOBILE);
			String clubname = orderJobj.getString(Constants.CLUBNME);
			String clubid = orderJobj.getString(Constants.CLUB_ID);
			String qrNumber = orderJobj.getString(Constants.QRNUMBER); 
			String ticketType = orderJobj.getString(Constants.TICKETTYPE);
			
			String cost = orderJobj.getString(Constants.COST);
			String costAfterDiscount = orderJobj.getString(Constants.COSTAFTERDISCOUNT);
			String paidAmount = orderJobj.getString(Constants.PAID_AMOUNT);
			String remainingAmount = orderJobj.getString(Constants.REMAINING_AMOUNT);
			String discount = orderJobj.getString(Constants.DISCOUNT);
			java.sql.Timestamp bookingTime = Utill.getCurrentTime();
			String bookingDate = Utill.getTodayDate();
			String eventDate = orderJobj.getString(Constants.DATE);
			String ticketDetails = orderJobj.getString(Constants.TICKET_DETAILS);
			// if ticketType is guestlist then check user has already booked any ticket for eventDate
			// if yes then do not let him book another ticket on guestlist for eventDate
			if(ticketType != null && ticketType.equalsIgnoreCase("guest list")) {
				String checkUserBookedTicket = "select * from bookingdetails where customerId = ? and eventDate = ? and tickettype = ?";
				PreparedStatement preparedStmtx = connection.prepareStatement(checkUserBookedTicket);
				preparedStmtx.setString(1, customerId);
				preparedStmtx.setString(2, eventDate);
				preparedStmtx.setString(3, ticketType);
				resultSetx = preparedStmtx.executeQuery();
				while(resultSetx.next()) {//if this true means already booked a guestlist
					return "fail";
				}
			}
			
			
			int availableTicketCount = 0;
			
			String tableSize = "0";
			
			String updateTicketdetailsTable = "update ticketdetails set availbletickets = ? where clubid  = ? and  date = ? "
					+" and type = ? and category = ? and size = ?";
			
			// now check if tickets are available or not
			//1: check if guestlist is available
			synchronized (DBHelper.class) {
				if(ticketType != null && ticketType.equalsIgnoreCase("guest list")) {
					String category = null;
					if(ticketDetails.toLowerCase().contains("couple")) {
						category = "couple";
					}else {
						category = "girl";
					}
					String checkGuestListSQL = "select availbletickets from ticketdetails where type ='guest list' and category = '"+category+"'"
							+" and clubid  = ? and date = ?";
					preparedStmt = connection.prepareStatement(checkGuestListSQL);
					preparedStmt.setString(1, clubid);
					preparedStmt.setString(2, eventDate);
					resultSetx = preparedStmt.executeQuery();
					while(resultSetx.next()) {
						String availableTicketCountStr = resultSetx.getString(Constants.AVAILBLE_TICKETS);
						availableTicketCount = Integer.parseInt(availableTicketCountStr);
					}
					preparedStmt = connection.prepareStatement(updateTicketdetailsTable);
					if(availableTicketCount >0) {
						availableTicketCount =  availableTicketCount -1;
						preparedStmt.setString(1, Integer.toString(availableTicketCount));
						preparedStmt.setString(2, clubid);
						preparedStmt.setString(3, eventDate);
						preparedStmt.setString(4, ticketType);
						preparedStmt.setString(5, category);
						preparedStmt.setString(6, tableSize);
						preparedStmt.execute();
					}else {
						return "sold out";
					}
				}
			}
			
			
//			// check for pass booking
//			if(ticketType != null && ticketType.equalsIgnoreCase("pass")) {
//				
//				String checkPassSQL = "select availbletickets from ticketdetails where type ='pass' and category = ?"
//						+" and clubid  = ? and date = ?";
//				PreparedStatement preparedStmtx = connection.prepareStatement(checkPassSQL);
//				
//				String returnString = "";
//				
//				HashMap<String, Integer> passMap = Utill.getPassMapFromTicketDetails(ticketDetails);
//				Iterator<String> itr = passMap.keySet().iterator();
//				while(itr.hasNext()) {
//					String category = itr.next();
//					Integer value = passMap.get(category);
//					
//					preparedStmtx.setString(1, category);
//					preparedStmtx.setInt(2, clubid);
//					preparedStmtx.setString(3, eventDate);
//					ResultSet resultSetx = preparedStmtx.executeQuery();
//					while(resultSetx.next()) {
//						String availableTicketCountStr = resultSetx.getString(Constants.AVAILBLE_TICKETS);
//						availableTicketCount = Integer.parseInt(availableTicketCountStr);
//					}
//					if(availableTicketCount >=value) { 
//						availableTicketCount =  availableTicketCount -value;
//						preparedStmtxx.setString(1, Integer.toString(availableTicketCount));
//						preparedStmtxx.setInt(2, clubid);
//						preparedStmtxx.setString(3, eventDate);
//						preparedStmtxx.setString(4, ticketType);
//						preparedStmtxx.setString(5, category);
//						preparedStmtxx.setString(6, tableSize);
//						preparedStmtxx.execute();
//					}else if(availableTicketCount == 0){
//						returnString = returnString + " "+category +" sold out";
//						//return category +" sold out";
//					}else {
//						returnString = returnString + "Only "+value+" Available for "+category;
//					}
//				}
//			}
//			
//			// table
//			
			
			synchronized (DBHelper.class) {
				boolean canBookTable = false;
				
				
				ConcurrentHashMap<String, TableRowData> tableDataMap = new ConcurrentHashMap<String, TableRowData>();
				
				if(ticketType != null && ticketType.equalsIgnoreCase("table")) {
					String tableId = orderJobj.getString(Constants.TABLE_ID);
					
					String verifyTableAvailable = "SELECT * FROM tablesdata WHERE clubid = ? AND eventdate = ? ";
					preparedStmt = connection.prepareStatement(verifyTableAvailable);
					preparedStmt.setString(1, clubid);
					//preparedStmt.setString(2, tableId);
					preparedStmt.setString(2, eventDate);
					resultSetx = preparedStmt.executeQuery();
					while(resultSetx.next()) {
						TableRowData trd = new TableRowData();
				        String clubidx = resultSetx.getString("clubid");
				        trd.setClubid(clubidx);
				        String tablenumber = resultSetx.getString("tablenumber");
				        trd.setTablenumber(tablenumber);
				        String tableid = resultSetx.getString("tableid");
				        trd.setTableid(tableid);
				        String isBooked = resultSetx.getString(Constants.ISBOOKED);
				        String booked = resultSetx.getString("booked");
				        trd.setBooked(booked);
				        if(tableid.equalsIgnoreCase(tableId)) {
					        	if(!isBooked.equalsIgnoreCase("booked")) {
									canBookTable = true;
									trd.setBooked("booked");
								}else {
									trd.setBooked(isBooked);
								}
				        }
				        String tabletype = resultSetx.getString("tabletype");
				        trd.setTabletype(tabletype);
				        String coords = resultSetx.getString("coords");
				        trd.setCoords(coords);
				        
				        String eventdate = eventDate.replaceAll("/", "");
				        String destFileName = clubidx+"-"+eventdate+".html";

				        String layoutURL = Constants.TABLE_LAYOUT_URL+destFileName;
				        trd.setLayoutURL(layoutURL);
				        tableDataMap.put(tableid, trd);

					}
					
					if(canBookTable) {
						String updateTableSQL = "UPDATE tablesdata SET booked = 'booked' WHERE clubid = ? AND tableid = ? AND eventdate = ? ";
						preparedStmt = connection.prepareStatement(updateTableSQL);
						
						
						preparedStmt.setString(1, clubid);
						preparedStmt.setString(2, tableId);
						preparedStmt.setString(3, eventDate);
						preparedStmt.execute();
						
						// create updated HTML 
						createHTML( clubid, tableDataMap, eventDate);
						
						
					}else {
						return "fail";
					}
				}
				
			}
			
			
			
			

			String insertOrderSQL = "INSERT INTO bookingdetails ( cutomername, mobile,  customerId, clubname, clubid, QRnumber,  "
					+ "tickettype, eventDate, cost, costafterdiscount, paidamount, remainingamount, discount, ticketDetails, bookingDate, bookingtime) "
					+ " VALUES  ( ?, ? , ?, ?, ? , ?, ? ,?, ? , ?, ?, ?,?, ?, ?, ?)";
			
			 
			// create the mysql insert preparedstatement
			preparedStmt = connection
					.prepareStatement(insertOrderSQL);
			preparedStmt.setString(1, cutomername);
			preparedStmt.setString(2, mobile);
			preparedStmt.setString(3, customerId);
			preparedStmt.setString(4, clubname);
			preparedStmt.setString(5, clubid);
			preparedStmt.setString(6, qrNumber);
			preparedStmt.setString(7, ticketType);
			preparedStmt.setString(8, eventDate);
			preparedStmt.setString(9, cost);
			preparedStmt.setString(10, costAfterDiscount);
			
			preparedStmt.setString(11, paidAmount);
			preparedStmt.setString(12, remainingAmount);
			preparedStmt.setString(13, discount);
			
			preparedStmt.setString(14, ticketDetails);
			preparedStmt.setString(15, bookingDate);
			preparedStmt.setTimestamp(16, bookingTime);

			// execute the preparedstatement
			preparedStmt.execute();

		} catch (Exception e) {
			e.printStackTrace();
			log.info(e.getMessage());
			// TODO: handle exception
			return "fail !! Please contact support";
		}finally {
            if (preparedStmt != null) {
            	preparedStmt.close();
            }
            if (connection != null) {
            	connection.close();
            }
        }
		
		log.info("end executing inserOrderDetails method, sent data");

		return "success";

	}
	
	
	
	
	
	
	
	private void createHTML(String clubid, ConcurrentHashMap<String, TableRowData> tableDataMap, String eventDate) {
		//ConcurrentHashMap<String, TableRowData> tableDataMap = new ConcurrentHashMap<String, TableRowData>();
		BufferedReader reader = null;
        
        FileWriter writer = null;
		
	    	try{
			
			
			File fileToBeModified = new File(Constants.COMMON_FILE);
			
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
            
            File tempfile = new File(Constants.TEMP_FILE_PATH+tempFileName+".html");
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
            
            eventDate = eventDate.replaceAll("/", "");
            String destFileName = clubid+"-"+eventDate;
            File destfile = new File(Constants.RENAME_FILE_PATH+destFileName+".html");
			
			
	    	
			if(tempfile.renameTo(destfile)){
				System.out.println(tempfile.getName() + " is renamed!");
			}else{
				System.out.println("renamed operation is failed.");
			}
		   
		}catch(Exception e){
			
			e.printStackTrace();
			
		}
		
	}	
	
	
//	public static void main(String[] arg) {
//		DBHelper db = new DBHelper();
//		JSONObject productObj = new JSONObject();
//		db.loadClubListFromDatabase(productObj);
//	}

}
