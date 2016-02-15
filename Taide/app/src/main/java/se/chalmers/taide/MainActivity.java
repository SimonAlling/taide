package se.chalmers.taide;

import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.List;

import se.chalmers.taide.model.EditorModel;
import se.chalmers.taide.model.ModelFactory;
import se.chalmers.taide.model.languages.Language;
import se.chalmers.taide.model.languages.LanguageFactory;
import se.chalmers.taide.model.languages.SyntaxBlock;
import se.chalmers.taide.util.Clipboard;

public class MainActivity extends AppCompatActivity {

    private EditText codeEditor;

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


        codeEditor = (EditText)findViewById(R.id.editText);
        final String sampleCode = "public class Main{\n\n\tpublic static void main(String[] args){\n\t\tSystem.out.println(\"Hello world!\");\n\t}\n\n}";
        codeEditor.setText(sampleCode);
        codeEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (codeEditor.getText().length() == 0) {
                    codeEditor.setText(sampleCode);
                    codeEditor.setSelection(101);    //Set focus after Syso statement
                }
            }
        });

        EditorModel model = ModelFactory.createEditorModel(codeEditor, "java");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        final MenuItem pasteMenu = menu.findItem(R.id.action_paste);
        pasteMenu.setEnabled(Clipboard.hasPasteContent(getApplicationContext()));
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
        }

        return super.onOptionsItemSelected(item);
    }
}
