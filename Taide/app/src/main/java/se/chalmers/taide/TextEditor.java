package se.chalmers.taide;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import se.chalmers.taide.model.EditorModel;
import se.chalmers.taide.model.ModelFactory;
import se.chalmers.taide.model.languages.LanguageFactory;
import se.chalmers.taide.util.TabUtil;



public class TextEditor extends Fragment {

    private EditText codeEditor;
    private EditorModel model;

    private OnFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text_editor, container, false);

        // Retrieve the code editor text field
        codeEditor = (EditText)view.findViewById(R.id.editText);
        // Init sample code
        final String sampleCode = "public class Main{\n\n"+ TabUtil.getTabs(1)+"public static void main(String[] args){\n"+TabUtil.getTabs(2)+"System.out.println(\"Hello world!\");\n"+TabUtil.getTabs(1)+"}\n\n}";
        codeEditor.setText(sampleCode);
        codeEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() <= sampleCode.length()+1){
                    getActivity().invalidateOptionsMenu();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bind code editor to the model. Use Java as language
        model = ModelFactory.createEditorModel(ModelFactory.editTextToTextSource(codeEditor), LanguageFactory.JAVA);
        Log.d("MainActivity", "Started model with language: " + model.getLanguage().getName());

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            //throw new RuntimeException(context.toString()
                    //+ " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
