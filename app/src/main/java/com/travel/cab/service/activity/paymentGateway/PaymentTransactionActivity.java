package com.travel.cab.service.activity.paymentGateway;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.travel.cab.service.BuildConfig;
import com.travel.cab.service.JsonParsing;
import com.travel.cab.service.R;
import com.travel.cab.service.network.ApiConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentTransactionActivity extends AppCompatActivity implements PaytmPaymentTransactionCallback {
    private String mid = BuildConfig.MerchantId;
    private static final String TAG = "PaymentTransactionActiv";
    private String orderId;
    private String customerId;
    private String mobile_number = "7042226632";
    private ApiConstant apiConstant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_transaction);
        apiConstant = new ApiConstant();
        getValueFromIntent();

    }

    private void getValueFromIntent() {
        Intent intent = getIntent();
        orderId = intent.getStringExtra("orderNumber");
        customerId = intent.getStringExtra("customerId");
        startTransaction();
    }

    private void startTransaction() {
        sendUserDetailTOServerd dl = new sendUserDetailTOServerd();
        dl.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onTransactionResponse(Bundle inResponse) {
        // response code 01 during successfully transaction RESPCODE=330 STATUS = TXN_FALIURE
        Log.i(TAG, "onTransactionResponse: ");
    }

    @Override
    public void networkNotAvailable() {
        Log.i(TAG, "networkNotAvailable: ");
    }

    @Override
    public void clientAuthenticationFailed(String inErrorMessage) {
        Log.i(TAG, "clientAuthenticationFailed: ");
    }

    @Override
    public void someUIErrorOccurred(String inErrorMessage) {
        Log.i(TAG, "someUIErrorOccurred: ");
    }

    @Override
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
        Log.i(TAG, "onErrorLoadingWebPage: ");
    }

    @Override
    public void onBackPressedCancelTransaction() {
        // when we click back button during transaction
        Log.i(TAG, "onBackPressedCancelTransaction: ");
    }

    @Override
    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
        Log.i(TAG, "onTransactionCancel: ");
    }

    public class sendUserDetailTOServerd extends AsyncTask<ArrayList<String>, Void, String> {
        //private String orderId , mid, custid, amt;
        String url = apiConstant.url;
        String verifyUrl = apiConstant.varifyUrl;
        // "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID"+orderId;
        String CHECKSUMHASH = "";
        private ProgressDialog dialog = new ProgressDialog(PaymentTransactionActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        protected String doInBackground(ArrayList<String>... alldata) {
            JsonParsing jsonParser = new JsonParsing(PaymentTransactionActivity.this);
            String param =
                    "MID=" + mid +
                            "&ORDER_ID=" + orderId +
                            "&CUST_ID=" + customerId +
                            //for testing
                            //"&CHANNEL_ID=WAP&TXN_AMOUNT=1&WEBSITE=WEBSTAGING" +
                            // for production
                            "&CHANNEL_ID=WAP&TXN_AMOUNT=1&WEBSITE=DEFAULT" +
                            // "EMAIL="+ "honeykumar8826@gmail.com"+
                            "&MOBILE_NO=" + mobile_number +
                            "&CALLBACK_URL=" + verifyUrl + "&INDUSTRY_TYPE_ID=Retail";
            JSONObject jsonObject = jsonParser.makeHttpRequest(url, "POST", param);
            // yaha per checksum ke saht order id or status receive hoga..
            // Log.e("CheckSum result >>", jsonObject.toString());
            if (jsonObject != null) {
                Log.e("CheckSum result >>", jsonObject.toString());
                try {
                    CHECKSUMHASH = jsonObject.has("CHECKSUMHASH") ? jsonObject.getString("CHECKSUMHASH") : "";
                    Log.e("CheckSum result >>", CHECKSUMHASH);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return CHECKSUMHASH;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(" setup acc ", "  signup result  " + result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            // PaytmPGService Service = PaytmPGService.getStagingService();
            // when app is ready to publish use production service
            PaytmPGService Service = PaytmPGService.getProductionService();
            // now call paytm service here
            //below parameter map is required to construct PaytmOrder object, Merchant should replace below map values with his own values
            HashMap<String, String> paramMap = new HashMap<>();
            //these are mandatory parameters
            paramMap.put("MID", mid); //MID provided by paytm
            paramMap.put("ORDER_ID", orderId);
            paramMap.put("CUST_ID", customerId);
            paramMap.put("CHANNEL_ID", "WAP");
            paramMap.put("TXN_AMOUNT", "1");
            paramMap.put("WEBSITE", "DEFAULT");
            paramMap.put("CALLBACK_URL", verifyUrl);
            //paramMap.put( "EMAIL" , "honeykumar8826@gmail.com");   // no need
            paramMap.put("MOBILE_NO", mobile_number);  // no need
            paramMap.put("CHECKSUMHASH", CHECKSUMHASH);
            //paramMap.put("PAYMENT_TYPE_ID" ,"CC");    // no need
            paramMap.put("INDUSTRY_TYPE_ID", "Retail");
            PaytmOrder Order = new PaytmOrder(paramMap);
            Log.e("checksum ", "param " + paramMap.toString());
            Service.initialize(Order, null);
            // start payment service call here
            Service.startPaymentTransaction(PaymentTransactionActivity.this, true, true,
                    PaymentTransactionActivity.this);
            Log.i(TAG, "onPostExecute: ");
        }
    }

}