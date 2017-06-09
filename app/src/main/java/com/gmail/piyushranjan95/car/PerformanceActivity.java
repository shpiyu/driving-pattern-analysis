package com.gmail.piyushranjan95.car;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gmail.piyushranjan95.car.classifiers.boosting.Adaboost;
import com.gmail.piyushranjan95.car.preprocessing.Converter;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

public class PerformanceActivity extends AppCompatActivity {

    private String name;
    private int score;
    private int savedScore, calculatedScore;
    private String localhost = "192.168.43.250"; //"172.16.4.193";//"192.168.1.105";

    private TextView scoreViewLabel, scoreView, stub;
    private RelativeLayout layout;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /** from adaboost **/
    private String[] inputs;

    private Converter converter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance);
        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        //initialize views
        layout = (RelativeLayout) findViewById(R.id.performanceView);
        scoreView = (TextView) findViewById(R.id.scoreView);
        scoreViewLabel = (TextView) findViewById(R.id.scoreViewLabel);
        stub = (TextView) findViewById(R.id.stub);
        stub.setVisibility(View.INVISIBLE);

        //get name from shared prefs
        SharedPreferences pref = getSharedPreferences("namePref", MODE_PRIVATE);
        name = pref.getString("username", "Hello");
        if (name.equals("")) name = "Hello";
        scoreViewLabel.setText(name + ", your driving score is");

        //get saved score from sharedpref
        SharedPreferences pref2 = getSharedPreferences("scorePref", MODE_PRIVATE);
        savedScore = pref2.getInt("score", 0);

        //calculatedScore = 52;
        // TODO: 28/5/17 calculate score from Adaboost test file


        /*********preprocessing starts **********************/

        converter = new Converter();
        converter.initializeConverter();
        converter.readInput("myinput.csv");
        converter.makeChangeRateList();
        converter.processing();


        /*********prerocessing ends***************************/


        /*********adaboost calculation starts ****************/


        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File("/sdcard/trainSamples.psv");
        //File file = new File("/sdcard/newTrain.psv");
        Adaboost boosting;
        Log.d("tag","hello");
        //ArrayList<String> values = new ArrayList<>();
        int sampleCount=0;
        // TODO: 8/6/17 changes made here
        //File testFile = new File("/sdcard/testSamples.psv");
        File testFile = new File("/sdcard/finalTest.csv");
        try {
            sampleCount = getTotalSamplesNumber(testFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sampleCount--;
        Log.d("tag",sampleCount+"");



        int[] labels = new int[sampleCount];
        //String[] inputs = new String[sampleCount];
        inputs = new String[sampleCount];
        try {
            //  inputs = getInputs(testFile,sampleCount);
            getInputs(testFile,sampleCount);
            boosting = Adaboost.train(file, 10, 10, 0);
            Log.d("tag",inputs[0]);
            for(int i=0;i<inputs.length;i++){
                Log.d("tag",inputs[i]);
                // TODO: 8/6/17 change to pipes if nothing works \\|
                // earlier  :
                // labels[i] = boosting.classify(inputs[i].split("\\,"));

                //int k=0;
                String results[] = inputs[i].split("\\,");
                String values[] = new String[results.length];
                for(int j=0;j<results.length;j++){
                    String str = results[j];
                    str = str.substring(0, str.length()-1);
                    str = str.substring(1);

                    if(Double.isNaN(Double.parseDouble(str))){
                        //values[j] = "0.0";
                        continue;
                    } else {
                        values[j] = str;
                    }
                }

                //labels[i] = boosting.classify(inputs[i].split("\\,"));

                labels[i] = boosting.classify(values);
                Log.d("tag",labels[i]+"");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }



        final double percent;
        int sum=0;
        for(int i=0;i<sampleCount;i++)
        {
            if(labels[i]==1){
                sum++;
                Log.d("tag",sum+"");
            }
        }
        calculatedScore = (int)((double)sum/sampleCount*100);


        /*********adaboost calculation ends ****************/

        Log.d("score", "calculated score is  " + calculatedScore + "\nSaved score is " + savedScore);


        if (calculatedScore != savedScore) {
            Log.d("score", "got inside if");
            //update sharedrepf score
            SharedPreferences.Editor editor = pref2.edit();
            editor.putInt("score", calculatedScore);
            editor.commit();
            //update score in database
            Log.d("score", "updatescore called");
            updateScore(calculatedScore);
        }


        //start score animation
        //score = 50;
        int dur = 2;
        startCountAnimation(calculatedScore, dur);

        //display stub after dur secs
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stub.setVisibility(View.VISIBLE);
            }
        }, dur * 1000);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void startCountAnimation(int score, int sec) {
        ValueAnimator animator = ValueAnimator.ofInt(0, score);
        animator.setDuration(sec * 1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                scoreView.setText(animation.getAnimatedValue().toString());
            }
        });
        animator.start();
    }

    private void updateScore(final int score) {
        // TODO: 28/5/17 update score in database
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://" + localhost + "/beproject/updateDriverScore.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Toast.makeText(PerformanceActivity.this, "data updated "+response, Toast.LENGTH_SHORT).show();
                // TODO: 28/5/17 make a better notification
                Snackbar snackbar = Snackbar.make(layout,"Data synced sucessfully",Snackbar.LENGTH_SHORT);
                snackbar.show();
                Log.d("score", "everything was successfull");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PerformanceActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                Log.d("score", "there's an error : " + error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("score", score + "");
                return params;
            }
        };

        queue.add(request);

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Performance Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.gmail.piyushranjan95.car/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Performance Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.gmail.piyushranjan95.car/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


    public void getInputs(File testFile, int samplesCount)
            throws IOException {
        //String[] inputs = new String[samplesCount];
        FileReader fileReader = new FileReader(testFile);
        BufferedReader breader = new BufferedReader(fileReader);
        String line;
        int i = 0;
        while ((line = breader.readLine()) != null) {
            inputs[i] = line;
            i++;
        }

        breader.close();

    }


    private static int getTotalSamplesNumber(File trainFile) throws IOException {

        LineNumberReader lineNumberReader = new LineNumberReader(
                new FileReader(trainFile));
        lineNumberReader.skip(Long.MAX_VALUE);
        lineNumberReader.close();
        return lineNumberReader.getLineNumber() + 1;

    }

}
