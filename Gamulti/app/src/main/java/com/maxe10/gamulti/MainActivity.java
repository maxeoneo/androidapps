package com.maxe10.gamulti;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity
{
  private MultiDrawView multiDrawView;
  private Button buttonReset;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    multiDrawView = (MultiDrawView) findViewById(R.id.multiDrawView);

    buttonReset = (Button) findViewById(R.id.buttonReset);
    buttonReset.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        multiDrawView.reset();
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
}
