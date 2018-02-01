package usersActions;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private String login;

    public Client(Socket socket){
        this.socket = socket;
    }

    public Client(String login, Socket socket , ObjectOutputStream oos , ObjectInputStream ois ){
        this.login = login;
        this.socket = socket;
        this.oos = oos;
        this.ois = ois;
    }

    public String getLogin() {return login;}

    public Socket getSocket() {
        return this.socket;
    }

    public ObjectOutputStream getThisObjectOutputStream() {
        return this.oos;
    }

    public ObjectInputStream getThisObjectInputStream() {
        return this.ois;
    }

    public void setThisObjectOutputStream(ObjectOutputStream oos) {
        this.oos = oos;
    }

    public void setThisObjectInputStream(ObjectInputStream ois) {
        this.ois = ois;
    }
}