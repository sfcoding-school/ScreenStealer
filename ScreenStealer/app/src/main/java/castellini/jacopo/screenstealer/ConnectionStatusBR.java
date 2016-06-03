package castellini.jacopo.screenstealer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionStatusBR extends BroadcastReceiver {

    Intent i = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null)
            if (info.isConnected()) {
                if (i == null)
                    i = new Intent(context, ScreenStealerService.class);
                context.startService(i);
            } else {
                if (i != null)
                    context.stopService(i);
            }
        else {
            if (i != null)
                context.stopService(i);
        }
    }
}