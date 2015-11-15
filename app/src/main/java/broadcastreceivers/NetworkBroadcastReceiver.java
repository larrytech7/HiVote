package broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.iceteck.services.UpdateService;

import sync.HiVoteSyncAdapter;

public class NetworkBroadcastReceiver extends BroadcastReceiver {
	public NetworkBroadcastReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// an Intent broadcast.
		boolean isNetworkDown = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,false);
		
		if(isNetworkDown) {//no network connection available, so stop service to preserve battery
			context.stopService(new Intent(context, UpdateService.class));
			
		}else{
			context.startService(new Intent(context, UpdateService.class));
			HiVoteSyncAdapter.synImmediately(context);
		}
	}
}
