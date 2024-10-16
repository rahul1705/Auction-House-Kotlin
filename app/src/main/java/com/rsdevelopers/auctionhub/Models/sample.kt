package com.rsdevelopers.auctionhub.Models

class sample { //    private EditText et_amount;
    //    private Button btn_pay;
    //    private TextView d_amount;
    //    private String samount;
    //    private float amount;
    //    private Users users;
    //    et_amount = findViewById(R.id.et_amount);
    //    btn_pay = findViewById(R.id.btn_pay);
    //    d_amount = findViewById(R.id.d_amount);
    //
    //        Checkout.preload(getApplicationContext());
    //
    //        try {
    //        laodWalletData();
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
    //
    //        btn_pay.setOnClickListener(v -> startPayment());
    //implements PaymentResultListener
    //    out of oncreate
    //private void laodWalletData() {
    //    FirestoreWallet.getWallet(getApplicationContext(), documentSnapshot -> {
    //        if (documentSnapshot.exists()) {
    //            // User data found in Firestore
    //            users = documentSnapshot.toObject(Users.class);
    //            assert users != null;
    //            String userBalance = "Rs. " + users.getBalance();
    //            d_amount.setText(userBalance);
    //        } else {
    //            // User data not found in Firestore
    //            d_amount.setText("0");
    //        }
    //    });
    //}
    //
    //    public void startPayment() {
    //
    //        final Checkout checkout = new Checkout();
    //        checkout.setKeyID("rzp_test_BlXQcyRtrNKqMG");
    //
    //        samount = et_amount.getText().toString();
    //
    //        amount = Float.parseFloat(samount) * 100;
    //
    //        //        checkout.setImage(R.drawable.logo);
    //
    //        final Activity activity = this;
    //
    //        try {
    //            JSONObject options = new JSONObject();
    //
    //            options.put("name", "Rahul Mandal");
    //            options.put("description", "Add Money to Wallet");
    //            options.put("send_sms_hash", true);
    //            options.put("allow_rotation", true);
    ////            options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
    //            options.put("currency", "INR");
    //            options.put("amount", amount);//pass amount in currency subunits
    //            options.put("prefill.email", "rahul@gmail.com");
    //            options.put("prefill.contact", "9696969696");
    //            JSONObject retryObj = new JSONObject();
    //            retryObj.put("enabled", true);
    //            retryObj.put("max_count", 4);
    //            options.put("retry", retryObj);
    //
    //            checkout.open(activity, options);
    //
    //        } catch (Exception e) {
    //            Toast.makeText(activity, "Error in starting Razorpay Checkout " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    //        }
    //    }
    //
    //
    //    @Override
    //    public void onPaymentSuccess(String s, PaymentData paymentData) {
    //        FirestoreWallet.addWallet(getApplicationContext(), String.valueOf((amount / 100) + Float.parseFloat(users.getBalance())));
    //        et_amount.setText("");
    //        Toast.makeText(this, paymentData.getData().toString(), Toast.LENGTH_SHORT).show();
    //    }
    //
    //    @Override
    //    public void onPaymentError(int i, String s, PaymentData paymentData) {
    //        et_amount.setText("");
    //        Toast.makeText(this, paymentData.getData().toString(), Toast.LENGTH_SHORT).show();
    //    }
}