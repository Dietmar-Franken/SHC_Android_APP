package de.rpi_controlcenter.shc.Fragment.RoomElements;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.rpi_controlcenter.shc.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class InputFragment extends Fragment {

    public final int STATE_TEXT_VIEW = (new Double(Math.random() * 1000)).intValue();
    public final int NAME_TEXT_VIEW = (new Double(Math.random() * 1000)).intValue();
    public final int ICON_IMAGE_VIEW = (new Double(Math.random() * 1000)).intValue();

    private TextView nameView;
    private TextView stateView;
    private ImageView iconView;

    public InputFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(getArguments() != null && getArguments().containsKey("useLargeLayout") && getArguments().getBoolean("useLargeLayout")) {

            return inflater.inflate(R.layout.fragment_input_large, container, false);
        }
        return inflater.inflate(R.layout.fragment_input_small, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //Elemente laden
        if(nameView == null) {

            LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.roomViewLayoutContainer);
            stateView = (TextView) linearLayout.findViewById(R.id.element_state);
            stateView.setId(STATE_TEXT_VIEW);
            nameView = (TextView) linearLayout.findViewById(R.id.element_name);
            nameView.setId(NAME_TEXT_VIEW);
            iconView = (ImageView) linearLayout.findViewById(R.id.element_icon);
            iconView.setId(ICON_IMAGE_VIEW);
        }

        //Raum Element Daten holen
        Bundle args = getArguments();
        final String id = args.getString("id");
        final String name = args.getString("name");
        final String icon = args.getString("icon");

        nameView.setText(name);
        updateData();
    }

    /**
     * aktualisiert die Werte in der UI
     */
    public void updateData() {

        Bundle args = getArguments();

        final int state = args.getInt("state");

        if(state == 1) {

            stateView.setText(R.string.elements_button_on);
            iconView.setImageResource(R.mipmap.input_state_high);
        } else {

            stateView.setText(R.string.elements_button_off);
            iconView.setImageResource(R.mipmap.input_state_low);
        }
    }
}
