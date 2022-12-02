import java.util.Scanner;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.sql.*;
import java.io.*;

public class DatabaseProject {
    public static String dbAddress = "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db57?autoReconnect=true&useSSL=false";
    public static String dbUsername = "Group57";
    public static String dbPassword = "CSCI3170";
    

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            try {
            	conn = DriverManager.getConnection(dbAddress,dbUsername,dbPassword);
            } catch (SQLException e) {
                System.out.println("Failed to connect to database.");
                System.exit(0);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Unable to load the driver class!"); 
            System.exit(0);
        }
        return conn;
    }

	private static void listAllSalesperson(Scanner scanner, Connection db) throws SQLException{
		String order;
		String searchSQL = "";
		while (true) {
			System.out.println("Choose ordering:");
			System.out.println("1. By ascending order");
			System.out.println("2. By descending order");
			System.out.print("Choose the list ordering: ");
			order = scanner.next();
			if (order.equals("1") || order.equals("2")) {
				break;
			} else {
				System.out.println("Invalid input! Please try again.");
			}
		}

		searchSQL += "SELECT S.sID, S.sName, S.sPhoneNumber, S.sExperience ";
		searchSQL += "FROM salesperson S ";

		if (order.equals("1")) {
			searchSQL += "ORDER BY S.sExperience ASC";
		} else if (order.equals("2")) {
			searchSQL += "ORDER BY S.sExperience DESC";
		}

		System.out.println("| ID | Name | Mobile Phone | Years of Experience |");
		PreparedStatement stmt = db.prepareStatement(searchSQL);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()){
			System.out.print("| " + rs.getString(1) + " | " + rs.getString(2) + " | ");
			System.out.println(rs.getString(3) + " | " + rs.getString(4) + " |");
		}
		System.out.println("End of Query");
		rs.close();
		stmt.close();
	}

	private static void countNoOfsales(Scanner scanner, Connection db) throws SQLException{
		int lowerYear, upperYear;
		String viewSQL = "";
		String resultSQL = "";
	
		
		while (true) {
			System.out.print("Type in the lower bound for years of experience: ");
			lowerYear = scanner.nextInt();
			System.out.print("Type in the upper bound for years of experience: ");
			upperYear = scanner.nextInt();
			if (lowerYear <= upperYear) {
				break;
			} else {
				System.out.println("Invalid input! Please try again.");
			}
		}

		System.out.println("Transaction Record: ");
		System.out.println("| ID | Name | Years of Experience | Number of Transaction |");

		viewSQL += "CREATE VIEW temp AS ";
	 	viewSQL += "SELECT T.sID, COUNT(*) AS noOfTran";
		viewSQL += "FROM transaction T ";
		viewSQL += "GROUP BY T.sID";
		PreparedStatement stmt = db.prepareStatement(viewSQL);
		stmt.execute();

		resultSQL += "SELECT S.sID, S.sName, S.sExperience, temp.noOfTran ";
		resultSQL += "FROM salesperson S, temp ";
		resultSQL += "WHERE S.sID = temp.sID AND S.sExperience >= " + lowerYear + " AND S.sExperience <= " + upperYear + " ";
		resultSQL += "ORDER BY S.sID DESC";

		stmt = db.prepareStatement(resultSQL);
		ResultSet rs = stmt.executeQuery();

		while(rs.next()){
			System.out.print("| " + rs.getString(1) + " | " + rs.getString(2) + " | ");
			System.out.println(rs.getString(3) + " | " + rs.getString(4) + " |");
		}
		System.out.println("End of Query");
		stmt = db.prepareStatement("DROP VIEW temp");
		stmt.execute();

		rs.close();
		stmt.close();
	}

	private static void showTotalSalesValue(Connection db) throws SQLException{
		String resultSQL = "";

		System.out.println("| Manufacturer ID | Manufacturer Name | Total Sales Value |");
		resultSQL += "SELECT M.mID, M.mName, SUM(P.pPrice) AS price ";
		resultSQL += "FROM manufacturer M, part P, transaction T ";
		resultSQL += "WHERE M.mID = P.mID AND P.pID = T.pID ";
		resultSQL += "GROUP BY M.mID ";
		resultSQL += "ORDER BY price DESC";

		PreparedStatement stmt = db.prepareStatement(resultSQL);
		ResultSet rs = stmt.executeQuery();

		while(rs.next()){
			System.out.print("| " + rs.getString(1) + " | " + rs.getString(2) + " | ");
			System.out.println(rs.getString(3) + " |");
		}
		System.out.println("End of Query");
		rs.close();
		stmt.close();
	}

	private static void showNMostPopularPart(Scanner scanner, Connection db) throws SQLException{
		String viewSQL = "";
		String resultSQL = "";

		System.out.print("Type in the number of parts: ");
		String numOfPart = scanner.next();
		System.out.println("| Part ID | Part Name | No. of Transaction |");

		viewSQL += "CREATE VIEW temp AS ";
		viewSQL += "SELECT T.pID, COUNT(*) AS numOfTrans ";
		viewSQL += "FROM transcation T ";
		viewSQL += "GROUP BY T.pID ";
		PreparedStatement stmt = db.prepareStatement(viewSQL);
		stmt.execute();

		resultSQL += "SELECT P.pID, P.pName, temp.numOfTrans ";
		resultSQL += "FROM part P, temp ";
		resultSQL += "WHERE P.pID = temp.pID ";
		resultSQL += "ORDER BY temp.numOfTrans DESC LIMIT " + numOfPart;

		stmt = db.prepareStatement(resultSQL);
		ResultSet rs = stmt.executeQuery();

		while(rs.next()){
			System.out.print("| " + rs.getString(1) + " | " + rs.getString(2) + " | ");
			System.out.println(rs.getString(3) + " |");
		}


		System.out.println("End of Query");
		stmt = db.prepareStatement("DROP VIEW temp");
		stmt.execute();

		rs.close();
		stmt.close();
	
	}

    private static void managerMenu(Scanner scanner, Connection db) throws SQLException{
		while (true) {
			try {
				System.out.println();
                System.out.println("-----Operations for manager menu-----");
                System.out.println("What kinds of operation would you like to perform?");
                System.out.println("1. List All salespersons");
                System.out.println("2. Count the no. of sales record of each salesperson under a specific range on years of experience");
				System.out.println("3. Show the total sales value of each manufacturer");
				System.out.println("4. Show the N most popular part");
                System.out.println("5. Return to the main menu");
				System.out.print("Enter Your Choice: ");
				int input = scanner.nextInt();

				if (input == 1) {
					listAllSalesperson(scanner, db);
				} else if (input == 2) {
					countNoOfsales(scanner, db);
				} else if (input == 3) {
					showTotalSalesValue(db);
				} else if (input == 4) {
					showNMostPopularPart(scanner, db);
				} else if (input == 5) {
					break;
				} else {
					System.out.println("Invalid input! Please try again.");
				}
			} catch (SQLException e) {
				System.out.println(e);
			}
		}
	
	}

	private static void searchForParts(Scanner scanner, Connection db) throws SQLException{
		String method;
		String keyword;
		String order;
		String searchSQL = "";


		searchSQL += "SELECT P.pID, P.pName, M.mName, C.cName, P.pAvailableQuantity, P.pWarrantyPeriod, P.pPrice";
		searchSQL += "FROM part P , manufacturer M, category C";
		searchSQL += "WHERE P.mID = M.mID AND P.cID = C.cID ";
		
		while (true) {
			System.out.println();
			System.out.println("Choose the search criterion:");
			System.out.println("1. Part Name");
			System.out.println("2. Manufacturer Name");
			System.out.print("Choose the search criterion: ");
			method = scanner.next();
			if (method.equals("1") || method.equals("2")) {
				break;
			} else {
				System.out.println("Invalid input! Please try again.");
			}
		}
		
		while (true) {
			System.out.print("Type in the search keyword:");
			keyword = scanner.next();
			if (!keyword.isEmpty())
				break;
		}
		
		if (method.equals("1")) {
			searchSQL += "AND P.pName LIKE '%" + keyword + "%'";
		} else if (method.equals("2")) {
			searchSQL += "AND M.mName LIKE '%" + keyword + "%'";
		}

		while (true) {
			System.out.println();
			System.out.println("Choose ordering:");
			System.out.println("1. By price, ascending order");
			System.out.println("2. By price, descending order");
			System.out.print("Choose the search criterion: ");
			order = scanner.next();
			if (order.equals("1") || order.equals("2")) {
				break;
			} else {
				System.out.println("Invalid input! Please try again.");
			}
		}

		if (order.equals("1")) {
			searchSQL += "ORDER BY P.pPrice ASC";
		} else if (order.equals("2")) {
			searchSQL += "ORDER BY P.pPrice DESC";
		}

		System.out.println("| ID | Name | Manufacturer | Category | Quantity | Warranty | Price |");
		PreparedStatement stmt = db.prepareStatement(searchSQL);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()){
			System.out.print("| " + rs.getString(1) + " | " + rs.getString(2) + " | ");
			System.out.print(rs.getString(3) + " | " + rs.getString(4) + " | ");
			System.out.print(rs.getString(5) + " | " + rs.getString(6) + " | ");
			System.out.println(rs.getString(7) + " |");
		}
		System.out.println("End of Query");
		rs.close();
		stmt.close();

	}

	private static void sellAPart(Scanner scanner, Connection db) throws SQLException{
		String pID;
		String sID;
		String checkPartSQL = "";
		while (true) {
			System.out.print("Enter The Part ID: ");
			pID = scanner.next();
			if (!pID.isEmpty())
				break;
		}
		
		while (true) {
			System.out.print("Enter The Salesperson ID: ");
			sID = scanner.next();
			if (!sID.isEmpty())
				break;
		}
		
		checkPartSQL += "SELECT P.pAvailableQuantity, P.pName ";
		checkPartSQL += "FROM part P ";
		checkPartSQL += "WHERE P.pID = " + pID ;

		PreparedStatement stmt = db.prepareStatement(checkPartSQL);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()){
			int pQuantity = rs.getInt(1);
			String pName = rs.getString(2);
			if (pQuantity > 0){
				// update part quantity
				pQuantity -= 1;
				stmt = db.prepareStatement("UPDATE part SET pAvailableQuantity = ? WHERE pID = ?");
				stmt.setInt(1, pQuantity);
				stmt.setInt(2, Integer.parseInt(pID));
				stmt.execute();
				System.out.println("Product: " + pName + "(id: " + pID + ") Remaining Quantity: " + pQuantity);
				
				// insert transaction record
				int tID = 0;
				stmt = db.prepareStatement("SELECT MAX(T.tID) FROM transaction");
				rs = stmt.executeQuery();
				if (rs.next()){
					tID = rs.getInt(1);
				}
				tID += 1;

				Calendar cal = Calendar.getInstance(); 
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				String dateInStr = sdf.format(cal.getTime());

				stmt = db.prepareStatement("INSERT INTO transaction (tID, pID, sID, tDate) VALUES (?,?,?,?)");
				stmt.setInt(1, tID);
				stmt.setInt(2, Integer.parseInt(pID));
                stmt.setInt(3, Integer.parseInt(sID));
				
                stmt.setString(4, dateInStr);
				stmt.executeUpdate();
			} else { 
				// print error message (part quantity <= 0), exit current function
				System.out.println("[Error] Part is not avalible."); 
				rs.close();
				stmt.close();
				return;
			}
					
		} else {
			// print error message, exit current function
			System.out.println("[Error] No part result found.");
			rs.close();
			stmt.close();
			return;
		}

		

	}


    private static void salespersonMenu(Scanner scanner, Connection db) throws SQLException{
		while (true) {
			try {
				System.out.println();
                System.out.println("-----Operations for salesperson menu-----");
                System.out.println("What kinds of operation would you like to perform?");
                System.out.println("1. Search for parts");
                System.out.println("2. Sell a part");
                System.out.println("3. Return to the main menu");
				System.out.print("Enter Your Choice: ");
				int input = scanner.nextInt();

				if (input == 1) {
					searchForParts(scanner, db);
				} else if (input == 2) {
					sellAPart(scanner, db);
				} else if (input == 3) {
					break;
				} else {
					System.out.println("Invalid input! Please try again.");
				}
			} catch (SQLException e) {
				System.out.println(e);
			}
		}
	
	}

	private static void createAllTables(Connection db) throws SQLException{
        Statement stmt = db.createStatement();

        String category_sql = "CREATE TABLE category (";
        category_sql += "cID INTEGER NOT NULL PRIMARY KEY,";
        category_sql += "cName VARCHAR(20) NOT NULL)";

        String manufacturer_sql = "CREATE TABLE manufacturer (";
        manufacturer_sql += "mID INTEGER NOT NULL PRIMARY KEY,";
        manufacturer_sql += "mName VARCHAR(20) NOT NULL,";
        manufacturer_sql += "mAddress VARCHAR(50) NOT NULL,";
        manufacturer_sql += "mPhoneNumber INTEGER NOT NULL)";

        String part_sql = "CREATE TABLE part (";
        part_sql += "pID INTEGER NOT NULL PRIMARY KEY,";
        part_sql += "pName VARCHAR(20) NOT NULL,";
        part_sql += "pPrice INTEGER NOT NULL,";
        part_sql += "mID INTEGER NOT NULL,";
        part_sql += "cID INTEGER NOT NULL,";
        part_sql += "pWarrantyPeriod INTEGER NOT NULL,";
        part_sql += "pAvailableQuantity INTEGER NOT NULL)";

        String salesperson_sql = "CREATE TABLE salesperson (";
        salesperson_sql += "sID INTEGER NOT NULL PRIMARY KEY,";
        salesperson_sql += "sName VARCHAR(20) NOT NULL,";
        salesperson_sql += "sAdress VARCHAR(50) NOT NULL,";
        salesperson_sql += "sPhoneNumber INTEGER NOT NULL,";
        salesperson_sql += "sExperience INTEGER NOT NULL)";

        String transaction_sql = "CREATE TABLE transaction (";
        transaction_sql += "tID INTEGER NOT NULL PRIMARY KEY,";
        transaction_sql += "pID INTEGER NOT NULL,";
        transaction_sql += "sID INTEGER NOT NULL";
        transaction_sql += "tDate DATE NOT NULL)";

        System.out.print("Processing...");
		stmt.execute(category_sql);
		stmt.execute(manufacturer_sql);
		stmt.execute(part_sql);
		stmt.execute(salesperson_sql);
        stmt.execute(transaction_sql);
		System.out.println("Done! Database is initialized!");
		stmt.close();
    }

    private static void deleteAllTables(Connection db) throws SQLException{
        Statement stmt = db.createStatement();
        System.out.print("Processing...");
		stmt.execute("DROP TABLE category");
		stmt.execute("DROP TABLE manufacturer");
		stmt.execute("DROP TABLE part");
		stmt.execute("DROP TABLE salesperson");
        stmt.execute("DROP TABLE transaction");
        System.out.println("Done! Database is removed!");
		stmt.close();
    }

    private static void loadFromDatafile(Scanner scanner, Connection db) throws SQLException{
        System.out.println();
        System.out.print("Type in the Source Data Folder Path: ");
        String path = scanner.next();
        System.out.print("Processing...");
        
        try {
			PreparedStatement stmt = db.prepareStatement("INSERT INTO category (cID, cName) VALUES (?,?)");
			String line;
			BufferedReader br = new BufferedReader(new FileReader(path + "/category.txt"));
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
                stmt.setInt(1, Integer.parseInt(data[0]));
				stmt.setString(2, data[1]);
				stmt.executeUpdate();
			}
			stmt.close();
			br.close();
		} catch (Exception e) {
			System.out.println(e);
		}

        try {
			PreparedStatement stmt = db.prepareStatement("INSERT INTO manufacturer (mID, mName, mAddress, mPhoneNumber) VALUES (?,?,?,?)");
			String line;
			BufferedReader br = new BufferedReader(new FileReader(path + "/manufacturer.txt"));
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
                stmt.setInt(1, Integer.parseInt(data[0]));
				stmt.setString(2, data[1]);
                stmt.setString(3, data[2]);
                stmt.setInt(4, Integer.parseInt(data[3]));
				stmt.executeUpdate();
			}
			stmt.close();
			br.close();
		} catch (Exception e) {
			System.out.println(e);
		}
        
        try {
			PreparedStatement stmt = db.prepareStatement("INSERT INTO part (pID, pName, pPrice, mID, cID, pWarrantyPeriod, pAvailableQuantity) VALUES (?,?,?,?,?,?,?)");
			String line;
			BufferedReader br = new BufferedReader(new FileReader(path + "/part.txt"));
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
                stmt.setInt(1, Integer.parseInt(data[0]));
				stmt.setString(2, data[1]);
                stmt.setInt(3, Integer.parseInt(data[2]));
                stmt.setInt(4, Integer.parseInt(data[3]));
                stmt.setInt(5, Integer.parseInt(data[4]));
                stmt.setInt(6, Integer.parseInt(data[5]));
                stmt.setInt(7, Integer.parseInt(data[6]));
				stmt.executeUpdate();
			}
			stmt.close();
			br.close();
		} catch (Exception e) {
			System.out.println(e);
		}

        try {
			PreparedStatement stmt = db.prepareStatement("INSERT INTO salesperson (sID, sName, sAdress, sPhoneNumber, sExperience) VALUES (?,?,?,?,?)");
			String line;
			BufferedReader br = new BufferedReader(new FileReader(path + "/salesperson.txt"));
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
                stmt.setInt(1, Integer.parseInt(data[0]));
				stmt.setString(2, data[1]);
                stmt.setString(3, data[2]);
                stmt.setInt(4, Integer.parseInt(data[3]));
                stmt.setInt(5, Integer.parseInt(data[4]));
				stmt.executeUpdate();
			}
			stmt.close();
			br.close();
		} catch (Exception e) {
			System.out.println(e);
		}

        try {
			PreparedStatement stmt = db.prepareStatement("INSERT INTO transaction (tID, pID, sID, tDate) VALUES (?,?,?,?)");
			String line;
			BufferedReader br = new BufferedReader(new FileReader(path + "/transaction.txt"));
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
                stmt.setInt(1, Integer.parseInt(data[0]));
				stmt.setInt(2, Integer.parseInt(data[1]));
                stmt.setInt(3, Integer.parseInt(data[2]));
                stmt.setString(4, data[3]);
				stmt.executeUpdate();
			}
			stmt.close();
			br.close();
		} catch (Exception e) {
			System.out.println(e);
		}

        System.out.println("Data is inputted to the database!");
    }
    
    private static void showContentOfTable(Scanner scanner, Connection db) throws SQLException{
        System.out.print("Which table would you like to show: ");
        String chosenTable = scanner.next();
        if (chosenTable.equals("category")){
			try{ 
				System.out.println("| cID | cName |");
				PreparedStatement stmt = db.prepareStatement("SELECT * FROM category");
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){
					System.out.println("| " + rs.getString(1) + " | " + rs.getString(2) + " |");
				}
				System.out.println("End of Query");
				rs.close();
				stmt.close();
			}catch (Exception e) {
				System.out.println(e);
			}
		}else if (chosenTable.equals("manufacturer")){
			try{ 
				System.out.println("| mID | mName | mAddress | mPhoneNumber |");
				PreparedStatement stmt = db.prepareStatement("SELECT * FROM manufacturer");
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){
					System.out.print("| " + rs.getString(1) + " | " + rs.getString(2) + " | ");
					System.out.println(rs.getString(3) + " | " + rs.getString(4) + " |");
				}
				System.out.println("End of Query");
				rs.close();
				stmt.close();
			}catch (Exception e) {
				System.out.println(e);
			}
			
		}else if (chosenTable.equals("part")){
			try{ 
				System.out.println("| pID | pName | pPrice | mID | cID | pWarrantyPeriod | pAvailableQuantity |");
				PreparedStatement stmt = db.prepareStatement("SELECT * FROM part");
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){
					System.out.print("| " + rs.getString(1) + " | " + rs.getString(2) + " | ");
					System.out.print(rs.getString(3) + " | " + rs.getString(4) + " | ");
					System.out.print(rs.getString(5) + " | " + rs.getString(6) + " | ");
					System.out.println(rs.getString(7) + " |");
				}
				System.out.println("End of Query");
				rs.close();
				stmt.close();
			}catch (Exception e) {
				System.out.println(e);
			}
			
		}else if (chosenTable.equals("salesperson")){
			try{ 
				System.out.println("| sID | sName | sAdress | sPhoneNumber | sExperience |");
				PreparedStatement stmt = db.prepareStatement("SELECT * FROM salesperson");
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){
					System.out.print("| " + rs.getString(1) + " | " + rs.getString(2) + " | ");
					System.out.print(rs.getString(3) + " | " + rs.getString(4) + " | ");
					System.out.println(rs.getString(5) + " |");
				}
				System.out.println("End of Query");
				rs.close();
				stmt.close();
			}catch (Exception e) {
				System.out.println(e);
			}
			
		}else if (chosenTable.equals("transaction")){
			try{ 
				System.out.println("| tID | pID | sID | tDate |");
				PreparedStatement stmt = db.prepareStatement("SELECT * FROM transaction");
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){
					System.out.print("| " + rs.getString(1) + " | " + rs.getString(2) + " | ");
					System.out.println(rs.getString(3) + " | " + rs.getString(4) + " |");
				}
				System.out.println("End of Query");
				rs.close();
				stmt.close();
			}catch (Exception e) {
				System.out.println(e);
			}
		}else {
			System.out.println("Invalid table name!");
		}
    
    }

    private static void adminMenu(Scanner scanner, Connection db) throws SQLException{
        while (true) {
			try {
				System.out.println();
                System.out.println("-----Operations for administrator menu-----");
                System.out.println("What kinds of operation would you like to perform?");
                System.out.println("1. Create all tables");
                System.out.println("2. Delete all tables");
                System.out.println("3. Load from datafile");
                System.out.println("4. Show content of a table");
                System.out.println("5. Return to the main menu");
				System.out.print("Enter Your Choice: ");
				int input = scanner.nextInt();

				if (input == 1) {
					createAllTables(db);
				} else if (input == 2) {
					deleteAllTables(db);
				} else if (input == 3) {
					loadFromDatafile(scanner, db);
                } else if (input == 4) {
					showContentOfTable(scanner, db);
				} else if (input == 5) {
					break;
				} else {
					System.out.println("Invalid input! Please try again.");
				}
			} catch (SQLException e) {
				System.out.println(e);
			}
		}
    
    }

    public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Welcome to sales system!");

		while (true) {
			try {
				Connection db = getConnection();
				System.out.println();
                System.out.println("-----Main menu-----");
                System.out.println("What kinds of operation would you like to perform?");
                System.out.println("1. Operations for administrator");
                System.out.println("2. Operations for salesperson");
                System.out.println("3. Operations for manager");
                System.out.println("4. Exit this program");
				System.out.print("Enter Your Choice: ");
				int input = scanner.nextInt();

				if (input == 1) {
					adminMenu(scanner, db);
				} else if (input == 2) {
					salespersonMenu(scanner, db);
				} else if (input == 3) {
					managerMenu(scanner, db);
				} else if (input == 4) {
					break;
				} else {
					System.out.println("Invalid input! Please try again.");
				}
			} catch (SQLException e) {
				System.out.println(e);
			}
		}

		scanner.close();
		System.exit(0);
	}

}
