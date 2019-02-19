package root.example.com.chatrack.tabFragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import root.example.com.chatrack.R;

public class TabProfile extends Fragment {
    private FragmentActivity listener;
    private String nama,uri;
    private Uri uriPhoto;
    private TextView Nama;
    private CircleImageView FotoFragment;

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
        uriPhoto = Uri.parse(uri);
        Nama = (TextView) view.findViewById(R.id.NamaFragment);
        FotoFragment = (CircleImageView) view.findViewById(R.id.FotoFragmentProfile);

        Nama.setText(nama);
        Glide.with(getActivity().getApplicationContext()).load(uriPhoto).into(FotoFragment);
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
