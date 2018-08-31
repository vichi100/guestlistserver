package com.mkanchwala.loggers.main;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

import javax.sql.DataSource;

public class ClubServer {
	
	static Logger log = LogManager.getLogger(ClubServer.class.getName());

	public static void main(String[] args) throws Exception {

		// create db connection
		final DBHelper dbhelper = new DBHelper();
		final DataSource dataSource = dbhelper.setUp();

		// configure and start server
		Configuration config = new Configuration();
		config.setCloseTimeout(36000);
		// config.setHostname("localhost");
		config.setPort(3080);//3080
		config.setMaxFramePayloadLength(65536000);
		// config.setAuthorizationListener(new AuthorizationListener() {
		// @Override
		// public boolean isAuthorized(HandshakeData data) {
		// String username = data.getSingleUrlParam("username");
		// String password = data.getSingleUrlParam("password");
		//// // if username and password correct
		//// return true;
		//// // else
		// return false;
		// }
		// });

		final SocketIOServer server = new SocketIOServer(config);

		// cassandraConnector.initPrepareStatment();

		server.addMessageListener(new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String event, AckRequest ackRequest) throws SQLException {

				JSONObject eventJObj = new JSONObject(event);
				String action = eventJObj.getString("action");

				if ("loadClubListFromDatabase".equalsIgnoreCase(action)) {
					List<JSONObject> clubsList = dbhelper.loadClubListFromDatabase(eventJObj, dataSource);
					JSONObject jObj = new JSONObject();
					jObj.put("jsonResponseList", clubsList);
					client.sendMessage(jObj.toString());
				} else if ("getTicketdetailsFromDatabase".equalsIgnoreCase(action)) {
					List<JSONObject> ticketDetailsList = dbhelper.getTicketdetailsFromDatabase(eventJObj, dataSource);
					JSONObject jObj = new JSONObject();
					jObj.put("jsonResponseList", ticketDetailsList);
					client.sendMessage(jObj.toString());

				} else if ("getEventDetailsFromDatabase".equalsIgnoreCase(action)) {

					List<JSONObject> eventsDetailList = dbhelper.getEventDetailsFromDatabase(eventJObj, dataSource);
					JSONObject jObj = new JSONObject();
					jObj.put("eventsDetailList", eventsDetailList);
					
					List<JSONObject> ticketDetailsList = dbhelper.getTicketdetailsFromDatabase(eventJObj, dataSource);
					jObj.put("ticketDetailsList", ticketDetailsList);
					
					List<JSONObject> tableDetailsList = dbhelper.getTabledetailsByDateForClubFromDatabase(eventJObj, dataSource);
					jObj.put("tableDetailsList", tableDetailsList);
					
					client.sendMessage(jObj.toString());

				} else if ("getbookedTicketFromDatabase".equalsIgnoreCase(action)) {

					List<JSONObject> bookedTicketList = dbhelper.getbookedTicketFromDatabase(eventJObj, dataSource);
					JSONObject jObj = new JSONObject();
					jObj.put("bookedTicketList", bookedTicketList);
					client.sendMessage(jObj.toString());

				}else if ("inserOrderDetails".equalsIgnoreCase(action)) {
					String result = dbhelper.inserOrderDetails(eventJObj, dataSource);
					
					client.sendMessage(result);

				}else if ("createNewCustomer".equalsIgnoreCase(action)) {
					String customerId = dbhelper.createNewCustomer(eventJObj, dataSource);
					
					JSONObject jObj = new JSONObject();
					jObj.put(Constants.CUSTOMERID, customerId);
					
					client.sendMessage(jObj.toString());

				}else if("clubLogindDataAndValidation".equalsIgnoreCase(action)) {
					//clubLogindDataAndValidation
					JSONObject jObj = dbhelper.clubLogindDataAndValidation(eventJObj, dataSource);
					client.sendMessage(jObj.toString());
					
				}else if ("getOffersFromDatabase".equalsIgnoreCase(action)) {
					
					List<JSONObject> offersList = dbhelper.getOffersFromDatabase(eventJObj, dataSource);
					JSONObject jObj = new JSONObject();
					jObj.put("offersList", offersList);
					client.sendMessage(jObj.toString());
					
					

				}else if ("getEventDetailsForOfferFromDatabase".equalsIgnoreCase(action)) {
					
					List<JSONObject> eventsDetailList = dbhelper.getEventDetailsForOfferFromDatabase(eventJObj, dataSource);
					JSONObject jObj = new JSONObject();
					jObj.put("eventsDetailList", eventsDetailList);
					
					List<JSONObject> ticketDetailsList = dbhelper.getTicketdetailsByDateFromDatabase(eventJObj, dataSource);
					jObj.put("ticketDetailsList", ticketDetailsList);
					
					List<JSONObject> tableDetailsList = dbhelper.getTabledetailsByDateFromDatabase(eventJObj, dataSource);
					jObj.put("tableDetailsList", tableDetailsList);
					
					client.sendMessage(jObj.toString());
					
					

				}else if("getDataForReportChartFromDatabase".equalsIgnoreCase(action)) {
					JSONObject jObj  = dbhelper.getDataForReportChartFromDatabase(eventJObj, dataSource);
					client.sendMessage(jObj.toString());
				}
//				else if("getTabledetailsByDateFromDatabase".equalsIgnoreCase(action)) {
//					JSONObject jObj = new JSONObject();
//					List<JSONObject> tableDetailsList = dbhelper.getTabledetailsByDateFromDatabase(eventJObj, dataSource);
//					jObj.put("tableDetailsList", tableDetailsList);
//					client.sendMessage(jObj.toString());
//				}

			}
		});

		// server.addJsonObjectListener(JSONObject.class, new DataListener<JSONObject>()
		// {
		// @Override
		// public void onData(SocketIOClient client, JSONObject obj, AckRequest
		// ackRequest) {
		//
		// log.info();
		//
		//
		// }
		// });

		server.addConnectListener(new ConnectListener() {
			@Override
			public void onConnect(SocketIOClient client) {
				log.info("on connect");
			}
		});

		server.addDisconnectListener(new DisconnectListener() {
			@Override
			public void onDisconnect(SocketIOClient client) {
				log.info("on disconnect");
			}
		});

		server.start();
		
		log.info("server started");

		//Thread.sleep(Integer.MAX_VALUE);

		// server.stop();
	}

}
