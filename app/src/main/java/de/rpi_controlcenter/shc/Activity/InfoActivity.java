package de.rpi_controlcenter.shc.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import de.rpi_controlcenter.shc.R;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void hyperlinkProjectPage(View v) {

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("http://agent4788.github.io/SHC_Framework/"));
        startActivity(i);
    }

    public void hyperlinkLicencePage(View v) {

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("http://opensource.org/licenses/gpl-license.php"));
        startActivity(i);
    }
}
