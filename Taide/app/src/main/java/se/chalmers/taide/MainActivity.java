package se.chalmers.taide;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.dropbox.chooser.android.DbxChooser;

import se.chalmers.taide.model.EditorModel;
import se.chalmers.taide.model.ModelFactory;
import se.chalmers.taide.model.ProjectType;
import se.chalmers.taide.model.filesystem.CodeFile;
import se.chalmers.taide.model.filesystem.FileSystem;
import se.chalmers.taide.model.filesystem.dropbox.DropboxFactory;
import se.chalmers.taide.model.languages.LanguageFactory;
import se.chalmers.taide.util.Clipboard;

/**
 * Created by Matz on 2016-01-25.
 *
 * Main class for the Android app UI.
 */


// massa problem har jag haft trot löst de men måste testa igen med git

public class MainActivity extends AppCompatActivity {

    private static final int DBX_CHOOSER_REQUEST = 0;

    private EditText codeEditor;
    private EditorModel model;

    private Dialog currentDialog;
    private boolean authenticatingDropbox;
    private DbxChooser dropboxChooser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retrieve the code editor text field
        codeEditor = (EditText)findViewById(R.id.editText);
        // Init sample code
        codeEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    invalidateOptionsMenu();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Bind code editor to the model. Use Java as language
        model = ModelFactory.createEditorModel(MainActivity.this, ModelFactory.editTextToTextSource(codeEditor), LanguageFactory.JAVA);
        model.createProject("TestProject", ProjectType.LOCAL_SYSTEM, null);
        Log.d("MainActivity", "Started model with language: " + model.getLanguage().getName());

        initDrawer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_paste).setEnabled(Clipboard.hasPasteContent(getApplicationContext()));
        menu.findItem(R.id.action_undo).setEnabled(true);//model.peekUndo() != null);
        menu.findItem(R.id.action_redo).setEnabled(true);//model.peekRedo() != null);
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(authenticatingDropbox){
            DropboxFactory.authenticationDone(this);
            showDropboxChooser();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:  break;
            case R.id.action_copy:      Clipboard.copyToClipboard(getApplicationContext(), codeEditor); break;
            case R.id.action_cut:       Clipboard.cutToClipboard(getApplicationContext(), codeEditor); break;
            case R.id.action_paste:     Clipboard.pasteFromClipboard(getApplicationContext(), codeEditor); break;
            case R.id.action_undo:      if(model.peekUndo()==null)Toast.makeText(getApplicationContext(), "No undo was found", Toast.LENGTH_LONG);
                                        else{model.undo();invalidateOptionsMenu();}break;
            case R.id.action_redo:      if(model.peekRedo()==null)Toast.makeText(getApplicationContext(), "No redo was found", Toast.LENGTH_LONG);
                                        else{model.redo();invalidateOptionsMenu();}break;
            case android.R.id.home:     openDrawer();return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DBX_CHOOSER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                DbxChooser.Result result = new DbxChooser.Result(data);
                loadDropboxProject(result);
            } else {
                // Failed or was cancelled by the user.
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initDrawer(){
        final Activity thisActivity = this;
        ((DrawerLayout) findViewById(R.id.drawer_layout)).setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(thisActivity.getCurrentFocus().getWindowToken(), 0);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(codeEditor, 0);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        ActionBar b = getSupportActionBar();
        if(b != null) {
            b.setDisplayHomeAsUpEnabled(true);
            b.setHomeAsUpIndicator(R.mipmap.ic_drawer);
        }

        ((ListView)findViewById(R.id.fileList)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    model.gotoFolder(null);
                    updateDrawer();
                } else {
                    CodeFile cf = model.getFilesInCurrentDir().get(position - 1);
                    if (cf.isDirectory()) {
                        model.gotoFolder(cf);
                        updateDrawer();
                    } else {
                        codeEditor.setVisibility(View.VISIBLE);
                        model.openFile(cf);
                        closeDrawer();
                    }
                }
            }
        });
        ((ListView)findViewById(R.id.projectActionMenus)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0: showTextDialog(R.string.add_new_project_description, new OnDialogActivation() {
                        @Override
                        public void onActivation(String textInput) {
                            model.createProject(textInput, ProjectType.LOCAL_SYSTEM, new FileSystem.OnProjectLoadListener() {
                                @Override
                                public void projectLoaded(boolean success) {
                                    updateDrawer();
                                }
                            });
                        }
                    });
                            break;
                    case 1: showChoiceDialog(R.string.load_project_description, model.getAvailableProjects(), new OnDialogActivation() {
                        @Override
                        public void onActivation(String textInput) {
                            model.setProject(textInput, ProjectType.LOCAL_SYSTEM, new FileSystem.OnProjectLoadListener() {
                                @Override
                                public void projectLoaded(boolean success) {
                                    updateDrawer();
                                }
                            });
                        }
                    });
                            break;
                    case 2: showDropboxChooser();break;
                    case 3: showTextDialog(R.string.add_new_file_description, new OnDialogActivation() {
                                @Override
                                public void onActivation(String textInput) {
                                    codeEditor.setVisibility(View.VISIBLE);
                                    model.openFile(model.createFile(textInput, false));
                                    updateDrawer();
                                    closeDrawer();
                                }
                            });
                            break;
                    case 4: showTextDialog(R.string.add_new_folder_description, new OnDialogActivation() {
                        @Override
                        public void onActivation(String textInput) {
                            model.createFile(textInput, true);
                            updateDrawer();
                        }
                    });
                            break;
                }
            }
        });


        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                if(menu.getViewType() == 0) {
                    SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                    openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                    openItem.setWidth((int) getResources().getDimension(R.dimen.drawer_action_button_width));
                    openItem.setTitle(R.string.rename_file);
                    openItem.setTitleSize(14);
                    openItem.setTitleColor(Color.WHITE);
                    menu.addMenuItem(openItem);

                    SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                    deleteItem.setBackground(new ColorDrawable(getResources().getColor(R.color.drawer_action_delete)));
                    deleteItem.setWidth((int) getResources().getDimension(R.dimen.drawer_action_button_width));
                    deleteItem.setIcon(R.mipmap.ic_delete);
                    menu.addMenuItem(deleteItem);
                }
            }
        };
        SwipeMenuListView fileList = (SwipeMenuListView) findViewById(R.id.fileList);
        fileList.setMenuCreator(creator);
        fileList.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT);
        fileList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                if (menu.getViewType() == 0) {
                    final CodeFile cf = model.getFilesInCurrentDir().get(position-1);
                    switch (index) {
                        case 0: showTextDialog(R.string.rename_file_description, new OnDialogActivation() {
                                    @Override
                                    public void onActivation(String textInput) {
                                        model.renameFile(cf, textInput);
                                        updateDrawer();
                                    }
                                });
                                break;
                        case 1: showTextDialog(R.string.remove_file_description, new OnDialogActivation() {
                                    @Override
                                    public void onActivation(String textInput) {
                                        model.deleteFile(cf);
                                        updateDrawer();
                                    }
                                }, false);
                                break;
                    }
                    // false : close the menu; true : not close the menu
                    return false;
                }else{
                    return false;
                }
            }
        });

        updateDrawer();
    }

    private void closeDrawer(){
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
    }

    private void openDrawer() {
        ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(Gravity.LEFT);
    }

    private void updateDrawer(){
        ListView view = (ListView)findViewById(R.id.fileList);
        view.setAdapter(new FileViewAdapter(getApplicationContext(), model.getFilesInCurrentDir().toArray(new CodeFile[0]), model.canStepUpOneFile()));
        ((TextView)findViewById(R.id.projectName)).setText(model.getActiveProject());
    }

    private void showDropboxChooser(){
        if(dropboxChooser == null){
            authenticatingDropbox = true;
            dropboxChooser = new DbxChooser(getResources().getString(R.string.dropbox_app_key));
            //Activate dropbox integration
            DropboxFactory.initDropboxIntegration(this);
        }

        if(DropboxFactory.isAuthenticated()){
            authenticatingDropbox = false;
            dropboxChooser.forResultType(DbxChooser.ResultType.DIRECT_LINK).launch(MainActivity.this, DBX_CHOOSER_REQUEST);
        }
    }

    private void loadDropboxProject(DbxChooser.Result result){
        Log.d("MainActivity", "Link: "+result.getLink().getPath());
        model.createProject(result.getLink().getPath(), ProjectType.DROPBOX, new FileSystem.OnProjectLoadListener() {
            @Override
            public void projectLoaded(boolean success) {
                updateDrawer();
            }
        });
    }

    private void showChoiceDialog(int messageResource, final String[] items, final OnDialogActivation listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);
        //builder.setMessage(messageResource);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onActivation(items[which]);
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        currentDialog = builder.create();
        currentDialog.show();
    }

    private void showTextDialog(int messageResource, final OnDialogActivation listener){
        showTextDialog(messageResource, listener, true);
    }

    private void showTextDialog(int messageResource, final OnDialogActivation listener, boolean showTextField){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);
        builder.setMessage(messageResource);
        if(showTextField) {
            builder.setView(getLayoutInflater().inflate(R.layout.dialog_input_text, null));
        }
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == Dialog.BUTTON_POSITIVE && listener != null) {
                    EditText source = ((EditText) currentDialog.findViewById(R.id.dialog_input_text));
                    String input = (source!=null?source.getText().toString():"");
                    listener.onActivation(input);
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        currentDialog = builder.create();
        currentDialog.show();
    }

    private interface OnDialogActivation {
        void onActivation(String textInput);
    }
}
