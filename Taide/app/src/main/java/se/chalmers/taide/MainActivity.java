package se.chalmers.taide;

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

public class MainActivity extends AppCompatActivity {

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


        final EditText codeEditor = (EditText)findViewById(R.id.editText);
        final String sampleCode = "public class Main{\n\n\t\tpublic static void main(String[] args){\n\t\t\t\tSystem.out.println(\"Hello world!\");\n\t\t}\n\n}";
        codeEditor.setText(sampleCode);
        codeEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(codeEditor.getText().length() == 0) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
