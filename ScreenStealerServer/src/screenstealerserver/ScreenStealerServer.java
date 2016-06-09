package screenstealerserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ScreenStealerServer {

    private static final int PORT = 7654;
    private static final String USER = "postgres";
    private static final String PASS = "postgres";

    public static void main(String[] args) {
        ServerSocket ss = null;
        Connection conn = null;
        Statement stat = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", USER, PASS);
            stat = conn.createStatement();
            System.out.println("Connection with DB established");
            stat.execute("CREATE TABLE IF NOT EXISTS screenstealer(serial VARCHAR, timestamp VARCHAR(21), hash VARCHAR(40) NOT NULL, PRIMARY KEY(serial, timestamp))");
            ss = new ServerSocket(PORT);
            System.out.println("Server running...");
            while (true) {
                Socket sock = ss.accept();
                new ClientThread(sock, stat).start();
            }
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (stat != null) {
                    stat.close();
                }
                if (ss != null) {
                    ss.close();
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
