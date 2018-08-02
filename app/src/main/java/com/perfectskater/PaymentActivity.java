package com.perfectskater;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PaymentActivity extends AppCompatActivity {


    EditText cardNumber,expDate,cvv;
    Button saveBut;

    SharedPreferences preff;
    public  static String PAYMENT_PREFF = "payment_preffs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        getSupportActionBar().setTitle("Payment");

        cardNumber = findViewById(R.id.cardNo);
        expDate = findViewById(R.id.expDate);
        cvv = findViewById(R.id.cvv);
        preff = getSharedPreferences(PAYMENT_PREFF,MODE_PRIVATE);

        saveBut = findViewById(R.id.saveData);
        saveBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = preff.edit();
                editor.putString("cardNumber",cardNumber.getText().toString());
                editor.putString("expDate",expDate.getText().toString());
                editor.putString("cvv",cvv.getText().toString());
                editor.commit();
                cardNumber.setText(preff.getString("cardNumber",""));
                expDate.setText(preff.getString("expDate",""));
                cvv.setText(preff.getString("cvv",""));
                DatabaseHelper dop  = new DatabaseHelper(PaymentActivity.this);
                int count =  dop.getCount(DatabaseHelper.PAY_TABLE);
                if (count == 0){
                    dop.putPayment(dop,cardNumber.getText().toString(),cvv.getText().toString(),expDate.getText().toString());
                }


            }
        });

        cardNumber.setText(preff.getString("cardNumber",""));
        expDate.setText(preff.getString("expDate",""));
        cvv.setText(preff.getString("cvv",""));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(PaymentActivity.this,MainActivity.class));
        finish();
    }
}
