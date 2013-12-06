
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class Server extends SwingWorker<Void, String>{
    
    private ServerSocket serverSocket=null;
    private Socket socket=null;
    
    private Connection connect = null;
    private Statement statement = null;
    
    private DataOutputStream out=null;
    private DataInputStream in=null;
    
    private boolean stopLoop = false;
    
    private boolean isDBconnected = false;
    private boolean isServerStarted = false;
    
    private javax.swing.JLabel dbConnectionStatus;
    
    Server(javax.swing.JLabel dbConnectionStatus){
        this.dbConnectionStatus = dbConnectionStatus;
    }
    
    public void connectToDB(String dbAddressTextField, String dbUsernameTextField, String dbPasswordTextField) {                                            
        //Connect to DB
        //
        if(connect != null || statement != null){
            dbConnectionStatus.setText("Status: Please disconnect first");
        }else
            // Create connection
            //
            if(connect == null){
                try {
                        Class.forName("com.mysql.jdbc.Driver");
                } catch (ClassNotFoundException e2) {
                        // TODO Auto-generated catch block
                        e2.printStackTrace();
                }
                try {
                        connect = DriverManager.getConnection("jdbc:mysql://"+dbAddressTextField,
                                dbUsernameTextField,
                                dbPasswordTextField);
                        dbConnectionStatus.setText("Status: Connected to DB");
                } catch (SQLException e1) {
                        dbConnectionStatus.setText("Status: Failed to connect");
                }
            }
        
        // Create statement
        //
        if(statement == null && connect != null){
            try {
                    statement = connect.createStatement();
                    isDBconnected = true;
            } catch (SQLException e1) {
                   disconnectFromDB();
                   dbConnectionStatus.setText("Status: Couldn't create statement. DB Connection is Closed");
                   
            } 
        }
    }  
    
    public void disconnectFromDB() { 
        if(!isServerStarted){
            try {
                if (statement != null) {
                        statement.close();
                        statement = null;
                }if (connect != null) {
                        connect.close();
                        connect = null;
                }
                dbConnectionStatus.setText("Status: Disconnected from DB");
                isDBconnected = false;
            } catch (Exception e) {
                dbConnectionStatus.setText("Status: Couldn't close DB connection");
            }
        }else{
            dbConnectionStatus.setText("Status: Stop Server first");
        }
    }
    
    public void truncateTable(String truncateTableTextField) { 
        if(!isServerStarted){
            if(statement != null){
                try {
                        statement.executeUpdate("TRUNCATE "+truncateTableTextField);
                        dbConnectionStatus.setText("Status: "+truncateTableTextField+" truncated");
                } catch (SQLException e1) {
                        dbConnectionStatus.setText("Status: Couldn't TRUNCATE table");
                }
            }else{
                 dbConnectionStatus.setText("Status: Connect to DB first");
            }
        }else{
            dbConnectionStatus.setText("Status: Stop Server first");
        }
    }
    
    public void close(){
        if(isServerStarted){
            stopLoop = true;
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    dbConnectionStatus.setText("Status: Couldn't close socket");
                }
            }if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                    dbConnectionStatus.setText("Status: Couldn't close serverSocket");
                }
            }
            dbConnectionStatus.setText("Status: Server Stopped");
            isServerStarted = false;
        }else{
            dbConnectionStatus.setText("Status: Start Server first");
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        if(!isServerStarted && isDBconnected){ 
            isServerStarted = true;
            stopLoop = false;

            serverSocket = new ServerSocket(8001);

            dbConnectionStatus.setText("Status: Socket created");

            try {
                socket = serverSocket.accept();
                dbConnectionStatus.setText("Status: Connection accepted");
            } catch (IOException e) {
                close();
                dbConnectionStatus.setText("Status: No connection Accepted");
                return null;
            }

            try {
                in = new DataInputStream(socket.getInputStream());
                dbConnectionStatus.setText("Status: Input data stream connected");
                out = new DataOutputStream(socket.getOutputStream());
                dbConnectionStatus.setText("Status: Output data stream connected");
            } catch (IOException e) {
                close();
                dbConnectionStatus.setText("Status: Socket Connection Error");
                return null;
            }

            try {
                    String message;
                    if((message = in.readUTF()) != null){
                        if(message.compareToIgnoreCase("true")==0){
                                dbConnectionStatus.setText("Status: Access to Accelerometer Granted");
                                while((message = in.readUTF()) != null){
                                    try {
                                            statement.executeUpdate(message);
                                    } catch (SQLException e1) {
                                            disconnectFromDB();
                                            dbConnectionStatus.setText("Status: DB error ocuured. DB connection is Closed");
                                            return null;
                                    }
                                }
                        }else{
                                dbConnectionStatus.setText("Status: Access to Accelerometer Not Granted");
                        }
                    }
            } catch (IOException e) {
                close();
                dbConnectionStatus.setText("Status: Connection is Lost. Server Stopped");
                return null;
            }
        }else{
            dbConnectionStatus.setText("Status: Connect to DB first");
        }
        return null;
    }
}
