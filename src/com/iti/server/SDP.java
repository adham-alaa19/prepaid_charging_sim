package com.iti.server;

import com.iti.managers.UserBalanceManager;
import com.iti.models.UserBalance;

public class SDP {
	
	public boolean checkBalance(String MSISDN) {
		UserBalanceManager manger = new UserBalanceManager();
		UserBalance userBalance = manger.getUserBalance(MSISDN);
		return userBalance!=null && userBalance.getBalance()>5;
	}
	
	public boolean userExists(String MSISDN) {
		UserBalanceManager manger = new UserBalanceManager();
		UserBalance userBalance = manger.getUserBalance(MSISDN);
		return (userBalance!=null);
	}
	public double deductBalance(String MSISDN) {
		UserBalanceManager manger = new UserBalanceManager();
		UserBalance userBalance = manger.updateUserBalance(MSISDN, -5);
		if (userBalance!=null)
		return userBalance.getBalance();
		else 
			return -1;
	}
	
	public double getBalance(String MSISDN) {
		UserBalanceManager manger = new UserBalanceManager();
		UserBalance userBalance = manger.getUserBalance(MSISDN);
		if (userBalance!=null)
		return userBalance.getBalance();
		else 
			return -1;
	}
	public UserBalance deductBalance(String MSISDN,double charge,int time) {
		
		UserBalanceManager manger = new UserBalanceManager();
		UserBalance userBalance = manger.updateUserBalance(MSISDN, charge*time);
		return userBalance;
	}

}
