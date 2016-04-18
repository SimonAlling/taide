package se.chalmers.taide;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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

/**
 * Created by Matz on 2016-04-11.
 */
public class FileNavigatorDrawer {

    public static final int DBX_CHOOSER_REQUEST = 0;
    private static final String DEFAULT_PROJECT_NAME = "DefaultProject";
    private static final String CURRENT_FILE_KEYNAME = "currentOpenedFile";

    private AppCompatActivity parentActivity;
    private EditorModel model;
    private EditText input;
    private CodeFile currentFile;

    private boolean authenticatingDropbox;
    private DbxChooser dropboxChooser;
    private ProgressDialog loadingDialog;
    private Dialog currentDialog;


    public FileNavigatorDrawer(AppCompatActivity parent, EditText input){
        this.parentActivity = parent;
        this.model = ModelFactory.getCurrentEditorModel();
        if(model == null){
            this.model = ModelFactory.createEditorModel(parent, ModelFactory.editTextToTextSource(input));
        }
        setTextInput(input);

        if(model.getActiveProject() == null){
            createProject(DEFAULT_PROJECT_NAME);
        }
    }

    public void saveInstanceState(Bundle saveInstanceState){
        if(currentFile != null) {
            saveInstanceState.putCharSequence(CURRENT_FILE_KEYNAME, currentFile.getName());
        }
    }

    public void onActivityResume(Bundle savedInstanceState){
        if(authenticatingDropbox){
            DropboxFactory.authenticationDone(parentActivity);
            if(DropboxFactory.isAuthenticated()) {
                showDropboxChooser();
            }
        }else{
            //if(savedInstanceState.get(CURRENT_FILE_KEYNAME) != null)
            if(currentFile != null){
                openFile(currentFile);
            }
        }
    }

    public void onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:     openDrawer();break;
        }
    }

    public void setTextInput(EditText input) {
        this.input = input;
    }

    public void initDrawer(){
        ((DrawerLayout) parentActivity.findViewById(R.id.drawer_layout)).setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                InputMethodManager inputMethodManager = (InputMethodManager) parentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(parentActivity.getCurrentFocus().getWindowToken(), 0);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                InputMethodManager inputMethodManager = (InputMethodManager) parentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(input, 0);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        ActionBar b = parentActivity.getSupportActionBar();
        if(b != null) {
            b.setDisplayHomeAsUpEnabled(true);
            b.setHomeAsUpIndicator(R.drawable.ic_drawer);
        }

        ((ListView)parentActivity.findViewById(R.id.fileList)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    model.gotoFolder(null);
                    updateDrawer();
                } else {
                    CodeFile cf = model.getFilesInCurrentDir().get(position - 1);
                    if(cf.isOpenable()) {
                        if (cf.isDirectory()) {
                            model.gotoFolder(cf);
                            updateDrawer();
                        } else {
                            openFile(cf);
                            closeDrawer();
                        }
                    }
                }
            }
        });
        ((ListView)parentActivity.findViewById(R.id.projectActionMenus)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0: showTextDialog(R.string.add_new_project_description, new OnDialogActivation() {
                        @Override
                        public void onActivation(String textInput) {
                            showLoadingDialog(R.string.load_project_loading);
                            createProject(textInput);
                        }
                    });
                        break;
                    case 1: showChoiceDialog(R.string.load_project_description, model.getAvailableProjects(), new OnDialogActivation() {
                        @Override
                        public void onActivation(String textInput) {
                            showLoadingDialog(R.string.load_project_loading);
                            model.setProject(textInput, new FileSystem.OnProjectLoadListener() {
                                @Override
                                public void projectLoaded(boolean success) {
                                    parentActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateDrawer();
                                            hideLoadingDialog();
                                        }
                                    });
                                }
                            });
                        }
                    });
                        break;
                    case 2: showDropboxChooser();break;
                    case 3: showTextDialog(R.string.add_new_file_description, new OnDialogActivation() {
                        @Override
                        public void onActivation(String textInput) {
                            openFile(model.createFile(textInput, false));
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
                    SwipeMenuItem openItem = new SwipeMenuItem(parentActivity.getApplicationContext());
                    openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                    openItem.setWidth((int) parentActivity.getResources().getDimension(R.dimen.drawer_action_button_width));
                    openItem.setTitle(R.string.rename_file);
                    openItem.setTitleSize(14);
                    openItem.setTitleColor(Color.WHITE);
                    menu.addMenuItem(openItem);

                    SwipeMenuItem deleteItem = new SwipeMenuItem(parentActivity.getApplicationContext());
                    deleteItem.setBackground(new ColorDrawable(parentActivity.getResources().getColor(R.color.drawer_action_delete)));
                    deleteItem.setWidth((int) parentActivity.getResources().getDimension(R.dimen.drawer_action_button_width));
                    deleteItem.setIcon(R.drawable.ic_delete_black);
                    menu.addMenuItem(deleteItem);
                }
            }
        };
        SwipeMenuListView fileList = (SwipeMenuListView) parentActivity.findViewById(R.id.fileList);
        fileList.setMenuCreator(creator);
        fileList.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT);
        fileList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                if (menu.getViewType() == 0) {
                    final CodeFile cf = model.getFilesInCurrentDir().get(position - 1);
                    switch (index) {
                        case 0:
                            showTextDialog(R.string.rename_file_description, new OnDialogActivation() {
                                @Override
                                public void onActivation(String textInput) {
                                    model.renameFile(cf, textInput);
                                    updateDrawer();
                                }
                            });
                            break;
                        case 1:
                            showTextDialog(R.string.remove_file_description, new OnDialogActivation() {
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
                } else {
                    return false;
                }
            }
        });
    }

    public void closeDrawer(){
        ((DrawerLayout) parentActivity.findViewById(R.id.drawer_layout)).closeDrawers();
    }

    public void openDrawer() {
        ((DrawerLayout) parentActivity.findViewById(R.id.drawer_layout)).openDrawer(Gravity.LEFT);
    }

    public void updateDrawer(){
        ListView actionView = (ListView)parentActivity.findViewById(R.id.projectActionMenus);
        int[] actionIcons = {R.attr.actionNewProject, R.attr.actionLoadProject, R.attr.actionImportProject, R.attr.actionNewFile, R.attr.actionNewFolder};
        actionView.setAdapter(new ListIconMenuAdapter(parentActivity.getApplicationContext(),
                                                      parentActivity.getResources().getStringArray(R.array.filesystem_default_menuitems),
                                                      actionIcons, true));
        ListView fileView = (ListView)parentActivity.findViewById(R.id.fileList);
        fileView.setAdapter(new FileViewAdapter(parentActivity.getApplicationContext(), model.getFilesInCurrentDir().toArray(new CodeFile[0]), model.canStepUpOneFile()));
        ((TextView)parentActivity.findViewById(R.id.projectName)).setText(model.getActiveProject());
    }

    private void createProject(String name){
        model.createProject(name, ProjectType.LOCAL_SYSTEM, new FileSystem.OnProjectLoadListener() {
            @Override
            public void projectLoaded(boolean success) {
                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDrawer();
                        hideLoadingDialog();
                    }
                });
            }
        });
    }

    private void openFile(CodeFile file){
        ((View)input.getParent()).setVisibility(View.VISIBLE);
        parentActivity.findViewById(R.id.noTextLoadedLabel).setVisibility(View.GONE);
        model.openFile(file);
        this.currentFile = file;
    }

    private void showDropboxChooser(){
        if(!DropboxFactory.isAuthenticated()){
            authenticatingDropbox = true;
            DropboxFactory.initDropboxIntegration(parentActivity);
        }

        if(DropboxFactory.isAuthenticated()){
            authenticatingDropbox = false;
            if(dropboxChooser == null){
                dropboxChooser = new DbxChooser(parentActivity.getResources().getString(R.string.dropbox_app_key));
            }
            dropboxChooser.forResultType(DbxChooser.ResultType.DIRECT_LINK).launch(parentActivity, DBX_CHOOSER_REQUEST);
        }
    }

    public void loadDropboxProject(Intent data){
        DbxChooser.Result result = new DbxChooser.Result(data);
        showLoadingDialog(R.string.load_project_loading);
        model.createProject(result.getLink().getPath(), ProjectType.DROPBOX, new FileSystem.OnProjectLoadListener() {
            @Override
            public void projectLoaded(boolean success) {
                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDrawer();
                        hideLoadingDialog();
                    }
                });
            }
        });
    }

    private void showLoadingDialog(int stringRef){
        if(loadingDialog != null){
            hideLoadingDialog();
        }
        loadingDialog = ProgressDialog.show(parentActivity, parentActivity.getString(R.string.loading), parentActivity.getString(stringRef), true);
    }

    private void hideLoadingDialog(){
        if(loadingDialog != null){
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    private void showChoiceDialog(int messageResource, final String[] items, final OnDialogActivation listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
        builder.setCancelable(true);
        builder.setMessage(messageResource);
        if(showTextField) {
            builder.setView(parentActivity.getLayoutInflater().inflate(R.layout.dialog_input_text, null));
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
        Log.d("FileNavDrawer", "Dialog is : " + currentDialog.getClass().getName());
    }

    private interface OnDialogActivation {
        void onActivation(String textInput);
    }
}