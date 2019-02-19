package root.example.com.chatrack;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import root.example.com.chatrack.dataModel.setUserData;

public class LengkapiData extends AppCompatActivity implements View.OnClickListener {

    private EditText EtNama, EtAlamat;
    private TextView TvTanggalLahir;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Spinner SpJenisKelamin;
    private Button BtnSubmit;

    private String Nama;
    private String Alamat;
    private String TanggalLahir;
    private String JenisKelamin;
    private String UserId;
    private String LinkFoto;

    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lengkapi_data);
        EtNama = (EditText) findViewById(R.id.EtNama);
        EtAlamat = (EditText) findViewById(R.id.EtAlamat);
        TvTanggalLahir = (TextView) findViewById(R.id.TvTanggalLahir);
        SpJenisKelamin = (Spinner) findViewById(R.id.SpJenisKelamin);
        BtnSubmit = (Button) findViewById(R.id.BtnSubmit);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        UserId = user.getUid();
        BtnSubmit.setOnClickListener(this);
        LinkFoto = user.getPhotoUrl().toString();

        //Database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        //TanggalLahir
        TvTanggalLahir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        LengkapiData.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = day + "/" + month + "/" + year;
                TvTanggalLahir.setText(date);
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.BtnSubmit:
                AddUser();
                break;
        }
    }

    private void AddUser() {
        Nama = EtNama.getText().toString().trim();
        Alamat = EtAlamat.getText().toString().trim();
        TanggalLahir = TvTanggalLahir.getText().toString().trim();
        JenisKelamin = SpJenisKelamin.getSelectedItem().toString().trim();
        setUserData mSetUserData = new setUserData(UserId, Nama, Alamat, TanggalLahir, "0", "0", JenisKelamin, LinkFoto);
        myRef.child("CHATRACK").child("USER").child(UserId).setValue(mSetUserData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(LengkapiData.this, "Data Berhasil Ditambahkan", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LengkapiData.this, MainMenu.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LengkapiData.this, "Data Gagal Ditambahkan", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
