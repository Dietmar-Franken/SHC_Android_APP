package de.rpi_controlcenter.shc.Fragment;

import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import de.rpi_controlcenter.shc.Data.Room;
import de.rpi_controlcenter.shc.Interface.BoundetShcService;
import de.rpi_controlcenter.shc.ListAdapter.RoomListAdapter;
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

        this.updateRoomData(false);
    }

    /**
     * Fragt die Räume vom SHC Server ab und
     *
     * @param clickFirstElemnet Feuert nach dem laden automatisch ein Ereignis welches den ersten Raum selektiert
     */
    public void updateRoomData(final boolean clickFirstElemnet) {

        //Update der Liste anstoßen
        ((BoundetShcService) getActivity()).getShcConnectorService().updateRoomList(new SHCConnectorService.RoomListCallback() {

            @Override
            public void roomDataUpdated(List<Room> rooms) {

                RoomListFragment.this.roomListAdapter = new RoomListAdapter(getActivity(), rooms);
                RoomListFragment.this.setListAdapter(RoomListFragment.this.roomListAdapter);

                if(clickFirstElemnet) {

                    RoomListFragment.this.clickFirstListElement();
                }
            }
        });
    }

    public void setRoomListItemClickListender(RoomListItemClicked roomListItemClickListender) {

        this.roomListItemClickListender = roomListItemClickListender;
    }

    public void clickFirstListElement() {

        Room room = (Room) roomListAdapter.getItem(0);
        roomListItemClickListender.listItemClicked(room.getId(), room.getName());
    }
}
