package com.infotech.isg.domain;

import java.util.Date;

/**
 * domain object representing topup transaction.
 *
 * @author Sevak Gharibian
 */
public class Transaction {
    // auto-id
    private long id;

    // service provider id, such as MTN=1, MCI=2, Jiring=3
    private int provider;

    // token received from MCI,Jiring in reponse to GET_Token request
    private String token;

    // action from request, column name = type
    private int action;

    // state prarmeter from request
    private String state;

    // orderId from request
    private String resNum;

    // bankReceipt/RRN from request
    private String refNum;

    // seems like not being used anymore
    private Long revNum;

    // remote IP makeing request, column name = clientip
    private String remoteIp;

    // amount from request, should be in valid range
    private long amount;

    // payment channel ID from request, should be defined active in DB
    private String channel;

    // consumer from request, means cell number
    private String consumer;

    // bankCode from request, should be valid
    private String bankCode;

    // client id defined in clients table, column name = client
    private int clientId;

    // customerIp from request
    private String customerIp;

    // current datetime once request receveived by ISG, column name = trtime
    private Date trDateTime;

    // filled by amount from request
    private Integer bankVerify;

    // filled by current datetime once request received by ISG, seems useless, column name = verifytime
    private Date verifyDateTime;

    // representing transaction/service result, 1=OK, otherwise=NOK{0,-1,-2,-3}
    private Integer status;

    // filled by response code from service provider, apparently 0=OK, otherwise=NOK, column name = operator
    private Integer operatorResponseCode;

    // filled by command status field from MTN request only, comumn name = oprcommand
    private String operatorCommand;

    // filled by response message/detail in service provider's response, column name = oprresponse
    private String operatorResponse;

    // fiiled by MTN/transctionId, MCI/response detail, Jiring/token, another words some additional info from response, column name = oprtid
    private String operatorTId;

    // filled by current datetime once response recevied from service provider, column name = operatortime
    private Date operatorDateTime;

    // =1 means this transaction is set to be processed by STF service, in case of failure.
    // =2 means stf checked and action was successfull.
    // =3 means stf checked and action was not successfull.
    private Integer stf;

    // apparently set by STF service
    private Integer stfResult;

    // seems like not being used anymore
    private Integer opReverse;

    // seems like not being used anymore
    private Integer bkReverse;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getProvider() {
        return provider;
    }

    public void setProvider(int provider) {
        this.provider = provider;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRefNum() {
        return refNum;
    }

    public void setRefNum(String refNum) {
        this.refNum = refNum;
    }

    public String getResNum() {
        return resNum;
    }

    public void setResNum(String resNum) {
        this.resNum = resNum;
    }

    public Long getRevNum() {
        return revNum;
    }

    public void setRevNum(Long revNum) {
        this.revNum = revNum;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getCustomerIp() {
        return customerIp;
    }

    public void setCustomerIp(String customerIp) {
        this.customerIp = customerIp;
    }

    public Date getTrDateTime() {
        return trDateTime;
    }

    public void setTrDateTime(Date trDateTime) {
        this.trDateTime = trDateTime;
    }

    public Integer getBankVerify() {
        return bankVerify;
    }

    public void setBankVerify(Integer bankVerify) {
        this.bankVerify = bankVerify;
    }

    public Date getVerifyDateTime() {
        return verifyDateTime;
    }

    public void setVerifyDateTime(Date verifyDateTime) {
        this.verifyDateTime = verifyDateTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getOperatorResponseCode() {
        return operatorResponseCode;
    }

    public void setOperatorResponseCode(Integer operatorResponseCode) {
        this.operatorResponseCode = operatorResponseCode;
    }

    public String getOperatorCommand() {
        return operatorCommand;
    }

    public void setOperatorCommand(String operatorCommand) {
        this.operatorCommand = operatorCommand;
    }

    public String getOperatorResponse() {
        return operatorResponse;
    }

    public void setOperatorResponse(String operatorResponse) {
        this.operatorResponse = operatorResponse;
    }

    public String getOperatorTId() {
        return operatorTId;
    }

    public void setOperatorTId(String operatorTId) {
        this.operatorTId = operatorTId;
    }

    public Date getOperatorDateTime() {
        return operatorDateTime;
    }

    public void setOperatorDateTime(Date operatorDateTime) {
        this.operatorDateTime = operatorDateTime;
    }

    public Integer getStf() {
        return stf;
    }

    public void setStf(Integer stf) {
        this.stf = stf;
    }

    public Integer getStfResult() {
        return stfResult;
    }

    public void setStfResult(Integer stfResult) {
        this.stfResult = stfResult;
    }

    public Integer getOpReverse() {
        return opReverse;
    }

    public void setOpReverse(Integer opReverse) {
        this.opReverse = opReverse;
    }

    public Integer getBkReverse() {
        return bkReverse;
    }

    public void setBkReverse(Integer bkReverse) {
        this.bkReverse = bkReverse;
    }

    @Override
    public String toString() {
        return String.format("[%d:(%s,%d,%s)RRN:%s,status:%d,operatorResCode:%d,STF:%d(%s)]",
                             id, consumer, amount, Operator.getName(provider), refNum, status, operatorResponseCode, stf, trDateTime);
    }
}
