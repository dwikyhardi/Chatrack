package root.example.com.chatrack.tabFragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import root.example.com.chatrack.ChatActivity;
import root.example.com.chatrack.MainMenu;
import root.example.com.chatrack.R;
import root.example.com.chatrack.adapter.ExpandableListViewAdapter;
import root.example.com.chatrack.dataModel.getUserData;
import root.example.com.chatrack.dataModel.setGroupData;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFriends extends Fragment {
    private final String TAG = "TabFriends";
    private FloatingActionButton fabFriends, fabAddFriends, fabAddGroup;
    private boolean showFab = false;

    //listView
    private ExpandableListView LvFriends;
    private ExpandableListViewAdapter mExpandableListViewAdapter;
    private ArrayList<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    private ArrayList<String> NamaProfile;
    private ArrayList<String> FotoProfile;
    private ArrayList<String> NamaGroup;
    private ArrayList<String> GroupId;
    private ArrayList<Uri> FotoGroup;
    private ArrayList<String> NamaTeman;
    private ArrayList<Uri> FotoTeman;


    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef,
            dbAmbilDataGroup;
    private FirebaseAuth firebaseAuth;
    private String UserId;

    //Fragment
    MainMenu mainMenu;
    TabProfile profileFragments;
    public boolean profileBuka = false;
    String NamaBundle, UriBundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.tab_friends, container, false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        } else if (showFab && !isVisibleToUser) {
            closeFab();
            Log.d(TAG, "setUserVisibleHint() returned: " + isVisibleToUser);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!getUserVisibleHint()) {
            return;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        dbAmbilDataGroup = mFirebaseDatabase.getReference().child("CHATRACK").child("GROUP");

        dbAmbilDataGroup.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AmbilDataGroup((Map<String, Object>) dataSnapshot.getValue());
                Log.d(TAG, "onDataChange() returned: dbAmbilDataGroup" + dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Authentication
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        UserId = user.getUid();
        Log.d(TAG, "onViewCreated() returned: " + UserId);

        //ArrayList
        NamaProfile = new ArrayList<>();
        FotoProfile = new ArrayList<>();
        NamaGroup = new ArrayList<>();
        GroupId = new ArrayList<>();
        FotoGroup = new ArrayList<>();
        NamaTeman = new ArrayList<>();
        FotoTeman = new ArrayList<>();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ambilProfile(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Fragment
        profileFragments = new TabProfile().newInstance();

        //ExpandableListView
        mainMenu = new MainMenu();
        LvFriends = (ExpandableListView) view.findViewById(R.id.LvFriends);
        prepareData();
        mExpandableListViewAdapter = new ExpandableListViewAdapter(getActivity(), listDataHeader, listDataChild);
        LvFriends.setAdapter(mExpandableListViewAdapter);
        LvFriends.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (listDataHeader.get(groupPosition).equals("Profile")) {

                } else if (listDataHeader.get(groupPosition).equals("Group")) {
                    Intent mIntent = new Intent(getActivity(), ChatActivity.class);
                    String namaGroup = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                    String idGroup = GroupId.get(childPosition);
                    Log.d(TAG, "onChildClick() returned: " + idGroup);
                    mIntent.putExtra("GroupName",namaGroup);
                    mIntent.putExtra("GroupId",idGroup);
                    startActivity(mIntent);
                }
                return false;
            }
        });

        //FloatingActionButton
        fabFriends = (FloatingActionButton) view.findViewById(R.id.fabFriends);
        fabAddFriends = (FloatingActionButton) view.findViewById(R.id.fabAddFriends);
        fabAddGroup = (FloatingActionButton) view.findViewById(R.id.fabAddGroup);
        fabFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!showFab) {
                    openFab();
                } else {
                    closeFab();
                }
            }
        });

        fabAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestNewGroup();
            }
        });

    }

    private void AmbilDataGroup(Map<String, Object> dataSnapshot) {
        final ArrayList<String> NamaGroup = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map namaGroup = (Map) entry.getValue();
            NamaGroup.add((String) namaGroup.get("GroupName"));
        }
        final ArrayList<String> GroupMember = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map groupMember = (Map) entry.getValue();
            GroupMember.add((String) groupMember.get("GroupMember"));
        }
        final ArrayList<String> GroupId = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map groupId = (Map) entry.getValue();
            GroupId.add((String) groupId.get("GroupId"));
        }

        String[] member = GroupMember.toString().split(",");
        String memberId;
        this.NamaGroup.clear();
        if (GroupId != null) {
            int i = 0;
            while (GroupId.size() > i) {
                memberId = GroupMember.get(i).replace("[", "").replace("]", "");
                Log.d(TAG, "AmbilDataGroup() returned: " + GroupMember.get(i).split(","));
                if (memberId.contains(UserId)) {
                    this.NamaGroup.add(NamaGroup.get(i));
                    this.GroupId.add(GroupId.get(i));
                }
                Log.d(TAG, "AmbilDataGroup() returned: int i" + i);
                i++;
            }
        }
    }

    public void lihatProfile() {
        FragmentTransaction mFragmentTransaction = getChildFragmentManager().beginTransaction();
        Bundle mBundle = new Bundle();
        if (!profileBuka) {
            mBundle.clear();
            Log.d(TAG, "lihatProfile() returned: " + UriBundle);
            mBundle.putString("Uri", UriBundle);
            mBundle.putString("Nama", NamaBundle);
            profileFragments.setArguments(mBundle);
            mFragmentTransaction.replace(R.id.ProfileContainer, profileFragments, "Profile");
            mFragmentTransaction.attach(profileFragments);
            mFragmentTransaction.addToBackStack(null);
            mFragmentTransaction.commit();
            Log.d(TAG, "lihatProfile() returned: Mashok");
            profileBuka = true;
            Log.d(TAG, "lihatProfile() returned: " + profileBuka);
        } else {
            profileBuka = true;
        }
    }

    public void removeFragments(Fragment mFragment) {
        FragmentTransaction mFragmentTransaction = getChildFragmentManager().beginTransaction();
        mFragmentTransaction.detach(mFragment).commit();
        profileBuka = false;
        Log.d(TAG, "removeFragments() returned: " + profileBuka);
    }

    private void ambilProfile(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            NamaProfile.clear();
            FotoProfile.clear();
            getUserData mGetUserData = new getUserData();
            try {
                mGetUserData.setLinkFoto(ds.child("USER").child(UserId).getValue(getUserData.class).getLinkFoto());
                mGetUserData.setNama(ds.child("USER").child(UserId).getValue(getUserData.class).getNama());

                /*Uri link = Uri.parse(mGetUserData.getLinkFoto());*/
                String link = mGetUserData.getLinkFoto();
                String nama = mGetUserData.getNama();
                System.out.println(nama);
                NamaProfile.add(nama + "," + link);
                NamaBundle = nama;
                UriBundle = link;
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void prepareData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("Profile");
        listDataHeader.add("Group");
        listDataHeader.add("Friends");


        listDataChild.put(listDataHeader.get(0), NamaProfile); // Header, Child data
        listDataChild.put(listDataHeader.get(1), NamaGroup);
        /*listDataChild.put(listDataHeader.get(2), comingSoon);*/
    }


    private void requestNewGroup() {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Dialog_Alert);
        mBuilder.setTitle("Create Group");
        final EditText namaGroup = new EditText(getActivity());
        namaGroup.setHint("Enter Your Group Name");
        mBuilder.setView(namaGroup);

        mBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String namaGroupStr = namaGroup.getText().toString();
                if (!namaGroupStr.equals("")) {
                    CreateNewGroup(namaGroupStr);
                } else {
                    Toast.makeText(getActivity(), "Enter Your Group Name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mBuilder.show();
    }

    private void CreateNewGroup(String namaGroupStr) {
        String key = myRef.child("CHATRACK").child("GROUP").push().getKey();
        ArrayList<String> GroupMember = new ArrayList<>();
        GroupMember.add(UserId);
        setGroupData mSetGroupData = new setGroupData(key, namaGroupStr, GroupMember.toString(), getCurrentTimeStamp());
        myRef.child("CHATRACK").child("GROUP").child(key).setValue(mSetGroupData);
    }

    public static String getCurrentTimeStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    private void openFab() {
        showFab = true;
        fabAddFriends.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fabAddGroup.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
    }

    private void closeFab() {
        showFab = false;
        fabAddFriends.animate().translationY(0);
        fabAddGroup.animate().translationY(0);
    }


}
