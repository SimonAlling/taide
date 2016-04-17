package se.chalmers.taide;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.lang.reflect.Field;

/**
 * Created by Matz on 2016-01-25.
 *
 * Main class for the Android app UI.
 */

public class MainActivity extends AppCompatActivity {

    private FileNavigatorDrawer fileNavigator;
    private boolean showSettingsNavigation;
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

        showTextEditorView();
        this.savedInstanceState = savedInstanceState;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem settingsMenuItem = menu.findItem(R.id.action_settings);
        settingsMenuItem.setVisible(showSettingsNavigation);
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
        fileNavigator.saveInstanceState(saveInstanceState);
        this.savedInstanceState = saveInstanceState;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        fileNavigator.onOptionsItemSelected(item);

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
                if(!showSettingsNavigation){
                    showSettingsNavigation = true;
                    updateFileNavigator(savedInstanceState);
                    invalidateOptionsMenu();
                }
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
        showSettingsNavigation = !(fragment instanceof SettingsFragment);
        FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.mainFragment, fragment);
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        ft.addToBackStack(null).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FileNavigatorDrawer.DBX_CHOOSER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                fileNavigator.loadDropboxProject(data);
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateFileNavigator(Bundle instanceState){
        Log.d("Main", "Update file nav");
        View v = findViewById(R.id.codeEditor);
        if(v != null) {
            if (fileNavigator == null) {
                Log.d("Main", "Reupdating");
                fileNavigator = new FileNavigatorDrawer(this, (EditText) v);
                fileNavigator.initDrawer();
            } else {
                Log.d("Main", "Just calling onResume");
                fileNavigator.setTextInput((EditText)v);
                fileNavigator.onActivityResume(instanceState);
            }
        }else{
            Log.d("Main", "Nope, not found");
        }
    }
}
