package sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class HiVoteAuthenticatorService extends Service {

	private AppAuthenticator mAuthenticator;

	@Override
	public void onCreate(){
		mAuthenticator = new AppAuthenticator(this);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mAuthenticator.getIBinder();
	}

}
