package finalir;

import java.sql.*;

/**
 * @author ziad hashem
 */
public class SendAndReceiveData {

    private Connection con;
    private Statement selectStatement;
    private ResultSet selectResult;

    public void ConnectToDataBase() {

        try {

            IR.Print("Loading the driver ...");

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // This will load the SQL driver, each DB has its own driver
            IR.Print("Connecting to database ...");

            con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=myTest_db;user=TestingUser;password=12345;");

            // Setup the connection with the DB
            IR.Print("Connecting to database ...");

            if (con.isClosed()) {
                IR.Print("connection  is  closed");
            } else if (con != null) {
                IR.Print("Connected to database ...yes");
            }

        } catch (SQLException sqle) {
            IR.Print("ff");
            IR.Print(sqle.getMessage());
        } catch (ClassNotFoundException cnfe) {
            IR.Print("fft");
            IR.Print(cnfe.getMessage());
        }
    }

    public void CloseDataBase() {
        try {
            if (con != null) {
                con.close();
            }
            IR.Print("con is closed");

        } catch (SQLException sqle) {
            IR.Print(sqle.getMessage());
        }
    }

    public void sendData(String query) {
        try {
            selectStatement = con.createStatement();
            IR.Print("Sending Query to database Table ...\n");

            selectStatement.executeUpdate(query);
            IR.Print("The Query  :  " + query + "\n");

        } catch (SQLException sqle) {
            IR.Print(sqle.getMessage());
        }
    }

    public void ReceiveData(String query) {
        try {
            selectStatement = con.createStatement();
            IR.Print("Sending Query to database Table ...\n");
            selectResult = selectStatement.executeQuery(query);
            IR.Print("The Query  :  " + query + "\n");

            
            while (selectResult.next()) {
                IR.Print(selectResult.getInt(1)
                        + "\t" + selectResult.getString(2)
                        + "\t" + selectResult.getInt(3));
            }
        } catch (SQLException sqle) {
            IR.Print(sqle.getMessage());
        }
    }
}
