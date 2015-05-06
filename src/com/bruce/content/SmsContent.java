package com.bruce.content;

import java.util.ArrayList;
import java.util.List;

import com.bruce.info.SmsInfo;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class SmsContent {

	private Activity activity;
	private Uri uri;
	List<SmsInfo> infos;
	List<SmsInfo> param;
    private SQLiteDatabase db;
    private final String table_Message="all_cryptmessage";
	private final String table_key="privatekey";
	public SmsContent(Activity activity, Uri uri,SQLiteDatabase db) {
		infos = new ArrayList<SmsInfo>();
		this.activity = activity;
		this.uri = uri;
		this.db=db;
		
	}



	// 获取短信
	public List<SmsInfo> getSmsInfo(String phoneNumber) {
		String[] projection = new String[] { "_id", "address", "person",
				"body", "date", "type" };
		Cursor cursor = null;
		if (phoneNumber == null) {
			cursor = activity.managedQuery(uri, projection, null, null,
					"date desc");

		} else {// 查询特定号码的信息
			String where = "address=\"" + phoneNumber + "\"";
			cursor = activity.managedQuery(uri, projection, where, null,
					"date desc");

		}
		int nameColumn = cursor.getColumnIndex("person");
		int phoneNumberColumn = cursor.getColumnIndex("address");
		int smsBodyColumn = cursor.getColumnIndex("body");
		int dateColumn = cursor.getColumnIndex("date");
		int typeColumn = cursor.getColumnIndex("type");
		if (cursor != null) {

			if (phoneNumber == null) {

				while (cursor.moveToNext()) {
					SmsInfo smsInfo = new SmsInfo();
					smsInfo.setName(cursor.getString(nameColumn));
					smsInfo.setDate(cursor.getString(dateColumn));
					smsInfo.setPhoneNumber(cursor.getString(phoneNumberColumn));
					smsInfo.setSmsBody(cursor.getString(smsBodyColumn));
					smsInfo.setType(cursor.getString(typeColumn));
					infos.add(smsInfo);
					Log.i("SIGN",
							smsInfo.getPhoneNumber() + smsInfo.getSmsBody());
				}
			} else {

				cursor.moveToLast();
				do {

					SmsInfo smsInfo = new SmsInfo();
					smsInfo.setName(cursor.getString(nameColumn));
					smsInfo.setDate(cursor.getString(dateColumn));
					smsInfo.setPhoneNumber(cursor.getString(phoneNumberColumn));
					smsInfo.setSmsBody(cursor.getString(smsBodyColumn));
					smsInfo.setType(cursor.getString(typeColumn));
					infos.add(smsInfo);

				} while (cursor.moveToPrevious());

			}

		}
		cursor.close();
		return infos;

	}
	
	//获取与每个通信人的最新的那条短信
		public List<SmsInfo> getLatestSmsInfo(String phoneNumber){
			String[] projection = new String[] { "phonenumber", "person",
					"content", "date", "type" };
			
			Cursor cursor = null;
			if (phoneNumber == null) {
				//cursor = activity.managedQuery(uri, projection, null, null,
				//		"date desc");

				String sql="select * from "+table_Message+" where date in (select max(date) from "+table_Message+" group by phonenumber)";
				cursor=db.rawQuery(sql, null);
				if(cursor.getCount()==0){
				
					return null;
				}
				
			} else {// 查询特定号码的信息
				//String where = "address=\"" + phoneNumber + "\"";
				String sql="select * from "+table_Message+" where phonenumber="+phoneNumber +" order by date";
				cursor=db.rawQuery(sql, null);
				//cursor = activity.managedQuery(uri, projection, where, null,
				//		"date desc");
				if(cursor.getCount()==0)
					return null;

			}
			db.close();
			int nameColumn = cursor.getColumnIndex("person");
			int phoneNumberColumn = cursor.getColumnIndex("phonenumber");
			int smsBodyColumn = cursor.getColumnIndex("content");
			int dateColumn = cursor.getColumnIndex("date");
			int typeColumn = cursor.getColumnIndex("type");
			if (cursor != null) {

				if (phoneNumber == null) {

					while (cursor.moveToNext()) {
						SmsInfo smsInfo = new SmsInfo();
						smsInfo.setName(cursor.getString(nameColumn));
						smsInfo.setDate(cursor.getString(dateColumn));
						smsInfo.setPhoneNumber(cursor.getString(phoneNumberColumn));
						smsInfo.setSmsBody(cursor.getString(smsBodyColumn));
						smsInfo.setType(cursor.getString(typeColumn));
						infos.add(smsInfo);
						Log.i("SIGN",
								smsInfo.getPhoneNumber() + smsInfo.getSmsBody());
					}
				} else {

					/*cursor.moveToLast();
					do {

						SmsInfo smsInfo = new SmsInfo();
						smsInfo.setName(cursor.getString(nameColumn));
						smsInfo.setDate(cursor.getString(dateColumn));
						smsInfo.setPhoneNumber(cursor.getString(phoneNumberColumn));
						smsInfo.setSmsBody(cursor.getString(smsBodyColumn));
						smsInfo.setType(cursor.getString(typeColumn));
						infos.add(smsInfo);

					} while (cursor.moveToPrevious());*/
					while (cursor.moveToNext()) {
						SmsInfo smsInfo = new SmsInfo();
						smsInfo.setName(cursor.getString(nameColumn));
						smsInfo.setDate(cursor.getString(dateColumn));
						smsInfo.setPhoneNumber(cursor.getString(phoneNumberColumn));
						smsInfo.setSmsBody(cursor.getString(smsBodyColumn));
						smsInfo.setType(cursor.getString(typeColumn));
						infos.add(smsInfo);
					}

				}

			}
			cursor.close();
			return infos;

		}

}
