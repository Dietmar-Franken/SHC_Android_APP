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
 * Messdaten eines DHT Sensors
 */
public class DhtFragment extends Fragment {

    public final int TEMP_TEXT_VIEW = (new Double(Math.random() * 1000)).intValue();
    public final int HUM_TEXT_VIEW = (new Double(Math.random() * 1000)).intValue();
    public final int NAME_TEXT_VIEW = (new Double(Math.random() * 1000)).intValue();
    public final int ICON_IMAGE_VIEW = (new Double(Math.random() * 1000)).intValue();

    private TextView nameView;
    private TextView tempView;
    private TextView humView;
    private ImageView iconView;

    public DhtFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(getArguments() != null &&getArguments().containsKey("useLargeLayout") && getArguments().getBoolean("useLargeLayout")) {

            return inflater.inflate(R.layout.fragment_dht_large, container, false);
        }
        return inflater.inflate(R.layout.fragment_dht_small, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //Elemente laden
        LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.roomViewLayoutContainer);
        nameView = (TextView) linearLayout.findViewById(R.id.element_name);
        nameView.setId(NAME_TEXT_VIEW);
        tempView = (TextView) linearLayout.findViewById(R.id.element_temp);
        tempView.setId(TEMP_TEXT_VIEW);
        humView = (TextView) linearLayout.findViewById(R.id.element_hum);
        humView.setId(HUM_TEXT_VIEW);
        iconView = (ImageView) linearLayout.findViewById(R.id.element_icon);
        iconView.setId(ICON_IMAGE_VIEW);

        //Raum Element Daten holen
        Bundle args = getArguments();
        final String id = args.getString("id");
        final String name = args.getString("name");
        final String icon = args.getString("icon");

        //Icon setzen
        setIcon(icon);

        //Daten setzen
        nameView.setText(name);
        updateData();
    }

    /**
     * aktualisiert die Werte in der UI
     */
    public void updateData() {

        Bundle args = getArguments();

        final String temp = args.getString("temp");
        final String hum = args.getString("hum");

        tempView.setText(temp);
        humView.setText(hum);
    }

    protected void setIcon(String icon) {

        //Icon setzen
        switch(icon) {

            case "shc-icon-lamp":

                iconView.setImageResource(R.mipmap.lamp);
                break;
            case "shc-icon-flashlight":

                iconView.setImageResource(R.mipmap.flashlight);
                break;
            case "shc-icon-power":

                iconView.setImageResource(R.mipmap.power);
                break;
            case "shc-icon-socket":

                iconView.setImageResource(R.mipmap.socket);
                break;
            case "shc-icon-countdown":

                iconView.setImageResource(R.mipmap.countdown);
                break;
            case "shc-icon-chip":

                iconView.setImageResource(R.mipmap.chip);
                break;
            case "shc-icon-clock":

                iconView.setImageResource(R.mipmap.clock);
                break;
            case "shc-icon-monitor":

                iconView.setImageResource(R.mipmap.monitor);
                break;
            case "shc-icon-nas":

                iconView.setImageResource(R.mipmap.nas);
                break;
            case "shc-icon-printer":

                iconView.setImageResource(R.mipmap.printer);
                break;
            case "shc-icon-tv":

                iconView.setImageResource(R.mipmap.tv);
                break;
            case "shc-icon-waterBoiler":

                iconView.setImageResource(R.mipmap.waterboiler);
                break;
            case "shc-icon-coffee":

                iconView.setImageResource(R.mipmap.coffee);
                break;
            case "shc-icon-rhythmbox":

                iconView.setImageResource(R.mipmap.rhythmbox);
                break;
            case "shc-icon-christmasTree":

                iconView.setImageResource(R.mipmap.christmastree);
                break;
            case "shc-icon-candles":

                iconView.setImageResource(R.mipmap.candles);
                break;
            case "shc-icon-christmasLights":

                iconView.setImageResource(R.mipmap.christmaslights);
                break;
            case "shc-icon-star":

                iconView.setImageResource(R.mipmap.star);
                break;
            case "shc-icon-rollo":

                iconView.setImageResource(R.mipmap.rollo);
                break;
            case "shc-icon-camera":

                iconView.setImageResource(R.mipmap.camera);
                break;
            case "shc-icon-camera2":

                iconView.setImageResource(R.mipmap.camera2);
                break;
            case "shc-icon-ds18x20":

                iconView.setImageResource(R.mipmap.ds18x20);
                break;
            case "shc-icon-dht":

                iconView.setImageResource(R.mipmap.dht);
                break;
            case "shc-icon-bmp":

                iconView.setImageResource(R.mipmap.bmp);
                break;
            case "shc-icon-rain":

                iconView.setImageResource(R.mipmap.rain);
                break;
            case "shc-icon-hygrometer":

                iconView.setImageResource(R.mipmap.hygrometer);
                break;
            case "shc-icon-ldr":

                iconView.setImageResource(R.mipmap.ldr);
                break;
            case "shc-icon-avmPowerSensor":

                iconView.setImageResource(R.mipmap.powersocket);
                break;
        }
    }
}
