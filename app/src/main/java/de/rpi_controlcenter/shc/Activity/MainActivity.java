package de.rpi_controlcenter.shc.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import de.rpi_controlcenter.shc.Fragment.RoomListFragment;
import de.rpi_controlcenter.shc.Fragment.RoomViewFragment;
import de.rpi_controlcenter.shc.Interface.BoundetShcService;
import de.rpi_controlcenter.shc.R;
import de.rpi_controlcenter.shc.Service.SHCConnectorService;

public class MainActivity extends AppCompatActivity implements BoundetShcService {

    private SHCConnectorService dataService = null;

    private RoomListFragment roomListFragment = null;

    private RoomViewFragment roomViewFragment = null;

    private boolean useTabletView = false;

    /**
     * Verbindung zum SHC Daten Service
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            dataService = ((SHCConnectorService.SHCConnectorBinder) service).getSHCConnectorService();
            roomListFragment.updateRoomData(useTabletView, true);

            //Action Bar
            if(useTabletView == false) {

                getSupportActionBar().setTitle(R.string.labelRooms);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            dataService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindDataService();

        //Prüfen ob Tablet View Aktiv
        if(findViewById(R.id.roomViewPlaceHolderTablet) != null) {

            //Tablet View Aktiv
            useTabletView = true;
        }

        //Fragment einbinden
        roomListFragment = new RoomListFragment();
        roomListFragment.setRoomListItemClickListender(new RoomListFragment.RoomListItemClicked() {

            @Override
            public void listItemClicked(int roomId, String roomName) {

                if(useTabletView) {

                    //Fragment einbinden
                    roomViewFragment = new RoomViewFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("roomID", roomId);
                    bundle.putBoolean("useTabletView", true);
                    roomViewFragment.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.roomViewPlaceHolderTablet, roomViewFragment).commit();

                    //Daten laden
                    roomViewFragment.updateRoomData(dataService);
                    roomViewFragment.startSync(dataService);

                    //Titel der Action Bar setzen
                    getSupportActionBar().setTitle(roomName);
                } else {

                    //Eigene Activity starten
                    Intent i = new Intent(MainActivity.this, RoomViewAcrivity.class);
                    i.putExtra("roomID", roomId);
                    i.putExtra("roomName", roomName);
                    startActivity(i);
                }
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.roomListPlaceHolder, roomListFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Menü öffnen
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_reload) {

            //Raum Liste aktualisieren
            if(useTabletView && dataService != null) {

                roomViewFragment.updateRoomData(dataService, true);
            } else if(useTabletView) {

                bindDataService();
            }
            roomListFragment.updateRoomData(false, true);
            return true;
        } else if (id == R.id.action_settings) {

            //Einstelluns Activity öffnen
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
            return true;
        } else if (id == R.id.action_info) {

            //Einstelluns Activity öffnen
            Intent settings = new Intent(this, InfoActivity.class);
            startActivity(settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

            Intent i = new Intent(this, SHCConnectorService.class);
            bindService(i, connection, Context.BIND_AUTO_CREATE);
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
            unbindService(connection);

            //Objekt löschen
            dataService = null;
        }
    }

    @Override
    public SHCConnectorService getShcConnectorService() {

        return dataService;
    }
}
