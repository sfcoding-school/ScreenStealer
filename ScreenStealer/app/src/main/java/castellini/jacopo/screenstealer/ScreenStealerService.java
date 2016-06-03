package castellini.jacopo.screenstealer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ScreenStealerService extends Service {

    private Thread t = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        t = new ClientThread();
        t.start();
        return START_STICKY;
    }

    public void onDestroy() {
        t.interrupt();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}