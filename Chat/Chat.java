import java.util.*;

public class Chat{

    private String name = "";
    private List<Clients> users = new ArrayList<>();

    public Chat(String name) {
        this.name = name;
    }

    public String getName(){return name;}

    public void setName(String name){this.name = name;}

    public void addUser(Clients user){users.add(user);}

    public void removeUser(Clients user){users.remove(user);}

    public boolean verifyUser(Clients user) {
        return users.contains(user);
    }

    public List<Clients> getUsers(){
        return users;
    }
}