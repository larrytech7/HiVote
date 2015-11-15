package sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class HivoteSyncService extends Service {
	private static HiVoteSyncAdapter mSyncAdapter = null;
	private static final Object syncAdapterLock = new Object();
	
	public HivoteSyncService() {
	}

	@Override
	public void onCreate(){
		synchronized(syncAdapterLock){
			if(mSyncAdapter == null){
				mSyncAdapter = new HiVoteSyncAdapter(getApplicationContext(), true);
			}
		}
	
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mSyncAdapter.getSyncAdapterBinder();
	}

}
