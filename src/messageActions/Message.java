package messageActions;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

public class Message implements Serializable {

    private String login;
    private String message;
    private ArrayList<String> users;
    private Date time;
    private String chatName;

    //Constructor for clients
    public Message(String login, String message, String name){
        this.login = login;
        this.message = message;
        this.time = java.util.Calendar.getInstance().getTime();
        this.chatName = name;
    }

    //Constructor for DB
    public Message(Date time, String login, String message){
        this.login = login;
        this.message = message;
        this.time = time;
    }

    //Constructor for commands
    public Message(String command, String action){
        this.login = command;
        this.message = action;
    }

    //Constructor for server
    public Message(String login, String message, ArrayList<String> users){
        this.login = login;
        this.message = message;
        this.time = java.util.Calendar.getInstance().getTime();
        this.users = users;
    }

    public void setOnlineUsers(ArrayList<String> users) {
        this.users = users;
    }

    public String getLogin() {
        return this.login;
    }

    public String getMessage() {
        return this.message;
    }

    public ArrayList<String> getUsers() {
        return this.users;
    }

    public String getChatName() { return this.chatName; }

    public String getDate(){
        Time tm = new Time(this.time.getTime());
        return tm.toString();
    }

    public Date getDateD(){
        return time;
    }
}


