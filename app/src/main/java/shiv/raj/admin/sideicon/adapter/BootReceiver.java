package shiv.raj.admin.sideicon.adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import shiv.raj.admin.sideicon.StandOutWindow;
import shiv.raj.admin.sideicon.floatingfolders.FloatingFolder;

/**
 * Created by shivraj on 16/02/2017.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"phone is turned on!",Toast.LENGTH_LONG).show();
        StandOutWindow.closeAll(context, FloatingFolder.class);
        FloatingFolder.showFolders(context);
    }
}
