package com.bruce.info;

public class SmsInfo {

	/*
	 * 发送短信的手机号码
	 */
	private String phoneNumber;
	/*
	 * 短信内容
	 */
	private String smsBody;
	/*
	 * 发送/接受日期
	 */
	private String date;
	/*
	 * 发送短信人的姓名
	 */
	private String name;
	/*
	 * 短信类型 1 接受 2发送
	 */
	private String type;
	public String getSmsBody() {
		return smsBody;
	}
	public void setSmsBody(String smsBody) {
		this.smsBody = smsBody;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
