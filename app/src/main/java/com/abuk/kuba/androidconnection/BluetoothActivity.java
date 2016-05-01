package com.abuk.kuba.androidconnection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Game Master on 2015-11-09.
 */
public class BluetoothActivity extends Activity {

    private final static UUID uuid = UUID.fromString("fc5ffc49-00e3-4c8b-9cf1-6b72aad1001a");
    private static final int FILE_SELECT_CODE = 0;
    private static final Object TEMP_FILE_NAME = FILE_SELECT_CODE; ;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Kiedy urządzenie BT odszukano
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // pobierz obiekt BluetoothDevice z intencji
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // dodaj imie i adres urządzenia do listy
                adapter.add(bluetoothDevice.getName() + "\n"
                        + bluetoothDevice.getAddress());
            }
        }
    };

    private BluetoothAdapter bluetoothAdapter;
    private ToggleButton toggleButton;
    private ListView listview;
    private ArrayAdapter<String> adapter;
    private static final int ENABLE_BT_REQUEST_CODE = 1;
    private static final int DISCOVERABLE_BT_REQUEST_CODE = 2;
    private static final int DISCOVERABLE_DURATION = 300;
////////////// dodane
    public static final int REQUEST_CODE = 3;
    protected Uri mMediaUri;
    ///////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ///////
        /*
        clientb = (Button) findViewById(R.id.clientbut);
        clientb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t2.setText("Jestem clientem!");
                BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice server = ba.getRemoteDevice(et1.getText().toString());
                new ClientBluetooth(server).start();
            }
        });
        */
        //////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);

        listview = (ListView) findViewById(R.id.listView);
        // ListView klikalna lista - Click Listener
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // ListView Clicked wartości po włączeniu BT
                String itemValue = (String) listview.getItemAtPosition(position);

                String MAC = itemValue.substring(itemValue.length() - 17);

                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC);
//////////////////////////////////

                pickFile();
                // inicjacja osobnego wątku do wysłania pliku po kliknięciu w liste

//////////////////////////////////
                ConnectingThread t = new ConnectingThread(bluetoothDevice);
                t.start();
            }
        });
        adapter = new ArrayAdapter<String>
                (this,android.R.layout.simple_list_item_1);
        listview.setAdapter(adapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    public void pickFile() { //"podnieś" wybrany plik z menedżera urządzeń
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("file/*");
        startActivityForResult(i, FILE_SELECT_CODE);
        //i.setAction(Intent.ACTION_PICK);

        /* działa wybór galerii + kontakty + inne
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("**");
        startActivityForResult(i, 1);
        */
        /* dobrze działa wybór z galerii zdjęć
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1);
        */
    }



    public void onToggleClicked(View view) { //02.01.2016 14:03 dodane int i i na dole switch do tego; 19:47 usuniete

        adapter.clear();

        ToggleButton toggleButton = (ToggleButton) view;

        if (bluetoothAdapter == null) {
            // Urządzenie nie wspiera BT
            Toast.makeText(getApplicationContext(), "Ups! Twoje urządzenie nie obsługuje Bluetooth",
                    Toast.LENGTH_SHORT).show();
            toggleButton.setChecked(false);
        } else {

            if (toggleButton.isChecked()){ // do włączenia BT
                if (!bluetoothAdapter.isEnabled()) {
                    // pojawienie się stanu urządzenia BT po włączeniu
                    Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetoothIntent, ENABLE_BT_REQUEST_CODE);
                } else {
                    Toast.makeText(getApplicationContext(), "Urządzenie zostało włączone" +
                                    "\n" + "Skanuje w poszukiwaniu urządzeń...",
                            Toast.LENGTH_SHORT).show();
                    // Wyszukiwanie urządzeń BT
                    discoverDevices();
                    // Uczyń urządzenie wykrywalnym
                    makeDiscoverable();
                }
            } else { // Wyłącz BT

                bluetoothAdapter.disable();
                adapter.clear();
                Toast.makeText(getApplicationContext(), "Urządzenie jest wyłączone.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {

//////////////////////
        super.onActivityResult(requestCode, resultCode, data);
        try { // wybierz sposób wysyłania pliku

            Uri selectedImage = data.getData();
            Intent sendIntent = new Intent();
            sendIntent.setType("*/*");
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, selectedImage);
            //startActivity(Intent.createChooser(sendIntent, ""));
            startActivity(Intent.createChooser(sendIntent, "Wyślij plik przez:"));
        }
        catch(Exception e)
        {
        }
//////////////////////////
/*
        switch (requestCode){
            case FILE_FOUND:
                filename = data.getData().getPath();
                if(filename.trim().lenght()>0){

                    if (chatBusinnessLogic.sendFile(filename)){
                        lblFilename.setText(filename);
                        toastUtil.showToast("Sukces");
                    }
                }
                else{
                    toastUtil.showToast("błąddd");
                }
        }
        */
        //////////////////////////
        /*
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    // Get the path
                    String path = null;
                    try {
                        path = BluetoothActivity.getPath(this, uri);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    // Get the file instance
                    // File file = new File(path);
                    try {
                        FileInputStream file = new FileInputStream(new File(path));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    DataOutputStream dataOut = new DataOutputStream(dataOutputStream);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
        */
/////////////////////////////////////


        if (requestCode == ENABLE_BT_REQUEST_CODE) {

            // BT włączone
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Włączyłeś Bluetooth." +
                                "\n" + "Skanuje w poszukiwaniu urządzeń...",
                        Toast.LENGTH_SHORT).show();

                // przeszukuje urządzenia w pobliżu
                discoverDevices();

                // bądź widoczny
                makeDiscoverable();

                // rozpoczęcie wątku do nadawania jako server
                ListeningThread t = new ListeningThread();
                t.start();

            } else { // RESULT_CANCELED pozwala lub nie włączyć BT
                Toast.makeText(getApplicationContext(), "Urządzenie jest wyłączone.",
                        Toast.LENGTH_SHORT).show();

                // włącz togglebutton
                toggleButton.setChecked(false);
            }
        } else if (requestCode == DISCOVERABLE_BT_REQUEST_CODE){

            if (resultCode == DISCOVERABLE_DURATION){
                Toast.makeText(getApplicationContext(), "Włączyłeś widoczność urządzenia na " +
                                DISCOVERABLE_DURATION + " sekund",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Błąd włączania widoczności na twoim urządzeniu",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    protected void discoverDevices(){
        // skanowanie aktywnych urządzeń
        if (bluetoothAdapter.startDiscovery()) {
            Toast.makeText(getApplicationContext(), "Poszukuję innych urządzeń...",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Poszukiwanie nie powiodło się.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    protected void makeDiscoverable(){
        // bądź wykrywalny
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivityForResult(discoverableIntent, DISCOVERABLE_BT_REQUEST_CODE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // rejestracja BroadcastReceiver dla ACTION_FOUND
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(broadcastReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // dodaje urządzenia aktywne do menu w ListView
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class ListeningThread extends Thread {
        private final BluetoothServerSocket bluetoothServerSocket;

        public ListeningThread() {
            BluetoothServerSocket temp = null;
            try {
                temp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), uuid);

            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothServerSocket = temp;
        }

        public void run() {
            BluetoothSocket bluetoothSocket;
            // powoduje nasłuchiwanie kiedy BluetoothSocket jest włączony lub występuje wyjątek
            while (true) {
                try {
                    bluetoothSocket = bluetoothServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // jeśli połączenie zaakceptowane
                if (bluetoothSocket != null) {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Sparowałem urządzenie.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    // zarządzaj połączeniem
                   /*
                       manageBluetoothConnection(bluetoothSocket);
                   */

                    try {
                        bluetoothServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // wyłącz nasłuchiwanie
        public void cancel() {
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class ConnectingThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice bluetoothDevice;

        public ConnectingThread(BluetoothDevice device) {

            BluetoothSocket temp = null;
            bluetoothDevice = device;

            //pobierz BluetoothSocket by połączyć się z BluetoothDevice
            try {
                temp = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothSocket = temp;
        }
        /////////////tcp


        public void run() {
//////////////////////////ftp
            /*
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
            */
            ///////////////////////////////////// \ftp
            // Cancel discovery as it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // This will block until it succeeds in connecting to the device
                // through the bluetoothSocket or throws an exception
                bluetoothSocket.connect();
            } catch (IOException connectException) {
                connectException.printStackTrace();
                try {
                    bluetoothSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
            }

            // Code to manage the connection in a separate thread
            /*
               manageBluetoothConnection(bluetoothSocket);
            */
        }

        // Cancel an open connection and terminate the thread
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





/*
        Intent intent = new Intent();
        intent.setType("**");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,FILE_SELECT_CODE);
        */
        /*
        File f = new File(Environment.getExternalStorageDirectory(), String.valueOf(FILE_SELECT_CODE));
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("**");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
        intent.setClassName("com.android.bluetooth", "com.broadcom.bt.app.opp.OppLauncherActivity");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Wybierz plik do wysłania"), Integer.parseInt(Intent.EXTRA_STREAM) );

*/
        /*
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Bluetooth is turned off, please enable it to proceed!", Toast.LENGTH_LONG).show();
        }
        else {
            File sourceFile = findFile(Environment.getExternalStorageDirectory(),"E-charge.apk");
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("application/vnd.android.package-archive");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sourceFile) );
            startActivity(intent);
        }
        */

/*
        try {
            File myFile = new File("filepath");
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String ext = myFile.getName().substring(myFile.getName().lastIndexOf(".")+1);
            String type = mime.getMimeTypeFromExtension(ext);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType(type);
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(myFile));
            startActivity(Intent.createChooser(i,"Wyślij przez:"));
        }
        catch(Exception e){
            Toast.makeText(getBaseContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        */
/*
        File f = new File(Environment.getExternalStorageDirectory(), String.valueOf(FILE_SELECT_CODE) );
        Intent intent = new Intent();
        intent.setType("**");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
        intent.setClassName("com.android.bluetooth", "com.broadcom.bt.app.opp.OppLauncherActivity");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        */

/*
        Intent i = new Intent();
        i.setType("image/");
        i.setAction(Intent.ACTION_SEND);
        File f = new File(Environment.getDataDirectory(), )
        startActivity(i);
        */
        /*String path = getFilesDir().getAbsolutePath() + File.separator  + TEMP_FILE_NAME; //i will get file from your /data/data/...
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("plain/text");//
        i.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:/" + path));
        startActivity(Intent.createChooser(i, "Send File"));
        */

        /*File f = new File(Environment.getExternalStorageDirectory(), String.valueOf(FILE_SELECT_CODE));
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
        startActivity(intent);*/

/*
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("//"); //wszystkie pliki
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Wybierz plik do wysłania"), Integer.parseInt(Intent.EXTRA_STREAM));
*/




}
