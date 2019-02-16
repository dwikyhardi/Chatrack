package root.example.com.chatrack.tabFragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import root.example.com.chatrack.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFriends extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.tab_friends, container, false);
    }

}
