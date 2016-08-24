/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yccheok.jstock.trading;


import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;


/**
 *
 * @author shuwnyuan
 */
public class DriveWealthAPI {

    public DriveWealthAPI() {
        this.user = new User();
    }

    public Map<String, Object> login(String username, String password) {
        this.user.username = username;
        this.user.password = password;

        Map<String, String> args = new HashMap<>();
        args.put("username",    this.user.username);
        args.put("password",    this.user.password);
        args.put("appTypeID",   "26");
        args.put("appVersion",  "0.1");
        args.put("languageID",  "en_US");
        args.put("osType",      "iOS");
        args.put("osVersion",   "iOS 9.1");
        args.put("scrRes",      "1920x1080");
        args.put("ipAddress",   "1.1.1.1");

        Map<String, Object> session = createSession(args);

        // error create session
        if (session.containsKey("code") && session.containsKey("message")) {
            System.out.println("create session ERROR, code: " + session.get("code") + ", message: " + session.get("message"));
            return session;
        }

        this.user.sessionKey = session.get("sessionKey").toString();
        this.user.userID = session.get("userID").toString();
        this.user.commissionRate = (double) session.get("commissionRate");
        
        List<Map<String, Object>> accounts = (ArrayList) session.get("accounts");

        for (Map<String, Object> a : accounts) {
            String accountID   = a.get("accountID").toString();
            String accountNo   = a.get("accountNo").toString();
            double accountType = (double) a.get("accountType");
            double cash        = (double) a.get("cash");

            final Account account = new Account(a);

            if (accountType == 1) {
                this.user.practiceAccount = account;
            } else if (accountType == 2) {
                this.user.liveAccount = account;
            }

            if (!this.user.accountsMap.containsKey(accountID)) {
                this.user.accountsMap.put(accountID, account);
            }
        }
        return session;
    }
    
    /********************
     * API Functions
     ********************/

    static final List<String> accountFields = new ArrayList<>(Arrays.asList(
        "accountID",
        "accountNo",
        "userID",
        "currencyID",
        "accountType",
        "cash",
        "freeTradeBalance",
        "goodFaithViolations",
        "interestFree",
        "orders",
        "patternDayTrades",
        "positions",
        "status",
        "tradingType",
        "bodMoneyMarket",
        "bodEquityValue",
        "bodCashAvailForWith",
        "bodCashAvailForTrading",
        "rtCashAvailForTrading",
        "rtCashAvailForWith",
        "accountMgmtType",
        "longOnly"
    ));
    
    static final List<String> userFields = new ArrayList<>(Arrays.asList(
        "emailAddress1",
        "firstName",
        "lastName",
        "username",
        "password",
        "languageID",
        "tranAmount",
        "referralCode",
        "wlpID",
        "utm_campaign",
        "utm_content",
        "utm_medium",
        "utm_source",
        "utm_term"
    ));     

    static final List<String> getUserFields = new ArrayList<>(Arrays.asList(
        "commissionRate",
        "emailAddress1",
        "languageID",
        "referralCode",
        "sessionKey",
        "username",
        "wlpID",
        "status",
        "lastLoginWhen",
        "ackSignedWhen",
        "addressLine1",
        "avatarUrl",
        "city",
        "coinBalance",
        "countryID",
        "displayName",
        "firstName",
        "lastName",
        "gender",
        "phoneHome",
        "stateProvince",
        "userID",
        "zipPostalCode",
        "usCitizen",
        "updatedWhen",
        "rebateCfdValue",
        "rebateEquityValue",
        "rebateFxValue",
        "brandAmbassador",
        "employerBusiness",
        "employmentStatus",
        "statementPrint",
        "confirmPrint",
        "citizenship",
        "createdWhen",
        "addressProofReviewWhen",
        "approvedWhen",
        "approvedBy",
        "kycWhen",
        "pictureReviewBy",
        "pictureReviewWhen",
        "annualIncome",
        "userAttributes"
    ));     
    
    static final List<String> liveAccountFields = new ArrayList<>(Arrays.asList(
        "ownershipType",
        "userID",
        "emailAddress1",
        "emailAddress2",
        "firstName",
        "lastName",
        "username",
        "password",
        "gender",
        "languageID",
        "phoneHome",
        "phoneWork",
        "phoneMobile",
        "addressLine1",
        "addressLine2",
        "city",
        "countryID",
        "stateProvince",
        "wlpID",
        "zipPostalCode",
        "dob",
        "maritalStatus",
        "idNo",
        "usCitizen",
        "referralCode",
        "tradingType",
        "citizenship",
        "utm_campaign",
        "utm_content",
        "utm_medium",
        "utm_source",
        "utm_term",
        "employerBusiness",
        "employerCompany",
        "employerAddressLine1",
        "employerAddressLine2",
        "employerCity",
        "employerStateProvince",
        "employerZipPostalCode",
        "employerCountryID",
        "employerIsBroker",
        "employmentPosition",
        "employmentStatus",
        "employmentYears",
        "annualIncome",
        "investmentObjectives",
        "investmentExperience",
        "networthLiquid",
        "networthTotal",
        "director",
        "directorOf",
        "politicallyExposed",
        "politicallyExposedNames",
        "riskTolerance",
        "disclosureAck",
        "disclosureRule14b",
        "ackCustomerAgreement",
        "ackSweep",
        "ackFindersFee",
        "ackForeignFindersFee",
        "ackMarketData",
        "ackSignedBy",
        "ackSignedWhen",
        "ackSigned",
        "accountMgmtType"
    ));
    
    static final List<String> userStatusFields = new ArrayList<>(Arrays.asList(
        "accountStatus",
        "accountReasonID",
        "accountReason",
        "accountApprovedWhen",
        "addressProofReviewWhen",
        "addressProofStatus",
        "addressProofReasonID",
        "addressProofReason",
        "idReviewWhen",
        "idStatus",
        "idReasonID",
        "idReason",
        "kycWhen",
        "kycStatus"
    ));
    
    static final List<String> updateUserFields = new ArrayList<>(Arrays.asList(
        "userID",
        "addressLine1",
        "addressLine2",
        "city",
        "countryID",
        "emailAddress1",
        "firstName",
        "lastName",
        "languageID",
        "phoneHome",
        "phoneWork",
        "phoneMobile",
        "stateProvince",
        "zipPostalCode"
    ));    
    
    static final List<String> resetPasswordFields = new ArrayList<>(Arrays.asList(
        "code",
        "passwordResetID",
        "password"
    ));

    static final List<String> getSessionFields = new ArrayList<>(Arrays.asList(
        "appTypeID",
        "appVersion",
        "commissionRate",
        "heartbeatWhen",
        "ipAddress",
        "loginState",
        "languageID",
        "loginWhen",
        "osType",
        "osVersion",
        "referralCode",
        "scrRes",
        "sessionKey",
        "userID",
        "wlpID",
        "accounts",
        "instruments"
    ));

    static final List<String> createSessionFields = new ArrayList<>(Arrays.asList(
        "username",
        "password",
	"appTypeID",
        "appVersion",
        "languageID",
        "osType",
        "osVersion",
        "scrRes",
	"ipAddress"
    ));

    static final List<String> sessionFields = new ArrayList<>(Arrays.asList(
        "appTypeID",
        "commissionRate",
        "loginState",
        "referralCode",
        "sessionKey",
        "userID",
        "wlpID",
        "accounts",
        "instruments"
    ));

    static final List<String> instrumentFields = new ArrayList<>(Arrays.asList(
        "name",
        "instrumentID",
        "currencyID",
        "exchangeID",
        "instrumentTypeID",
        "isLongOnly",
        "orderSizeMax",
        "orderSizeMin",
        "orderSizeStep",
        "rateAsk",
        "rateBid",
        "ratePrecision",
        "symbol",
        "tags",
        "tradeSatus",
        "tradingHours",
        "longOnly"
    ));

    static final List<String> getInstrumentFields = new ArrayList<>(Arrays.asList(
        "name",
        "instrumentID",
        "currencyID",
        "description",
        "exchangeID",
        "orderSizeMax",
        "orderSizeMin",
        "orderSizeStep",
        "rateAsk",
        "rateBid",
        "symbol",
        "tags",
        "tradeStatus",
        "tradingHours",
        "urlImage",
        "urlInvestor",
        "fundamentalDataModel",
        "sector",
        "longOnly",
        "instrumentTypeID",
        "ratePrecision",
        "lastTrade"
    ));

    static final List<String> searchInstrumentFields = new ArrayList<>(Arrays.asList(
        "instrumentID",
        "name",
        "category",
        "currencyID",
        "exchangeID",
        "limitStatus",
        "instrumentTypeID",
        "isLongOnly",
        "marginCurrencyID",
        "orderSizeMax",
        "orderSizeMin",
        "orderSizeStep",
        "rateAsk",
        "rateBid",
        "ratePrecision",
        "symbol",
        "tags",
        "tradeStatus",
        "tradingHours",
        "uom",
        "urlImage",
        "urlInvestor",
        "sector",
        "longOnly",
        "lastTrade"
    ));
    
    static final List<String> createOrderFields = new ArrayList<>(Arrays.asList(
        "symbol",
        "instrumentID",
        "accountID",
        "accountNo",
        "userID",
        "accountType",
        "ordType",
        "side",
        "orderQty",
        "comment",

        // for Market Order only
        "amountCash",
        "autoStop",

        // for Stop Order only
        "price",

        // for Limit Order only
        "limitPrice"
    ));
    
    static final List<String> orderFields = new ArrayList<>(Arrays.asList(
        "execID",
        "orderID",
        "cumQty",
        "instrumentID",
        "execType",
        "grossTradeAmt",
        "leavesQty",
        "ordType",
        "side",
        "ordStatus",
        "limitPrice",
        "timeInForce",
        "expireTimestamp",
        "statusPath"
    ));
    
    static final List<String> orderStatusFields = new ArrayList<>(Arrays.asList(
        "orderID",
        "accountID",
        "userID",
        "cumQty",
        "accountNo",
        "comment",
        "commission",
        "createdByID",
        "createdWhen",
        "executedWhen",
        "execType",
        "grossTradeAmt",
        "instrumentID",
        "leavesQty",
        "orderNo",
        "orderQty",
        "ordStatus",
        "ordType",
        "side",
        "accountType",
        "autoStop",
        "ordRejReason",

        // for Stop Order only
        "price",

        // for Limit Order only
        "limitPrice",
            
        // a) Market & Limit order are DAY order, not persist and are cancelled if not filled at end of exchange trading day.
        //      => timeInForce = 0 / null
        //
        // b) Stop order persists across trading days, are GOOD UNTIL CANCEL.
        //      => timeInforce = 1
        "timeInForce"
    ));
    
    static final List<String> marketDataFields = new ArrayList<>(Arrays.asList(
        "symbol",
        "bid",
        "ask",
        "lastTrade"
    ));
    
    static final List<String> settingFields = new ArrayList<>(Arrays.asList(
        "userID",
        "key",
        // only for create setting, list all settings
        "value"
    ));

    static final List<String> chartFields = new ArrayList<>(Arrays.asList(
        "instrumentID",
        "compression",
        "dateStart",
        "dateEnd",
        "tradingDay"
    ));

    static final List<String> financialTxnFields = new ArrayList<>(Arrays.asList(
        "accountAmount",
        "accountBalance",
        "comment",
        "currencyID",
        "finTranID",
        "finTranTypeID",
        "orderID",
        "orderNo",
        "systemAmount",
        "tranAmount",
        "tranWhen",
        "wlpAmount",
        "execID",
        "dnb"
    ));
    
    static final List<String> orderTxnFields = new ArrayList<>(Arrays.asList(
        "orderNo",
        "transactTime",
        "execType",
        "ordStatus",
        "ordType",
        "side",
        "symbol",
        "lastShares",
        "lastPx",
        "cumQty",
        "leavesQty"
    ));

    static final List<String> openPosFields = new ArrayList<>(Arrays.asList(
        "costBasis",
        "initQty",
        "side",
        "openQty",
        "instrument",
        "price",
        "mtm",
        "mtmPL"
    ));
    
    static final List<String> stockFields = new ArrayList<>(Arrays.asList(
        "instrument",
        "instrumentTypeDescr",
        "tradeStatusDescr",
        "tagDescr",
        "linkable10K"
    ));

    static final List<String> stockInstrumentFields = new ArrayList<>(Arrays.asList(
        "currencyID",
        "instrumentID",
        "limitStatus",
        "instrumentTypeID",
        "marginCurrencyID",
        "name",
        "orderSizeMax",
        "orderSizeMin",
        "orderSizeStep",
        "symbol",
        "tradeStatus",
        "urlInvestor",
        "priorClose",
        "marketState",
        "minTic",
        "pipMultiplier",
        "rebateSpread"
    ));
    
    static final List<String> listStatementFields = new ArrayList<>(Arrays.asList(
        "displayName",  // Display name of the file
        "fileKey"       // Key to access the file via "Get Statements"
    ));
    
    static final List<String> getStatementFields = new ArrayList<>(Arrays.asList(
        "accountID",
        "url"           // URL to download statement directly. Note: this url will expire in 30 minutes.
    ));
    
    /********************
     * API: Accounts
     ********************/

    public AccountBlotter accountBlotter(String userID, String accountID) {
        System.out.println("\n[Account Blotter] userID:" + userID + ", accountID:" + accountID);
        return new AccountBlotter(this, userID, accountID);
    }
            
    public Map<String, Object> getAccount(String userID, String accountID) {
        System.out.println("\n[getAccount] " + accountID);
        
        Map<String, Object> respondMap = executeGet("users/" + userID + "/accounts/" + accountID, this.getSessionKey());
        Map<String, Object> result = gson.fromJson(respondMap.get("respond").toString(), HashMap.class);

        Map<String, Object> account = new HashMap<>();
        for (String k: this.accountFields) {
            if (result.containsKey(k)) {
                Object v = result.get(k);
                account.put(k, v);
                //System.out.println("key: " + k + ", value: " + v);
            }
        }
        return account;
    }
    
    public List<Map<String, Object>> listAllAccounts(String userID) {
        System.out.println("\n[listAllAccounts]");
        
        Map<String, Object> respondMap = executeGet("users/" + userID + "/accounts", this.getSessionKey());
        List<Map<String, Object>> result = gson.fromJson(respondMap.get("respond").toString(), ArrayList.class);
        List<Map<String, Object>> accounts = new ArrayList<>();
        
        for (Map<String, Object> a : result) {
            Map<String, Object> account = new HashMap<>();
            for (String k: this.accountFields) {
                if (a.containsKey(k)) {
                    Object v = a.get(k);
                    account.put(k, v);
                    //System.out.println("key: " + k + ", value: " + v);
                }
            }
            accounts.add(account);
            
            String accountID = account.get("accountID").toString();
            String accountNo = account.get("accountNo").toString();
            double accountTypeID = (double) account.get("accountType");
            double cash = (double) account.get("cash");

            // accountTypeID: 1 => Practice a/c, 2 => Live a/c
            String accountType = (accountTypeID == 1) ? "Practice" : "Live";

            Account acc = this.new Account(account);
            if (accountTypeID == 1) {
                this.user.practiceAccount = acc;
            } else if (accountTypeID == 2) {
                this.user.liveAccount = acc;
            }
            
            if (!this.user.accountsMap.containsKey(accountID)) {
                this.user.accountsMap.put(accountID, acc);
            }

            
            System.out.println("accountID: " + accountID + ", accountNo: " + accountNo + ", accountType: " + accountType + ", cash" + cash);
        }
        return accounts;
    }

    /* API endpoint: "signups/practice"
        if user not exists, POST creates user + practice a/c
        if user exist but no practice a/c, POST with userID creates practice a/c
        if user & practice account exist, POST with userID returns existing accountID
    */

    public Account createPracticeAccount(Map<String, Object> args) {
        System.out.println("\n[create Practice Account]");
        String api = "signups/practice";
        
        if (args.containsKey("userID")) {
            // user already exist, create practice a/c
            String userID = args.get("userID").toString();
            
            // existing user requires SignIn: to create SessionKey
            if (this.user == null || !this.user.userID.equals(userID)) {
                System.out.println("Please Sign In, userID: " + userID);
                return null;
            }

            // practice acc exists
            if (this.user.practiceAccount != null) {
                Account acc = this.user.practiceAccount;
                System.out.println("Practice a/c exists: accountID: " + acc.accountID + ", accountNo: " + acc.accountNo);
                return acc;
            }

            // create practice account
            Map<String, Object> params = new HashMap<>();
            params.put("userID", userID);

            Map<String, Object> respondMap = executePost(api, params, this.getSessionKey());
            String respond = respondMap.get("respond").toString();
            Map<String, Object> result = gson.fromJson(respond, HashMap.class);

            String accountID = result.get("accountID").toString();
            System.out.println("user already exist, created practice accountID: " + accountID);

            this.login(this.user.username, this.user.password);
            
            return this.user.practiceAccount;
        } else {
            // create new user + practice a/c
            System.out.println("create User + Practice a/c + funded");

            Map<String, Object> params = new HashMap<>();
            for (String k: this.userFields) {
                if (args.containsKey(k)) {
                    Object v = args.get(k);
                    params.put(k, v);
                    //System.out.println("key: " + k + ", value: " + v);
                }
            }
            Map<String, Object> respondMap = executePost(api, params, null);

            String respond = respondMap.get("respond").toString();
            Map<String, Object> result = gson.fromJson(respond, HashMap.class);
            int statusCode = (int) respondMap.get("code");

            Account acc = null;
            // status code: 400 => duplicate username, 200 => OK
            if (statusCode == 200) {
                this.login(result.get("username").toString(), result.get("password").toString());
                acc = this.user.practiceAccount;
                
                System.out.println("New user + practice a/c created");
            } else if (statusCode == 400) {
                System.out.println("User already exists, error: " + result.get("message").toString());
            }

            return acc;
        }
    }

    public Map<String, Object> createLiveAccount(Map<String, Object> args) {
        // user already exist, create live account
        System.out.println("\n[Create Live Account]");

        Map<String, Object> params = new HashMap<>();
        for (String k: this.liveAccountFields) {
            if (args.containsKey(k)) {
                Object v = args.get(k);
                params.put(k, v);
                //System.out.println("key: " + k + ", value: " + v);
            }
        }

        Map<String, Object> respondMap = executePost("signups/live", params, this.getSessionKey());
        String respond = respondMap.get("respond").toString();
        Map<String, Object> result = gson.fromJson(respond, HashMap.class);
        
        Map<String, Object> liveAc = new HashMap<>();
        liveAc.put("useID", result.get("userID"));
        liveAc.put("username", result.get("username"));
        
        return liveAc;
    }

    public boolean addDocument (Map<String, Object> args) {
        System.out.println("\n[add Document]\n\n\n");

        String userID = args.get("userID").toString();
        String documentType = args.get("documentType").toString();
        File file = (File) args.get("file");
        String fileFormat = args.get("fileFormat").toString();

        Part[] parts = new Part[3];
        parts[0] = new StringPart("token", userID);
        parts[1] = new StringPart("documentType", documentType);

        try {
            parts[2] = new FilePart("documentImage", file, fileFormat, null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DriveWealthAPI.class.getName()).log(Level.SEVERE, null, ex);
        }

        PostMethod postMethod = new PostMethod(hostURL + "documents");
        postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
        postMethod.addRequestHeader("x-mysolomeo-session-key", getSessionKey());
        Map<String, Object> result = executeHttpCall(postMethod);
        
        return (int) result.get("code") == 200;
    }
    
    /********************
     * API: Users
     ********************/

    public Map<String, Object> createUser(Map<String, String> args) {
        System.out.println("\n[create User Only]");

        Map<String, Object> params = new HashMap<>();
        for (String k: this.userFields) {
            if (k.equals("tranAmount")) {
                continue;
            }
            
            if (args.containsKey(k)) {
                Object v = args.get(k);
                params.put(k, v);
                //System.out.println("key: " + k + ", value: " + v);
            }
        }
        
        Map<String, Object> respondMap = executePost("signups/live", params, null);
        String respond = respondMap.get("respond").toString();
        Map<String, Object> result  = gson.fromJson(respond, HashMap.class);
        int code = (int) result.get("code");
        
        Map<String, Object> user = new HashMap<>();
        
        if (code == 200) {
            user.put("password", result.get("password").toString());
            user.put("userID", result.get("userID").toString());
            user.put("username", result.get("username").toString());
        } else if (code == 400) {
            System.out.println("create user, error: " + result.get("message").toString());
        }
        return user;
    }
    
    public Map<String, Object> getUser(String userID) {
        System.out.println("\n[getUser]");
        
        Map<String, Object> respondMap = executeGet("users/" + userID, this.getSessionKey());
        Map<String, Object> result  = gson.fromJson(respondMap.get("respond").toString(), HashMap.class);

        Map<String, Object> user = new HashMap<>();
        for (String k: this.getUserFields) {
            if (result.containsKey(k)) {
                Object v = result.get(k);
                user.put(k, v);
                //System.out.println("key: " + k + ", value: " + v);
            }
        }
        return user;
    }
    
    public boolean checkUserNameAvailability(String username) {
        System.out.println("\n[checkUserNameAvailability]");

        Map<String, Object> respondMap = executeGet("users?username=" + username, null);
        int statusCode  = (int) respondMap.get("code");

        // 404 : username not found => available
        // 200 : username found => unavailable
        return statusCode == 404;
    }
    
    public Map<String, Object> userStatus(String userID) {
        System.out.println("\n[userStatus]");
        
        Map<String, Object> respondMap = executeGet("users/" + userID + "/status", this.getSessionKey());
        Map<String, Object> result  = gson.fromJson(respondMap.get("respond").toString(), HashMap.class);

        Map<String, Object> status = new HashMap<>();
        for (String k: this.userStatusFields) {
            if (result.containsKey(k)) {
                Object v = result.get(k);
                status.put(k, v);
                //System.out.println("key: " + k + ", value: " + v);
            }
        }

        // approved: accountStatus, addressProofStatus, idStatus, kycStatus = 2
        double accountStatus        = (double) result.get("accountStatus");
        double addressProofStatus   = (double) result.get("addressProofStatus");
        double idStatus             = (double) result.get("idStatus");
        double kycStatus            = (double) result.get("kycStatus");

        boolean approved = false;
        if (accountStatus == 2 && addressProofStatus == 2 && idStatus == 2 && kycStatus == 2) {
            approved = true;
        }
        System.out.println("\n[User Status] Approved status: " + approved + ", accountStatus: " + accountStatus
            + ", addressProofStatus: " + addressProofStatus + ", idStatus: " + idStatus
            + ", kycStatus: " + kycStatus);

        return status;
    }

    public boolean updateUser(Map<String, String> args) {
        System.out.println("\n[updateUser]");
        
        String userID = args.get("userID").toString();

        Map<String, Object> params = new HashMap<>();
        for (String k: this.updateUserFields) {
            if (args.containsKey(k)) {
                String v = args.get(k);
                
                if (k.equals("languageID")) {
                    List<String> languages = new ArrayList<>(Arrays.asList("en_US", "zh_CN", "es_ES", "pt_BR", "ja_JP"));

                    // default to en_US, for invalid languages set
                    if (!languages.contains(v)) {
                        v = "en_US";
                    }
                }
                params.put(k, v);
                //System.out.println("key: " + k + ", value: " + v);
            }
        }
        
        Map<String, Object> respondMap = executePut("users/" + userID, params, this.getSessionKey());
        Map<String, Object> result  = gson.fromJson(respondMap.get("respond").toString(), HashMap.class);

        return (int) respondMap.get("code") == 200;
    }
    
    public String forgotPassword(String username) {
        System.out.println("\n[Forgot password]");
        
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);

        Map<String, Object> respondMap = executePost("users/passwords", params, null);
        
        String respond = respondMap.get("respond").toString();
        Map<String, Object> result  = gson.fromJson(respond, HashMap.class);
        
        String passwordResetID = null;
        if ((int) respondMap.get("code") == 200) {
            passwordResetID = result.get("passwordResetID").toString();
            System.out.println("Forgot password, passwordResetID: " + passwordResetID);
        }
        return passwordResetID;
    }
    
    public boolean resetPassword (Map<String, String> args) {
        System.out.println("\n[Reset password]");

        String passwordResetID = args.get("passwordResetID");
        Map<String, Object> params = new HashMap<>();
        for (String k: this.resetPasswordFields) {
            if (args.containsKey(k)) {
                String v = args.get(k);
                params.put(k, v);
                //System.out.println("key: " + k + ", value: " + v);
            }
        }

        Map<String, Object> respondMap = executePut("users/passwords/" + passwordResetID, params, null);
        Map<String, Object> result  = gson.fromJson(respondMap.get("respond").toString(), HashMap.class);

        return (int) respondMap.get("code") == 200;
    }

    /********************
     * API: Session
     ********************/
    
    public Map<String, Object> getSession(String sessionKey) {
        System.out.println("\n[Get Session]");

        Map<String, Object> respondMap = executeGet("userSessions/" + sessionKey, this.getSessionKey());
        Map<String, Object> result  = gson.fromJson(respondMap.get("respond").toString(), HashMap.class);
        
        double loginState = (double) result.get("loginState");
        boolean login = false;
        if (loginState == 1) {
            login = true;
        }
        System.out.println("Get Session, login state: " + login);

        Map<String, Object> session = new HashMap<>();
        for (String k: this.getSessionFields) {
            if (result.containsKey(k)) {
                Object v = result.get(k);
                session.put(k, v);
                //System.out.println("key: " + k + ", value: " + v);
            }
        }
        return session;
    }
    
    public Map<String, Object> createSession(Map<String, String> args) {
        System.out.println("\n[Create Session]");
 
        Map<String, Object> params = new HashMap<>();
        for (String k: this.createSessionFields) {
            if (args.containsKey(k)) {
                String v = args.get(k);
                params.put(k, v);
                //System.out.println("key: " + k + ", value: " + v);
            }
        }

        Map<String, Object> respondMap = executePost("userSessions", params, null);
        String respond = respondMap.get("respond").toString();
        Map<String, Object> result  = gson.fromJson(respond, HashMap.class);

        Map<String, Object> session = new HashMap<>();
        if ((int) respondMap.get("code") == 200) {
            for (String k: this.sessionFields) {
                if (result.containsKey(k)) {
                    Object v = result.get(k);
                    session.put(k, v);
                    //System.out.println("key: " + k + ", value: " + v);
                }
            }
        } else {
            session = this.getError(result);
        }
        return session;
    }

    public boolean cancelSession(String sessionKey) {
        System.out.println("\n[Cancel Session] sessionKey: " + sessionKey);

        Map<String, Object> result = executeDelete("userSessions/" + sessionKey, this.getSessionKey());
        int statusCode = (int) result.get("code");

        if (statusCode == 200) {
            this.user.sessionKey = null;
            return true;
        }
        return false;
    }
    
    /********************
     * API: Instruments
     ********************/
    
    public List<Map<String, Object>> listAllInstruments() {
        System.out.println("\n[listAllInstruments]");

        Map<String, Object> respondMap = executeGet("instruments?tradeStatus=1", this.getSessionKey());
        List<Map<String, Object>> instruments = new ArrayList<>();
        
        if ((int) respondMap.get("code") == 200) {
            List<Map<String, Object>> result = gson.fromJson(respondMap.get("respond").toString(), ArrayList.class);

            for (Map<String, Object> i : result) {
                Map<String, Object> instrument = new HashMap<>();
                for (String k: this.instrumentFields) {
                    if (i.containsKey(k)) {
                        Object v = i.get(k);
                        instrument.put(k, v);
                        //System.out.println("key: " + k + ", value: " + v);
                    }
                }
                instruments.add(instrument);
            }
        }
        return instruments;
    }

    public Map<String, Object> getInstrument (String instrumentID) {
        System.out.println("\n[getInstrument]");

        Map<String, Object> respondMap = executeGet("instruments/" + instrumentID + "?options=F", this.getSessionKey());
        Map<String, Object> instrument = new HashMap<>();
        
        if ((int) respondMap.get("code") == 200) {
            Map<String, Object> result = gson.fromJson(respondMap.get("respond").toString(), HashMap.class);

            for (String k: this.getInstrumentFields) {
                if (result.containsKey(k)) {
                    Object v = result.get(k);
                    instrument.put(k, v);
                    //System.out.println("key: " + k + ", value: " + v);
                }
            }
        }
        return instrument;
    }

    public List<Map<String, Object>> searchInstruments (Map<String, String> args) {
        System.out.println("\n[searchInstruments]");

        // 1) For exact symbol match (only return 1 symbol)
        //      https://api.drivewealth.io/v1/instruments?symbols=SCS
        // 2) search by symbol pattern (return >= 1 symbols)
        //      https://api.drivewealth.io/v1/instruments?symbol=SCS
        
        String params = null;
        final List<String> searchFields = new ArrayList<>(Arrays.asList(
            "symbol",
            "symbols",
            "name",
            "tag"
        ));

        for (String k: searchFields) {
            if (args.containsKey(k)) {
                String kv = k + "=" + args.get(k);
                if (params == null) {
                    params = kv;
                } else {
                    params += "&" + kv;
                }
            }
        }
        //System.out.println(params);

        Map<String, Object> respondMap = executeGet("instruments?" + params, this.getSessionKey());
        List<Map<String, Object>> instruments = new ArrayList<>();
        
        if ((int) respondMap.get("code") == 200) {
            List<Map<String, Object>> result = gson.fromJson(respondMap.get("respond").toString(), ArrayList.class);

            int cnt = 0;
            for (Map<String, Object> i : result) {
                Map<String, Object> instrument = new HashMap<>();
                for (String k: this.searchInstrumentFields) {
                    if (i.containsKey(k)) {
                        Object v = i.get(k);
                        instrument.put(k, v);
                        //System.out.println(cnt + ": key: " + k + ", value: " + v);
                    }
                }
                instruments.add(instrument);
                cnt++;
            }
        }
        return instruments;
    }

    /********************
     * API: Orders
     ********************/

    public Map<String, Object> validateBuy (OrderType orderType, Map<String, Object> args) {
        System.out.println("\n validate Buy Order - " + orderType);
        
        String accountID  = args.get("accountID").toString();
        String symbol     = args.get("symbol").toString();
        double orderQty   = Double.parseDouble(args.get("orderQty").toString());
        double commission = this.user.commissionRate;
        
        // get market price (use "Ask Price" for Buy)
        ArrayList<String> symbols = new ArrayList<>(Arrays.asList(symbol));
        List<Map<String, Object>> dataArray = this.getMarketData(symbols, false);
        double askPrice = (double) dataArray.get(0).get("ask");

        double price = 0;
        Map<String, Object> status = new HashMap<>();
        
        if (orderType == OrderType.MARKET) {
            price = askPrice;
        } else if (orderType == OrderType.STOP) {
            price = Double.parseDouble(args.get("price").toString());

            // Check price >= ask price + 0.05
            if (price < (askPrice + 0.05)) {
                String error = "Stop price must be >= ask price + 0.05, Stop price: " + price + ", Ask Price: " + askPrice;
                System.out.println(error);
                
                status.put("error", error);
                status.put("status", false);
                
                return status;
            }
        } else if (orderType == OrderType.LIMIT) {
            // BUY will execute in the market when the market ask price is at or below the order limit price
            // If the price entered is above the current ask price, order will be immediately executed

            // SELL will execute in the market when the market bid price is at or above the order limit price
            // If the price entered is below the current ask price, order will be immediately executed
            price = Double.parseDouble(args.get("limitPrice").toString());
        }
        
        // get balance
        double balance = this.accountBlotter(this.user.userID, accountID).getTradingBalance();

        // check for insufficient balance
        double amount = orderQty * price + commission;

        System.out.println("Check balance: amount: " + amount + ", balance: " + balance
                + ", market price (Ask): " + price + ", Qty: " + orderQty
                + ", commission: " + commission);

        if (balance < amount) {
            String error = "Insufficient balance";
            System.out.println(error);

            status.put("status", false);
            status.put("error", error);
        } else {
            status.put("status", true);
        }
        
        return status;
    }

    public Map<String, Object> validateSell (OrderType orderType, Map<String, Object> args) {
        System.out.println("\n validate Sell order - " + orderType);
        
        String accountID    = args.get("accountID").toString();
        String symbol       = args.get("symbol").toString();
        String instrumentID = args.get("instrumentID").toString();
        double orderQty     = Double.parseDouble(args.get("orderQty").toString());
        
        Map<String, Object> status = new HashMap<>();
        
        if (orderType == OrderType.STOP) {
            // get market price (use "Bid Price" for Sell)
            ArrayList<String> symbols = new ArrayList<>(Arrays.asList(symbol));
            List<Map<String, Object>> dataArray = this.getMarketData(symbols, false);
            double bidPrice = (double) dataArray.get(0).get("bid");
            
            double price = Double.parseDouble(args.get("price").toString());

            // Check Stop Price <= Bid Price - 0.05
            if (price > (bidPrice - 0.05)) {
                String error = "Stop price must be <= Bid Price - 0.05, Stop price: " + price + ", Bid Price: " + bidPrice;
                System.out.println(error);
                
                status.put("error", error);
                status.put("status", false);
                
                return status;
            }
        }
        
        // get available trading Qty
        double availQty = this.accountBlotter(this.user.userID, accountID).getAvailableTradingQty(instrumentID);

        System.out.println("availableForTradingQty: " + availQty + ", orderQty: " + orderQty);

        // check insufficient Qty for sell
        if (availQty < orderQty) {
            String error = "Insufficient Qty for sell";
            System.out.println(error);
            
            status.put("status", false);
            status.put("error", error);
            
            return status;
        }
        
        status.put("status", true);
        return status;
    }
    
    public static enum OrderSide {
        BUY("B", "buy"),
        SELL("S", "sell");
        
        private final String value;
        private final String name;

        private OrderSide(String value, String name) {
           this.value = value;
           this.name = name;
        }
        
        public String getValue () {
            return this.value;
        }
        
        public String getName () {
            return this.name;
        }
    }
    
    public static enum OrderType {
        MARKET(1, "market"),
        LIMIT(2, "limit"),
        STOP(3, "stop");
        
        private final int value;
        private final String name;

        private OrderType(int value, String name) {
           this.value = value;
           this.name = name;
        }
        
        public int getValue () {
            return this.value;
        }
        
        public String getName () {
            return this.name;
        }
    }
    
    public Map<String, Object> createOrder (OrderSide orderSide, OrderType orderType, Map<String, Object> args) {
        System.out.println("\n[create order]: " + orderSide + ", " + orderType);

        Map<String, Object> params = new HashMap<>();
        for (String k: this.createOrderFields) {
            if (args.containsKey(k)) {
                Object v = args.get(k);
                params.put(k, v);
                //System.out.println("key: " + k + ", value: " + v);
            }
        }
        
        Map <String, Object> validate;
        if (orderSide == OrderSide.BUY) {
            validate = this.validateBuy(orderType, params);
        } else {
            validate = this.validateSell(orderType, params);
        }
        
        // validation error
        if ( Boolean.parseBoolean(validate.get("status").toString()) == false) {
            return validate;
        }

        // Note For LIMIT order:
        //      BUY will execute in the market when the market ask price is at or below the order limit price
        //      If the price entered is above the current ask price, order will be immediately executed

        //      SELL will execute in the market when the market bid price is at or above the order limit price
        //      If the price entered is below the current ask price, order will be immediately executed
        
        params.put("ordType", orderType.getValue());
        params.put("side", orderSide.getValue());

        // create order
        Map<String, Object> respondMap = executePost("orders", params, this.getSessionKey());
        String respond = respondMap.get("respond").toString();
        Map<String, Object> result = gson.fromJson(respond, HashMap.class);

        Map<String, Object> order = new HashMap<>();
        for (String k: this.orderFields) {
            if (result.containsKey(k)) {
                Object v = result.get(k);
                order.put(k, v);
                System.out.println("key: " + k + ", value: " + v);
            }
        }

        return order;
    }
    
    public boolean cancelOrder (String orderID) {
        System.out.println("\n[Cancel order]: " + orderID);

        Map<String, Object> result = executeDelete("orders/" + orderID, this.getSessionKey());
        int statusCode = (int) result.get("code");

        return statusCode == 200;
    }

    public static enum OrderStatus {
        ACCEPTED,
        FILLED,
        PARTIALFILLED,
        CANCELLED,
        REJECTED,
        // something went wrong, eg: failed to call create order, validation error, etc
        ERROR;
    }
    
    public Map<String, Object> orderStatus (String orderID) {
        System.out.println("\n[Order Status]");

        Map<String, Object> respondMap = executeGet("orders/" + orderID, this.getSessionKey());
        Map<String, Object> result = gson.fromJson(respondMap.get("respond").toString(), HashMap.class);
        Map<String, Object> status = new HashMap<>();

        for (String k: this.orderStatusFields) {
            if (result.containsKey(k)) {
                Object v = result.get(k);
                status.put(k, v);
                System.out.println("key: " + k + ", value: " + v);
            }
        }
        
        double cumQty      = (double) status.get("cumQty");
        double leavesQty   = (double) status.get("leavesQty");
        double orderQty    = (double) status.get("orderQty");
        String execType    = status.get("execType").toString();
        String ordStatus   = status.get("ordStatus").toString();
        
        OrderStatus orderStatus = OrderStatus.ERROR;
        // accepted
        if (    orderQty == leavesQty
                && execType.equals("0")
                && ordStatus.equals("0")
        ) {
            orderStatus = OrderStatus.ACCEPTED;
            System.out.println("Order accepted: " + orderID);
        }
        // filled
        else if (  orderQty == cumQty
                && execType.equals("2")
                && ordStatus.equals("2")
        ) {
            orderStatus = OrderStatus.FILLED;
            System.out.println("Order filled: " + orderID);
        }
        // partially filled
        else if (  orderQty > cumQty
                && execType.equals("1")
                && ordStatus.equals("1")
        ) {
            orderStatus = OrderStatus.PARTIALFILLED;
            System.out.println("Order partially filled: " + orderID);
        }
        // Cancelled
        else if (  orderQty == leavesQty
                && execType.equals("4")
                && ordStatus.equals("4")
        ) {
            orderStatus = OrderStatus.CANCELLED;
            System.out.println("Order cancelled: " + orderID);
        }
        // Rejected
        else if (  leavesQty == 0
                && execType.equals("8")
                && ordStatus.equals("8")
        ) {
            orderStatus = OrderStatus.REJECTED;
            System.out.println("Order cancelled: " + orderID + ", reason: "
                    + status.get("ordRejReason").toString());
        }
        status.put("ordStatus", orderStatus);
        
        return status;
    }
    
    /************************
     * Market Data
     ************************/
    
    public List<Map<String, Object>> getMarketData (ArrayList<String> args, boolean lastTradeOnly) {
        System.out.println("\n[Get Market Data]");

        String symbols = null;
        for (int i = 0; i < args.size(); i++) {
	    String symbol = args.get(i);
            
            if (i == 0) {
                symbols = symbol;
            } else {
                symbols = symbols + "," + symbol;
            }
	    System.out.println(i + ": symbol: " + symbol);
	}
        System.out.println("symbols: " + symbols);
        
        String url = "quotes?symbols=" + symbols;
        // get last Traded price only, no bid & ask return
        if (lastTradeOnly == true) {
            url = url + "&lastTrade=true";
        }
        
        Map<String, Object> respondMap = executeGet(url, this.getSessionKey());
        List<Map<String, Object>> marketData = new ArrayList<>();

        if ((int) respondMap.get("code") == 200) {
            List<Map<String, Object>> result = gson.fromJson(respondMap.get("respond").toString(), ArrayList.class);

            for (Map<String, Object> i : result) {
                Map<String, Object> data = new HashMap<>();
                for (String k: this.marketDataFields) {
                    if (i.containsKey(k)) {
                        Object v = i.get(k);
                        data.put(k, v);
                        //System.out.println("key: " + k + ", value: " + v);
                    }
                }
                marketData.add(data);
            }
        }
        return marketData;
    }
    
    /********************
     * API: Settings
     ********************/

    public Map<String, Object> getSetting (String key) {
        System.out.println("\n[Get Setting]");

        Map<String, Object> respondMap = executeGet("users/" + this.user.userID + "/settings/" + key, this.getSessionKey());
        Map<String, Object> result = gson.fromJson(respondMap.get("respond").toString(), HashMap.class);
        Map<String, Object> setting = new HashMap<>();

        for (String k: this.settingFields) {
            if (result.containsKey(k)) {
                Object v = result.get(k);
                setting.put(k, v);
                //System.out.println("key: " + k + ", value: " + v);
            }
        }
        return setting;
    }
    
    public List<Map<String, Object>> listAllSettings () {
        System.out.println("\n[List all Settings]");

        Map<String, Object> respondMap = executeGet("users/" + this.user.userID + "/settings", this.getSessionKey());
        List<Map<String, Object>> result = gson.fromJson(respondMap.get("respond").toString(), ArrayList.class);
        List<Map<String, Object>> settings = new ArrayList<>();
        
        for (Map<String, Object> a : result) {
            Map<String, Object> setting = new HashMap<>();
            for (String k: this.settingFields) {
                if (a.containsKey(k)) {
                    Object v = a.get(k);
                    setting.put(k, v);
                    //System.out.println("key: " + k + ", value: " + v);
                }
            }
            settings.add(setting);
        }
        return settings;
    }

    public Map<String, Object> createSetting (Map<String, String> args) {
        System.out.println("\n[Create Setting]");

        Map<String, Object> params = new HashMap<>();
        params.put("userID", this.user.userID);
        params.put("key", args.get("key"));
        params.put("value", args.get("value"));
        
        Map<String, Object> respondMap = executePost("users/" + this.user.userID + "/settings", params, this.getSessionKey());
        String respond = respondMap.get("respond").toString();
        Map<String, Object> result = gson.fromJson(respond, HashMap.class);

        Map<String, Object> setting = new HashMap<>();
            
        for (String k: this.settingFields) {
            if (result.containsKey(k)) {
                Object v = result.get(k);
                setting.put(k, v);
                //System.out.println("key: " + k + ", value: " + v);
            }
        }
        return setting;
    }
    
    public boolean deleteSetting (String key) {
        System.out.println("\n[Delete Setting]");

        Map<String, Object> result = executeDelete("users/" + this.user.userID + "/settings/" + key, this.getSessionKey());
        int statusCode = (int) result.get("code");

        return statusCode == 200;
    }
    
    /********************
     * API: Charts
     ********************/

    public static enum ChartCompression {
        DAILY(0),
        ONE_MINUTE(1),
        TWO_MINUTE(2),
        THREE_MINUTE(3),
        FIVE_MINUTE(4),
        TEN_MINUTE(5),
        FIFTEEN_MINUTE(6),
        TWENTY_MINUTE(7),
        THIRTY_MINUTE(8),
        ONE_HOUR(9),
        TWO_HOUR(102),
        FOUR_HOUR(14),
        EIGHT_HOUR(108),
        WEEKLY(10),
        MONTHLY(11),
        YEARLY(12);
        
        private final int value;

        private ChartCompression(int value) {
           this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }

    public List<String[]> getCharts (ChartCompression chartCompression, Map<String, Object> args) {
        System.out.println("\n[get Charts]");

        // required fields: instrumentID, compression
        // if tradingDays set, dateStart & dateEnd are ignored
        
        int compression = chartCompression.getValue();
        String instrumentID = args.get("instrumentID").toString();
        
        String url = "bars?instrumentID=" + instrumentID + "&compression=" + compression;

        if (args.containsKey("dateStart") && args.containsKey("dateEnd")) {
            String dateStart = args.get("dateStart").toString();
            String dateEnd = args.get("dateEnd").toString();
            url = url + "&dateStart=" + dateStart + "&dateEnd=" + dateEnd;
        } else if (args.containsKey("tradingDays")) {
            url = url + "&tradingDays=" + args.get("tradingDays").toString();
        }
        System.out.println("\n url: " + url);
        
        Map<String, Object> respondMap = executeGet(url, this.getSessionKey());
        Map<String, Object> result = gson.fromJson(respondMap.get("respond").toString(), HashMap.class);

        List<String[]> data = new ArrayList<String[]>();
        
        if (   result.get("instrumentID").toString().equals(instrumentID)
            && (int) result.get("compression") == compression)
        {
            // Format: date_time, open, high, low, close, volume
            // 2016-01-11T14:49:00Z,7.34,7.34,7.34,7.34,100|2016-01-11T15:05:00Z,7.33,7.33,7.33,7.33,100
            String line = result.get("data").toString();
            System.out.println("data: " + line);

            String[] items = line.split("\\|");
            
            for (int i=0; i<items.length; i++) {
                String[] ohlc = items[i].split(",");
                data.add(ohlc);
            }
        }
        return data;
    }
    
    /********************
     * API: Reports
     ********************/

    // Reports TODO: Referral Summary
    
    public static enum ReportName {
        FIN_TRANS("FinTrans"),
        ORDER_TRANS("OrderTrans"),
        POSITION_RESTING_ORDER("PositionRestingOrder"),
        INSTRUMENT("Instrument"),
        REFERRAL_SUMMARY_PERFORMANCE("ReferralSummaryPerformance");

        private final String value;

        private ReportName(String value) {
           this.value = value;
        }
        
        public String getValue () {
            return this.value;
        }
    }

    /*
        "transactionReport" covers 3 types:

        1) Financial Transaction
            This report allows users to generate all of their financial transactions for their specified account.
        2) Order Transaction
            This report allows users to generate order related transactions for the specified account number
            over the specified date interval. These transactions can be also filtered by symbol if desired.
        3) Open Positions and Resting Orders
            Provides the user their current open positions and resting orders.
    */
    
    public List<Map<String, Object>> transactionReport (ReportName reportName, Map<String, String> args) {
        System.out.println("\n[Transaction Report] - " + reportName);

        String AccountNumber = args.get("AccountNumber");
        String DateStart = "";
        String DateEnd = "";
        String symbol = "";
        List<String> txnFields;

        switch (reportName) {
            case FIN_TRANS:
                DateStart = "&DateStart=" + args.get("DateStart");
                DateEnd = "&DateEnd=" + args.get("DateEnd");

                txnFields = this.financialTxnFields;
                break;
            case ORDER_TRANS:
                DateStart = "&DateStart=" + args.get("DateStart");
                DateEnd = "&DateEnd=" + args.get("DateEnd");

                // for Order Transaction, "symbol" is optional
                if (args.containsKey("symbol")) {
                    symbol = "&symbol=" + args.get("symbol");
                }

                txnFields = this.orderTxnFields;
                break;
            case POSITION_RESTING_ORDER:
                txnFields = this.openPosFields;
                break;
            default:
                System.out.println("Unsupported reportType: " + reportName.getValue());
                return null;
        }
        
        String url = "DriveWealth?ReportFormat=JSON&wlpID=DW&LanguageID=en_US"
                + "&ReportName="    + reportName.getValue()
                + "&sessionKey="    + this.getSessionKey()
                + "&AccountNumber=" + AccountNumber
                + DateStart
                + DateEnd
                + symbol;

        System.out.println("URL: " + url);
        
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> respondMap = executePost(url, params, this.getSessionKey(), "reportServer");

        int statusCode = (int) respondMap.get("code");
        if (statusCode != 200) {
            System.out.println("Error status code: " + statusCode);
            return null;
        }

        String respond = respondMap.get("respond").toString();
        Map<String, Object> result;
        try {
            result = gson.fromJson(respond, HashMap.class);
        } catch(com.google.gson.JsonSyntaxException ex) { 
            System.out.println("Error respond: " + respond);
            return null;
        }
        
        String accountNo = result.get("accountNo").toString();
        String accountID = result.get("accountID").toString();
        String dateRange = null;
        String accountType = null;
        
        if (result.containsKey("accountType")) {
            accountType = result.get("accountType").toString();
        }
        if (result.containsKey("dateRange")) {
            dateRange = result.get("dateRange").toString();
        }
        
        System.out.println("accountNo: " + accountNo + ", accountID: " + accountID
                + ", accountType: " + accountType + ", dateRange: " + dateRange);


        List<Map<String, Object>> resultTxn;
        if (reportName == ReportName.POSITION_RESTING_ORDER) {
            resultTxn = (ArrayList) result.get("positions");
        } else {
            resultTxn = (ArrayList) result.get("transaction");
        }

        // convert List node from LinkedTreeMap -> HashMap
        List<Map<String, Object>> txns = new ArrayList<>();
        int cnt = 0;
        for (Map<String, Object> a : resultTxn) {
            System.out.println("\n\n txn [" + cnt++ + "]\n\n");

            Map<String, Object> txn = new HashMap<>();
            for (String k: txnFields) {
                if (a.containsKey(k)) {
                    Object v = a.get(k);
                    txn.put(k, v);
                    System.out.println("key: " + k + ", value: " + v);
                }
            }
            txns.add(txn);
        }
        return txns;
    }

    // View all instruments available on the DriveWealth platform
    public List<Map<String, Object>> stocksReport (Map<String, String> args) {
        System.out.println("\n[Stocks, ETFs and ADRs Report]");

        String url = "DriveWealth?ReportFormat=JSON&wlpID=DW&LanguageID=en_US"
                + "&ReportName="    + ReportName.INSTRUMENT.getValue()
                + "&sessionKey="    + this.getSessionKey()
                + "&AccountNumber=" + args.get("AccountNumber")
                + "&DateStart="     + args.get("DateStart")
                + "&DateEnd="       + args.get("DateEnd")
                // All: -1, Inactive: 0, Active: 1
                + "&TradeStatus=-1"
                // All: -1, Stocks: 6, ETFs: 7
                + "&InstrumentType=-1";

        System.out.println("URL: " + url);

        Map<String, Object> params = new HashMap<>();
        Map<String, Object> respondMap = executePost(url, params, this.getSessionKey(), "reportServer");

        int statusCode = (int) respondMap.get("code");
        if (statusCode != 200) {
            System.out.println("Error status code: " + statusCode);
            return null;
        }

        String respond = respondMap.get("respond").toString();
        List<Map<String, Object>> result;
        try {
            result = (ArrayList) gson.fromJson(respond, HashMap.class).get("instruments");
        } catch(com.google.gson.JsonSyntaxException ex) { 
            System.out.println("Error respond: " + respond);
            return null;
        }

        List<Map<String, Object>> stocks = new ArrayList<>();
        int cnt = 0;
        for (Map<String, Object> a : result) {
            System.out.println("\n\n stock [" + cnt++ + "]\n\n");

            Map<String, Object> stock = new HashMap<>();
            for (String k: this.stockFields) {
                if (a.containsKey(k)) {
                    Object v = a.get(k);
                    stock.put(k, v);
                    System.out.println("key: " + k + ", value: " + v);
                }
            }
            
            // convert instrument from LinkedTreeMap -> HashMap
            Map<String, Object> ins = (Map<String, Object>) stock.get("instrument");
            Map<String, Object> instrument = new HashMap<>();

            for (String k: this.stockInstrumentFields) {
                if (ins.containsKey(k)) {
                    Object v = ins.get(k);
                    instrument.put(k, v);
                    System.out.println("key: " + k + ", value: " + v);
                }
            }
            stock.put("instrument", instrument);
            stocks.add(stock);
        }
        return stocks;
    }

    /********************
     * API: Statements
     ********************/

    public List<Map<String, Object>> listStatements (String type, Map<String, String> args) {
        switch (type) {
            // List trade confirms
            case "trade":
                type = "01";
                break;
            // List statements
            case "statement":
                type = "02";
                break;
            // List 1099-B Documents
            case "doc":
                type = "03";
                break;
            default:
                System.out.println("Unknown listStatements - type: " + type);
                return null;
        }

        String url = "statements?accountID=" + args.get("accountID")
                + "&startDate=" + args.get("startDate")
                + "&endDate="   + args.get("endDate")
                + "&type="      + type;

        System.out.println("URL: " + url);

        Map<String, Object> respondMap = executeGet(url, this.getSessionKey());
        List<Map<String, Object>> result = gson.fromJson(respondMap.get("respond").toString(), ArrayList.class);
        List<Map<String, Object>> statements = new ArrayList<>();
        
        for (Map<String, Object> a : result) {
            Map<String, Object> statement = new HashMap<>();
            for (String k: this.listStatementFields) {
                if (a.containsKey(k)) {
                    Object v = a.get(k);
                    statement.put(k, v);
                    System.out.println("key: " + k + ", value: " + v);
                }
            }
            statements.add(statement);
        }
        return statements;
    }
    
    public Map<String, Object> getStatement (String accountID, String fileKey) {
        String url = "statements/" + accountID + "/" + fileKey;
        System.out.println("URL: " + url);

        Map<String, Object> respondMap = executeGet(url, this.getSessionKey());
        Map<String, Object> result = gson.fromJson(respondMap.get("respond").toString(), HashMap.class);

        Map<String, Object> statement = new HashMap<>();
        for (String k: this.getStatementFields) {
            if (result.containsKey(k)) {
                Object v = result.get(k);
                statement.put(k, v);
                System.out.println("key: " + k + ", value: " + v);
            }
        }                
        return statement;
    }
    
    
    
    /************************
     * API Utility Functions
     ************************/
    
    public String getSessionKey() {
        if (this.user.sessionKey == null) {
            Map<String, Object> result = login(this.user.username, this.user.password);
            if (result.containsKey("code") && result.containsKey("message")) {
                return null;
            }
        }
        return this.user.sessionKey;
    }
    
    public void setUser (Map<String, Object> params) {
        this.user = new User(params.get("username").toString(), params.get("password").toString());
    }
    
    /************************
     * Http Helper functions
     ************************/

    private static HttpMethod setJsonHeader (HttpMethod httpMethod, String sessionKey) {
        httpMethod.addRequestHeader("Accept", "application/json");
        httpMethod.addRequestHeader("Content-Type", "application/json");

        if (sessionKey != null) {
            httpMethod.addRequestHeader("x-mysolomeo-session-key", sessionKey);
        }
        return httpMethod;
    }

    private static Map<String, Object> executeHttpCall (HttpMethod httpMethod) {
        HttpClient httpClient = new HttpClient();
        int statusCode = 0;
        try {
            statusCode = httpClient.executeMethod(httpMethod);
            System.out.println("status code: " + statusCode);
        } catch (IOException ex) {
            Logger.getLogger(DriveWealthAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String respond = null;
        try {
            StringBuilder resultStr = new StringBuilder();
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(httpMethod.getResponseBodyAsStream()))) {
                String line;
                while ((line = rd.readLine()) != null) {
                    resultStr.append(line);
                }
            }
            respond = resultStr.toString();
            httpMethod.releaseConnection();
        } catch (IOException ex) {
            Logger.getLogger(DriveWealthAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", statusCode);
        result.put("respond", respond);
        
        System.out.println("respond: " + respond);
        
        return result;
    }

    public static Map<String, Object> executeGet(String url, String sessionKey) {
        GetMethod getMethod = new GetMethod(hostURL + url);
        getMethod = (GetMethod) setJsonHeader(getMethod, sessionKey);
        return executeHttpCall(getMethod);
    }

    public static Map<String, Object> executePost(String url, Map<String, Object> params, String sessionKey, String server) {
        Gson gson = new Gson();
        String paramsJson = gson.toJson(params);

        StringRequestEntity requestEntity = null;
        try {
            requestEntity = new StringRequestEntity(
                    paramsJson,
                    "application/json",
                    "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DriveWealthAPI.class.getName()).log(Level.SEVERE, null, ex);
        }

        String host = null;
        if (server.equals("apiServer")) {
            host = hostURL;
        } else if (server.equals("reportServer")) {
            host = reportURL;
        } else {
            System.out.println("[executePost] unknown hostType: " + server);
            return null;
        }
        
        PostMethod postMethod = new PostMethod(host + url);
        postMethod.setRequestEntity(requestEntity);

        postMethod = (PostMethod) setJsonHeader(postMethod, sessionKey);
        return executeHttpCall(postMethod);
    }
    
    private static Map<String, Object> executePost(String url, Map<String, Object> params, String sessionKey) {
        return executePost(url, params, sessionKey, "apiServer");
    }
    
    private static Map<String, Object> executePut(String url, Map<String, Object> params, String sessionKey) {
        Gson gson = new Gson();
        String paramsJson = gson.toJson(params);

        StringRequestEntity requestEntity = null;
        try {
            requestEntity = new StringRequestEntity(
                    paramsJson,
                    "application/json",
                    "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DriveWealthAPI.class.getName()).log(Level.SEVERE, null, ex);
        }

        PutMethod putMethod = new PutMethod(hostURL + url);
        putMethod.setRequestEntity(requestEntity);

        putMethod = (PutMethod) setJsonHeader(putMethod, sessionKey);
        return executeHttpCall(putMethod);
    }

    private static Map<String, Object> executeDelete(String url, String sessionKey) {
        DeleteMethod deleteMethod = new DeleteMethod(hostURL + url);
        deleteMethod = (DeleteMethod) setJsonHeader(deleteMethod, sessionKey);
        return executeHttpCall(deleteMethod);
    }
    
    private static Map<String, Object> getError (Map<String, Object> result) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", result.get("code"));
        error.put("message", result.get("message"));
        return error;
    }
    
    /************************
     * inner class, Variables
     ************************/

    public class User {
        public User () {}

        public User (String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String username;
        public String password;
        public String sessionKey;
        public String userID;
        public double commissionRate;
        
        public Account practiceAccount;
        public Account liveAccount;
        public Map<String, Account> accountsMap = new HashMap<>();
    }
    
    public class Account {
        public Account (Map<String, Object> account) {
            this.accountID      = account.get("accountID").toString();
            this.accountNo      = account.get("accountNo").toString();
            this.accountType    = (double) account.get("accountType");
            this.cash           = (double) account.get("cash");
            this.nickname       = account.get("nickname").toString();
        }

        public String accountID;
        public String accountNo;
        public double accountType;
        public double cash;
        public String nickname;
    }
    
    private final Gson gson = new Gson();
    public static String hostURL = "https://api.drivewealth.io/v1/";
    public static String reportURL = "http://reports.drivewealth.io/";
    
    public User user;
}
