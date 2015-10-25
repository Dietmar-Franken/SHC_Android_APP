package de.rpi_controlcenter.shc.Fragment;

import android.support.v7.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import de.rpi_controlcenter.shc.Activity.MainActivity;
import de.rpi_controlcenter.shc.Activity.SettingsActivity;
import de.rpi_controlcenter.shc.Data.Room;
import de.rpi_controlcenter.shc.Interface.BoundetShcService;
import de.rpi_controlcenter.shc.ListAdapter.RoomListAdapter;
import de.rpi_controlcenter.shc.R;
import de.rpi_controlcenter.shc.Service.SHCConnectorService;

/**
 * Zeigt die Liste mit den Räumen an
 */
public class RoomListFragment extends ListFragment {

    private RoomListAdapter roomListAdapter = null;

    private RoomListItemClicked roomListItemClickListender = null;

    private SHCConnectorService dataService = null;

    /**
     * Verbindung zum SHC Daten Service
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            dataService = ((SHCConnectorService.SHCConnectorBinder) service).getSHCConnectorService();
            updateRoomData(MainActivity.isUseTabletView(), false);

            //Action Bar
            if(MainActivity.isUseTabletView() == false) {

                if (getActivity() != null) {

                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.labelRooms);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            dataService = null;
        }
    };

    public interface RoomListItemClicked {

        /**
         * die Methode wird aufgerufen wenn ein Raum ausgewählt wurde
         *
         * @param roomId Raum ID
         * @param roomName Name des Raumes
         */
        void listItemClicked(int roomId, String roomName);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Room room = (Room) roomListAdapter.getItem(position);
        if(roomListItemClickListender != null) {

            roomListItemClickListender.listItemClicked(room.getId(), room.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        bindDataService();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Fragt die Räume vom SHC Server ab und
     *
     * @param clickFirstElemnet Feuert nach dem laden automatisch ein Ereignis welches den ersten Raum selektiert
     * @param force bei True werden immer neue Daten vom Server abgerufen
     */
    public void updateRoomData(final boolean clickFirstElemnet, final boolean force) {

        //Update der Liste anstoßen
        dataService.updateRoomList(new SHCConnectorService.RoomListCallback() {

            @Override
            public void roomDataUpdated(List<Room> rooms) {

                if (rooms == null) {

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

                if(getActivity() != null) {

                    RoomListFragment.this.roomListAdapter = new RoomListAdapter(getActivity(), rooms);
                    RoomListFragment.this.setListAdapter(RoomListFragment.this.roomListAdapter);

                    if (clickFirstElemnet) {

                        RoomListFragment.this.clickFirstListElement();
                    }
                }
            }
        }, force);
    }

    public void setRoomListItemClickListender(RoomListItemClicked roomListItemClickListender) {

        this.roomListItemClickListender = roomListItemClickListender;
    }

    public void clickFirstListElement() {

        Room room = (Room) roomListAdapter.getItem(0);
        roomListItemClickListender.listItemClicked(room.getId(), room.getName());
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
