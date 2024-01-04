import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class ChatServer
{
  // A pre-allocated buffer for the received data
  static private final ByteBuffer buffer = ByteBuffer.allocate( 16384 );

  // Decoder for incoming text -- assume UTF-8
  static private final Charset charset = Charset.forName("UTF8");
  static private final CharsetDecoder decoder = charset.newDecoder();
  static private List<Clients> clients = new ArrayList<>();
  static private List<String> nicks = new ArrayList<>();
  static private List<Chat> chats = new ArrayList<>();


  static public void main( String args[] ) throws Exception {
    // Parse port from command line
    int port = Integer.parseInt( args[0] );
    
    try {
      // Instead of creating a ServerSocket, create a ServerSocketChannel
      ServerSocketChannel ssc = ServerSocketChannel.open();

      // Set it to non-blocking, so we can use select
      ssc.configureBlocking( false );

      // Get the Socket connected to this channel, and bind it to the
      // listening port
      ServerSocket ss = ssc.socket();
      InetSocketAddress isa = new InetSocketAddress( port );
      ss.bind( isa );

      // Create a new Selector for selecting
      Selector selector = Selector.open();

      // Register the ServerSocketChannel, so we can listen for incoming
      // connections
      ssc.register( selector, SelectionKey.OP_ACCEPT );
      System.out.println( "Listening on port "+port );
      

      while (true) {
        // See if we've had any activity -- either an incoming connection,
        // or incoming data on an existing connection
        int num = selector.select();
        

        // If we don't have any activity, loop around and wait again
        if (num == 0) {
          continue;
        }

        
        // Get the keys corresponding to the activity that has been
        // detected, and process them one by one
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> it = keys.iterator();
        while (it.hasNext()) {
          // Get a key representing one of bits of I/O activity
          SelectionKey key = it.next();

          // What kind of activity is it?
          if (key.isAcceptable()) {

            // It's an incoming connection.  Register this socket with
            // the Selector so we can listen for input on it
            Socket s = ss.accept();
            System.out.println( "Got connection from "+s );

            // Make sure to make it non-blocking, so we can use a selector
            // on it.
            SocketChannel sc = s.getChannel();
            sc.configureBlocking( false );

            // Register it with the selector, for reading
            sc.register( selector, SelectionKey.OP_READ );
            Clients client = new Clients(s.getChannel());
            clients.add(client);

          } else if (key.isReadable()) {

            SocketChannel sc = null;

            try {

              // It's incoming data on a connection -- process it
              sc = (SocketChannel)key.channel();
              Clients client = null;
              for(int i = 0; i < clients.size(); i++){
                if(clients.get(i).getSC() == sc) {
                  client = clients.get(i);
                  break;
                }
              }

              boolean ok = processInput( client );

              // If the connection is dead, remove it from the selector
              // and close it
              if (!ok) {
                key.cancel();
                clients.remove(client);
                Socket s = null;
                try {
                  s = sc.socket();
                  System.out.println( "Closing connection to "+s );
                  s.close();
                } catch( IOException ie ) {
                  System.err.println( "Error closing socket "+s+": "+ie );
                }
              }

            } catch( IOException ie ) {

              // On exception, remove this channel from the selector
              key.cancel();

              try {
                sc.close();
              } catch( IOException ie2 ) { System.out.println( ie2 ); }

              System.out.println( "Closed "+sc );
            }
          }
        }

        // We remove the selected keys, because we've dealt with them.
        keys.clear();
      }
    } catch( IOException ie ) {
      System.err.println( ie );
    }
  }


  // Just read the message from the socket and send it to stdout
  static private boolean processInput( Clients client ) throws IOException {
    // Read the message to the buffer

    buffer.clear();
    client.getSC().read( buffer );
    buffer.flip();
    
    //bufferWrite(client);

    // If no data, close the connection
    if (buffer.limit()==0) {
      return false;
    }

    
    String message = decoder.decode(buffer).toString();

    String[] splited = message.split(" ");

    

    buffer.clear();
    /*System.out.println(client.getUser());
    System.out.println(client.getState());
    System.out.println(splited[0]);
    System.out.println();*/
    switch (client.getState()) {

        case("init"):

            switch (splited[0]) {
              
              case ("/nick"):
                  
                  if(!nicks.contains(splited [1])) {

                    nicks.add(splited [1]);
                    client.setUser(splited [1]);
                    client.setState("outside");
                    buffer.put(charset.encode("OK\n"));
                    buffer.flip();
                    
                  } else {

                    buffer.put(charset.encode("ERROR\n"));
                    buffer.flip();

                  }
                  
                  bufferWrite(client);

                  break;

              case ("/bye"):
                  
                  buffer.put(charset.encode("BYE\n"));
                  buffer.flip();
                  
                  bufferWrite(client);

                  client.getSC().close();
              
                  break;

              default: 
                  
                  buffer.put(charset.encode("ERROR\n"));
                  buffer.flip();
                  
                  bufferWrite(client);

                  break;
                  
            }
            break;

        case("outside"):

            switch (splited[0]) {

              case ("/nick"):
                  
                  if(!nicks.contains(splited [1])) {

                    nicks.add(splited [1]);
                    nicks.remove(client.getUser());
                    client.setUser(splited [1]);
                    buffer.put(charset.encode("OK\n"));
                    buffer.flip();
                    
                  } else {

                    buffer.put(charset.encode("ERROR\n"));
                    buffer.flip();

                  }

                  bufferWrite(client);

                  break;

              case ("/bye"):
                  
                  buffer.put(charset.encode("BYE\n"));
                  buffer.flip();
                  bufferWrite(client);

                  client.getSC().close();
              
                  break;

              case ("/join"):

                  Chat chat = null;
                  int numChats = chats.size();
                  splited[1] = splited[1].replace("\n", "");
                  for(int i = 0; i < numChats; i++){
                    System.out.println(chats.get(i).getName() + " = " + splited[1]);
                    if(chats.get(i).getName().equals(splited[1])){
                      chat = chats.get(i);
                    }
                  }

                  if(chat == null){
                    //System.out.println("Chat: " + splited[1]);
                    //System.out.println("--------------------------------------------------------");
                    Chat newChat = new Chat(splited[1]);
                    chats.add(newChat);
                    //System.out.println(chats.size());
                    newChat.addUser(client);
                  } else {
                    //System.out.println("entrei");
                    List<Clients> users = new ArrayList<>();
                    users = chat.getUsers();
                    buffer.put(charset.encode("JOINED " + client.getUser()));
                    buffer.flip();
                    int k = 1;
                    for(Clients user : users){
                        System.out.println("User "+k+": "+user.getSC().toString());
                        bufferWrite(user);
                        k++;
                    }
                    chat.addUser(client);
                  }
                  client.setState("inside");
                  buffer.clear();
                  buffer.put(charset.encode("OK\n"));
                  buffer.flip();
                  bufferWrite(client);

                  break;

              default: 
                  
                  buffer.put(charset.encode("ERROR\n"));
                  buffer.flip();
                  bufferWrite(client);

                  break;
                  
            }

            break;

        case("inside"):
            break;
    }
    

    /*for(SocketChannel client : clients){
    if(!(sc.equals(client))){
      buffer.clear();
      buffer.put(charset.encode("Client:" + sc.socket().getRemoteSocketAddress() + " Message:" + message));
      buffer.flip();
      while(buffer.hasRemaining()){
          try{
              client.write(buffer);
          } catch (IOException e){
              client.close();
              clients.remove(client);
          }
      }
      
        
        sc.write( buffer );}
}*/
    buffer.flip();

    return true;

  }

  public static void bufferWrite(Clients client) throws IOException {

    while(buffer.hasRemaining()){
      
      client.getSC().write(buffer);

    }
  }
}