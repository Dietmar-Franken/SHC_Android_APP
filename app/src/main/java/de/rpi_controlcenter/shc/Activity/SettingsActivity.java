package de.rpi_controlcenter.shc.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.rpi_controlcenter.shc.Fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
}
