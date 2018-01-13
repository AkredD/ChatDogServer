package data;

import java.sql.*;
import java.util.ArrayList;

//Singleton
public class DataChats {
    private static Connection conn;
    private static Statement statmt;
    private static ResultSet resSet;
    private static final int ONE = 1;
    private static DataChats instance;

    private DataChats(){

    }
    public static DataChats getInstance(){
        if (instance == null){
            instance = new DataChats();
        }
        return instance;
    }

    public static void Connect() throws ClassNotFoundException, SQLException
    {
        conn = null;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:TEST1.s3db");
        System.out.println("Connecting to chatDB");
        statmt = conn.createStatement();
        statmt.execute("CREATE TABLE if not exists 'chats' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' text);");
    }


    public static void AddChatToDB(String name) throws SQLException
    {
        PreparedStatement statement = conn.prepareStatement("INSERT INTO 'chats' ('name')" + "VALUES (?)");
        statement.setString(ONE, name);
        System.out.println("adding " + name + " to ChatDB");
    }

    public static ArrayList<String> ReadDB() throws ClassNotFoundException, SQLException
    {
        resSet = statmt.executeQuery("SELECT * FROM chats");
        ArrayList<String> chats = new ArrayList();
        while(resSet.next())
        {
            int id = resSet.getInt("id");
            String  name = resSet.getString("name");
            chats.add(name);
        }
        System.out.println("Tables was written");
        return chats;
    }

    public static void CloseDB() throws ClassNotFoundException, SQLException
    {
        conn.close();
        statmt.close();
        resSet.close();
        System.out.println("ChatDB was closed");
    }

}