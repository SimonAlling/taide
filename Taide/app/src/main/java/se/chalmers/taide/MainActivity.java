package se.chalmers.taide;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
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

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import se.chalmers.taide.model.EditorModel;
import se.chalmers.taide.model.ModelFactory;
import se.chalmers.taide.model.filesystem.CodeFile;
import se.chalmers.taide.model.filesystem.FileSystem;
import se.chalmers.taide.model.languages.LanguageFactory;
import se.chalmers.taide.util.Clipboard;
import se.chalmers.taide.util.TabUtil;

/**
 * Created by Matz on 2016-01-25.
 *
 * Main class for the Android app UI.
 */


// massa problem har jag haft trot löst de men måste testa igen med git

public class MainActivity extends AppCompatActivity {

    private EditText codeEditor;
    private EditorModel model;

    private Dialog currentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        // Retrieve the code editor text field
        codeEditor = (EditText)findViewById(R.id.editText);
        // Init sample code
        final String sampleCode = "public class Main{\n\n"+TabUtil.getTabs(1)+"public static void main(String[] args){\n"+TabUtil.getTabs(2)+"System.out.println(\"Hello world!\");\n"+TabUtil.getTabs(1)+"}\n\n}";
        codeEditor.setText(sampleCode);
        codeEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() <= sampleCode.length()+1){
                    invalidateOptionsMenu();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bind code editor to the model. Use Java as language
        model = ModelFactory.createEditorModel(getApplicationContext(), ModelFactory.editTextToTextSource(codeEditor), LanguageFactory.JAVA);
        model.createProject("TestProject");
        Log.d("MainActivity", "Started model with language: " + model.getLanguage().getName());

        initDrawer();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        menu.findItem(R.id.action_undo).setEnabled(model.peekUndo()!=null);
        menu.findItem(R.id.action_redo).setEnabled(model.peekRedo() != null);
        return true;
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
            case R.id.action_undo:      model.undo();invalidateOptionsMenu(); break;
            case R.id.action_redo:      model.redo();invalidateOptionsMenu(); break;
            case android.R.id.home:     openDrawer();return true;
        }

        return super.onOptionsItemSelected(item);
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
            public void onDrawerSlide(View drawerView, float slideOffset) {}
            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        ((ListView)findViewById(R.id.fileList)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    model.gotoFolder(null);
                    updateDrawer();
                }else {
                    CodeFile cf = model.getFilesInCurrentDir().get(position-1);
                    if (cf.isDirectory()) {
                        model.gotoFolder(cf);
                        updateDrawer();
                    } else {
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
                                    model.createProject(textInput);
                                    updateDrawer();
                                }
                            });
                            break;
                    case 1: showChoiceDialog(R.string.load_project_description, model.getAvailableProjects(), new OnDialogActivation() {
                        @Override
                        public void onActivation(String textInput) {
                            model.setProject(textInput);
                            updateDrawer();
                        }
                    });
                            break;
                    case 2: showTextDialog(R.string.add_new_file_description, new OnDialogActivation() {
                                @Override
                                public void onActivation(String textInput) {
                                    model.openFile(model.createFile(textInput, false));
                                    updateDrawer();
                                    closeDrawer();
                                }
                            });
                            break;
                    case 3: showTextDialog(R.string.add_new_folder_description, new OnDialogActivation() {
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
                                        Log.d("MainActvity", "Rename went OK? "+model.renameFile(cf, textInput));
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

    private void openDrawer(){
        ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(Gravity.LEFT);
    }

    private void updateDrawer(){
        ListView view = (ListView)findViewById(R.id.fileList);
        view.setAdapter(new FileViewAdapter(getApplicationContext(), model.getFilesInCurrentDir().toArray(new CodeFile[0]), model.canStepUpOneFile()));
        ((TextView)findViewById(R.id.projectName)).setText(model.getActiveProject());
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
