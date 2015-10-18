package de.rpi_controlcenter.shc.Fragment;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import java.util.List;

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

    /**
     * Fragt die Räume vom SHC Server ab und
     */
    public void updateRoomData() {

        this.updateRoomData(false, false);
    }

    /**
     * Fragt die Räume vom SHC Server ab und
     *
     * @param clickFirstElemnet Feuert nach dem laden automatisch ein Ereignis welches den ersten Raum selektiert
     * @param force bei True werden immer neue Daten vom Server abgerufen
     */
    public void updateRoomData(final boolean clickFirstElemnet, final boolean force) {

        //Update der Liste anstoßen
        ((BoundetShcService) getActivity()).getShcConnectorService().updateRoomList(new SHCConnectorService.RoomListCallback() {

            @Override
            public void roomDataUpdated(List<Room> rooms) {

                if(rooms == null) {

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

                RoomListFragment.this.roomListAdapter = new RoomListAdapter(getActivity(), rooms);
                RoomListFragment.this.setListAdapter(RoomListFragment.this.roomListAdapter);

                if(clickFirstElemnet) {

                    RoomListFragment.this.clickFirstListElement();
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
}
