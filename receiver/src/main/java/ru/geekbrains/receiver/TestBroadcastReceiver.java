package ru.geekbrains.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

// Получим бродкаст и создадим новый
public class TestBroadcastReceiver extends BroadcastReceiver {
    final String sendBroadcastEventKey = "ru.geekbrains.action.TestedReceiver";

    // Получение бродкаста по интент-фильтру, указанному в манифесте
    @Override
    public void onReceive(Context context, Intent intent) {
        String eventData = receiveMessage(context, intent);
        sendAnswer(context, eventData);
    }

    private void sendAnswer(Context context, String eventData) {
        // Сформируем ответ
        Intent intentSend = new Intent(sendBroadcastEventKey);
        intentSend.putExtra("hello", eventData);
        intentSend.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        // И пошлем новый бродкаст
        context.sendBroadcast(intentSend);
    }

    private String receiveMessage(Context context, Intent intent) {
        // Тут прочитаем сообщение из бродкаста  и покажем его
        String eventData = intent.getStringExtra("hello");
        Toast.makeText(context, "TestBroadcastReceiver, data: " + eventData, Toast.LENGTH_SHORT).show();
        return eventData;
    }
}

