package com.bruce.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.bruce.content.SmsContent;
import com.bruce.interfaces.AllFinalInfo;
import com.bruce.info.*;
import com.bruce.service.LocalService;
import com.bruce.ui.DetailActivity.MyThread;
import com.bruce.ui_thread.UpdateThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SMSUIActivity extends Activity {
	private ListView listView=null;
	private List<SmsInfo> infos = null;

	// 数据库相关
	private final String dbName = "cryptedmessage";
	private final String tableName = "all_cryptmessage";
	private final String table_key = "privatekey";
	private SQLiteDatabase db = null;
	private SmsListAdapter adapter;

	//
	private Handler handler;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_v2);

		Uri uri = Uri.parse(AllFinalInfo.SMS_URI_ALL);
		db = this.openOrCreateDatabase(dbName, MODE_PRIVATE, null);
		
		SmsContent content = new SmsContent(this, uri, db);
		// infos = content.getSmsInfo(null);
		// 创建短信以及密钥数据库
		createBD();
		infos = content.getLatestSmsInfo(null);
		

		listView = (ListView) this.findViewById(R.id.listView_sms2);

		// ArrayAdapter<CharSequence>
		// adapter=ArrayAdapter.createFromResource(this, R.array.ctype,
		// android.R.layout.simple_list_item_checked);
		// listView.setAdapter(adapter);
		
		if (infos == null) {
		/*	ArrayAdapter<CharSequence> adapter = ArrayAdapter
					.createFromResource(this, R.array.ctype,
							android.R.layout.simple_list_item_checked);
			listView.setAdapter(adapter);*/

			new AlertDialog.Builder(SMSUIActivity.this).setTitle("Message")
			.setMessage("第一次使用短信列表里不会有任何信息。。。")
			.setNegativeButton("确定", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

				}
			}).show();

		} else {
			adapter=new SmsListAdapter(this);
			listView.setAdapter(adapter);

			listView.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View arg1,
						int pos, long id) {
					// TODO Auto-generated method stub

					parent.getItemAtPosition(pos);
					RelativeLayout rLayout = (RelativeLayout) parent
							.getChildAt(pos);
					TextView contacts = (TextView) arg1
							.findViewById(R.id.tv_contacts);
					// TextView contacts=(TextView)
					// rLayout.findViewById(R.id.tv_contacts);
					// TextView
					// contacts=(TextView)parent.findViewById(R.id.tv_contacts);
					String p_Number = contacts.getText().toString();
					Intent intent = new Intent(SMSUIActivity.this,
							DetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putCharSequence("phoneNumber", p_Number);
					intent.putExtras(bundle);
					SMSUIActivity.this.startActivity(intent);
					// startActivityForResult(intent,CODE);
				}

			});
		}
		// 获取写短信按钮
		Button writeM_Btn = (Button) this.findViewById(R.id.sendSMS);
		writeM_Btn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SMSUIActivity.this,
						EditActivity.class);
				startActivity(intent);
			}

		});

		Log.v("OOXX", "HERE2");
        
		
		// 启动监听短信服务
		Intent intent = new Intent(SMSUIActivity.this, LocalService.class);
		startService(intent);
		
		//handler
		handler=new Handler(){
			public void handleMessage(Message msg){
				//更新UI中的ListView 
				if(msg.obj.equals("update")){
					Uri uri = Uri.parse(AllFinalInfo.SMS_URI_ALL);
					SQLiteDatabase db =SMSUIActivity.this.openOrCreateDatabase(dbName,
							MODE_PRIVATE, null); 
					SmsContent content = new SmsContent(SMSUIActivity.this, uri,db);
		            List<SmsInfo> info_get=content.getLatestSmsInfo(null);
					if(infos!=info_get){
						infos=info_get;
						if(adapter!=null&&listView!=null){
							adapter.notifyDataSetChanged();
							listView.postInvalidate();
						}
						
					}
				}
		
				
			}
		};
		//启动线程更新listView
		new Thread(new UpdateThread(this)).start();
	}

	public Handler getHandler(){
		return this.handler;
	}
	private void createBD() {
		// TODO Auto-generated method stub
		// 创建数据库
		try {

			db = this.openOrCreateDatabase(dbName, MODE_PRIVATE, null);
			Toast.makeText(getApplicationContext(), "创建数据库成功!!",
					Toast.LENGTH_SHORT);
		} catch (Exception exc) {
			Toast.makeText(getApplicationContext(), "创建数据库失败!!",
					Toast.LENGTH_SHORT);
		}
		// 创建数据表
		if (db != null) {
			createTable();
		} else {
			Toast.makeText(getApplicationContext(), "没有数据库!!",
					Toast.LENGTH_SHORT).show();
		}

	}

	private void createTable() {
		// TODO Auto-generated method stub
		// SQL语句
		String sql = "CREATE TABLE IF NOT EXISTS "
				+ tableName
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,phonenumber VARCHAR(20),person VARCHAR(20),content VARCHAR(200),date VARCHAR(20),type CHAR(1));";

		// String
		// sql="CREATE TABLE IF NOT EXISTS "+"sample_1"+"(id INTEGER PRIMARY KEY AUTOINCREMENT,uname VARCHAR(50));";
		db.execSQL(sql);
		sql = "CREATE TABLE IF NOT EXISTS "
				+ table_key
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,phonenumber VARCHAR(20),key VARCHAR(200),date VARCHAR(20));";
		db.execSQL(sql);

		Toast.makeText(getApplicationContext(), "创建数据表成功!!", Toast.LENGTH_SHORT)
				.show();
		// InsertData();

		// 查询是否有这张表
		Cursor cursor = db.query("sqlite_master", new String[] { "name" },
				"type=?", new String[] { "table" }, null, null, null, null);
		String tables = "";
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				tables = tables + cursor.getString(0) + "\n";
				cursor.moveToNext();
			}
		}

		db.close();
		/*new AlertDialog.Builder(SMSUIActivity.this).setTitle("Message")
				.setMessage(tables)
				.setNegativeButton("确定", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				}).show();*/
	}

	class SmsListAdapter extends BaseAdapter {

		private LayoutInflater layoutinflater;
		private View myView;

		public SmsListAdapter(Context c) {
			layoutinflater = LayoutInflater.from(c);
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return infos.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView tv_contacts, tv_time, tv_content;

			if (convertView == null) {
				myView = layoutinflater.inflate(R.layout.smsitem, null);
				// 显示联系人
				tv_contacts = (TextView) myView.findViewById(R.id.tv_contacts);
				tv_time = (TextView) myView.findViewById(R.id.tv_time);
				tv_content = (TextView) myView.findViewById(R.id.tv_content);
			} else {
				myView = (View) convertView;
				tv_contacts = (TextView) myView.findViewById(R.id.tv_contacts);
				tv_time = (TextView) myView.findViewById(R.id.tv_time);
				tv_content = (TextView) myView.findViewById(R.id.tv_content);
			}

			tv_contacts.setText(infos.get(position).getPhoneNumber());
			{
				//将时间转换格式
				String time=infos.get(position).getDate();
				long timeL=Long.valueOf(time);
				SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
				Date date=new Date(timeL);
				String re_time=formatter.format(date);
				tv_time.setText(re_time);
			}
			tv_content.setText(infos.get(position).getSmsBody());

			return myView;

		}

	}
	@Override
	protected void onRestart(){
		super.onResume();
		 new MyThread().run();
		
	}
	@Override
	protected void onStart(){
	  super.onStart();
	 // new SubThread().run();
	}
	
	class MyThread implements Runnable{
		public void run(){
			Uri uri = Uri.parse(AllFinalInfo.SMS_URI_ALL);
			SQLiteDatabase db =SMSUIActivity.this.openOrCreateDatabase(dbName,
					MODE_PRIVATE, null); 
			SmsContent content = new SmsContent(SMSUIActivity.this, uri,db);
            List<SmsInfo> info_get=content.getLatestSmsInfo(null);
			if(infos!=info_get){
				infos=info_get;
				adapter.notifyDataSetChanged();
				listView.postInvalidate();
			}
			
			
		}
	}
	

 
		
	
	
	
}