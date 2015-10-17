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

import de.rpi_controlcenter.shc.Fragment.RoomViewFragment;
import de.rpi_controlcenter.shc.Interface.BoundetShcService;
import de.rpi_controlcenter.shc.R;
import de.rpi_controlcenter.shc.Service.SHCConnectorService;

public class RoomViewAcrivity extends AppCompatActivity implements BoundetShcService {

    private SHCConnectorService dataService = null;

    private RoomViewFragment roomViewFragment = null;

    /**
     * Verbindung zum SHC Daten Service
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            dataService = ((SHCConnectorService.SHCConnectorBinder) service).getSHCConnectorService();
            roomViewFragment.updateRoomData(dataService);
            roomViewFragment.startSync(dataService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            dataService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_view_acrivity);

        bindDataService();

        //Fragment erzeugen
        roomViewFragment = new RoomViewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("roomID", getIntent().getIntExtra("roomID", 0));
        roomViewFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.roomViewPlaceHolder, roomViewFragment).commit();

        getSupportActionBar().setTitle(getIntent().getStringExtra("roomName"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
            roomViewFragment.updateRoomData(dataService);
            return true;
        } else if (id == R.id.action_settings) {

            //Einstelluns Activity öffnen
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();

        bindDataService();
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
