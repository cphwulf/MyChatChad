package chadchat.entries;

import chadchat.domain.User;
import chadchat.domain.UserExists;
import chadchat.domain.UserRepository;
import chadchat.infrastructure.Database;
import chadchat.ui.Protocol;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        //Vi sætter en port og en serversocket, som indeholder vores port.
        final int port = 6666;
        final ServerSocket serverSocket = new ServerSocket(port);

        while(true) {
            Socket socket = serverSocket.accept();
            //Printer ud til vores terminal hvem der ankommer til vores server, med IP og port.
            System.out.println("[CONNECTED]" + socket.getInetAddress() + " port " + socket.getPort());

            //Vi laver en thread, som indeholder en eller flere protocoller.
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Protocol p = new Protocol(
                                new Scanner(socket.getInputStream()), new PrintWriter(socket.getOutputStream()));
                        p.run();
                        socket.close();
                    } catch (IOException | ClassNotFoundException | InterruptedException | UserExists e){
                        try {
                            socket.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            });
            thread.start();

        }
        //new Protocol(new Scanner(System.in), new PrintWriter(System.out)).run();
    }
}
