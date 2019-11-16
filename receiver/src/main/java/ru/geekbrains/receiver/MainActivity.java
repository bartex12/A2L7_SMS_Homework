package ru.geekbrains.receiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewVidget;
    private RecyclerViewChatAdapter recyclerViewAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initListAdapter();
    }

    private void initListAdapter() {
        recyclerViewVidget = findViewById(R.id.chatList);
        recyclerViewAdapter = new RecyclerViewChatAdapter();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerViewVidget.setLayoutManager(manager);
        recyclerViewVidget.setAdapter(recyclerViewAdapter);
    }
}
