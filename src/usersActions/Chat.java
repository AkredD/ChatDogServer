package usersActions;


import messageActions.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Chat{
    private static ChatHistory history;
    private static UsersList onlineUsers;

    public Chat(){
        history = new ChatHistory();
        onlineUsers = new UsersList();
    }
    public static ChatHistory getChatHistory(){
        return history;
    }
    public static UsersList getOnlineUsers(){
        return onlineUsers;
    }
    public static void addMessage(Message m){
        history.addMessage(m);
    }
    public static void addUserToChat(String login, Socket socket, ObjectOutputStream oos, ObjectInputStream ois){
        onlineUsers.addUser(login, socket, oos, ois);
    }
    public static void deleteUserFromChat(String login){
        onlineUsers.deleteUser(login);
    }

}