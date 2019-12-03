package com.maxeoneo.runtimepermissions;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean mustUseRuntimePermission()
    {
      final int nUsedSdk = Build.VERSION.SDK_INT;
      final int nMinSdkForRuntimePermissions = Build.VERSION_CODES.M;

      return nUsedSdk <= nMinSdkForRuntimePermissions;
    }

    public boolean checkPermission(final String strPermission)
    {
      boolean bPermissionGranted = false;

      if (mustUseRuntimePermission())
      {
        final int nHasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (nHasPermission == PackageManager.PERMISSION_DENIED)
        {
          if (ActivityCompat.shouldShowRequestPermissionRationale(this, strPermission))
          {
            showExplanationDialog("Title", "explanation", strPermission);
          }
          else
          {
            requestPermission(strPermission);
          }
        }
      }
      else
      {
        bPermissionGranted = true;
      }

      return bPermissionGranted;
    }

    private void showExplanationDialog(final String strTitle, final String strExplanation, final String strPermission)
    {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);

      builder.setMessage(strExplanation).setTitle(strTitle);
      builder.setPositiveButton("ok", new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface dialog, int id)
        {
          // User clicked OK button
          requestPermission(strPermission);
        }
      });
    }

    private void requestPermission(final String strPermission)
    {
      ActivityCompat.requestPermissions(this, new String[]{strPermission}, 0);
    }
}


