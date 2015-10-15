package de.rpi_controlcenter.shc.Fragment;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import de.rpi_controlcenter.shc.Data.RoomElement;
import de.rpi_controlcenter.shc.Fragment.RoomElements.AvmMeasuringSocketFragment;
import de.rpi_controlcenter.shc.Fragment.RoomElements.BmpFragment;
import de.rpi_controlcenter.shc.Fragment.RoomElements.BoxFragment;
import de.rpi_controlcenter.shc.Fragment.RoomElements.DS18x20Fragment;
import de.rpi_controlcenter.shc.Fragment.RoomElements.DhtFragment;
import de.rpi_controlcenter.shc.Fragment.RoomElements.DoubleButtonFragment;
import de.rpi_controlcenter.shc.Fragment.RoomElements.InputFragment;
import de.rpi_controlcenter.shc.Fragment.RoomElements.SingleButtonFragment;
import de.rpi_controlcenter.shc.Fragment.RoomElements.SingleValueFragment;
import de.rpi_controlcenter.shc.Interface.BoundetShcService;
import de.rpi_controlcenter.shc.R;
import de.rpi_controlcenter.shc.Service.SHCConnectorService;

/**
 * Zeigt einen Raum an
 */
public class RoomViewFragment extends Fragment {

    private LinearLayout roomViewLayout = null;

    private List<Fragment> fragmentList = new ArrayList<>();

    public RoomViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_room_view, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        roomViewLayout = (LinearLayout) getActivity().findViewById(R.id.roomViewLayoutContainer);

        //TODO Sync Task implementieren der die Anzeige jede Sekunde aktualisiert
    }

    /**
     * Fragt die Liste der Elemente eines Raumes ab
     *
     * @param service
     */
    public void updateRoomData(SHCConnectorService service) {

        int roomId = getArguments().getInt("roomID");

        SHCConnectorService.RoomElementsCallback callback = new SHCConnectorService.RoomElementsCallback() {

            @Override
            public void roomElementsUpdated(List<RoomElement> roomElements) {

                //alte Fragments entfernen und Liste leeren
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                roomViewLayout.removeAllViews();
                fragmentList.clear();

                //Fragment Transaktion initialisieren
                ft = getFragmentManager().beginTransaction();

                //Fragmente Erzeugen und der View hinzufügen
                for (RoomElement re : roomElements) {

                    Bundle args = new Bundle();
                    if(getArguments().containsKey("useTabletView") && getArguments().getBoolean("useTabletView")) {

                        args.putBoolean("useLargeLayout", true);
                    }
                    Fragment f = null;
                    switch (re.getType()) {

                        case "Activity":
                        case "AvmSocket":
                        case "Countdown":
                        case "RadioSocket":
                        case "RpiGpioOutput":

                            //Daten zur übergabe vorbereiten
                            args.putString("id", re.getId());
                            args.putString("name", re.getName());
                            args.putString("icon", re.getIcon());
                            args.putInt("state", re.getState());
                            args.putString("buttonText", re.getData("buttonText"));

                            //Fragment erzeugenund einfügen
                            f = new DoubleButtonFragment();
                            f.setArguments(args);
                            fragmentList.add(f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                        case "Script":

                            //Daten zur übergabe vorbereiten
                            args.putString("id", re.getId());
                            args.putString("name", re.getName());
                            args.putString("icon", re.getIcon());
                            args.putInt("state", re.getState());
                            args.putString("buttonText", re.getData("buttonText"));
                            args.putString("function", re.getData("function"));

                            //Fragment erzeugenund einfügen
                            f = new DoubleButtonFragment();
                            f.setArguments(args);
                            fragmentList.add(f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                        case "FritzBox":

                            //Daten zur übergabe vorbereiten
                            String function = re.getData("function");
                            args.putString("id", re.getId());
                            args.putString("name", re.getName());
                            args.putString("icon", re.getIcon());
                            switch (function) {

                                case "1":
                                case "2":
                                case "3":

                                    //2 Buttons
                                    args.putString("buttonText", "1");
                                    args.putInt("state", re.getState());

                                    //Fragment erzeugen und einfügen
                                    f = new DoubleButtonFragment();
                                    f.setArguments(args);
                                    fragmentList.add(f);
                                    ft.add(R.id.roomViewLayoutContainer, f);
                                    break;
                                case "4":

                                    //1 Button
                                    args.putString("type", "FritzBoxReboot");

                                    //Fragment erzeugenund einfügen
                                    f = new SingleButtonFragment();
                                    f.setArguments(args);
                                    fragmentList.add(f);
                                    ft.add(R.id.roomViewLayoutContainer, f);
                                case "5":

                                    //1 Button
                                    args.putString("type", "FritzBoxReconnect");

                                    //Fragment erzeugenund einfügen
                                    f = new SingleButtonFragment();
                                    f.setArguments(args);
                                    fragmentList.add(f);
                                    ft.add(R.id.roomViewLayoutContainer, f);
                                    break;
                            }

                            break;
                        case "Reboot":
                        case "Shutdown":
                        case "WakeOnLan":

                            //Daten zur übergabe vorbereiten
                            args.putString("id", re.getId());
                            args.putString("name", re.getName());
                            args.putString("icon", re.getIcon());
                            args.putString("type", re.getType());
                            args.putInt("state", re.getState());

                            //Fragment erzeugen und einfügen
                            f = new SingleButtonFragment();
                            f.setArguments(args);
                            fragmentList.add(f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                        case "Input":

                            //Daten zur übergabe vorbereiten
                            args.putString("id", re.getId());
                            args.putString("name", re.getName());
                            args.putInt("state", re.getState());

                            //Fragment erzeugen und einfügen
                            f = new InputFragment();
                            f.setArguments(args);
                            fragmentList.add(f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                        case "AvmMeasuringSocket":

                            //Daten zur übergabe vorbereiten
                            args.putString("id", re.getId());
                            args.putString("name", re.getName());
                            args.putString("icon", re.getIcon());
                            args.putString("temp", re.getData("temp"));
                            args.putString("power", re.getData("power"));
                            args.putString("energy", re.getData("energy"));

                            //Fragment erzeugenund einfügen
                            f = new AvmMeasuringSocketFragment();
                            f.setArguments(args);
                            fragmentList.add(f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                        case "BMP":

                            //Daten zur übergabe vorbereiten
                            args.putString("id", re.getId());
                            args.putString("name", re.getName());
                            args.putString("icon", re.getIcon());
                            args.putString("temp", re.getData("temp"));
                            args.putString("press", re.getData("press"));
                            args.putString("alti", re.getData("alti"));

                            //Fragment erzeugenund einfügen
                            f = new BmpFragment();
                            f.setArguments(args);
                            fragmentList.add(f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                        case "DHT":

                            //Daten zur übergabe vorbereiten
                            args.putString("id", re.getId());
                            args.putString("name", re.getName());
                            args.putString("icon", re.getIcon());
                            args.putString("temp", re.getData("temp"));
                            args.putString("hum", re.getData("hum"));

                            //Fragment erzeugenund einfügen
                            f = new DhtFragment();
                            f.setArguments(args);
                            fragmentList.add(f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                        case "DS18x20":

                            //Daten zur übergabe vorbereiten
                            args.putString("id", re.getId());
                            args.putString("name", re.getName());
                            args.putString("icon", re.getIcon());
                            args.putString("temp", re.getData("temp"));

                            //Fragment erzeugenund einfügen
                            f = new DS18x20Fragment();
                            f.setArguments(args);
                            fragmentList.add(f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                        case "Hygrometer":
                        case "LDR":
                        case "RainSensor":

                            //Daten zur übergabe vorbereiten
                            args.putString("id", re.getId());
                            args.putString("name", re.getName());
                            args.putString("icon", re.getIcon());
                            args.putString("type", re.getType());
                            args.putString("val", re.getData("val"));

                            //Fragment erzeugenund einfügen
                            f = new SingleValueFragment();
                            f.setArguments(args);
                            fragmentList.add(f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                        case "boxStart":

                            //Daten zur übergabe vorbereiten
                            args.putString("name", re.getName());

                            //Fragment erzeugenund einfügen
                            f = new BoxFragment();
                            f.setArguments(args);
                            fragmentList.add(f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                        case "boxEnd":

                            //Fragment erzeugenund einfügen
                            f = new BoxFragment();
                            fragmentList.add(f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                    }
                }

                //änderungen commiten
                ft.commit();
            }
        };

        if(service != null) {

            service.updateRoomElementList(roomId, callback);
        } else {

            ((BoundetShcService) getActivity()).getShcConnectorService().updateRoomElementList(roomId, callback);
        }
    }
}
