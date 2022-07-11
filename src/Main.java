//Barabás Balázs, 521/1, Lab2 Szerver oldal

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main{
    public static void main(String[] args) throws IOException {
        int i = 0;
        ServerSocket serverSocket = new ServerSocket(3333);
            while (true) {
                Socket socket = serverSocket.accept(); //give the socket a timeout, so it closes after it doesn't get enough info
                ClientHandler clientSocket = new ClientHandler(socket,i);
                new Thread(clientSocket).start();
                i++;
            }
    }
}
