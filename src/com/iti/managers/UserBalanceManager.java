package com.iti.managers;

import java.util.List;

import com.iti.database.DB_Handler;
import com.iti.database.SQL_Condition;
import com.iti.database.psql.PSQL_Handler;
import com.iti.models.UserBalance;

public class UserBalanceManager {
	  DB_Handler db_handler;  	  
	  public UserBalanceManager()
	  {
		  db_handler= new PSQL_Handler();
	  }
	public UserBalance updateUserBalance(String MSISDN , double amount) {
		UserBalance userBalance = null;
		db_handler.connect();
		List<UserBalance> list =db_handler.readByValue(UserBalance.class,"MSISDN",SQL_Condition.EQUAL,MSISDN);
		if (!list.isEmpty())
		{
		  userBalance = list.getFirst();
	      userBalance.setBalance(userBalance.getBalance()+amount);
	      userBalance=db_handler.updateByValue(userBalance,"MSISDN",SQL_Condition.EQUAL,MSISDN);
		}
		db_handler.disconnect();
	    return userBalance;
	}
	
	public UserBalance getUserBalance(String MSISDN ) {
		UserBalance userBalance = null;
		db_handler.connect();
		List<UserBalance> list =db_handler.readByValue(UserBalance.class,"MSISDN",SQL_Condition.EQUAL,MSISDN);
		if (!list.isEmpty())
			userBalance = list.getFirst();
		db_handler.disconnect();
	    return userBalance;
	}

}
