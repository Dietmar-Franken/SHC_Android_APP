package de.rpi_controlcenter.shc.Fragment.RoomElements;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.rpi_controlcenter.shc.Activity.MainActivity;
import de.rpi_controlcenter.shc.Interface.BoundetShcService;
import de.rpi_controlcenter.shc.R;
import de.rpi_controlcenter.shc.Service.SHCConnectorService;

/**
 * Schaltbares Element mit 2 Buttons
 */
public class DoubleButtonFragment extends Fragment {

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
    public final int OFF_BUTTON = (new Double(Math.random() * 1000)).intValue();
    public final int NAME_TEXT_VIEW = (new Double(Math.random() * 1000)).intValue();
    public final int ICON_IMAGE_VIEW = (new Double(Math.random() * 1000)).intValue();

    private TextView nameView;
    private Button onButton;
    private Button offButton;
    private ImageView iconView;

    public DoubleButtonFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        bindDataService();

        if(getArguments() != null &&getArguments().containsKey("useLargeLayout") && getArguments().getBoolean("useLargeLayout")) {

            return inflater.inflate(R.layout.fragment_double_button_large, container, false);
        }
        return inflater.inflate(R.layout.fragment_double_button_small, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //Elemente laden
        if(getActivity().findViewById(R.id.element_onButton) != null) {

            onButton = (Button) getActivity().findViewById(R.id.element_onButton);
            onButton.setId(ON_BUTTON);
            offButton = (Button) getActivity().findViewById(R.id.element_offButton);
            offButton.setId(OFF_BUTTON);
            nameView = (TextView) getActivity().findViewById(R.id.element_name);
            nameView.setId(NAME_TEXT_VIEW);
            iconView = (ImageView) getActivity().findViewById(R.id.element_icon);
            iconView.setId(ICON_IMAGE_VIEW);
        } else {

            return;
        }

        //Raum Element Daten holen
        Bundle args = getArguments();
        final String id = args.getString("id");
        final String name = args.getString("name");
        final String icon = args.getString("icon");
        final String buttonText = args.getString("buttonText");

        //Icon setzen
        setIcon(icon);

        //Name setzen
        nameView.setText(name);

        //Status setzen
        updateState();

        //Button Text setzen
        if(!buttonText.equals("")) {

            switch(buttonText) {

                case "2":

                    onButton.setText(getActivity().getString(R.string.elements_button_up));
                    offButton.setText(getActivity().getString(R.string.elements_button_down));
                    break;
                case "4":

                    onButton.setText(getActivity().getString(R.string.elements_button_open));
                    offButton.setText(getActivity().getString(R.string.elements_button_close));
                    break;
                case "5":

                    onButton.setText(getActivity().getString(R.string.elements_button_start));
                    offButton.setText(getActivity().getString(R.string.elements_button_stop));
                    break;
                default:

                    onButton.setText(getActivity().getString(R.string.elements_button_on));
                    offButton.setText(getActivity().getString(R.string.elements_button_off));
                    break;
            }
        }

        //Button Click Listener anmelden
        onButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

                if(ready) {
                    dataService.sendOnCommand(id, new SHCConnectorService.CommandExecutedEvent() {

                        @Override
                        public void commandExecuted(String error) {

                            //bereit
                            if (error.equals("")) {

                                //kein Fehler
                                Toast.makeText(v.getContext(), R.string.errors_sendCommand_succsess, Toast.LENGTH_SHORT).show();
                                getArguments().putInt("state", 1);
                                updateState();
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

        offButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

                if(dataService != null) {

                    if(ready) {

                        dataService.sendOffCommand(id, new SHCConnectorService.CommandExecutedEvent() {

                            @Override
                            public void commandExecuted(String error) {

                                //Bereit
                                if (error.equals("")) {

                                    //kein Fehler
                                    Toast.makeText(v.getContext(), R.string.errors_sendCommand_succsess, Toast.LENGTH_SHORT).show();
                                    getArguments().putInt("state", 0);
                                    updateState();
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
            }
        });

        //Bei Script Elmementen Buttons dynamisch ausblenden
        if(args.containsKey("function")) {

            switch(args.getString("function")) {

                case "on":

                    offButton.setVisibility(View.INVISIBLE);
                    break;
                case "off":

                    onButton.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    }

    /**
     * aktualisiert die Statusanzeige des Elements
     */
    public void updateState() {

        Bundle args = getArguments();
        int state = args.getInt("state");

        //Scripte mit nur einem Button ausschliesen
        if(args.containsKey("function") && !args.getString("function").equals("both")) {

            return;
        }

        //Status aktualisieren
        if(state == 1) {

            onButton.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.check, 0, 0, 0);
            offButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {

            onButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            offButton.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.check, 0, 0, 0);
        }
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
