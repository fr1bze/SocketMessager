package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

class ServerSomething extends Thread{
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public ServerSomething(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        Server.story.printStory(out);

        start();
    }
    @Override
    public void run() {
        String word;
        try {
            //nickname
            word = in.readLine();
            try {
                out.write(word + "\n");
                out.flush();
            } catch (IOException ignored) {}
            try {
                while (true) {
                    word = in.readLine();
                    if (word.equals("stop")) {
                        this.downService();
                        break;
                    }
                    System.out.println("Echoing:" + word);
                    Server.story.addStoryElem(word);
                    for (ServerSomething server : Server.serverList) {
                            server.send(word);
                    }
                }
            } catch (NullPointerException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {}
    }

    private void downService() {
        try {
            if(!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                for (ServerSomething sr : Server.serverList) {
                    if (sr.equals(this))
                        sr.interrupt();
                    Server.serverList.remove(sr);
                }
            }
        }catch (IOException ignored) {}
    }
}

class Story {
    private LinkedList<String> story = new LinkedList<>();

    public void addStoryElem(String el) {
        if (story.size() >= 10) {
            story.removeFirst();
            story.add(el);
        }
        else {
            story.add(el);
        }
    }

    public void printStory(BufferedWriter writer) {
        if (story.size() > 0) {
            try {
                writer.write("History message:" + "\n");
                for (String str: story) {
                    writer.write(str + "\n");
                }
                writer.write("/...." + "\n");
                    writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

public class Server {
    public static final int PORT = 8080;
    public static LinkedList<ServerSomething> serverList = new LinkedList<>();
    public static Story story;

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        story = new Story();
        System.out.println("Server started");
        try {
            while (true) {
                Socket socket = server.accept();
                try {
                    serverList.add(new ServerSomething(socket));
                } catch (IOException e) {
                    socket.close();
                }
            }
        } finally {
            server.close();
        }
    }
}
