package de.rpi_controlcenter.shc.Fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.rpi_controlcenter.shc.R;

/**
 * Created by oliver on 02.10.15.
 */
public class SettingsFragment extends PreferenceFragment{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Einstellungs XML laden
        addPreferencesFromResource(R.xml.settings);
    }


}
