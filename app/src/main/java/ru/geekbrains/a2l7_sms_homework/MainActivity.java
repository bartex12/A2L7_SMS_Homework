package ru.geekbrains.a2l7_sms_homework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "33333";
    private EditText etPhone;
    private EditText etMessage;
    private Button btnSend;
    private RecyclerView recyclerViewVidget;
    private RecyclerViewChatAdapter recyclerViewAdapter = null;

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initListAdapter();
        sendMessage();
        receiveSms();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(
                "ru.geekbrains.a2l7_sms_homework.action.SmsReceiver");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initViews() {
        etPhone = findViewById(R.id.telephoneInput);
        etMessage = findViewById(R.id.smsInput);
        btnSend = findViewById(R.id.sendSms);
        recyclerViewVidget = findViewById(R.id.chatList);
    }

    private void initListAdapter() {
        recyclerViewAdapter = new RecyclerViewChatAdapter();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerViewVidget.setLayoutManager(manager);
        recyclerViewVidget.setAdapter(recyclerViewAdapter);
    }

    private void sendMessage() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = etPhone.getText().toString().replaceAll("-", "");
                String smsText = etMessage.getText().toString();
                if (smsText.trim().isEmpty()) {
                    showToast(R.string.inputMessage);
                }else if((phoneNumber.trim().isEmpty())){
                    showToast(R.string.input);
                }  else{
                    //посылаем интент для отправки sms
                    sendSmsIntent(phoneNumber, smsText);
                    //посылаем широковещательное сообщение с текстом sms
                    sendBroadcastIntent(smsText);

                    etMessage.setText("");
                    //TODO добавление в список сделать через получение широковещательного сообщения
                    // и скорее всего , в другом приложении- чате
                }
            }

            private void sendBroadcastIntent(String smsText) {
                // Отправляем бродкаст
                Intent intentBroadcast = new Intent("ru.geekbrains.a2l7_sms_homework.action.SmsReceiver");
                intentBroadcast.putExtra("sms", smsText);
                intentBroadcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(intentBroadcast);
                Log.d(TAG, "MainActivity sendBroadcastIntent smsText = " + smsText );
            }

            private void sendSmsIntent(String phoneNumber, String smsText) {
                //посылаем сообщение sms
                Log.d(TAG, "MainActivity sendMessage smsText = "
                        + smsText +" phoneNumber = " + phoneNumber);
                String toNumberSms="smsto:" + phoneNumber;
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(toNumberSms));
                intent.putExtra("sms_body", smsText);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            private void showToast(int p) {
                Toast.makeText(MainActivity.this,
                        getResources().getString(p),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void receiveSms() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String answer = intent.getStringExtra("sms");
                Log.d(TAG, "MainActivity receiveSms answer = " + answer );
                recyclerViewAdapter.addItem(answer);
            }
        };
    }
}


