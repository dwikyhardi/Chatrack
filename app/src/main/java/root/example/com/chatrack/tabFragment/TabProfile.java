package root.example.com.chatrack.tabFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import de.hdodenhof.circleimageview.CircleImageView;
import root.example.com.chatrack.R;

public class TabProfile extends Fragment {
    private FragmentActivity listener;
    private String nama,uri,uid;
    private Uri uriPhoto;
    private TextView Nama;
    private CircleImageView FotoFragment;
    private ImageView imageQr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab_profile, container, false);
    }

    public static TabProfile newInstance(){return new TabProfile();}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        nama = getArguments().getString("Nama");
        uri = getArguments().getString("Uri");
        uid = getArguments().getString("uid");
        uriPhoto = Uri.parse(uri);
        Nama = (TextView) view.findViewById(R.id.NamaFragment);
        FotoFragment = (CircleImageView) view.findViewById(R.id.FotoFragmentProfile);
        imageQr = (ImageView) view.findViewById(R.id.imageQr);

        Nama.setText(nama);
        Glide.with(getActivity().getApplicationContext()).load(uriPhoto).into(FotoFragment);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(uid, BarcodeFormat.QR_CODE, 200, 200);//200,200 ukuran barcodenya
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageQr.setImageBitmap(bitmap);
        } catch (WriterException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }
}
