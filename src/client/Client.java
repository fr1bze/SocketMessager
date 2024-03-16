package client;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

class ClientSomething {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private BufferedReader inputUser;
    private String addr;
    private int port;
    private String nickname;
    private Date date;
    private String dtime;
    private SimpleDateFormat dt1;

    public ClientSomething(String addr, int port) {
        this.addr = addr;
        this.port = port;
        try {
            this.socket = new Socket(addr, port);
        } catch (IOException ignored) {
            System.out.println("Socket failed");
        }
        try {
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.pressNickName();
            new ReadMsg().start();
            new WriteMsg().start();
        } catch (IOException e) {
            this.downService();
        }
    }

    private void downService() {
        try {
            if(!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                inputUser.close();
            }
        } catch (IOException e) {}
    }

    private void pressNickName() {
        System.out.println("Press your nickname, please:");
        try {
            nickname = inputUser.readLine();
            out.write("Hello " + nickname + "\n");
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ReadMsg extends Thread {
        @Override
        public void run() {
            String str;

            try {
                while (true) {
                    str = in.readLine();
                    if (str.equals("stop")) {
                        ClientSomething.this.downService();
                        break;
                    }
                    System.out.println(str);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private class WriteMsg extends Thread {
        @Override
        public void run() {
            while (true) {
                String userWord;
                try {
                    date = new Date();
                    dt1 = new SimpleDateFormat("HH:mm:ss");
                    dtime = dt1.format(date);
                    userWord = inputUser.readLine();
                    if (userWord.equals("stop")) {
                        out.write("stop" + "\n");
                        ClientSomething.this.downService();
                        break;
                    } else {
                        out.write("(" + date + ") " + nickname + ": " + userWord + "\n");
                    }
                    out.flush();
                } catch (IOException e) {
                    ClientSomething.this.downService();
                }
            }
        }
    }
}
public class Client {
    private static String addr = "localhost";
    private static int port = 8080;

    public static void main(String[] args) {
        new ClientSomething(addr, port);
    }

}
