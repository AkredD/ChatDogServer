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
            MainServer.getUserList().deleteUser(login);
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
            boolean flag1 = true;
            while(flag1) {
                try {
                    //System.out.print(inputStream.readObject().toString());
                    this.c = (Message) inputStream.readObject();
                    flag1 = false;
                } catch (ClassNotFoundException e) {
                    System.out.print("error");
                    flag1 = true;
                }
            }

            //Читаем логин отправителя
            this.login = this.c.getLogin();

            if (!this.c.getMessage().equals("User join to the chat(Auto-message)")) { //Если это не регистрационное сообщение
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
            //this.broadcast(MainServer.getUserList().getClientsList(), this.c);

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
                        //Удаляем клиента из списка доступных и информируем всех
                        MainServer.getUserList().deleteUser(login);
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
                if (this.c instanceof Ping) {
                    this.inPacks++;
                    System.out.println(this.inPacks + " in");
                } else if (!c.getMessage().equals("User join to the chat(Auto-message)")) {
                    System.out.println("[" + login + "]: " + c.getMessage());
                    MainServer.getChatHistory().addMessage(this.c);
                }

                this.c.setOnlineUsers(MainServer.getUserList().getUsers());

                if (!(c instanceof Ping) && !c.getMessage().equals("User join to the chat(Auto-message)")) {
                    System.out.println("Send broadcast Message:"  + c.getMessage());
                    this.broadcast(MainServer.getUserList().getClientsList(), this.c);
                }
            }

        } catch (SocketException e) {
            System.out.println(login + " disconnected!");
            this.broadcast(MainServer.getUserList().getClientsList(), new Message("Server-Bot", "The user " + login + " has been disconnect", MainServer.getUserList().getUsers()));
            this.timer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}