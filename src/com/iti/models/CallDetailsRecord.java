package com.iti.models;

import java.time.Duration;
import java.time.Instant;

public class CallDetailsRecord {
    private String MSISDN;               
    private String destinationNumber;    
    private Instant callStartTime;      
    private Instant callEndTime;        
    private String callType;             
    private String billingStatus;       
    private double balanceBefore;       
    private double balanceAfter;        

    public CallDetailsRecord(String MSISDN, String destinationNumber, Instant callStartTime, Instant callEndTime,
                             String callType, String billingStatus, double balanceBefore, double balanceAfter) {
        this.MSISDN = MSISDN;
        this.destinationNumber = destinationNumber;
        this.callStartTime = callStartTime;
        this.callEndTime = callEndTime;
        this.callType = callType;
        this.billingStatus = billingStatus;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
    }
    
    public CallDetailsRecord(String MSISDN) {
    	 this.MSISDN = MSISDN;
      	 this.callStartTime = Instant.now();
}
    
    public CallDetailsRecord(String MSISDN,String billingStatus) {
   	 this.MSISDN = MSISDN;
   	 this.callStartTime = Instant.now();
   	 this.billingStatus = billingStatus;
}
    
 

    // Getters and setters
    public String getMSISDN() {
        return MSISDN;
    }

    public void setMSISDN(String MSISDN) {
        this.MSISDN = MSISDN;
    }

    public String getDestinationNumber() {
        return destinationNumber;
    }

    public void setDestinationNumber(String destinationNumber) {
        this.destinationNumber = destinationNumber;
    }

    public Instant getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(Instant callStartTime) {
        this.callStartTime = callStartTime;
    }

    public Instant getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(Instant callEndTime) {
        this.callEndTime = callEndTime;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getBillingStatus() {
        return billingStatus;
    }

    public void setBillingStatus(String billingStatus) {
        this.billingStatus = billingStatus;
    }

    public double getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(double balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
    
    
    public double getCost() {
        return balanceBefore - balanceAfter;
    }

    public long getCallDuration() {
        if (callStartTime == null || callEndTime == null) {
            return 0;
        }
        Duration duration = Duration.between(callStartTime, callEndTime);
        return duration.toMinutes(); 
    }

    public String getDurationString() {
        if (callStartTime == null || callEndTime == null) {
            return "00:00:00"; 
        }
        Duration duration = Duration.between(callStartTime, callEndTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds); // HH:mm:ss format
        } else {
            return String.format("%02d:%02d", minutes, seconds); // mm:ss format
        }
    }

    @Override
    public String toString() {
        String cdr = "{"+  MSISDN +  "," + callStartTime + "," + callEndTime + "," + getDurationString() + ","
                + callType + "," + billingStatus + "," + getCost() + "," + balanceAfter+ "}";
        return cdr;
    }
    
    
    public String toString2() {
        String cdr = "{"+  MSISDN + "," + destinationNumber + "," + callStartTime + "," + callEndTime + "," + getDurationString() + ","
                + callType + "," + billingStatus + "," + balanceBefore + "," + balanceAfter+ "}";
        return cdr;
    }
}
