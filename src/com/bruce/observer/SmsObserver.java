package com.bruce.observer;

//import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.util.ArrayList;

import com.bruce.crypt.DESCrypt;
import com.bruce.diffhell.SelfDefineDH;
import com.bruce.listener.GenerateKeyListener;
import com.bruce.ui.DetailActivity;
import com.bruce.ui.EditActivity;
import com.bruce.ui.SMSUIActivity;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;
import android.net.Uri;
import android.os.Handler;
import android.telephony.SmsManager;
import android.widget.TextView;
import android.widget.Toast;

public class SmsObserver extends ContentObserver {

	
	private ContentResolver mResolver;

	public static String key = null;
	public static String secretKey;
	private SQLiteDatabase db = null;
	private Service sv;

	private final String tableName = "all_cryptmessage";
	private final String table_key = "privatekey";

	public SmsObserver(SQLiteDatabase db,  Service service,
			NotificationManager notify, ContentResolver mResolver,
			Handler handler) {
		super(handler);
		this.mResolver = mResolver;
		this.db = db;
	
		this.sv=service;
		// TODO Auto-generated constructor stub
	}

	public void onChange(boolean selfChange) {

		// 获取接受到的短信
		String[] projection = new String[] { "_id", "thread_id", "address",
				"person", "date", "read", "status", "type", "body", };
		
		Cursor cursor = null;

		cursor = mResolver.query(Uri.parse("content://sms/inbox"), projection,
				null, null, "date desc");

		int idColumn = cursor.getColumnIndex("_id");
		

		int phoneNumberColumn = cursor.getColumnIndex("address");
		int nameColumn = cursor.getColumnIndex("person");
		int dateColumn = cursor.getColumnIndex("date");
		int readColumn = cursor.getColumnIndex("read");
		
		int typeColumn = cursor.getColumnIndex("type");
		int smsbodyColumn = cursor.getColumnIndex("body");

		if (cursor.getCount() != 0) {
			if (cursor.moveToNext()) {
				String phoneNumber;
				String smsBody, name, date, type;
				if (cursor.getInt(readColumn) == 0) {
					phoneNumber = cursor.getString(phoneNumberColumn);
					smsBody = cursor.getString(smsbodyColumn);
					name = cursor.getString(nameColumn);
					date = cursor.getString(dateColumn);
					type = cursor.getString(typeColumn);

					

					int ret = this.checkTag(smsBody);
					if (ret == 1) {
						Log.i("SMS","\n收到\naddress:" + phoneNumber);
						Log.i("SMS","\nbody:" + smsBody);
						if(EditActivity.infoText!=null){
							EditActivity.infoText.append("\n收到\naddress:" + phoneNumber);
							EditActivity.infoText.append("\nbody:" + smsBody);
						}
						this.generateKeyPair(smsBody, phoneNumber);
						Toast.makeText(sv.getApplicationContext(), phoneNumber+"请求与你协商密钥,你的密钥已经成功产生...", Toast.LENGTH_LONG).show();
					} else {
						if (ret == 2) {
							Log.i("SMS","\n收到\naddress:" + phoneNumber);
							Log.i("SMS","\nbody:" + smsBody);
							if(EditActivity.infoText!=null){
								EditActivity.infoText.append("\n收到\naddress:" + phoneNumber);
								EditActivity.infoText.append("\nbody:" + smsBody);
							}
							secretKey = this.generateKey2(smsBody, phoneNumber);
							if(EditActivity.infoText!=null){
								EditActivity.infoText.append("\nsecret key:" + secretKey + "\n");
							}
							Log.i("SMS","\nsecret key:" + secretKey + "\n");
							Toast.makeText(sv.getApplicationContext(), "密钥协商成功!!", Toast.LENGTH_LONG).show();
						} else {
							if (ret == 3) {

								if(EditActivity.infoText!=null){
									EditActivity.infoText.append("\n收到\naddress:" + phoneNumber);
								}
								Log.i("SMS","\n收到\naddress:" + phoneNumber);
								String decText=this.decryptSMS(smsBody, phoneNumber);
								// 插入数据库中
								{

									String sql = "insert into "
											+ tableName
											+ "(phonenumber,person,content,date,type) values (\""
											+ phoneNumber + "\",\"" + name + "\",\""
											+ decText + "\",\"" + date + "\",\"" + type
											+ "\")";
									db.execSQL(sql);
									if(EditActivity.infoText!=null){
										EditActivity.infoText.append("\n收到并插入数据库成功!!");	
									}
									
					
								}
							
								if(EditActivity.infoText!=null){
									EditActivity.infoText.append("\ndate:"+ cursor.getString(dateColumn));
								}
							  Log.i("SMS","\ndate:"
										+ cursor.getString(dateColumn));
							  if(EditActivity.infoText==null){
								  Toast.makeText(sv.getApplicationContext(), "收到来自"+phoneNumber+"的加密消息!!", Toast.LENGTH_LONG).show();
							  }
						
							}

						}
					}
					//修改为已读
					ContentValues value = new ContentValues();
					try {
						value.put("read", 1);
						this.mResolver.update(Uri.parse("content://sms/"),
								value, "_id=?",
								new String[] { cursor.getString(idColumn) });
					} catch (Exception exc) {
						Log.i("SMS","\n\\O_O/");
					}
				}

			}
			cursor.close();
		}
		// ------------------------------------------------------------

		super.onChange(selfChange);
	}

	private int checkTag(String message) {
		String[] receive = new String[4];
		receive = message.split("&");
		if (receive[0].equals("rk"))
			return 1;
		else if ("tk".equals(receive[0]))
			return 2;
		else if ("CM".equals(receive[0]))
			return 3;
		else
			return 0;
	}

	// 接受协商密钥的短信，并完成本端的密钥协商工作
	private void generateKeyPair(String message, String number) {
		String[] receive = new String[4];
		receive = message.trim().split("&");
		BigInteger[] bgArr = new BigInteger[2];
		bgArr[0] = new BigInteger(receive[1]);
		bgArr[1] = new BigInteger(receive[2]);
		BigInteger[] Bxy = new BigInteger[2];
		Bxy = SelfDefineDH.generateXY_B(bgArr);

		if(EditActivity.infoText!=null){
			EditActivity.infoText.append("Xb:" + Bxy[0].toString() + "\n");
			EditActivity.infoText.append("Yb:" + Bxy[1].toString() + "\n");
		}
		Log.i("SMS","Xb:" + Bxy[0].toString() + "\n");
		Log.i("SMS","Yb:" + Bxy[1].toString() + "\n");
		String send = Bxy[1].toString();
		//

		this.sendKeySpec(send, number);
		secretKey = this.generateKey(receive[3], Bxy[0].toString(), receive[1],
				number);
		
		if(EditActivity.infoText!=null){
			EditActivity.infoText.append("密钥:" + secretKey + "\n");
		}
		Log.i("SMS","密钥:" + secretKey + "\n");

	}

	private void sendKeySpec(String pubKeyEnc, String number) {

		SmsManager sms = SmsManager.getDefault();

		pubKeyEnc = "tk&" + pubKeyEnc;
		if (pubKeyEnc.length() > 70) {
			ArrayList<String> msgs = sms.divideMessage(pubKeyEnc);
			for (String msg : msgs) {
				sms.sendTextMessage(number, null, msg, null, null);
			}
		} else {
			sms.sendTextMessage(number, null, pubKeyEnc, null, null);
		}

	}

	private String generateKey(String y, String x, String p, String pNumber) {

		BigInteger key = BigInteger.ONE;
		key = new BigInteger(y).modPow(new BigInteger(x), new BigInteger(p));
		String strKey = key.toString();
		// 写入数据库
		{

			Cursor cursor = db.query(table_key, null, "phonenumber=?",
					new String[] { pNumber }, null, null, null);

			if (cursor.getCount() != 0) {
				
				String updateSql = "update " + table_key + " set key='"
						+ strKey + "' where phonenumber='" + pNumber + "'";
				db.execSQL(updateSql);
				
				if(EditActivity.infoText!=null){
					EditActivity.infoText.append("\n更新密钥数据表成功--!!\n");
				}
				Log.i("SMS","\n更新密钥数据表成功--!!\n");
				
				Cursor cursor2=db.query(table_key, null, "phonenumber=?",
						new String[] { pNumber }, null, null, null);
				
			
				
				if(cursor2.getCount()!=0){
					//Toast.makeText(this.sv, "\n密钥为:SB2",Toast.LENGTH_LONG).show();
					cursor2.moveToNext();
					String key2=cursor2.getString(cursor2.getColumnIndex("key"));
					if(EditActivity.infoText!=null){
						EditActivity.infoText.append("\n密钥为:"+key2);
					}
					Log.i("SMS","\n密钥为:"+key2);
					
				}
			
				cursor2.close();
			} else {// 插入该号码的记录
				
				String sql = "insert into " + table_key
						+ " (phonenumber,key,date) values (\"" + pNumber + "\",\""
						+ strKey + "\",\"" + System.currentTimeMillis() + "\")";
				db.execSQL(sql);
				if(EditActivity.infoText!=null){
					EditActivity.infoText.append("\n插入密钥数据表成功!!\n");
				}
				Log.i("SMS","\n插入密钥数据表成功!!\n");
				Cursor cursor2=db.query(table_key, null, "phonenumber=?",
						new String[] { pNumber }, null, null, null);

				
				if(cursor2.getCount()!=0){
					while(cursor2.moveToNext()){
						String pKey=cursor2.getString(cursor2.getColumnIndex("key"));
						if(EditActivity.infoText!=null){
							EditActivity.infoText.append("\n密钥为:"+pKey);
						}
						Log.i("SMS","\n密钥为:"+pKey);
					}
				}
				cursor2.close();
			}
			cursor.close();
		}
		return strKey;
	}

	// 接受协商密钥的短信，并完成本端的密钥协商工作
	private String generateKey2(String message, String number) {

		String[] receive = new String[2];
		receive = message.split("&");
		BigInteger Yb = new BigInteger(receive[1]);
		BigInteger key = Yb.modPow(GenerateKeyListener.Axy[0],
				GenerateKeyListener.bgArr[0]);
		String strKey = key.toString();
		// 写入数据库
		// 由于本App中只存放最新的密钥,不存放历史密钥
		// 所以先进行查找看看是否已经存在某个PhoneNumber的记录,如果有,那么就更新它的key字段,如果没有就插入一个记录
		{

			Cursor cursor = db.query(table_key, null, "phonenumber=?",
					new String[] { number }, null, null, null);

			if (cursor.getCount() != 0) {
				
				String updateSql = "update " + table_key + " set key='"
						+ strKey + "' where phonenumber='" + number + "'";
				db.execSQL(updateSql);
				if(EditActivity.infoText!=null){
					EditActivity.infoText.append("\n更新密钥数据表成功--!!");
				}
				Log.i("SMS","\n更新密钥数据表成功--!!");
				
				Cursor cursor2=db.query(table_key, null, "phonenumber=?",
						new String[] { number }, null, null, null);

				
				if(cursor2.getCount()!=0){
					while(cursor2.moveToNext()){
						String pKey=cursor2.getString(cursor2.getColumnIndex("key"));
						if(EditActivity.infoText!=null){
							EditActivity.infoText.append("\n密钥为:"+pKey);
						}
						Log.i("SMS","\n密钥为:"+pKey);
					}
				}
				cursor2.close();
				
			} else {// 插入该号码的记录
				String sql = "insert into " + table_key
						+ "(phonenumber,key,date) values (\"" + number + "\",\""
						+ strKey + "\",\"" + System.currentTimeMillis() + "\")";
				db.execSQL(sql);
				Log.i("SMS","\n插入密钥数据表成功!!");
				if(EditActivity.infoText!=null){
					EditActivity.infoText.append("\n插入密钥数据表成功!!");
				}
				Cursor cursor2=db.query(table_key, null, "phonenumber=?",
						new String[] { number }, null, null, null);

				
				if(cursor2.getCount()!=0){
					while(cursor2.moveToNext()){
						String pKey=cursor2.getString(cursor2.getColumnIndex("key"));
						if(EditActivity.infoText!=null){
							EditActivity.infoText.append("\n密钥为:"+pKey);
						}
						Log.i("SMS","\n密钥为:"+pKey);
					}
				}
				cursor2.close();
			}
			cursor.close();
		}
		return strKey;

	}

	// 解密加密密文
	private String decryptSMS(String message, String number) {
		String key = null;
		String[] receive = new String[2];
		receive = message.split("&");
		String cryptMessage=receive[1];
		byte[] cipher = Base64.decode(cryptMessage.getBytes(), Base64.DEFAULT);
		// 获取密钥数据库中相应的密钥
//new String[] { "key" }
		Cursor cursor = db.query(table_key,null ,
				"phonenumber=?", new String[] { number }, null, null, null);
		if(cursor.getCount()!=0){
			if (cursor.moveToNext()) {
				key = cursor.getString(cursor.getColumnIndex("key")).toString();
			}

	
		}

		cursor.close();
		byte[] plain = DESCrypt.decrypt(cipher, key);
		
		String result=new String(plain);
		if(EditActivity.infoText!=null){
			EditActivity.infoText.append("\n内容:" + message + "->" + result + "\n");
		}
		Log.i("SMS","\n内容:" + message + "->" + result + "\n");
		return result;//result;
	}
}
