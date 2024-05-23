/*
Name: Christopher Deluigi
Course: CNT 4714 Spring 2024
Assignment title: Project 3 – A Two-tier Client-Server Application
Date: March 10, 2024
Class: Enterprise Computing
*/
package the_Accountant;

import java.io.IOException;
import java.io.InputStream;
//A TableModel that supplies ResultSet data to a JTable.
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.table.AbstractTableModel;


public class ResultSetTableModel extends AbstractTableModel 
{
private Connection connection;
private Statement statement;
private ResultSet resultSet;
private ResultSetMetaData metaData;
private int numberOfRows;

private boolean connectedToDatabase = false;


public ResultSetTableModel(Connection c, String query ) 
   throws SQLException, ClassNotFoundException
{         

   connection = c;

   // create Statement to query database
   statement = connection.createStatement( 
      ResultSet.TYPE_SCROLL_INSENSITIVE,
      ResultSet.CONCUR_READ_ONLY );

   // update database connection status
   connectedToDatabase = true;
   
   StringBuilder firstLetter = new StringBuilder();
   firstLetter.append(query.charAt(0));
   if(firstLetter.toString().equalsIgnoreCase("s"))
   {
 	  // set query and execute it
 	  setQuery( query );
   }
   else
 	  setUpdate(query);
		
} // end constructor ResultSetTableModel


public Class getColumnClass( int column ) throws IllegalStateException
{
   // ensure database connection is available
   if ( !connectedToDatabase ) 
      throw new IllegalStateException( "Not Connected to Database" );

   
   try 
   {
      String className = metaData.getColumnClassName( column + 1 );
      
      // return Class object that represents className
      return Class.forName( className );
   } // end try
   catch ( Exception exception ) 
   {
      exception.printStackTrace();
   } // end catch
   
   return Object.class; // if problems occur above, assume type Object
} // end method getColumnClass

// get number of columns in ResultSet
public int getColumnCount() throws IllegalStateException
{   
   // ensure database connection is available
   if ( !connectedToDatabase ) 
      throw new IllegalStateException( "Not Connected to Database" );

   // determine number of columns
   try 
   {
      return metaData.getColumnCount(); 
   } // end try
   catch ( SQLException sqlException ) 
   {
      sqlException.printStackTrace();
   } // end catch
   
   return 0; // if problems occur above, return 0 for number of columns
} // end method getColumnCount

// get name of a particular column in ResultSet
public String getColumnName( int column ) throws IllegalStateException
{    
   // ensure database connection is available
   if ( !connectedToDatabase ) 
      throw new IllegalStateException( "Not Connected to Database" );

   // determine column name
   try 
   {
      return metaData.getColumnName( column + 1 );  
   } // end try
   catch ( SQLException sqlException ) 
   {
      sqlException.printStackTrace();
   } // end catch
   
   return ""; // if problems, return empty string for column name
} // end method getColumnName

// return number of rows in ResultSet
public int getRowCount() throws IllegalStateException
{      
   // ensure database connection is available
   if ( !connectedToDatabase ) 
      throw new IllegalStateException( "Not Connected to Database" );

   return numberOfRows;
} // end method getRowCount

// obtain value in particular row and column
public Object getValueAt( int row, int column ) 
   throws IllegalStateException
{
   // ensure database connection is available
   if ( !connectedToDatabase ) 
      throw new IllegalStateException( "Not Connected to Database" );

   // obtain a value at specified ResultSet row and column
   try 
   {
		   resultSet.next();  /* fixes a bug in MySQL/Java with date format */
      resultSet.absolute( row + 1 );
      return resultSet.getObject( column + 1 );
   } // end try
   catch ( SQLException sqlException ) 
   {
      sqlException.printStackTrace();
   } // end catch
   
   return ""; // 
} // end method getValueAt

// set new database query string
public void setQuery(String query) throws SQLException, IllegalStateException {
    // ensure database connection is available
    if (!connectedToDatabase)
        throw new IllegalStateException("Not Connected to Database");

    // Get the username of the user executing the query
    String username = System.getProperty("user.name"); // You may need a better way to get the username

    // specify query and execute it
    resultSet = statement.executeQuery(query);

    // obtain meta data for ResultSet
    metaData = resultSet.getMetaData();

    // determine number of rows in ResultSet
    resultSet.last();                   // move to last row
    numberOfRows = resultSet.getRow();  // get row number

    // Update operationslog with user and query count
    updateOperationsLog(username, countQueries(query), numberOfRows);

    // notify JTable that model has changed
    fireTableStructureChanged();
}

// Helper method to count the number of queries in a SQL statement
private int countQueries(String query) {
    // Simple logic: count occurrences of "SELECT" in the query
    int count = query.split("(?i)SELECT").length - 1;
    return count > 0 ? count : 1; // At least one query
}

// Helper method to update operationslog
private void updateOperationsLog(String username, int queryCount, int updateCount) {
    // Load properties from the project3app.properties file
    Properties appProperties = new Properties();

    try (InputStream input = getClass().getClassLoader().getResourceAsStream("project3app.properties")) {
        if (input != null) {
            appProperties.load(input);
        } else {
            
            return;
        }
    } catch (IOException e) {
        e.printStackTrace();
        // Handle the exception (e.g., log or display an error)
        return;
    }

    String appUsername = appProperties.getProperty("MYSQL_DB_USERNAME");
    String appPassword = appProperties.getProperty("MYSQL_DB_PASSWORD");

    try (Connection operationsLogConnection = DriverManager.getConnection("jdbc:mysql://localhost:3312/operationslog", appUsername, appPassword)) {
        
        String updateQuery = "UPDATE log SET query_count = query_count + ?, update_count = update_count + ? WHERE username = ?";
        try (PreparedStatement preparedStatement = operationsLogConnection.prepareStatement(updateQuery)) {
            preparedStatement.setInt(1, queryCount);
            preparedStatement.setInt(2, updateCount);
            preparedStatement.setString(3, username);
            preparedStatement.executeUpdate();
        }
    } catch (SQLException e) {
        // Handle the exception (e.g., log or display an error)
        e.printStackTrace();
    }
}


//set new database update-query string
public void setUpdate( String query ) 
   throws SQLException, IllegalStateException 
{
	    int res;
   // ensure database connection is available
   if ( !connectedToDatabase ) 
      throw new IllegalStateException( "Not Connected to Database" );

   // specify query and execute it
   res = statement.executeUpdate( query );

   fireTableStructureChanged();
} // end method setUpdate


// close Statement and Connection               
public void disconnectFromDatabase()            
{              
   if ( !connectedToDatabase )                  
      return;

   // close Statement and Connection            
   try                                          
   {                                            
      statement.close();                        
      connection.close();                       
   } // end try                                 
   catch ( SQLException sqlException )          
   {                                            
      sqlException.printStackTrace();           
   } // end catch                               
   finally  // update database connection status
   {                                            
      connectedToDatabase = false;              
   } // end finally                             
} // end method disconnectFromDatabase          
}  // end class ResultSetTableModel
