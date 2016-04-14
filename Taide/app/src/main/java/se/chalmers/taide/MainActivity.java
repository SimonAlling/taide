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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.dropbox.chooser.android.DbxChooser;

import se.chalmers.taide.model.filesystem.dropbox.DropboxFactory;

/**
 * Created by Matz on 2016-01-25.
 *
 * Main class for the Android app UI.
 */

public class MainActivity extends AppCompatActivity {

    private FileNavigatorDrawer fileNavigator;
    private boolean showSettingsNavigation;

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

        //showTextEditorView();
        fileNavigator = new FileNavigatorDrawer(this, getCodeEditor());
        fileNavigator.initDrawer();
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
        fileNavigator.onActivityResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:  showSettingsMenu();return true;
            case android.R.id.home:     fileNavigator.openDrawer();return true;
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
                    invalidateFileNavigator();
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
        FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.textField, fragment);
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        ft.addToBackStack(null).commit();
        invalidateFileNavigator();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FileNavigatorDrawer.DBX_CHOOSER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                fileNavigator.loadDropboxProject(data);
            } else {
                // Failed or was cancelled by the user.
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void invalidateFileNavigator(){
        if(fileNavigator != null) {
            boolean enabled = (getFragmentManager().findFragmentById(R.id.textField) instanceof TextEditorFragment);
            fileNavigator.setEnabled(enabled);
            if (enabled) {
                fileNavigator.setTextInput(getCodeEditor());
                fileNavigator.updateModelReference();
            }
        }
    }

    private EditText getCodeEditor(){
        Fragment f = getFragmentManager().findFragmentById(R.id.textField);
        if(f instanceof TextEditorFragment){
            return ((TextEditorFragment)f).getCodeEditor();
        }else{
            return null;
        }
    }
}
