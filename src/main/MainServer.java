package main;//package usersActions.*;

import config.Config;
import main.ClientThread;
import usersActions.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class MainServer {
    public static Map<String, Chat> chats = new HashMap<>();

    public static void main(String[] args) {
        try {
            //Создаем слушатель
            ServerSocket socketListener = new ServerSocket(Config.PORT);

            while (true) {
                Socket client = null;
                while (client == null) {
                    client = socketListener.accept();
                }
                new ClientThread(client);
            }
        } catch (SocketException e) {
            System.err.println("Socket exception");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("I/O exception");
            e.printStackTrace();
        }
    }


    public synchronized static void createChat(String name) throws Exception{
        if (!chats.containsKey(name)) {
            chats.put(name, new Chat());
            return;
        }
        throw new Exception("Cannot create chat, chat with name already created");
    }

    public synchronized static Chat getChat(String name) throws Exception{
        if (chats.containsKey(name)) {
            return chats.get(name);
        }
        throw new Exception("There is no chat with this name");
    }

    public synchronized static void deleteChat(String name) throws Exception{
        if (chats.containsKey(name)) {
            chats.remove(name);
            return;
        }
        throw new Exception("There is no chat with this name");
    }

}