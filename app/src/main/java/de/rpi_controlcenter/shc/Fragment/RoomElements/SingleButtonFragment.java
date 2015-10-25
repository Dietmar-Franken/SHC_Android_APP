package de.rpi_controlcenter.shc.Fragment.RoomElements;


import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.rpi_controlcenter.shc.Interface.BoundetShcService;
import de.rpi_controlcenter.shc.R;
import de.rpi_controlcenter.shc.Service.SHCConnectorService;

/**
 * A simple {@link Fragment} subclass.
 */
public class SingleButtonFragment extends Fragment {

    private SHCConnectorService dataService = null;

    private boolean ready = false;

    /**
     * Verbindung zum SHC Daten Service
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            dataService = ((SHCConnectorService.SHCConnectorBinder) service).getSHCConnectorService();
            ready = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            ready = false;
            dataService = null;
        }
    };

    public final int ON_BUTTON = (new Double(Math.random() * 1000)).intValue();
    public final int NAME_TEXT_VIEW = (new Double(Math.random() * 1000)).intValue();
    public final int ICON_IMAGE_VIEW = (new Double(Math.random() * 1000)).intValue();

    private TextView nameView;
    private Button onButton;
    private ImageView iconView;

    public SingleButtonFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        bindDataService();

        // Inflate the layout for this fragment
        if(getArguments() != null && getArguments().containsKey("useLargeLayout") && getArguments().getBoolean("useLargeLayout")) {

            return inflater.inflate(R.layout.fragment_single_button_large, container, false);
        }
        return inflater.inflate(R.layout.fragment_single_button_small, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //Elemente laden
        if(nameView == null) {

            LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.roomViewLayoutContainer);
            onButton = (Button) linearLayout.findViewById(R.id.element_onButton);
            onButton.setId(ON_BUTTON);
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

        //Button Text setzen
        nameView.setText(name);
        switch(args.getString("type")) {

            case "Reboot":
            case "FritzBoxReboot":

                onButton.setText(R.string.elements_button_reboot);
                iconView.setImageResource(R.mipmap.reboot);
                break;
            case "Shutdown":

                onButton.setText(R.string.elements_button_shutdown);
                iconView.setImageResource(R.mipmap.shutdown);
                break;
            case "WakeOnLan":

                onButton.setText(R.string.elements_button_start);
                updateWolState();
                break;
            case "FritzBoxReconnect":

                onButton.setText(R.string.elements_button_reconnect);
                iconView.setImageResource(R.mipmap.reconnect);
                break;
        }

        //Button Click Listener anmelden
        onButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

                if(ready) {

                    //bereit zum senden
                    dataService.sendOnCommand(id, new SHCConnectorService.CommandExecutedEvent() {

                        @Override
                        public void commandExecuted(String error) {

                            if (error.equals("")) {

                                //kein Fehler
                                Toast.makeText(v.getContext(), R.string.errors_sendCommand_succsess, Toast.LENGTH_SHORT).show();
                            } else {

                                Toast.makeText(v.getContext(), R.string.errors_sendCommand_error + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {

                    //noch nicht bereit zum senden
                    Toast.makeText(v.getContext(), R.string.errors_notRady, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void updateWolState() {

        Bundle args = getArguments();
        if(args.getString("type").equals("WakeOnLan") && args.containsKey("state") && args.getInt("state") == 1) {

            iconView.setImageResource(R.mipmap.wol_state_online);
        } else if(args.getString("type").equals("WakeOnLan")) {
            iconView.setImageResource(R.mipmap.wol_state_offline);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        unbindDataService();
    }

    /**
     * SHC Daten Service Binden
     */
    private void bindDataService() {

        if(dataService == null) {

            Intent i = new Intent(getActivity(), SHCConnectorService.class);
            getActivity().bindService(i, connection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * SHC Daten Service trennen
     */
    private void unbindDataService() {

        if(dataService != null) {

            //Service zum stoppen auffordern
            dataService.stopSelf();

            //Bindung lösen
            getActivity().unbindService(connection);

            //Objekt löschen
            dataService = null;
        }
    }
}
