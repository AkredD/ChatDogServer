package data;

import messageActions.Message;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class db {
    final static String A = "kaefChat";
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        DataMessages.getInstance();
        DataChats.getInstance();
        DataChats.Connect();
        DataMessages.Connect();
        DataChats.AddChatToDB(A);
        DataMessages.CreateChat(A);
        DataMessages.AddMessageToChat(new Message("AkredD", "abrakadabra"), A);
        DataMessages.AddMessageToChat(new Message("AkredD", "abrakadabri"), A);
        DataMessages.AddMessageToChat(new Message("figraa", "abrakadabra"), A);
        List<Message> a = DataMessages.ReturnHistory(A).getHistory();
        for (int i = 0; i < a.size(); ++i){
            Message local = a.get(i);
            System.out.println("[" + local.getDate()+"]"+ local.getLogin() +": "+local.getMessage());
        }
        //testUpdate
    }
}