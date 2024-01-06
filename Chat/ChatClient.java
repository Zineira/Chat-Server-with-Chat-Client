import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.awt.*;
import java.awt.event.*;

import javax.sound.midi.Soundbank;
import javax.swing.*;

public class ChatClient {

    // Variáveis relacionadas com a interface gráfica --- * NÃO MODIFICAR *
    JFrame frame = new JFrame("Chat Client");
    private JTextField chatBox = new JTextField();
    private JTextArea chatArea = new JTextArea();
    // --- Fim das variáveis relacionadas coma interface gráfica

    // Se for necessário adicionar variáveis ao objecto ChatClient, devem
    // ser colocadas aqui

    static private final Charset charset = Charset.forName("UTF8");
    static private final CharsetDecoder decoder = charset.newDecoder();
    static private final ByteBuffer bufferIn = ByteBuffer.allocate(16384);
    static private final ByteBuffer bufferOut = ByteBuffer.allocate(16384);
    static private SocketChannel sc = null;

    // Método a usar para acrescentar uma string à caixa de texto
    // * NÃO MODIFICAR *
    public void printMessage(final String message) {
        chatArea.append(message);
    }

    // Construtor
    public ChatClient(String server, int port) throws IOException {

        // Inicialização da interface gráfica --- * NÃO MODIFICAR *
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chatBox);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.setSize(500, 300);
        frame.setVisible(true);
        chatArea.setEditable(false);
        chatBox.setEditable(true);
        chatBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    newMessage(chatBox.getText());
                } catch (IOException ex) {
                } finally {
                    chatBox.setText("");
                }
            }
        });
        frame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                chatBox.requestFocusInWindow();
            }
        });
        // --- Fim da inicialização da interface gráfica

        // Se for necessário adicionar código de inicialização ao
        // construtor, deve ser colocado aqui
        sc = SocketChannel.open();
        sc.configureBlocking(false);
        sc.connect(new InetSocketAddress(server, port));

    }

    // Método invocado sempre que o utilizador insere uma mensagem
    // na caixa de entrada
    public void newMessage(String message) throws IOException {
        // PREENCHER AQUI com código que envia a mensagem ao servidor
        message = message + "\n";
        bufferOut.clear();
        bufferOut.put(charset.encode(message));
        bufferWrite();
        printMessage(message);
    }

    public static void bufferWrite() throws IOException {

        while (bufferOut.hasRemaining()) {

            sc.write(bufferOut);

        }
    }

    // Método principal do objecto
    public void run() throws IOException {
        bufferIn.clear();
        while (true) {
            System.out.println("1");
            while (!sc.finishConnect()) {
                System.out.println("2");
                while (sc.read(bufferIn) > 0) {
                    System.out.println("3");
                    bufferIn.flip();
                    String response = decoder.decode(bufferIn).toString();
                    bufferIn.clear();
                    printMessage(response + "\n");
                    System.out.println(response);
                }
            }
        }

    }

    // Instancia o ChatClient e arranca-o invocando o seu método run()
    // * NÃO MODIFICAR *
    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
        client.run();
    }

}