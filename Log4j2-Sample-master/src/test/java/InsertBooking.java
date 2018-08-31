import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InsertBooking {
	
	static String  insertData = "	t3	99999	99999	 True Tramm Trunk 	99999	1534935641374	 table      	22/Aug/2018	15000	15000	4500	10500	0	 NORMAL	Table No. 10; personal service	22/Aug/2018	2018-08-22 11:00:41.353	";

	public static void main(String[] args) throws Exception {
		
BufferedReader reader = null;
        
        FileWriter writer = null;
		
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		
		String URL= "jdbc:mysql://192.168.0.5:3306/club?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=GMT&verifyServerCertificate=false&useSSL=false";
		Connection conn = DriverManager.getConnection(URL, "root", "vichi123");
		
		String query = "INSERT INTO bookingdetails (cutomername,mobile,customerId,clubname,clubid,QRnumber,tickettype,eventDate,cost,costafterdiscount,paidamount,remainingamount,discount,ticketDetails,bookingdate)\n" + 
				"VALUES\n" + 
				"('t3','99999','99999','t3','99999','1534935641374','table','22/Aug/2018','15000','15000','4500','10500','0 ','NORMAL, Table No. 10, personal service','22/Aug/2018')\n" + 
				"";
		
		File fileToBeModified = new File("/Users/vichi/insertSql.txt");
		
		reader = new BufferedReader(new FileReader(fileToBeModified));
        
        //Reading all the lines of input text file into oldContent
        String line = reader.readLine();
        StringBuilder sb = new StringBuilder();
        
        while (line != null ) 	
        {
        		sb.append(line);
        		line = reader.readLine();
        }

	      // create the java statement
	      Statement st = conn.createStatement();
	      
	      // execute the query, and get a java resultset
	      st.execute(sb.toString());
		
		// TODO Auto-generated method stub
//		String after = insertData.trim().replaceAll(" +", " ");
//		String insertDatax = after.replaceAll(" ", "'");
//		System.out.println(insertDatax);
//		String[] insertDataTokens = insertDatax.split(":");
//		for(String insertDataToken: insertDataTokens) {
//			System.out.println(insertDataToken);
//		}
		
	}

}
