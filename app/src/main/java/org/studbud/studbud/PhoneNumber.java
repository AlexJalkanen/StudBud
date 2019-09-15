package org.studbud.studbud;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.github.reinaldoarrosi.maskededittext.MaskedEditText;

public class PhoneNumber extends AppCompatActivity {

    private MaskedEditText editTextPhone;
    public String phoneNumber;

    public static Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_number);
        thisActivity = this;


        editTextPhone = findViewById(R.id.phoneNumber);


        findViewById(R.id.getCodeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = editTextPhone.getText(true).toString();

                if (phoneNumber.isEmpty()) {
                    editTextPhone.setError("Phone Number is Required.");
                    editTextPhone.requestFocus();
                    return;
                }

                if (phoneNumber.length() < 10 || phoneNumber.length() > 10 || !android.text.TextUtils.isDigitsOnly(phoneNumber)) {
                    editTextPhone.setError("Please Enter a Valid Phone Number.");
                    editTextPhone.requestFocus();
                    return;
                }


                phoneNumber = "+1 " + phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, 6) +
                        "-" + phoneNumber.substring(6, phoneNumber.length());

                VerifyCode verifyCode = new VerifyCode();
                //verifyCode.setPhoneNumber(phoneNumber);

                Intent code = new Intent(PhoneNumber.this, VerifyCode.class);
                code.putExtra("phone_number",phoneNumber);
                startActivity(code);
                finish();
            }
        });

    }

    public String getPhone() {
        return phoneNumber;
    }




}
