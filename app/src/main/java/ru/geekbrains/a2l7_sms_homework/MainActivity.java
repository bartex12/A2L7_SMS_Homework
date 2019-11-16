package ru.geekbrains.a2l7_sms_homework;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
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
    private final int permissionRequestCode = 123;
    int messageId = 0;
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    static final String NOTIFICATION_CHANNEL_ID = "10001";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = new String[]{Manifest.permission.RECEIVE_SMS};
            ActivityCompat.requestPermissions(this, permissions, permissionRequestCode);
        }

        initViews();
        initListAdapter();
        sendMessage();
        setupBroadcastReceiver();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == permissionRequestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Спасибо!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Извините, апп без данного разрешения может работать неправильно",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
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

                    etMessage.setText("");
                }
            }

            private void sendSmsIntent(String phoneNumber, String smsText) {
                //посылаем сообщение sms
                Log.d(TAG, "MainActivity sendMessage smsText = "
                        + smsText +" phoneNumber = " + phoneNumber);
                String toNumberSms="smsto:" + phoneNumber;

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(toNumberSms, null, smsText, null, null);
            }

            private void showToast(int p) {
                Toast.makeText(MainActivity.this,
                        getResources().getString(p),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Минимальные проверки
                if (intent != null && intent.getAction() != null &&
                        ACTION.compareToIgnoreCase(intent.getAction()) == 0) {

                    // Получаем сообщения
                    Object[] pdus = (Object[]) Objects.requireNonNull(intent.getExtras()).get("pdus");
                    SmsMessage[] messages = new SmsMessage[Objects.requireNonNull(pdus).length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    String smsFromPhoneNumber = messages[0].getDisplayOriginatingAddress();
                    StringBuilder body = new StringBuilder();
                    for (SmsMessage message : messages) {
                        body.append(message.getMessageBody());
                    }
                    final String bodyText = body.toString();
                    Log.d(TAG, "MainActivity setupBroadcastReceiver bodyText " +bodyText );

                    //вывод уведомления в строке состояния - по большому счёту не нужно-
                    // система отлично справляется и сама
                    makeNote(context, smsFromPhoneNumber, bodyText);
                    Toast.makeText(getApplicationContext(),smsFromPhoneNumber +
                            " -> " + bodyText, Toast.LENGTH_LONG).show();

                    recyclerViewAdapter.addItem(bodyText);
                }
            }
        };
    }

    // Вывод уведомления в строке состояния
    private void makeNote(Context context, String addressFrom, String message) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            showOldNotifications(context, addressFrom, message);
        } else {
            showNewNotifications(context, message);
        }
    }

    @SuppressLint("NewApi")
    private void showNewNotifications(Context context, String message) {

        Intent resultIntent = new Intent(context , MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("Not. title")
                .setContentText(message)
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                "NOTIFICATION_CHANNEL_NAME", importance);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(
                new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        Objects.requireNonNull(mNotificationManager).createNotificationChannel(notificationChannel);
        mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
    }

    private void showOldNotifications(Context context, String addressFrom, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "2")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(String.format("Sms [%s]", addressFrom))
                .setContentText(message);
        Intent resultIntent = new Intent(context, BroadcastReceiver.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Objects.requireNonNull(notificationManager).notify(messageId++, builder.build());
    }
}


