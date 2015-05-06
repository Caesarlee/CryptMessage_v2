package com.bruce.ui;

import java.util.ArrayList;
import java.util.List;

import com.bruce.content.SmsContent;
import com.bruce.crypt.DESCrypt;
import com.bruce.interfaces.AllFinalInfo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.LayoutParams;

import com.bruce.info.*;
import com.bruce.listener.GenerateKeyListener;
import com.bruce.ui_thread.UpdateThread2;

public class DetailActivity extends Activity {

	private ListView listView = null;
	private List<SmsInfo> infos = null;
	private Intent intent;
	String pNumber, message;
	SmsContent content;
	SQLiteDatabase db;
	EditText editText;
	DetailSmsAdapter adapter;
	// 数据库相关
	private final String dbName = "cryptedmessage";
	private final String tableName = "all_cryptmessage";
	private final String table_key = "privatekey";
	/** 发送与接收的广播 **/
	String SENT_SMS_ACTION = "SENT_SMS_ACTION";
	String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
	// Handler
	private Handler handler;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.details);
		// 获取主Activity传过来的号码
		intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		pNumber = bundle.getString("phoneNumber");

		// 获取返回按钮
		Button btn = (Button) this.findViewById(R.id.backToMain);
		btn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				// setResult(0x717,intent);
				Intent intent = new Intent(DetailActivity.this,
						SMSUIActivity.class);
				startActivity(intent);
				finish();
			}

		});
		// 获取协商密钥按钮
		// 获取协商密钥按钮
		Button gen_KeyBtn = (Button) this.findViewById(R.id.gen_key);
		EditText et = new EditText(this);
		et.setText(pNumber);

		gen_KeyBtn.setOnClickListener(new GenerateKeyListener(null, et,
				DetailActivity.this));
		// 设置标题
		TextView tv_title = (TextView) this.findViewById(R.id.title);
		tv_title.setText(pNumber);

		Uri uri = Uri.parse(AllFinalInfo.SMS_URI_ALL);
		SQLiteDatabase db = this.openOrCreateDatabase(dbName, MODE_PRIVATE,
				null);
		content = new SmsContent(this, uri, db);

		infos = content.getLatestSmsInfo(pNumber);
		// infos = content.getSmsInfo(pNumber);
		listView = (ListView) this.findViewById(R.id.bothMessage);

		adapter = new DetailSmsAdapter();
		listView.setAdapter(adapter);

		// 获取编辑的短信
		editText = (EditText) this.findViewById(R.id.editMessage);

		// 获取发送按钮
		Button sendButton = (Button) this.findViewById(R.id.send);

		sendButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(DetailActivity.this, "你点击了发送按钮!!",
						Toast.LENGTH_LONG).show();
				/** 拿到输入的手机号码 **/
				String number = pNumber;
				/** 拿到输入的短信内容 **/

				String text = editText.getText().toString();
				;

				/** 手机号码 与输入内容 必需不为空 **/
				if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(text)) {

					Toast.makeText(DetailActivity.this, "进入if!!",
							Toast.LENGTH_LONG).show();
					sendSMS(number, text);
				}
			}

		});

		// handler
		handler = new Handler() {
			public void handleMessage(Message msg) {
				// 更新UI中的ListView
				if (msg.obj.equals("update")) {
					Uri uri = Uri.parse(AllFinalInfo.SMS_URI_ALL);
					SQLiteDatabase db = DetailActivity.this
							.openOrCreateDatabase(dbName, MODE_PRIVATE, null);
					SmsContent content = new SmsContent(DetailActivity.this,
							uri, db);
					List<SmsInfo> info_get = content.getLatestSmsInfo(pNumber);
					if (infos != info_get) {
						infos = info_get;
						adapter.notifyDataSetChanged();
						listView.postInvalidate();
					}
				}

			}
		};
		// 启动线程更新listView
		new Thread(new UpdateThread2(this)).start();
		// 启动线程更新listView
		// new UpdateView().run();
		// 注册广播 发送消息
		registerReceiver(sendMessage, new IntentFilter(SENT_SMS_ACTION));
		registerReceiver(receiver, new IntentFilter(DELIVERED_SMS_ACTION));
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

	public Handler getHandler(){
		return this.handler;
	}
	class DetailSmsAdapter extends BaseAdapter {

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
			RelativeLayout rLayout = new RelativeLayout(DetailActivity.this);

			TextView tv;
			tv = new TextView(DetailActivity.this);

			tv.setTextColor(Color.BLACK);
			tv.setText(infos.get(position).getSmsBody());
			// tv.setBackgroundColor(Color.LTGRAY);
			LayoutParams params = new LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			tv.setLayoutParams(params);

			tv.setMaxWidth(400);
			if ("1".equals(infos.get(position).getType())) {
				tv.setGravity(Gravity.CENTER_HORIZONTAL);
				tv.setBackgroundResource(R.drawable.reveive);
				// tv.setPadding(10, 10, 8, 8);
				rLayout.setGravity(Gravity.LEFT | Gravity.TOP);
				rLayout.addView(tv);
				// tv.setLayoutParams(params);
			} else {
				tv.setGravity(Gravity.CENTER);
				tv.setBackgroundResource(R.drawable.send);
				// tv.setPadding(10, 10, 8, 8);
				rLayout.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
				rLayout.addView(tv);
			}

			return rLayout;
		}

	}

	// -----------------发送短信
	private void sendSMS(String phoneNumber, String message) {
		// ---sends an SMS message to another device---
		try {

			SmsManager sms = SmsManager.getDefault();

			Toast.makeText(DetailActivity.this.getApplicationContext(),
					"发送短信...", Toast.LENGTH_LONG);
			// create the sentIntent parameter
			Intent sentIntent = new Intent(SENT_SMS_ACTION);
			PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
					sentIntent, 0);

			// create the deilverIntent parameter
			Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
			PendingIntent deliverPI = PendingIntent.getBroadcast(this, 0,
					deliverIntent, 0);
			// String key=SmsObserver.secretKey.substring(0,12 );
			String key = null;// SmsObserver.secretKey;
			// 从数据库中获取密钥
			{
				SQLiteDatabase db = this.openOrCreateDatabase(dbName,
						MODE_PRIVATE, null);
				Cursor cursor = db.query(table_key, null, "phonenumber=?",
						new String[] { phoneNumber }, null, null, null);
				if (cursor.getCount() != 0) {
					if (cursor.moveToNext()) {
						key = cursor.getString(cursor.getColumnIndex("key"))
								.toString();
					}

				}

				cursor.close();
				db.close();
			}
			byte[] cipher = DESCrypt.crypt(message.getBytes(), key);
			String cipherText = "CM&"
					+ new String(Base64.encode(cipher, Base64.DEFAULT));
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
			// 插入数据库
			{
				SQLiteDatabase db = this.openOrCreateDatabase(dbName,
						MODE_PRIVATE, null);
				String sql = "insert into " + tableName
						+ "(phonenumber,person,content,date,type) values (\""
						+ phoneNumber + "\",\"" + "none" + "\",\"" + message
						+ "\",\"" + System.currentTimeMillis() + "\",\"" + "2"
						+ "\")";
				db.execSQL(sql);
				db.close();
			}
			new MyThread().run();
			// 更新ListView
			// infos = content.getSmsInfo(pNumber);
			// listView.invalidate();
			// infoText.append("\n发给:"+phoneNumber+"\n"+message+"->"+cipherText);
		} catch (Exception exc) {
			Toast.makeText(this, "error", Toast.LENGTH_LONG);
		}
	}

	// 更新listView

	class MyThread implements Runnable {
		public void run() {
			Uri uri = Uri.parse(AllFinalInfo.SMS_URI_ALL);
			SQLiteDatabase db = DetailActivity.this.openOrCreateDatabase(
					dbName, MODE_PRIVATE, null);
			SmsContent content = new SmsContent(DetailActivity.this, uri, db);
			List<SmsInfo> info_get = content.getLatestSmsInfo(pNumber);
			if (infos != info_get) {
				infos = info_get;
				adapter.notifyDataSetChanged();
				listView.postInvalidate();
			}

			/*
			 * infos = content.getLatestSmsInfo(pNumber);
			 * adapter.notifyDataSetChanged(); listView.postInvalidate();
			 */
		}
	}



}
