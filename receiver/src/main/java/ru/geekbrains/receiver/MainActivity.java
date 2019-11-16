package ru.geekbrains.receiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import static ru.geekbrains.receiver.SmsReceiver.NOTIFICATION_CHANNEL_ID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "33333";
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

        initListAdapter();
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

    private void initListAdapter() {
        recyclerViewVidget = findViewById(R.id.chatList);
        recyclerViewAdapter = new RecyclerViewChatAdapter();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerViewVidget.setLayoutManager(manager);
        recyclerViewVidget.setAdapter(recyclerViewAdapter);
    }

    private void setupBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Минимальные проверки
                if (intent != null && intent.getAction() != null &&
                        ACTION.compareToIgnoreCase(intent.getAction()) == 0) {
                    Log.d(TAG, "MainActivity setupBroadcastReceiver");
                    // Получаем сообщения
                    Object[] pdus = (Object[]) intent.getExtras().get("pdus");
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    String smsFromPhone = messages[0].getDisplayOriginatingAddress();
                    StringBuilder body = new StringBuilder();
                    for (SmsMessage message : messages) {
                        body.append(message.getMessageBody());
                    }
                    final String bodyText = body.toString();
                    Log.d(TAG, "MainActivity setupBroadcastReceiver bodyText " +bodyText );
                    makeNote(context, smsFromPhone, bodyText);

                    Toast.makeText(getApplicationContext(),"Текст sms:" +bodyText,
                            Toast.LENGTH_LONG).show();

                    recyclerViewAdapter.addItem(bodyText);

                }
            }
        };
    }

    // Вывод уведомления в строке состояния
    private void makeNote(Context context, String addressFrom, String message) {
        Log.d(TAG, "MainActivity makeNote");
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            showOldNotifications(context, addressFrom, message);
        } else {
            showNewNotifications(context, message);
        }
    }

    @SuppressLint("NewApi")
    private void showNewNotifications(Context context, String message) {
        Log.d(TAG, "MainActivity showNewNotifications");
        /**Creates an explicit intent for an Activity in your app**/
        Intent resultIntent = new Intent(context , MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("New sms")
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
        mNotificationManager.createNotificationChannel(notificationChannel);
        mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
    }

    private void showOldNotifications(Context context, String addressFrom, String message) {
        Log.d(TAG, "MainActivity showOldNotifications");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "2")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(String.format("Sms [%s]", addressFrom))
                .setContentText(message);
        Intent resultIntent = new Intent(context, SmsReceiver.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(messageId++, builder.build());
    }
}