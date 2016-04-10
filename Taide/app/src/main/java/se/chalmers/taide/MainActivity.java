package se.chalmers.taide;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by Matz on 2016-01-25.
 *
 * Main class for the Android app UI.
 */

public class MainActivity extends AppCompatActivity {

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
        showTextEditorView();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:  showSettingsMenu();return true;
            default:                    return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        FragmentManager fm = getFragmentManager();
        if(fm != null){
            if(fm.getBackStackEntryCount()>1) {     //Check that entries exist and ignore first population
                fm.popBackStack();
                return;
            }
        }

        //If not fully handled yet here, call parent
        super.onBackPressed();
    }

    private void showTextEditorView(){
        showFragment(new TextEditorFragment());
    }

    private void showSettingsMenu() {
        showFragment(new SettingsFragment());
    }

    private void showFragment(Fragment fragment){
        FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.mainFragment, fragment);
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        ft.addToBackStack(null).commit();
    }
}
