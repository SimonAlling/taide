package se.chalmers.taide;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import se.chalmers.taide.model.EditorModel;
import se.chalmers.taide.model.ModelFactory;
import se.chalmers.taide.model.languages.LanguageFactory;
import se.chalmers.taide.util.Clipboard;
import se.chalmers.taide.util.TabUtil;

/**
 * Created by Matz on 2016-01-25.
 *
 * Main class for the Android app UI.
 */

public class MainActivity extends AppCompatActivity {

    private EditText codeEditor;
    private EditorModel model;

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


        //Retrieve the code editor text field
        codeEditor = (EditText)findViewById(R.id.editText);
        //Init sample code
        final String sampleCode = "public class Main{\n\n"+ TabUtil.getTabs(1)+"public static void main(String[] args){\n"+ TabUtil.getTabs(2)+"System.out.println(\"Hello world!\");\n"+TabUtil.getTabs(1)+"}\n\n}";
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

        //Bind code editor to the model. Use java as language
        model = ModelFactory.createEditorModel(codeEditor, LanguageFactory.JAVA);
        Log.d("MainActivity", "Started model with language: " + model.getLanguage().getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
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

        switch(id){
            case R.id.action_settings:  break;
            case R.id.action_copy:      Clipboard.copyToClipboard(getApplicationContext(), codeEditor);break;
            case R.id.action_cut:       Clipboard.cutToClipboard(getApplicationContext(), codeEditor);break;
            case R.id.action_paste:     Clipboard.pasteFromClipboard(getApplicationContext(), codeEditor);break;
            case R.id.action_undo:      model.undo();invalidateOptionsMenu();break;
            case R.id.action_redo:      model.redo();invalidateOptionsMenu();break;
        }

        return super.onOptionsItemSelected(item);
    }
}
