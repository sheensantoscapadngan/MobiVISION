package com.example.android.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.Result;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;


public class MainActivity extends AppCompatActivity implements AdapterCallback{

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private PagerAdapter pagerAdapter;
    public  TextView actionbar_title;
    private ImageView mic,qr_img;
    private static final UUID myUUID =  UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter myBluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private Set<BluetoothDevice> deviceList;
    private String address,name,current_path_location;
    private Handler handler;
    private TextToSpeech textToSpeech;
    private float speed,pitch;
    private static final int REQUEST_CAMERA = 1;
    private String objectName = "-1",path_destination,path_start,path_angle_string,table_name = "",prev_location = "";
    private int micState = 0;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private SendReceive sendReceive;
    private Map<String,ArrayList<String>> locations;
    private DatabaseConnect databaseConnect;
    private Cursor tableContent;
    private Map<String,String> tableOfValues;
    private int path_state = 0, path_listen = 0,path_angle,current_angle,qr_state = 0,path_request = 0,switch_request = 0, download_request = 0;
    private Stack<String> path;
    private String currentAngle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();
        checkPermissions();
        activateListeners();
        activateVoiceRecognition();
        activateTexttoSpeech();

        if(!table_name.equals(""))
            setupSQLTable();

        databaseConnect = new DatabaseConnect(this);

    }

    private void setupSQLTable() {

        table_name = table_name.replaceAll("\\s","5");
        tableContent = databaseConnect.getTable(table_name);
        while(tableContent.moveToNext()){

            tableOfValues.put(tableContent.getString(1),tableContent.getString(0));
            ArrayList<String> adj = new ArrayList<String>();
            if(!tableContent.getString(2).equals("."))
                adj.add(tableContent.getString(2).toLowerCase());
            if(!tableContent.getString(4).equals("."))
                adj.add(tableContent.getString(4).toLowerCase());
            if(!tableContent.getString(6).equals("."))
                adj.add(tableContent.getString(6).toLowerCase());

            locations.put(tableContent.getString(0),adj);

        }

    }

    private void setupPathFindingSequel() {

        Queue<String> line = new LinkedList<>();
        Map<String,String> visited = new HashMap<String,String>();
        path_start = "Chowking";
        line.add(path_start);
        visited.put(path_start,path_start);

        while(line.size() > 0){

            String current_location = line.peek();
            line.remove();
            ArrayList<String> current_info = new ArrayList<String>();
            current_info = locations.get(current_location);
            if(locations.containsKey(current_location)){
                for(String s : current_info){
                    if(!visited.containsKey(s)){
                        line.add(s);
                        visited.put(s,current_location);
                        if(s.equals(path_destination)){
                            break;
                        }
                    }
                }
            }
        }

        /*

        String current_location = path_destination;
        String current_parent = visited.get(path_destination);
        path.push(current_location);

        while(!current_location.equals(current_parent)){
            path.push(current_parent);
            current_location = current_parent;
            current_parent = visited.get(current_location);
        }

        */


        textToSpeech.speak("You are at the " + "Chowking", TextToSpeech.QUEUE_FLUSH,null);

        textToSpeech.speak("The next destination is " + "Jollibee",TextToSpeech.QUEUE_ADD,null);

        tableContent = databaseConnect.getTable(table_name);
        while(tableContent.moveToNext()){
            if(tableContent.getString(0).equals("Chowking")){
                if(tableContent.getString(2).equals("Jollibee"))
                    path_angle_string = tableContent.getString(3);
                else if(tableContent.getString(4).equals("Jollibee"))
                    path_angle_string = tableContent.getString(5);
                else
                    path_angle_string = tableContent.getString(7);
            }
        }

        textToSpeech.speak("It is " + path_angle_string + " degrees from this position", TextToSpeech.QUEUE_ADD, null);
        while(textToSpeech.isSpeaking()){
            //left empty
        }
        path_angle = Integer.parseInt(path_angle_string);
        path_listen = 1;
        String flag = "5";
        sendReceive.write(flag.getBytes());

    }

    private void activateVoiceRecognition() {

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {

                if(download_request == 1){

                    ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if(result != null){
                        String configuration_name = result.get(0);
                        configuration_name = configuration_name.toUpperCase();
                        configuration_name = "SM CITY CEBU";

                        if(configuration_name.equals("SM CITY SEASIDE") || configuration_name.equals("SM CITY CEBU")) {

                            /*
                            Intent intent = new Intent(MainActivity.this, DownloadLoadingActivity.class);
                            intent.putExtra("table_name", configuration_name);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            */

                            textToSpeech.speak("Configuration downloaded",TextToSpeech.QUEUE_ADD,null);

                        }else{
                            textToSpeech.speak("Configuration is not found",TextToSpeech.QUEUE_ADD,null);
                        }
                    }

                    download_request = 0;

                }
                else if(path_request == 1){

                    Toast.makeText(MainActivity.this, "Processing...", Toast.LENGTH_SHORT).show();
                    ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if(result != null){
                        path_destination = "";
                        Toast.makeText(MainActivity.this, path_destination, Toast.LENGTH_SHORT).show();
                        setupPathFindingSequel();
                        path_request = 0;
                    }else{
                        textToSpeech.speak("Initialization failed", TextToSpeech.QUEUE_ADD,null);
                    }

                    speechRecognizer.stopListening();

                }else if(switch_request == 1){

                    ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                    if(result != null){

                        String playResult = result.get(0);
                        Cursor searchTable = databaseConnect.getTableList();
                        playResult = playResult.toUpperCase();
                        playResult = "SM CITY CEBU";
                        Boolean found = false;
                        if(searchTable.getCount() > 0){
                            while(searchTable.moveToNext()){
                                Log.d("MAIN",searchTable.getString(0));
                                if(playResult.equals(searchTable.getString(0)))
                                    found = true;
                            }
                        }
                        Toast.makeText(MainActivity.this, playResult.toUpperCase(), Toast.LENGTH_SHORT).show();

                        if(found){
                            table_name = playResult;
                            setupSQLTable();
                            viewPager.setCurrentItem(0);
                            viewPager.setCurrentItem(1);
                            textToSpeech.speak("Configuration switched to " + "SM CITY CEBU", TextToSpeech.QUEUE_ADD,null);
                        }else{
                            textToSpeech.speak("Configuration is not found", TextToSpeech.QUEUE_ADD,null);
                        }

                    }
                    switch_request = 0;
                    speechRecognizer.stopListening();
                }else{

                ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(result != null) {

                    String message = result.get(0);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

                    if (message.equals("close"))
                        finish();
                    else if (message.equals("open qr") || message.equals("open QR")) {

                        textToSpeech.speak("Opening QR scanner", TextToSpeech.QUEUE_ADD, null);
                        if (qr_state == 0) {
                            Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                            startActivityForResult(intent, 2);
                            qr_state = 1;
                        }
                        speechRecognizer.stopListening();

                    } else if (message.equals("construct way")) {

                        textToSpeech.speak("Where do you want to go?", TextToSpeech.QUEUE_ADD, null);
                        while (textToSpeech.isSpeaking()) {
                            //purposely left empty
                        }
                        if (path_state == 0) {
                            path_state = 1;
                            path_request = 1;
                            speechRecognizer.stopListening();
                        }

                    } else if (message.equals("delete way")) {

                        textToSpeech.speak("Deleting path", TextToSpeech.QUEUE_ADD, null);
                        while (textToSpeech.isSpeaking()) {
                            path_state = 0;
                            path.clear();
                        }
                        speechRecognizer.stopListening();

                    } else if (message.equals("play")) {

                        textToSpeech.speak("What configuration do you want to play?", TextToSpeech.QUEUE_ADD, null);
                        while (textToSpeech.isSpeaking()) {
                            //left empty
                        }
                        switch_request = 1;
                        speechRecognizer.stopListening();

                    }else if(message.equals("download")){

                        textToSpeech.speak("What configuration do you want to download?",TextToSpeech.QUEUE_ADD,null);
                        while(textToSpeech.isSpeaking()){

                        }
                        download_request = 1;
                        speechRecognizer.stopListening();

                    }
                    else {
                        textToSpeech.speak("I cannot understand you", TextToSpeech.QUEUE_ADD,null);
                        speechRecognizer.stopListening();
                    }
                }

                }

                micState = 0;
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });


    }

    private void checkPermissions() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkPermission()){

            }else{
                requestPermission();
            }
        }

    }

    private boolean checkPermission(){
        return ((ContextCompat.checkSelfPermission(MainActivity.this, CAMERA) == PackageManager.PERMISSION_GRANTED)&&
                (ContextCompat.checkSelfPermission(MainActivity.this, RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED));
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{CAMERA,RECORD_AUDIO},REQUEST_CAMERA);
    }

    public void onRequestPermissionsResult(final int requestCode, String permission[], int grantResults[]){
        switch (requestCode){
            case REQUEST_CAMERA:
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {

                    }else{
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if(shouldShowRequestPermissionRationale(CAMERA)){
                                displayAlertMessage("You need to allow access for both permissions"
                                        , new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA,RECORD_AUDIO},REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }

    }

    public void displayAlertMessage(String message, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK",listener)
                .setNegativeButton("Cancel",null)
                .create();
    }

    private void activateListeners() {

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0 :
                        actionbar_title.setText("AVAILABLE");
                        break;
                    case 1 :
                        actionbar_title.setText("MobiVISION");
                        break;
                    case 2 :
                        actionbar_title.setText("SEARCH");
                        break;
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(micState == 0){
                    speechRecognizer.stopListening();
                    micState = 1;
                    Toast.makeText(MainActivity.this, "Started!", Toast.LENGTH_SHORT).show();
                    speechRecognizer.startListening(recognizerIntent);
                }else{
                    Toast.makeText(MainActivity.this, "Ended!", Toast.LENGTH_SHORT).show();
                    micState = 0;
                    speechRecognizer.stopListening();
                }

            }
        });

        qr_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ScannerActivity.class);
                startActivityForResult(intent,2);
            }
        });

    }

    private void setupViews() {

        Toast.makeText(this, objectName, Toast.LENGTH_SHORT).show();

        tabLayout = (TabLayout) findViewById(R.id.tabLayoutMain);
        viewPager = (ViewPager) findViewById(R.id.viewPagerMain);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        actionbar_title = (TextView) findViewById(R.id.textViewActionbarText);
        qr_img = (ImageView) findViewById(R.id.imageViewActionBarQR);

        mic = (ImageView) findViewById(R.id.imageViewMicHomeActionBar);

        locations = new HashMap<String,ArrayList<String>>();
        tableOfValues = new HashMap<String,String>();
        path = new Stack<String>();

        handler = new Handler(){
            public void handleMessage(android.os.Message msg){
                switch (msg.what){
                    case 1 :
                        byte[] readBuff = (byte[]) msg.obj;
                        String message = new String(readBuff,0,msg.arg1);
                        Log.d("MAIN","MESSAGE IS " + message);
                        //-----------HMC INTEGRATION--------------//

                        if(path_listen == 1){

                            if(message.equals(">")){

                                StringBuilder sb = new StringBuilder(currentAngle);
                                currentAngle = sb.toString();


                                Log.d("MAIN",currentAngle);
                                current_angle = Integer.parseInt(currentAngle);

                                if(current_angle <= 400)
                                    textToSpeech.speak(currentAngle, TextToSpeech.QUEUE_FLUSH, null);

                                if(Math.abs(current_angle - path_angle) <= 10){
                                    textToSpeech.speak("You are enroute", TextToSpeech.QUEUE_ADD, null);
                                    path_listen = 0;
                                    sendReceive.write("5".getBytes());
                                }

                                currentAngle = "";

                            }else if(!message.equals("<")){
                                currentAngle += message;
                            }

                        }

                        //--------------------------------//

                        //-----------STOP INDICATOR--------------//

                        else if(message.equals("1") && micState == 0){
                            textToSpeech.speak("STOP!",TextToSpeech.QUEUE_FLUSH,null);
                        }

                        //---------------------------------//


                        //-----------ACTIVATE VOICE-------------//
                        else if(message.equals("2")){
                            if(qr_state == 1){
                                ScannerActivity.getInstance().finish();
                                qr_state = 0;
                            }else {
                                if (micState == 0) {
                                    micState = 1;
                                    Toast.makeText(MainActivity.this, "Started!", Toast.LENGTH_SHORT).show();
                                    speechRecognizer.startListening(recognizerIntent);
                                }else{
                                    micState = 0;
                                    speechRecognizer.stopListening();
                                }
                            }
                        }
                        //--------------------------------//


                        //----------POSITION RECOGNITION-----------//
                        else if(micState == 0){

                            String place = tableOfValues.get(message);;

                            if(path_state == 0 && !message.equals("5")) {
                                if(!place.equals(prev_location)){
                                    path_start = place;
                                    readText(place);
                                    prev_location = place;
                                }
                            }
                            else if(!message.equals("5")){

                                if(place.equals(path.peek()) && path.size() > 1){
                                    textToSpeech.speak("You are at the " + place, TextToSpeech.QUEUE_FLUSH,null);
                                    path.pop();
                                    textToSpeech.speak("The next destination is " + path.peek(),TextToSpeech.QUEUE_ADD,null);

                                    tableContent = databaseConnect.getTable(table_name);
                                    while(tableContent.moveToNext()){
                                        if(tableContent.getString(0) == place){
                                            if(tableContent.getString(2).equals(path.peek()))
                                                path_angle_string = tableContent.getString(3);
                                            else if(tableContent.getString(4).equals(path.peek()))
                                                path_angle_string = tableContent.getString(5);
                                            else
                                                path_angle_string = tableContent.getString(7);
                                        }
                                    }

                                    textToSpeech.speak("It is " + path_angle_string + " degrees from this position", TextToSpeech.QUEUE_ADD, null);

                                    while(textToSpeech.isSpeaking()){
                                        //left empty
                                    }

                                    path_angle = Integer.parseInt(path_angle_string);
                                    path_listen = 1;
                                    Log.d("MAIN","NOTIFYING");
                                    sendReceive.write("5".getBytes());
                                }else{

                                    textToSpeech.speak("You are at the " + place, TextToSpeech.QUEUE_ADD,null);
                                    while(textToSpeech.isSpeaking()){

                                    }
                                    textToSpeech.speak("You have arrived at your destination",TextToSpeech.QUEUE_ADD,null);
                                    path_state = 0;

                                }

                                prev_location = place;

                            }
                        }
                        break;
                        //------------------------------//
                }
            }
        };

        prev_location = "Chowking";
        speed = 1;
        pitch = 1;
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(1);
        setupTabs();
        setupBluetooth();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2 && resultCode == RESULT_OK){
            objectName = data.getStringExtra("RESULT_STRING");
            readText(objectName);
        }

        if(requestCode == 3){

        }

    }

    private void activateTexttoSpeech() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){

                    int res = textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.setSpeechRate(speed);
                    textToSpeech.setPitch(pitch);

                }
            }
        });
    }

    private void readText(String message){

        textToSpeech.speak(message,TextToSpeech.QUEUE_FLUSH,null);

    }

    private void setupBluetooth() {

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList = myBluetoothAdapter.getBondedDevices();

        if(deviceList.size() > 0){
            for(BluetoothDevice bt : deviceList){
                address = bt.getAddress();
                name = bt.getName();
            }
        }

        BluetoothDevice bt = myBluetoothAdapter.getRemoteDevice(address);
        try{

            bluetoothSocket = bt.createInsecureRfcommSocketToServiceRecord(myUUID);
            bluetoothSocket.connect();

            sendReceive = new SendReceive(bluetoothSocket);
            sendReceive.start();

        }catch(IOException e){
            e.printStackTrace();
        }

    }

    private void setupTabs() {

        tabLayout.getTabAt(0).setIcon(R.drawable.download_tab);
        tabLayout.getTabAt(1).setIcon(R.drawable.home_tab);
        tabLayout.getTabAt(2).setIcon(R.drawable.search_tab);

    }

    public Bundle getGlobalData(){

        Bundle bundle = new Bundle();
        bundle.putString("name",name);
        bundle.putString("table_name",table_name);
        return bundle;
    }


    @Override
    public void foo(String table_name) {

        this.table_name = table_name;
        setupSQLTable();
        viewPager.setCurrentItem(1);


    }

    public class SendReceive extends Thread {

        InputStream inputStream;
        OutputStream outputStream;
        BluetoothSocket socket;

        public SendReceive(BluetoothSocket socket){

            this.socket = socket;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        public void run(){
            byte[] buffer = new byte[256];
            int bytes;

            while(true){
                Log.d("MAINS","here");
                try{

                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(1,bytes,-1,buffer).sendToTarget();

                }catch (Exception e){
                    break;
                }
            }

            }

        public void write(byte[] bytes){
            try{
                Log.d("")
                outputStream.write(bytes);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }


}
