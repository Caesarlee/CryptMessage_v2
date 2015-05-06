package com.bruce.service;

import com.bruce.observer.SmsObserver;
import com.bruce.ui.SMSUIActivity;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class LocalService extends Service{

	private String TAG="LOCALSERVICE";
	private NotificationManager NM;
	private final IBinder mBinder=new LocalBinder();
	public class LocalBinder extends Binder{
		LocalService getService(){
			return LocalService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG, "this is onbind");
		return mBinder;
	}
	
	@Override
	public void onCreate(){
	
		NM=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		Log.i(TAG, "this is oncreate");
		showNotification();
		registerObserver();
	}
	
	private void registerObserver() {
		// TODO Auto-generated method stub
		ContentResolver cr = LocalService.this.getContentResolver();
		SQLiteDatabase db=this.openOrCreateDatabase("cryptedmessage", MODE_PRIVATE, null);
		TextView tv=null;
		cr.registerContentObserver(Uri.parse("content://sms/"), true,
				new SmsObserver(db,this, NM, cr, new Handler()));
		
		
	}

	@Override
	public int onStartCommand(Intent intent,int flags,int startId){
		Log.i(TAG, "Received start id "+startId+":"+intent);
		return START_STICKY;
	}
	
	@Override
	public void onDestroy(){
		//NM.cancel("Local Service has stopped!",0);
		NM.cancelAll();
		Log.i(TAG, "this is ondestroy");
		Toast.makeText(this, "Local Service has stopped!", Toast.LENGTH_LONG).show();
		
	}

	@Override
	public boolean onUnbind(Intent intent){
		Log.i(TAG, "this is onUnbind!");
		return super.onUnbind(intent);
	}
	//显示Notification
	private void showNotification() {
		// TODO Auto-generated method stub
/*		
		Notification notification=new Notification();
		String tickerText="测试Notification";
		//显示时间
		long when=System.currentTimeMillis();
		
		notification.icon=R.drawable.ic_launcher;//设置通知的图标
		notification.tickerText=tickerText;//显示在状态栏中的文字
		notification.when=when;//设置来通知的时间
		notification.defaults=Notification.DEFAULT_SOUND;//设置声音
		
		notification.flags=Notification.FLAG_AUTO_CANCEL;//点击清除按钮时就会清除消息通知,但是点击通知栏的通知时不会消失
		
		Intent intent=new Intent(this,SMS2erviceActivity.class);
		PendingIntent pIntent=PendingIntent.getActivity(this,0, intent, 0);
		notification.setLatestEventInfo(this,"标题","内容", pIntent);
		//发出通知
		NM.notify(1, notification);
		
	*/	
		Toast.makeText(this, "Local service has started!", Toast.LENGTH_LONG).show();
		
		
		
	}

	
}
