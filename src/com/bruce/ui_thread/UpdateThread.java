package com.bruce.ui_thread;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.bruce.ui.SMSUIActivity;
public class UpdateThread extends Thread{
	
	private Context ctx;
	
	public UpdateThread(Context para){
		this.ctx=para;
		
	}
	public void run(){
		String str="update";
		Handler handler=((SMSUIActivity) ctx).getHandler();
		while(true){
			
			Message msg=Message.obtain();
			msg.obj=str;
			//通过hanlder发布消息
			handler.sendMessage(msg);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	
}
