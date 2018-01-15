package main;

import config.Config;
import messageActions.*;
import usersActions.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ClientThread extends Thread {
    private final static int DELAY = 30000;

    private Socket socket;
    private Message c;
    private String login;
    private int inPacks = 0;
    private int outPacks = 0;
    private boolean flag = false;
    private Timer timer;
    private ArrayList<String> chats = new ArrayList<>();

    public ClientThread(Socket socket) {
        this.socket = socket;
        this.start();
    }

    private void broadcast(ArrayList<Client> clientsArrayList, Message message) {
        try {
            for (Client client : clientsArrayList) {
                client.getThisObjectOutputStream().writeObject(message);
            }
        } catch (SocketException e) {
            System.out.println("in broadcast: " + login + " disconnected!");
            try{
                MainServer.getChat(message.getChatName()).getOnlineUsers().deleteUser(login);
            }catch (Exception er){
                System.out.println(er.getMessage());
            }
            //this.broadcast(MainServer.getUserList().getClientsList(), new Message("System", "The user " + login + " has been disconnected", MainServer.getUserList().getUsers()));
            timer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            //Создаем потоки ввода-вывода для работы с сокетом
            final ObjectInputStream inputStream = new ObjectInputStream(this.socket.getInputStream());
            final ObjectOutputStream outputStream = new ObjectOutputStream(this.socket.getOutputStream());
            //Читаем Message из потока
            this.c = (Message) inputStream.readObject();
            //Читаем логин отправителя
            this.login = this.c.getLogin();

            /*if (!this.c.getMessage().equals("User join to the chat(Auto-message)")) { //Если это не регистрационное сообщение
                System.out.println("[" + this.c.getLogin() + "]: " + this.c.getMessage());
                MainServer.getChatHistory().addMessage(this.c); //То добавляем его к истории чата
            } else {
                //outputStream.writeObject(MainServer.getChatHistory()); //Иначе, отправляем новичку историю чата
                for (Message message : MainServer.getChatHistory().getHistory()) {
                    outputStream.writeObject(message);
                }
                this.broadcast(MainServer.getUserList().getClientsList(), new Message("Server-Bot", "The user " + login + " has been connect")); //И сообщаем всем клиентам, что подключился новый пользователь
            }
            //Добавляем к списку пользователей - нового
            MainServer.getUserList().addUser(login, socket, outputStream, inputStream);

            //Для ответа, указываем список доступных пользователей
            this.c.setOnlineUsers(MainServer.getUserList().getUsers());

            //Передаем всем сообщение пользователя
            this.broadcast(MainServer.getUserList().getClientsList(), this.c);
            */
            this.timer = new Timer(DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try { //Если количество входящих пакетов от клиента рано исходящему, значит клиент еще не в ауте
                        if (inPacks == outPacks) {
                            outputStream.writeObject(new Ping());
                            outPacks++;
                            System.out.println(outPacks + " out");
                        } else { //Иначе, в ауте
                            throw new SocketException();
                        }
                    } catch (SocketException ex1) {
                        System.out.println("packages not clash");
                        System.out.println(login + " disconnected!");
                        for (String chat : chats){
                            try {
                                MainServer.getChat(chat).getOnlineUsers().deleteUser(login);
                            }catch (Exception er){
                                System.out.println(er.getMessage());
                            }
                        }
                        //Удаляем клиента из списка доступных и информируем всех
                        //MainServer.getUserList().deleteUser(login);
                        //broadcast(MainServer.getUserList().getClientsList(), new Message("Server-Bot", "The user " + login + " has been disconnect", MainServer.getUserList().getUsers()));
                        flag = true;
                        timer.stop();
                    }  catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                }
            });

            this.timer.start();

            //Начинаем пинговать клиента
            outputStream.writeObject(new Ping());
            this.outPacks++;
            System.out.println(outPacks + " out");

            while (true) {
                if(this.flag) {
                    this.flag = false;
                    break;
                }
                this.c = (Message) inputStream.readObject();
                //Комманды на создание/удаление/присоединение/отсоединения
                // command - name of command
                // action - name of chat
                if (this.c instanceof Command ){
                    String command = this.c.getLogin();
                    String action = this.c.getMessage();
                    System.out.println("waiting for command execution " + command + " " + action );
                    try {
                        if (command.equals("create")) {
                            MainServer.createChat(action);
                            MainServer.getChat(action).addUserToChat(login, socket, outputStream, inputStream);
                            System.out.println("Chat was created successfully");
                            chats.add(action);
                        }
                        if (command.compareTo("delete") == 0) {
                            MainServer.deleteChat(action);
                            System.out.println("Chat was deleted successfully");
                            chats.remove(action);
                        }
                        if (command.compareTo("connect") == 0) {
                            if (!chats.contains(action)) {
                                MainServer.getChat(action).addUserToChat(login, socket, outputStream, inputStream);
                                chats.add(action);
                                System.out.println("The user " + login + " connected to " + action + "  successfully");
                                this.broadcast(MainServer.getChat(action).getOnlineUsers().getClientsList(),
                                        new Message("Server-Bot", "The user " + login + " connected", action));

                            } else {
                                System.out.println("Error(connected): there is no chat with this name");
                            }
                        }
                        if (command.compareTo("disconnect") == 0) {
                            if (!chats.contains(action)) {
                                MainServer.getChat(action).deleteUserFromChat(login);
                                chats.remove(action);
                                System.out.println("The user " + login + " disconnected from    " + action + "  successfully");
                                this.broadcast(MainServer.getChat(action).getOnlineUsers().getClientsList(),
                                        new Message("Server-Bot", "The user " + login + " has been disconnect", action));
                            } else {
                                System.out.println("Error(disconnected): there is no chat with this name");
                            }
                        }
                        continue;
                    }catch (Exception e){
                        System.out.print(e.getMessage());
                    }


                }
                if (this.c instanceof Ping) {
                    this.inPacks++;
                    System.out.println(this.inPacks + " in");
                } else if (!c.getMessage().equals("User join to the chat(Auto-message)") && !(this.c instanceof Command)) {
                    System.out.println("[" + login + "]: " + c.getMessage());
                    try {
                        MainServer.getChat(c.getChatName()).getChatHistory().addMessage(this.c);
                    }catch(Exception e){
                        System.out.println(e.getMessage());
                    }

                }

                //this.c.setOnlineUsers(MainServer.getUserList().getUsers());

                if (!(c instanceof Ping) && !c.getMessage().equals("User join to the chat(Auto-message)") && !(c instanceof Command)) {
                    System.out.println("Send broadcast Message to chat\"" + c.getChatName() + "\" :"  + c.getMessage());
                    try{
                        this.broadcast(MainServer.getChat(c.getChatName()).getOnlineUsers().getClientsList(), this.c);
                    }catch (Exception e){

                    }
                }
            }

        } catch (SocketException e) {
            System.out.println(login + " disconnected!");
            for (String chat : chats){
                try {
                    MainServer.getChat(chat).getOnlineUsers().deleteUser(login);
                    broadcast(MainServer.getChat(chat).getOnlineUsers().getClientsList(),
                            new Message("Server-Bot", "The user " + login + " has been disconnect", chat));
                }catch (Exception er){
                    System.out.println(er.getMessage());
                }
            }

            this.timer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}