package data;

import config.Config;
import messageActions.Message;
import usersActions.ChatHistory;

import java.sql.*;
import java.util.ArrayList;

//Singleton
public class DataMessages{
    private static Connection conn;
    private static Statement statmt;
    private static ResultSet resSet;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static DataMessages instance;

    private DataMessages(){

    }
    public static DataMessages getInstance(){
        if (instance == null){
            instance = new DataMessages();
        }
        return instance;
    }

    public static void Connect() throws ClassNotFoundException, SQLException {
        conn = null;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:TEST1.s3db");
        statmt = conn.createStatement();
        System.out.println("Connecting to MessagesDB");
    }

    public static void CreateChat(String name) throws ClassNotFoundException, SQLException{
        /*PreparedStatement statement = conn.prepareStatement("CREATE TABLE if not exists" + name + "('id' INTEGER PRIMARY KEY AUTOINCREMENT,'data' DATE(), 'name' text, 'message' text)");
        statement.setString(ONE, name);
        statement.execute();*/
        statmt.execute("CREATE TABLE if not exists " +  name + " ('id' INTEGER PRIMARY KEY AUTOINCREMENT,'data' DATE , 'name' text, 'message' text);");
        System.out.println("created " + name + " in DataMessages");
    }

    public static void AddMessageToChat(Message m, String chat) throws SQLException{
        PreparedStatement statement = conn.prepareStatement("INSERT INTO " + chat + " ('data','name','message')" + "VALUES (?,?,?)");
        statement.setObject(ONE, m.getDateD());
        statement.setString(TWO, m.getLogin());
        statement.setString(THREE, m.getMessage());
        statement.execute();
        System.out.println("adding message of" + m.getLogin() + " in to " + chat + " to ChatDB");
    }

    public static ChatHistory ReturnHistory(String chat) throws SQLException{
        ArrayList<Message> b = new ArrayList<>();
        resSet = statmt.executeQuery("SELECT * FROM " + chat);
        ArrayList<Message> messages = new ArrayList();
        while(resSet.next())
        {
            messages.add(new Message(resSet.getDate(ONE), resSet.getString(TWO), resSet.getString(THREE)));
        }
        ChatHistory a = new ChatHistory();
        for (int i = Math.min(Config.HISTORY_LENGTH, messages.size()) ; i > 0 ; i--){
            a.addMessage(messages.get(messages.size() - i));
        }
        return a;
    }
}