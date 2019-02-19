package root.example.com.chatrack;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import root.example.com.chatrack.adapter.ListViewAdapter;
import root.example.com.chatrack.dataModel.getChatHistory;

public class ChatActivity extends AppCompatActivity {

    private final String TAG = "ChatActivity";
    private TextView groupName, SenderName, TimeStamp, ChatInBuble;
    private EditText TextToSend;
    private ImageView btnSend;
    private Toolbar toolbarChat;
    private String namaGroup, idGroup;
    private ImageView Back;
    private ScrollView Scroll;
    private ListView ChatList;
    private ArrayList<getChatHistory> chatDetails;
    private static ListViewAdapter adapter;

    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference UserRef, GroupNameRef, GroupMessageKeyRef;
    private FirebaseAuth firebaseAuth;
    private String UserId, CurrentName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        namaGroup = getIntent().getStringExtra("GroupName");
        idGroup = getIntent().getStringExtra("GroupId");
        toolbarChat = (Toolbar) findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbarChat);
        initView();

        //Database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        UserRef = mFirebaseDatabase.getReference().child("CHATRACK").child("USER");
        GroupNameRef = mFirebaseDatabase.getReference().child("CHATRACK").child("GROUP").child(idGroup).child("MESSAGE");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        UserId = user.getUid();
        chatDetails = new ArrayList<>();
        chatDetails.clear();

        getUserInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() returned: nyampe onStart");
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded() returned: nyampe onChildAdded");
                if (dataSnapshot.exists()) {
                    DisplayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    DisplayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getUserInfo() {
        UserRef.child(UserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    CurrentName = dataSnapshot.child("Nama").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initView() {

        /*footerView = ((LayoutInflater) ChatActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_chat, null, false);*/
        btnSend = (ImageView) findViewById(R.id.btnSend);
        TextToSend = (EditText) findViewById(R.id.TextToSend);
        SenderName = (TextView) findViewById(R.id.SenderName);
        TimeStamp = (TextView) findViewById(R.id.TimeStamp);
        ChatInBuble = (TextView) findViewById(R.id.ChatInBuble);
        groupName = (TextView) toolbarChat.findViewById(R.id.toolbar_title);
        ChatList = (ListView) findViewById(R.id.ListChat);
        Back = (ImageView) toolbarChat.findViewById(R.id.Back);
        groupName.setText(namaGroup);


        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatActivity.this, MainMenu.class));
                finish();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();

                TextToSend.setText("");
            }
        });
    }

    private void sendMessage() {
        String Message = TextToSend.getText().toString();
        String MessageKey = GroupNameRef.push().getKey();
        if (TextUtils.isEmpty(Message)) {

        } else {
            Calendar mCalendarDate = Calendar.getInstance();
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = mSimpleDateFormat.format(mCalendarDate.getTime());

            Calendar mCalendarTime = Calendar.getInstance();
            SimpleDateFormat mSimpleTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = mSimpleTimeFormat.format(mCalendarTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(MessageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("UserName", CurrentName);
            messageInfoMap.put("UserId", UserId);
            messageInfoMap.put("Message", Message);
            messageInfoMap.put("Date", currentDate);
            messageInfoMap.put("Time", currentTime);

            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ChatActivity.this, MainMenu.class));
        finish();
        super.onBackPressed();
    }

    private void DisplayMessage(DataSnapshot dataSnapshot) {
        Iterator mIterator = dataSnapshot.getChildren().iterator();
        while (mIterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot) mIterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) mIterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) mIterator.next()).getValue();
            String UserId = (String) ((DataSnapshot) mIterator.next()).getValue();
            String chatUserName = (String) ((DataSnapshot) mIterator.next()).getValue();
            
            /*SenderName.setText(chatUserName);
            ChatInBuble.setText(chatMessage);
            TimeStamp.setText(chatTime);*/
            chatDetails.add(new getChatHistory(chatDate, chatMessage, chatTime, UserId, chatUserName));
            Log.d(TAG, "DisplayMessage() returned: " + chatDetails.size());
            Log.d(TAG, "DisplayMessage() returned: nyampe Display");
        }
        adapter = new ListViewAdapter(chatDetails, getApplicationContext());
        ChatList.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.addMember) {

        }
        if (item.getItemId() == R.id.exitGroup) {

        }
        return true;
    }
}
