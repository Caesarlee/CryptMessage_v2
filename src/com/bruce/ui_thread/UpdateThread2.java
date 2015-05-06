package com.bruce.ui_thread;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.bruce.ui.DetailActivity;
public class UpdateThread2 extends Thread{
	
	private Context ctx;
	
	public UpdateThread2(Context para){
		this.ctx=para;
		
	}
	public void run(){
		String str="update";
		Handler handler=((DetailActivity) ctx).getHandler();
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
