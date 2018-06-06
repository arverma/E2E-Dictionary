package in.ac.iiitmanipur.dictionary;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    TextView textView_rawOP;
    EditText editText;
    Button button;
    int i = 0;
    int j = 0;
    private ProgressBar spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.output);
        textView_rawOP = findViewById(R.id.raw_output);
        editText = (EditText) findViewById(R.id.input);
        button = (Button) findViewById(R.id.go);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
    }

    private String dictionaryEntries() {
        final String language = "en";
        final String word = editText.getText().toString();
        final String word_id = word.toLowerCase(); //word id is case sensitive and lowercase is required

        if(j == 0) {
            return "https://od-api.oxforddictionaries.com:443/api/v1/entries/" + language + "/" + word_id;
        }
        else {
            return "https://od-api.oxforddictionaries.com:443/api/v1/search/" + language + "?q=" + word_id;
        }
    }

    public void OnClick(View view) {
            new CallbackTask().execute(dictionaryEntries());
            spinner.setVisibility(View.VISIBLE);
    }

    //in android calling network requests on the main thread forbidden by default
    //create class to do async job
    private class CallbackTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

            //TODO: replace with your own app id and app key
            final String app_id = "**********";
            final String app_key = "***************************";
            try {
                URL url = new URL(params[0]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept","application/json");
                urlConnection.setRequestProperty("app_id",app_id);
                urlConnection.setRequestProperty("app_key",app_key);

                // read the output from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                return stringBuilder.toString();

            }
            catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            int left=0;
            String temp = result;
            textView_rawOP.setText(temp);
            super.onPostExecute(result);
            if(result.length()>=100 ){
                if(j==0){
                    left = result.indexOf("definitions");
                    result = result.substring(left,result.length());
                    int right = left+result.indexOf("]");
                    textView.setText(result.substring(0, right+1-left));

                }
                else{
                    int right = 0;
                    textView.setText("Did you mean:\n");
                    while(left>=0){//result.indexOf(" \"id\": \"")>=0
                        left = result.indexOf(" \"id\": \"");
                        result = result.substring(left+7,result.length());
                        right = left+7+result.indexOf("\",");
                        textView.append( Integer.toString(left)+ " " +Integer.toString(right)+ result.substring(0,right-left)+"\n");
                        left = result.indexOf(" \"id\": \"");
                    }
                    j=0;
                }
                spinner.setVisibility(View.GONE);
            }
            else {
                j = 1;
                new CallbackTask().execute(dictionaryEntries());
            }
        }
    }
}
