package com.iti.models;

public class UserBalance {

	private int id;
	private double balance;
	private String MSISDN;
	
	public UserBalance() {
		// TODO Auto-generated constructor stub
	}
	public UserBalance(String MSISDN,double balance) {
		this.MSISDN=MSISDN;
		this.balance=balance;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	public String getMSISDN() {
		return MSISDN;
	}
	public void setMSISDN(String MSISDN) {
		this.MSISDN = MSISDN;
	}
	
	
}
