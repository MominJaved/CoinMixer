package com.coinmixer;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.DoubleStream;

/**
 * Poller polls a deposit address, transfers coins to HOUSE if deposit received and
 * schedules random transactions at random times for user addresses mapped to that
 * deposit address.
 */

public class Poller {

    private static final String API_ROOT = "http://jobcoin.gemini.com/exemption/api";
    private static final String ADDRESS_INFO_API = API_ROOT + "/addresses/";
    private static final String TRANSACTION_API = API_ROOT + "/transactions";
    private static final long MAX_DELAY = 60000;

    private Map map;
    private AppLogger appLogger;

    // asynchronous HTTP client for Poller's API calls
    private CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();


    // Poller takes in the central map, appLogger and starts an HTTP client
    public Poller (Map map, AppLogger appLogger) {
        this.map = map;
        this.appLogger = appLogger;
        httpclient.start();
    }


    // poll the given address to check for deposit and transfer it to HOUSE if received
    public void poll(String depositAddr) {

        HttpGet pollRequest = new HttpGet(ADDRESS_INFO_API + depositAddr);
        httpclient.execute(pollRequest, new FutureCallback<HttpResponse>(){

            // when async pollRequest completes
            public void completed(HttpResponse response) {
                try {
                    String pollResponseString = EntityUtils.toString(response.getEntity());
                    JSONObject pollResponse = new JSONObject(pollResponseString);
                    double balance = pollResponse.getDouble("balance");

                    // if deposit received, transfer it to HOUSE account
                    if (balance > 0) {
                        appLogger.log(Level.INFO, "DEPOSIT RECEIVED [depositAddress: " + depositAddr +
                                "] [amount: " + balance + "]");
                        transferToHouse(depositAddr, Double.toString(balance));
                    }
                }
                catch (java.io.IOException e) {
                    System.out.println(e);
                }
            }

            public void failed(Exception e) {
                appLogger.log(Level.SEVERE, "poll request failed [depositAddress: " + depositAddr + "]\n" + e);
            }

            public void cancelled() {
                appLogger.log(Level.WARNING, "poll request cancelled [depositAddress: " + depositAddr + "]");
            }

        });
    }


    // transfer amount from given deposit address to HOUSE
    public void transferToHouse(String depositAdr, String amount) throws UnsupportedEncodingException {
        HttpPost toHouse = getTransferRequest(depositAdr, "HOUSE", amount);
        httpclient.execute(toHouse, new FutureCallback<HttpResponse>(){

            // if successfully transferred to HOUSE, disburse funds to user accounts
            public void completed(HttpResponse response){
                appLogger.log(Level.INFO, "TRANSFERRED TO HOUSE [depositAddress: " + depositAdr +
                        "] [amount: " + amount + "]");
                disburseFunds(depositAdr, amount);
            }

            public void failed(Exception e) {
                appLogger.log(Level.SEVERE, "house transfer failed [depositAddress: " + depositAdr + "]\n" + e);
            }

            public void cancelled() {
                appLogger.log(Level.WARNING, "house transfer cancelled [depositAddress: " + depositAdr + "]");
            }
        });
    }


    // schedule randomly timed transactions for user addresses mapped to given deposit address
    public void disburseFunds(String depositAdd, String amount) {
        synchronized (map) {
            if (map.containsKey(depositAdd)) {
                // get user addresses array
                String userAddressStr = map.get(depositAdd).toString();
                String[] userAddrsArray = userAddressStr.trim().split(",");

                long numUserAddresses = userAddrsArray.length;
                double totalAmount = Double.parseDouble(amount);

                // create array of random doubles and sort
                Random rand = new Random();
                DoubleStream randomDstream = rand.doubles(numUserAddresses-1, 0.0, totalAmount);
                double[] randomDarray = randomDstream.toArray();
                Arrays.sort(randomDarray);

                double disburseAmnt = 0.0;

                // use random doubles as partitions for the random amount
                for(int i=0; i<numUserAddresses; i++) {
                    if (i==0) {
                        disburseAmnt = randomDarray[i];
                    }
                    else if (i == numUserAddresses - 1) {
                        disburseAmnt = totalAmount - randomDarray[i-1];
                    }
                    else {
                        disburseAmnt = randomDarray[i] - randomDarray[i-1];
                    }

                    final String userAddress = userAddrsArray[i];
                    final double finalDisburseAmnt = disburseAmnt;

                    final String counter = (i+1) + "/" + numUserAddresses;

                    // schedule each transaction with the background Timer thread
                    TimerTask disburseTask = new TimerTask() {
                        public void run() {
                            try {
                                HttpPost toUserAddr = getTransferRequest("HOUSE", userAddress, String.valueOf(finalDisburseAmnt));
                                httpclient.execute(toUserAddr, new FutureCallback<HttpResponse>(){

                                    public void completed(HttpResponse response) {
                                        appLogger.log(Level.INFO, "DISBURSEMENT COMPLETED [" + counter + "] [depositAddress: " +
                                                depositAdd + "] [userAddress: " + userAddress + "] [amount: " + finalDisburseAmnt + "]" );
                                    }

                                    public void failed(Exception e) {
                                        appLogger.log(Level.INFO, "disbursement failed [depositAddress: " +
                                                depositAdd + "] [userAddress: " + userAddress + "]" );
                                    }

                                    public void cancelled() {
                                        appLogger.log(Level.INFO, "disbursement cancelled [depositAddress: " +
                                                depositAdd + "] [userAddress: " + userAddress + "]" );
                                    }
                                });
                            }
                            catch (UnsupportedEncodingException e){
                                System.out.println(e);
                            }
                        }
                    };

                    Timer timer = new Timer();
                    timer.schedule(disburseTask, ThreadLocalRandom.current().nextLong(MAX_DELAY));
                }
                map.remove(depositAdd);
                appLogger.log(Level.INFO, "DISBURSEMENT SCHEDULED [depositAddress: " + depositAdd + "]");
            }
        }
    }


    // transfer request creation helper
    public HttpPost getTransferRequest(String fromAddress, String toAddress, String amount) throws UnsupportedEncodingException {
        HttpPost transferReq = new HttpPost(TRANSACTION_API);
        List<NameValuePair> params = new ArrayList<NameValuePair>(3);
        params.add(new BasicNameValuePair("fromAddress", fromAddress));
        params.add(new BasicNameValuePair("toAddress", toAddress));
        params.add(new BasicNameValuePair("amount", amount));
        transferReq.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        return transferReq;
    }

}
