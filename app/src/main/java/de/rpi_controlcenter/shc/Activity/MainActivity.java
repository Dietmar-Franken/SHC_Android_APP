package de.rpi_controlcenter.shc.Activity;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import de.rpi_controlcenter.shc.Fragment.RoomListFragment;
import de.rpi_controlcenter.shc.Fragment.RoomViewFragment;
import de.rpi_controlcenter.shc.Interface.BoundetShcService;
import de.rpi_controlcenter.shc.R;
import de.rpi_controlcenter.shc.Service.SHCConnectorService;

public class MainActivity extends AppCompatActivity {

    private RoomListFragment roomListFragment = null;

    private RoomViewFragment roomViewFragment = null;

    private static boolean useTabletView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

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

                    //Titel der Action Bar setzen
                    getSupportActionBar().setTitle(" " + roomName);
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
            if(isUseTabletView()) {

                roomViewFragment.updateRoomData(true);
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

    public static boolean isUseTabletView() {

        return useTabletView;
    }
}
