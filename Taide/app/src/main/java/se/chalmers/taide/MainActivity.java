package se.chalmers.taide;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Matz on 2016-01-25.
 *
 * Main class for the Android app UI.
 */

public class MainActivity extends AppCompatActivity {

    private FileNavigatorDrawer fileNavigator;
    private Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Load default preferences if not set
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //Init view
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.colorSecondaryText));
        setSupportActionBar(toolbar);

        this.savedInstanceState = savedInstanceState;
        findViewById(R.id.markup).setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onResume(){
        super.onResume();
        updateFileNavigator(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState){
        super.onSaveInstanceState(saveInstanceState);
        if(fileNavigator != null) {
            fileNavigator.saveInstanceState(saveInstanceState);
        }
        this.savedInstanceState = saveInstanceState;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(fileNavigator != null) {
            fileNavigator.onOptionsItemSelected(item);
        }

        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:  showSettingsMenu();return true;
            default:                    return super.onOptionsItemSelected(item);
        }
    }

    private void showSettingsMenu() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Only handle ourselves if file navigator does not consume event
        if (fileNavigator == null || !fileNavigator.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateFileNavigator(Bundle instanceState) {
        View v = findViewById(R.id.editText);
        if (v != null) {
            if (fileNavigator == null) {
                fileNavigator = new FileNavigatorDrawer(this, (EditText)v);
                fileNavigator.initDrawer();
            } else {
                fileNavigator.setTextInput((EditText) v);
            }
            fileNavigator.onActivityResume(instanceState);
        }
    }
}
