package com.bruce.ui;

import java.util.ArrayList;

import com.bruce.crypt.DESCrypt;
import com.bruce.listener.GenerateKeyListener;
import com.bruce.service.LocalService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends Activity {
	public static TextView infoText;
	EditText et_content, et_number;
	/** 发送与接收的广播 **/
	String SENT_SMS_ACTION = "SENT_SMS_ACTION";
	String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";

	// 数据库相关
	private final String dbName = "cryptedmessage";
	private final String tableName = "all_cryptmessage";
	private final String table_key = "privatekey";
	private SQLiteDatabase db = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		infoText = (TextView) this.findViewById(R.id.infoText);

		Button startbutton = (Button) this.findViewById(R.id.startButton);
		startbutton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				Intent intent = new Intent(EditActivity.this,
						LocalService.class);
				startService(intent);

			}
		});

		Button stopbutton = (Button) this.findViewById(R.id.stopButton);
		stopbutton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopService(new Intent(EditActivity.this, LocalService.class));
			}

		});
		// 获取返回按钮
		Button backBtn = (Button) this.findViewById(R.id.editToUI);
		backBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(EditActivity.this,
						SMSUIActivity.class);
				startActivity(intent);
				finish();
			}

		});
		// 获取收件人号码
		et_number = (EditText) this.findViewById(R.id.number);

		// 获取协商密钥按钮
		Button cor_keyBtn = (Button) this.findViewById(R.id.keyBtn);
		cor_keyBtn.setOnClickListener(new GenerateKeyListener(infoText,
				et_number, this));
		// 获取短信内容
		et_content = (EditText) this.findViewById(R.id.message);

		
	
		// 获取发送按钮
		Button btn=(Button)this.findViewById(R.id.uniqueBtn);

		btn.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				// TODO Auto-generated method stub
				infoText.append("\n你点击了发送按钮!!");
				String number = et_number.getText().toString();
				String text = et_content.getText().toString();
				if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(text)) {
					infoText.append("\nnumber:"+number+"\n"+"content:"+text);
					sendSMS(number, text);
				}
			}
			
		});
      /*  btn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				
				// TODO Auto-generated method stub
				/** 拿到输入的手机号码 **/
	/*			String number = et_number.getText().toString();
				/** 拿到输入的短信内容 **/
/*				String text = et_content.getText().toString();

				/** 手机号码 与输入内容 必需不为空 **/
/*				if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(text)) {
					sendSMS(number, text);
				}
			}

		});*/

		// 注册广播 发送消息
		registerReceiver(sendMessage, new IntentFilter(SENT_SMS_ACTION));
		registerReceiver(receiver, new IntentFilter(DELIVERED_SMS_ACTION));
		// 创建数据库以及短信数据表
		createBD();

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

	/*	new AlertDialog.Builder(EditActivity.this).setTitle("Message")
				.setMessage(tables)
				.setNegativeButton("确定", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				}).show();*/
		db.close();
	}

	private BroadcastReceiver sendMessage = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 判断短信是否发送成功
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				Toast.makeText(context, "短信发送成功", Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(context, "发送失败", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 表示对方成功收到短信
			Toast.makeText(context, "对方接收成功", Toast.LENGTH_LONG).show();
		}
	};

	// -----------------发送短信
	private void sendSMS(String phoneNumber, String message) {
		// ---sends an SMS message to another device---
		try {

			infoText.append("\nSTEP HERE!!");
			infoText.append("\nphoneNumber:"+phoneNumber+"\nmessage:"+message);
			SmsManager sms = SmsManager.getDefault();

			// create the sentIntent parameter
			Intent sentIntent = new Intent(SENT_SMS_ACTION);
			PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
					sentIntent, 0);

			// create the deilverIntent parameter
			Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
			PendingIntent deliverPI = PendingIntent.getBroadcast(this, 0,
					deliverIntent, 0);

			String key = "";// SmsObserver.secretKey;

			// 从数据库中获取加密密钥

			infoText.append("\nSTEP BEFORE!!");
			if(phoneNumber.length()<5){
				phoneNumber="1555521"+phoneNumber;
			}else{
				if(phoneNumber.startsWith("+86")){
					
				}else{
					if(phoneNumber.startsWith("86")){
						phoneNumber="+"+phoneNumber;
					}else{
						phoneNumber="+86"+phoneNumber;
					}
				}
			}
			
			db = this.openOrCreateDatabase(dbName, MODE_PRIVATE, null);
			Cursor cursor = db.query(table_key, null, "phonenumber=?",
					new String[] { phoneNumber }, null, null, null);
			if (cursor.getCount() != 0) {
				if (cursor.moveToNext()) {
					key = cursor.getString(cursor.getColumnIndex("key"))
							.toString();
					infoText.append("\n从密钥数据库中获取密钥为:"+key+"\n");
				}

			}

			infoText.append("\nSTEP AFTER!!");
			cursor.close();
			db.close();
//---------------------------------------------------------------------------
			// 插入数据库
			infoText.append("\nInsert Before!!");
			try{
				SQLiteDatabase db = this.openOrCreateDatabase(dbName,
						MODE_PRIVATE, null);
				
			
				String dateTime=String.valueOf(System.currentTimeMillis());
				String sql = "insert into " + tableName
						+ " (phonenumber,content,date,type) values (\""
						+ phoneNumber + "\",\"" + message + "\",\""
						+  dateTime+ "\",\"" + "2" + "\")";
				db.execSQL(sql);
				infoText.append("\n插入消息成功!!\n");
				
				{//查询出来进行比较
					String sql2="select * from "+tableName+" where date=\""+dateTime+"\"";
					cursor=db.rawQuery(sql2, null);
					if(cursor.getCount()!=0){
					
						cursor.moveToNext();
						String num=cursor.getString(cursor.getColumnIndex("phonenumber"));
					
						String con=cursor.getString(cursor.getColumnIndex("content"));
						EditActivity.infoText.append("查询结果:\nnumber:"+num+"\ncontent:"+con);
					}
					cursor.close();
					
				}
				
			}catch(Exception exc){
				exc.printStackTrace();
				//Log.i("SMSError", exc.printStackTrace());
				infoText.append("\nSomething Wrong!!");
			}
			infoText.append("\nInsert After!!");
			db.close();
//---------------------------------------------------------------------------
			
			byte[] cipher = DESCrypt.crypt(message.getBytes(), key);
			String cipherText = "CM&"
					+ new String(Base64.encode(cipher, Base64.DEFAULT));
			infoText.append("\n发给:" + phoneNumber + "\n" + message + "->"
					+ cipherText);
			// 如果短信内容超过70个字符 将这条短信拆成多条短信发送出去
			if (cipherText.length() > 70) {
				ArrayList<String> msgs = sms.divideMessage(cipherText);
				for (String msg : msgs) {
					sms.sendTextMessage(phoneNumber, null, msg, sentPI,
							deliverPI);
				}
			} else {
				sms.sendTextMessage(phoneNumber, null, cipherText, sentPI,
						deliverPI);
			}
		
		} catch (Exception exc) {
			Toast.makeText(this, "error", Toast.LENGTH_LONG);
		}
	}
}
