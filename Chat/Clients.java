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

    public Clients(SocketChannel sc){
        this.sc = sc;
        state = "init";
    }

    public String getUser(){return user;}

    public void setUser(String user){this.user = user;}

    public SocketChannel getSC(){return sc;}

    public void setSC(SocketChannel sc){this.sc = sc;}

    public String getState(){return state;}

    public void setState(String state){this.state = state;}
}