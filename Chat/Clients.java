import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class Clients {

    private String user;
    private SocketChannel sc;
    private String state;
    private String chat;

    public Clients(SocketChannel sc) {
        this.sc = sc;
        state = "init";
        chat = "";
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setSC(SocketChannel sc) {
        this.sc = sc;
    }

    public SocketChannel getSC() {
        return sc;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public String getChat() {
        return chat;
    }
}