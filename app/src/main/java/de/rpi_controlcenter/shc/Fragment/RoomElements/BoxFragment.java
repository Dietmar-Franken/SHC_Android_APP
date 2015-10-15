package de.rpi_controlcenter.shc.Fragment.RoomElements;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.rpi_controlcenter.shc.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BoxFragment extends Fragment {


    public BoxFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(getArguments() != null &&getArguments().containsKey("useLargeLayout") && getArguments().getBoolean("useLargeLayout") && getArguments() != null && getArguments().containsKey("name")) {

            return inflater.inflate(R.layout.fragment_box_start_large, container, false);
        } else if(getArguments() != null && getArguments().containsKey("name")) {

            return inflater.inflate(R.layout.fragment_box_start, container, false);
        }
        return inflater.inflate(R.layout.fragment_box_end, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(getArguments() != null && getArguments().containsKey("name")) {

            TextView start = ((TextView) getActivity().findViewById(R.id.element_boxStart));
            start.setText(getArguments().getString("name"));
        }
    }
}
