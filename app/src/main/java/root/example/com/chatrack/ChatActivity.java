package root.example.com.chatrack;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import root.example.com.chatrack.adapter.ListViewAdapter;
import root.example.com.chatrack.dataModel.User;
import root.example.com.chatrack.dataModel.getChatHistory;
import root.example.com.chatrack.tabFragment.Tracking;

public class ChatActivity extends AppCompatActivity {

    private final String TAG = "ChatActivity";
    private TextView groupName, SenderName, TimeStamp, ChatInBuble;
    private EditText TextToSend;
    private ImageView btnSend;
    private Toolbar toolbarChat;
    private String namaGroup, idGroup, idTeman;
    private String[] memberOld ;
    private ArrayList<String> memberNew;
    private ImageView Back;
    private ScrollView Scroll;
    private ListView ChatList;
    private ArrayList<getChatHistory> chatDetails;
    private static ListViewAdapter adapter;
    private ArrayList<User> mUserList = new ArrayList<>();


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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        initView();

        //Database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        UserRef = mFirebaseDatabase.getReference().child("CHATRACK").child("USER");
        GroupNameRef = mFirebaseDatabase.getReference().child("CHATRACK").child("GROUP").child(idGroup).child("MESSAGE");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        UserId = user.getUid();
        chatDetails = new ArrayList<>();
        memberNew = new ArrayList<>();
        memberNew.clear();
        chatDetails.clear();

        getUserInfo();
        DatabaseReference add = mFirebaseDatabase.getReference().child("CHATRACK").child("GROUP").child(idGroup).child("GroupMember");
        add.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ambilDataMember(dataSnapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ambilDataMember(DataSnapshot dataSnapshot) {
        memberOld = dataSnapshot.getValue().toString()
                .replace("]","")
                .replace("[","")
                .split(",");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() returned: nyampe onStart");
        chatDetails.clear();
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
        groupName.setText(namaGroup);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();

                TextToSend.setText("");
            }
        });
        hideSoftKeyboard();
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

        if (item.getItemId() == R.id.usingBarcode) {
            IntentIntegrator integrator = new IntentIntegrator(ChatActivity.this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("Add Friends");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(false);
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.setBarcodeImageEnabled(false);
            integrator.initiateScan();
        }

        if (item.getItemId() == R.id.usingUserName) {
            Toast.makeText(this, "Mantul", Toast.LENGTH_SHORT).show();
        }

        if (item.getItemId() == R.id.addMember) {

        }
        if (item.getItemId() == R.id.exitGroup) {
            Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show();
        }
        if (item.getItemId() == R.id.Track){
            inflateUserListFragment();
        }
        if (item.getItemId() == android.R.id.home){
            Tracking fragment =
                    (Tracking) getSupportFragmentManager().findFragmentByTag("User List");
            if(fragment != null){
                if(fragment.isVisible()){
                    getSupportFragmentManager().popBackStack();
                    return true;
                }
            }else {
                finish();
            }
        }
        return true;
    }

    private void inflateUserListFragment(){
        hideSoftKeyboard();

        Tracking fragment = Tracking.newInstance();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("intent_user_list", mUserList);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.FrameUser, fragment,"User List");
        transaction.addToBackStack("User List");
        transaction.commit();
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void addMember() {
        memberNew.clear();
        DatabaseReference add = mFirebaseDatabase.getReference().child("CHATRACK").child("GROUP").child(idGroup).child("GroupMember");
        int i=0;
        while (memberOld.length > i){
            memberNew.add(memberOld[i]);
            i++;
        }
        memberNew.add(idTeman);
        add.setValue(memberNew.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ChatActivity.this, "Success Adding Member", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            idTeman = result.getContents();
            addMember();
        }
    }
}
