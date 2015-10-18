package de.rpi_controlcenter.shc.Fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.rpi_controlcenter.shc.Activity.SettingsActivity;
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

    private Map<String, Fragment> fragmentList = new HashMap<>();

    private Thread syncThread;

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
    }

    /**
     * startet den Syc Thread
     *
     * @param service
     */
    public void startSync(final SHCConnectorService service) {

        final Handler handler = new Handler();
        syncThread = new Thread() {

            @Override
            public void run() {

                while (!isInterrupted()) {

                    //Wartezeit
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                        interrupt();
                    }

                    //Synchronisieren
                    service.sync(getArguments().getInt("roomID"), new SHCConnectorService.SyncCallback() {

                        @Override
                        public void syncFinished(final List<RoomElement> roomElements) {

                            handler.post(new Runnable() {

                                @Override
                                public void run() {

                                    //Fehler beim Syncronisieren
                                    if(roomElements == null) {

                                        Toast.makeText(getActivity(), R.string.errors_syncError, Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    //UI aktualisieren
                                    for (RoomElement re : roomElements) {

                                        //prüfen ob Element vorhanden
                                        if (fragmentList.containsKey(re.getId())) {

                                            Fragment f = fragmentList.get(re.getId());
                                            if (f instanceof DoubleButtonFragment) {

                                                //schaltbares element
                                                Bundle args = f.getArguments();
                                                args.putInt("state", re.getState());
                                                ((DoubleButtonFragment) f).updateState();
                                            } else if (f instanceof SingleButtonFragment) {

                                                //WOL
                                                Bundle args = f.getArguments();
                                                args.putInt("state", re.getState());
                                                ((SingleButtonFragment) f).updateWolState();
                                            } else if (f instanceof InputFragment) {

                                                //Eingang
                                                Bundle args = f.getArguments();
                                                args.putInt("state", re.getState());
                                                ((InputFragment) f).updateData();
                                            } else if (f instanceof AvmMeasuringSocketFragment) {

                                                //AVM Steckdose
                                                Bundle args = f.getArguments();
                                                args.putString("temp", re.getData("temp"));
                                                args.putString("power", re.getData("power"));
                                                args.putString("energy", re.getData("energy"));
                                                ((AvmMeasuringSocketFragment) f).updateData();
                                            } else if (f instanceof BmpFragment) {

                                                //BMP Sensor
                                                Bundle args = f.getArguments();
                                                args.putString("temp", re.getData("temp"));
                                                args.putString("press", re.getData("press"));
                                                args.putString("alti", re.getData("alti"));
                                                ((BmpFragment) f).updateData();
                                            } else if (f instanceof DhtFragment) {

                                                //DHT Sensor
                                                Bundle args = f.getArguments();
                                                args.putString("temp", re.getData("temp"));
                                                args.putString("hum", re.getData("hum"));
                                                ((DhtFragment) f).updateData();
                                            } else if (f instanceof DS18x20Fragment) {

                                                //DS18x20 Sensor
                                                Bundle args = f.getArguments();
                                                args.putString("temp", re.getData("temp"));
                                                args.putString("temp", re.getData("temp"));
                                                ((DS18x20Fragment) f).updateData();
                                            } else if (f instanceof SingleValueFragment) {

                                                //DS18x20 Sensor
                                                Bundle args = f.getArguments();
                                                args.putString("val", re.getData("val"));
                                                ((SingleValueFragment) f).updateData();
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            }
        };
        syncThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        //Synchronisierung anhalten
        if(syncThread != null) {

            syncThread.interrupt();
        }
    }

    /**
     * Fragt die Liste der Elemente eines Raumes ab
     *
     * @param service
     */
    public void updateRoomData(SHCConnectorService service) {

        this.updateRoomData(service, false);
    }

    /**
     * Fragt die Liste der Elemente eines Raumes ab
     *
     * @param service
     * @param force bei True werden immer neue Daten vom Server abgerufen
     */
    public void updateRoomData(SHCConnectorService service, final boolean force) {

        int roomId = getArguments().getInt("roomID");

        SHCConnectorService.RoomElementsCallback callback = new SHCConnectorService.RoomElementsCallback() {

            @Override
            public void roomElementsUpdated(List<RoomElement> roomElements) {

                if(roomElements == null) {

                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.errors_nocConnection_title)
                            .setMessage(R.string.errors_nocConnection_message)
                            .setPositiveButton(R.string.errors_nocConnection_settings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent settings = new Intent(getActivity(), SettingsActivity.class);
                                    startActivity(settings);
                                }
                            })
                            .setNegativeButton(R.string.errors_nocConnection_exit, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    System.exit(0);
                                }
                            })
                            .show();
                    return;
                }

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
                            fragmentList.put(re.getId(), f);
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
                            fragmentList.put(re.getId(), f);
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
                                    fragmentList.put(re.getId(), f);
                                    ft.add(R.id.roomViewLayoutContainer, f);
                                    break;
                                case "4":

                                    //1 Button
                                    args.putString("type", "FritzBoxReboot");

                                    //Fragment erzeugenund einfügen
                                    f = new SingleButtonFragment();
                                    f.setArguments(args);
                                    fragmentList.put(re.getId(), f);
                                    ft.add(R.id.roomViewLayoutContainer, f);
                                case "5":

                                    //1 Button
                                    args.putString("type", "FritzBoxReconnect");

                                    //Fragment erzeugenund einfügen
                                    f = new SingleButtonFragment();
                                    f.setArguments(args);
                                    fragmentList.put(re.getId(), f);
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
                            fragmentList.put(re.getId(), f);
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
                            fragmentList.put(re.getId(), f);
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
                            fragmentList.put(re.getId(), f);
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
                            fragmentList.put(re.getId(), f);
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
                            fragmentList.put(re.getId(), f);
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
                            fragmentList.put(re.getId(), f);
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
                            fragmentList.put(re.getId(), f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                        case "boxStart":

                            //Daten zur übergabe vorbereiten
                            args.putString("name", re.getName());

                            //Fragment erzeugenund einfügen
                            f = new BoxFragment();
                            f.setArguments(args);
                            fragmentList.put(re.getId(), f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                        case "boxEnd":

                            //Fragment erzeugenund einfügen
                            f = new BoxFragment();
                            fragmentList.put(re.getId(), f);
                            ft.add(R.id.roomViewLayoutContainer, f);
                            break;
                    }
                }

                //änderungen commiten
                ft.commit();
            }
        };

        if(service != null) {

            service.updateRoomElementList(roomId, callback, force);
        } else {

            ((BoundetShcService) getActivity()).getShcConnectorService().updateRoomElementList(roomId, callback, force);
        }
    }
}
