package com.devmel.apps.serialterminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.devmel.communication.IUart;
import com.devmel.communication.linkbus.Usart;
import com.devmel.communication.android.UartBluetooth;
import com.devmel.communication.android.UartUsbOTG;
import com.devmel.storage.Node;
import com.devmel.storage.SimpleIPConfig;
import com.devmel.storage.android.UserPrefs;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.text.method.ScrollingMovementMethod;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static com.devmel.apps.serialterminal.R.string.connect;


public class MainActivity extends Activity {
    public final static String sharedPreferencesName = "com.devmel.apps.serialterminal";
    Dialog customDialog = null;
    private int locosyfunciones[][] = new int[30][11];
    private UserPrefs userPrefs;
    private int err;
    private boolean stop_recibir;

    private Button connectBt;
    private Button sentidoBt;
    private TextView statusText;
    private TextView accesorio;
    private TextView loco;

    private Thread thread;
    private IUart device;
    private String portName = null;
    private Class<?> portClass = null;


    private ArrayList<String> Items_log = new ArrayList<String>();
    private List<String> estado_s88_List;
    private ArrayAdapter adaptador1;
    private ArrayAdapter<String> adaptador_s88;
    private ListView lv1;
    private GridView gridview_s88;
    private CheckBox checkBox_accesorio;
    private CheckBox checkBox_loco;
    private int locomotora = 0;
    private int posicion_loco = 0;
    private StartPointSeekBar velocidad;
    boolean esperando_ok = false;
    boolean accesorio_lanzado = false;
    private int columnas_cola = 18;
    private int lineas_cola = 9;
    private  BluetoothAdapter mBluetoothAdapter ;



    private static final String LOGTAG = "MainActivity";
    byte[] recibido = new byte[]{};
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        statusText = (TextView) findViewById(R.id.statusText);


        gridview_s88 = (GridView) findViewById(R.id.gridview_s88);
        gridview_s88.setVisibility(View.INVISIBLE);
        final String[] estado_s88 = new String[lineas_cola * columnas_cola];
        int bloque = 0;

        for (int idx = 0; idx < 162; idx++) {

            if (idx > 17 & ((idx % 18 == 0) || (idx % 18 == 9))) {
                bloque++;
                estado_s88[idx] = String.valueOf(bloque);
            } else {
                estado_s88[idx] = "0".toString();
            }

        }
        for (int idx = 1; idx < 9; idx++) {
            estado_s88[idx] = String.valueOf(idx);
        }
        for (int idx = 10; idx < 18; idx++) {
            estado_s88[idx] = String.valueOf(idx - 9);
        }

        estado_s88_List = Arrays.asList(estado_s88);
        gridview_s88.setAdapter(new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, estado_s88_List) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                // Convert the view as a TextView widget
                TextView tv = (TextView) view;

                //tv.setTextColor(Color.DKGRAY);
                ViewGroup.LayoutParams params1 = gridview_s88.getLayoutParams();


                // Set the layout parameters for TextView widget
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                );
                tv.setLayoutParams(lp);

                // Get the TextView LayoutParams
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tv.getLayoutParams();

                // Set the width of TextView widget (item of GridView)
                // params.width = getPixelsFromDPs(MainActivity.this,20);

                // Set the TextView layout parameters
                //   tv.setLayoutParams(params);

                tv.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 66));

                // Display TextView text in center position
                tv.setGravity(Gravity.CENTER);

                // Set the TextView text font family and text size
                tv.setTypeface(Typeface.SERIF, Typeface.BOLD);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
                tv.setPadding(0, 0, 0, 0);

                // Set the TextView text (GridView item text)
                tv.setText(estado_s88_List.get(position));

                // Set the TextView background color

                if (position < 18 || (position % 18 == 0) || (position % 18 == 9)) {
                    tv.setBackgroundColor(Color.parseColor("#FF82DE81"));
                } else {
                    if (position > 18) {
                        if (tv.getText().toString() == "0") {
                            tv.setBackgroundColor(Color.parseColor("#eeeeee"));
                            tv.setTextColor(Color.parseColor("#eeeeee"));
                        } else {
                            tv.setBackgroundColor(Color.parseColor("#ff0000"));
                            tv.setTextColor(Color.parseColor("#ff0000"));

                        }
                    }
                }

                // Return the TextView widget as GridView item
                return tv;

            }
        });


        gridview_s88 .setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                estado_s88();
            }
        });



        adaptador_s88 = (ArrayAdapter) gridview_s88.getAdapter();


        connectBt = (Button) findViewById(R.id.connectBt);
        accesorio = (TextView) findViewById(R.id.accesorio);
        loco = (TextView) findViewById(R.id.loco);

        velocidad = (StartPointSeekBar) findViewById(R.id.velocidad);
        velocidad.setOnSeekBarChangeListener(new StartPointSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onOnSeekBarValueChange(StartPointSeekBar bar, double value) {
                mensaje_locos(value);
            }
        });

        velocidad.setProgress(0);


        Items_log.add("-> Inicio");

        lv1 = (ListView) findViewById(R.id.listview_log);
        lv1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Items_log) {
                           @Override
                           public View getView(int position, View convertView, ViewGroup parent) {
                               TextView textView = (TextView) super.getView(position, convertView, parent);
                               textView.setTextSize(15);
                               if (textView.getText().toString().substring(0, 2).toString().equals("->")) {
                                   textView.setTextColor(Color.parseColor("#64A477"));
                               } else {
                                   textView.setTextColor(Color.parseColor("#2326DB"));
                               }
                               ViewGroup.LayoutParams params = textView.getLayoutParams();
                               params.height = 80;
                               textView.setLayoutParams(params);

                               return textView;
                           }
                       }
        );


        adaptador1 = (ArrayAdapter) lv1.getAdapter();


        //Listener
        connectBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectClick(v);
            }
        });
        connectBt.setText(connect);

        final Button stopBt = (Button) findViewById(R.id.stopBt);
        stopBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stop_maquina(v);
            }
        });
        final Button stopAllBt = (Button) findViewById(R.id.stopallBt);
        stopAllBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stop_all(v);
            }
        });
        sentidoBt = (Button) findViewById(R.id.sentidoBt);
        sentidoBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cambio_de_sentido_maquina(v);
            }
        });

        final Button activar_accesorioBt = (Button) findViewById(R.id.activar_accesorioBt);
        activar_accesorioBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mensaje_accesorio(v);
            }
        });
        final Button desactivar_accesorioBt = (Button) findViewById(R.id.desactivar_accesorioBt);
        desactivar_accesorioBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mensaje_accesorio(v);
            }
        });
        final Button cambiolocoBt = (Button) findViewById(R.id.cambiolocoBt);
        cambiolocoBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cambiolocoBt();
            }
        });
        Button auxBt = (Button) findViewById(R.id.auxBt);
        auxBt.setText("S88");

        auxBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                auxBt(v);
            }
        });
        checkBox_accesorio = (CheckBox) findViewById(R.id.checkBox_accesorio);
        checkBox_accesorio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = ((CheckBox) view).isChecked();

                if (isChecked) {
                    checkBox_loco.setChecked(false);
                } else {
                    checkBox_loco.setChecked(true);
                    ;
                }
            }
        });

        checkBox_loco = (CheckBox) findViewById(R.id.checkBox_loco);
        checkBox_loco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = ((CheckBox) view).isChecked();

                if (isChecked) {
                    checkBox_accesorio.setChecked(false);
                } else {
                    checkBox_accesorio.setChecked(true);
                    ;
                }
            }
        });
        checkBox_accesorio.setChecked(true);


        //Listener para botones numéricos

        View.OnClickListener marcar = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSoundEffectsEnabled(true);
                v.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
                marcador_numerico(v);
            }
        };

       /*View.OnTouchListener tocar = new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                   v.setBackgroundColor(Color.RED);
                    //Button Pressed
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundColor(Color.BLUE);
                    //finger was lifted
                }
                return false;
            }
        };
        ((Button) findViewById(R.id.button0)).setOnTouchListener(tocar);
*/
        //Asignación de listener para botones numéricos
         findViewById(R.id.button0).setOnClickListener(marcar);
         findViewById(R.id.button1).setOnClickListener(marcar);
         findViewById(R.id.button2).setOnClickListener(marcar);
         findViewById(R.id.button3).setOnClickListener(marcar);
         findViewById(R.id.button4).setOnClickListener(marcar);
         findViewById(R.id.button5).setOnClickListener(marcar);
         findViewById(R.id.button6).setOnClickListener(marcar);
         findViewById(R.id.button7).setOnClickListener(marcar);
         findViewById(R.id.button8).setOnClickListener(marcar);
         findViewById(R.id.button9).setOnClickListener(marcar);


        //Listener para botones de funciones
        View.OnClickListener laspfes = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mensaje_funciones(v);
            }
        };

        //Asignación de listener para botones de funciones

        findViewById(R.id.buttonfl).setOnClickListener(laspfes);
        findViewById(R.id.buttonf1).setOnClickListener(laspfes);
        findViewById(R.id.buttonf2).setOnClickListener(laspfes);
        findViewById(R.id.buttonf3).setOnClickListener(laspfes);
        findViewById(R.id.buttonf4).setOnClickListener(laspfes);
        findViewById(R.id.buttonf5).setOnClickListener(laspfes);
        findViewById(R.id.buttonf6).setOnClickListener(laspfes);
        findViewById(R.id.buttonf7).setOnClickListener(laspfes);
        findViewById(R.id.buttonf8).setOnClickListener(laspfes);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        initPreferences();
        statusText.setText("Puerto : " + portName);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void stop_maquina(View v) {

        mensaje_locos(0);
        velocidad.setProgress(0);
    }

    private void stop_all(View v) {

        byte[] orden = new byte[]{(byte) 0x80, (byte) 0x80};
        enviar_orden(orden);
        velocidad.setProgress(0);

        for (int idx = 0; idx <= (locosyfunciones.length - 1); idx++) {
            locosyfunciones[idx][9] = (int) (0);
        }
    }

    private void cambio_de_sentido_maquina(View v) {

        int nueva_velocidad = locosyfunciones[posicion_loco][9] * -1;
        mensaje_locos(nueva_velocidad);
        velocidad.setProgress(nueva_velocidad);
    }

    private void auxBt(View v) {


        if (((Button) v).getText().toString() == "S88") {
            ((Button) v).setText("Log");
            gridview_s88.setVisibility(View.VISIBLE);
            lv1.setVisibility(View.INVISIBLE);
            estado_s88();
        } else {
            ((Button) v).setText("S88");
            gridview_s88.setVisibility(View.INVISIBLE);
            lv1.setVisibility(View.VISIBLE);
        }
    }

    // Method for converting DP value to pixels
    public static int getPixelsFromDPs(Activity activity, int dps) {
        Resources r = activity.getResources();
        int px = (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dps, r.getDisplayMetrics()));
        return px;
    }

    private void cambiolocoBt() {
        if (locomotora > 0) {
            return;
        }
        int locomo;
        try {
            locomo = Integer.parseInt(loco.getText().toString());
        } catch (Exception e) {
            return;
            //						e.printStackTrace();
        }
        if (locomo == 0) {
            return;
        }
        locomotora = locomo;
        loco.setBackgroundColor(Color.GREEN);

        for (posicion_loco = 0; posicion_loco <= (locosyfunciones.length - 1); posicion_loco++) {
            if ((locosyfunciones[posicion_loco][10] == locomotora) || (locosyfunciones[posicion_loco][10] == 0)) {
                break;

            }
        }

        if (posicion_loco > locosyfunciones.length - 1) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Ha alcanzado el numero maximo de locomotoras que es de " + String.valueOf( locosyfunciones.length));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            return;
        }

        if (locosyfunciones[posicion_loco][0] == 0) {
            locosyfunciones[posicion_loco][10] = locomotora;
        }
        velocidad.setProgress(locosyfunciones[posicion_loco][9]);
        View v = findViewById(R.id.buttonfl); //La inicializo para que no se queje
        for (int x = 0; x < 9; x++) {
            switch (x) {
                case 0:
                    v = findViewById(R.id.buttonfl);
                    break;
                case 1:
                    v = findViewById(R.id.buttonf1);
                    break;
                case 2:
                    v = findViewById(R.id.buttonf2);
                    break;
                case 3:
                    v = findViewById(R.id.buttonf3);
                    break;
                case 4:
                    v = findViewById(R.id.buttonf4);
                    break;
                case 5:
                    v = findViewById(R.id.buttonf5);
                    break;
                case 6:
                    v = findViewById(R.id.buttonf6);
                    break;
                case 7:
                    v = findViewById(R.id.buttonf7);
                    break;
                case 8:
                    v = findViewById(R.id.buttonf8);
                    break;
            }

            Drawable d = v.getBackground();
            if (locosyfunciones[posicion_loco][x] == 0) {
                PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);
                d.setColorFilter(filter);
            } else {
                PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                d.setColorFilter(filter);

            }

        }

    }

    private void mensaje_locos(double speed) {
        /*
        E4 10+n 00 ADR SPD xx Control de locomotora en 14,27,28 y 128 pasos
        Este es el formato de la versión 3 que soporta 14, 27, 28 y 128 pasos, los valores de velocidad son los
        mismos que en la orden Locomotive information request (v.3). A partir de ahora la locomotora se
        controlará con los pasos especificados.
        n: 0: 14 pasos 1: 27 pasos (se usaran 28 pasos pero sin máxima velocidad) 2: 28 pasos 3: 128 pasos

        SPD (RVVVVVVV): Bit 7 (R): Dirección, 1:Adelante, 0:Atrás
                        Bit 0 a 6 (V): Velocidad actual (0: stop, 1: stop emergencia)
                                      14 pasos: 0 a 15
                                  27, 28 pasos: Bit 0 a 3: ve
                                     128 pasos: 0 a 127
         */


        if (locomotora == 0) {
            return;
        }
        if (esperando_ok) {
            //          return;
        }
        byte[] orden = new byte[]{(byte) 0xE4, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        orden[1] = (byte) 0x10 + 3;  //lo hago siempre con 128 pasasos. veremos

        orden[3] = (byte) (locomotora & 0xff);
        int l_speed = (int) (speed);
        if (l_speed > 0) {
            orden[4] = (byte) (0x80); // ponemos el byte 7 a 1
            orden[4] += (byte) (l_speed);
        } else {
            orden[4] += (byte) (l_speed * -1);
        }

        int ixor = (orden[0] ^ orden[1] ^ orden[2] ^ orden[3] ^ orden[4]);
        if (ixor > 0x80) {
            ixor = (ixor - 0x80) * -1;
        }
        orden[5] = (byte) (ixor & 0xFF);
        enviar_orden(orden);

        esperando_ok = true;
      //  statusText.setText(Double.toString(speed));
        locosyfunciones[posicion_loco][9] = (int) (speed);

        if (speed >= 0) {
            sentidoBt.setText("-->");
        } else {
            sentidoBt.setText("<--");

        }


    }

    private void mensaje_funciones(View v) {
       /* Locomotive Function Operations (XpressNet v.3)
        Header Data Xor Descripción comando
        E4 20 00 ADR FNA xx Control de las funciones
        Este es el formato de la versión 3 ya que se controlan aparte las funciones, los valores de las funciones
        FL, F1 a F4 son los mismos que en la orden Locomotive information request (v.3).*/

        // if(device==null || device.isOpen()==false) {return;}
        if (locomotora == 0) {
            return;
        }

        Button botonpf = (Button) v;

        int tecla_funcion;
        if (String.valueOf(botonpf.getText().charAt(1)).equals("L")) {
            tecla_funcion = 0;
        } else {
            tecla_funcion = Character.getNumericValue(botonpf.getText().charAt(1));
        }

        Drawable d = v.getBackground();
        if (locosyfunciones[posicion_loco][tecla_funcion] == 0) {
            locosyfunciones[posicion_loco][tecla_funcion] = 1;
            PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
            d.setColorFilter(filter);
        } else {

            locosyfunciones[posicion_loco][tecla_funcion] = 0;
            PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);
            d.setColorFilter(filter);
        }

        byte[] orden = new byte[]{(byte) 0xE4, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

        orden[3] = (byte) (locomotora & 0xff);
        int suma_funciones = 0;
        if (tecla_funcion < 5) {
            orden[1] = (byte) 0x20;
            for (int x = 1; x < 5; x++) {
                suma_funciones += ((locosyfunciones[posicion_loco][x]) * x);
            }
            if (locosyfunciones[posicion_loco][0] == 1) {
                suma_funciones += 16; // es el bit 1 del nibble superior y representa FL
            }
        } else {
            orden[1] = (byte) 0x21;
            for (int x = 5; x < 9; x++) {
                suma_funciones += ((locosyfunciones[posicion_loco][x]) * x);
            }
        }
        orden[4] = (byte) (suma_funciones & 0xFF);
        int ixor = (orden[0] ^ orden[1] ^ orden[2] ^ orden[3] ^ orden[4]);
        if (ixor > 0x80) {
            ixor = (ixor - 0x80) * -1;
        }
        orden[5] = (byte) (ixor & 0xFF);
        enviar_orden(orden);
        //

    }

    private void mensaje_accesorio(View v) {

        int num_accesorio;
        try {
            num_accesorio = Integer.parseInt(accesorio.getText().toString());

        } catch (Exception e) {
            return;
            //						e.printStackTrace();
        }

        if (num_accesorio == 0) {
            return;
        }
        accesorio_lanzado = true;

        //  Toast.makeText(MainActivity.this, 'desconectar', Toast.LENGTH_SHORT).show();
        byte[] orden = new byte[]{(byte) 0x52, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        int i_direccion = (num_accesorio - 1) / 4;
        int i_salida = (num_accesorio % 4);

        if (i_salida == 0) {
            i_salida = 4;
        }
        i_salida = (i_salida - 1) * 2;

        orden[1] = (byte) i_direccion;

        if (v.getId() == R.id.desactivar_accesorioBt) {
            i_salida++;
        }

        orden[2] = (byte) (0x80 + 0x08 + i_salida);
        int ixor = (orden[0] ^ orden[1] ^ orden[2]);
        if (ixor > 0x80) {
            ixor = (ixor - 0x80) * -1;
        }
        orden[3] = (byte) (ixor & 0xFF);
        // textSent(orden);
        enviar_orden(orden);

        do {
            try {
                wait(500);
            } catch (Exception e) {
                return;
                //						e.printStackTrace();
            }

        }
        while (esperando_ok);


        orden[2] = (byte) (0x80 + i_salida);
        ixor = (orden[0] ^ orden[1] ^ orden[2]);
        if (ixor > 0x80) {
            ixor = (ixor - 0x80) * -1;
        }
        orden[3] = (byte) (ixor & 0xFF);
        //   textSent(orden);
        enviar_orden(orden);


    }

    private void estado_s88() {

        if (device != null && device.isOpen() == true) {
            byte[] orden = new byte[]{(byte) 0x42, (byte) 0x00, (byte) 0x00, (byte) 0x00};
            for (int idx = 0; idx < 16; idx++) {
                for (int idxbis = 0; idxbis < 2; idxbis++) {
                    orden[1] = (byte) (0x40 + idx);
                    orden[2] = (byte) (0x80 + idxbis);
                    int ixor = (orden[0] ^ orden[1] ^ orden[2]);
                    if (ixor > 0x80) {
                        ixor = (ixor - 0x80) * -1;
                    }
                    orden[3] = (byte) (ixor & 0xFF);
                    enviar_orden(orden);

                }

            }
        }
    }

    private void textSent(final byte[] enviado) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuffer hexString = new StringBuffer();
                for (byte b : enviado) {
                    int intVal = b & 0xff;
                    if (intVal < 0x10) hexString.append("0");
                    hexString.append(Integer.toHexString(intVal));
                    hexString.append(" ");

                }
                actualizar_log("<-" + hexString.toString().toUpperCase());

            }
        });
    }

    private void textReceived(final byte[] text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuffer hexString = new StringBuffer();
                for (byte ch : text) {
                    hexString.append(Integer.toHexString(0xFF & ch) + " ");

                }
                actualizar_log("->" + hexString.toString().toUpperCase());
            }
        });
    }

    private void respuesta_s88() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
 /*               recibido = new byte[4];
                  recibido[0] = (byte) 0x42;
                  recibido[1] = (byte) 0x40;
                  recibido[2] = (byte) 0x51;
                  recibido[3] = (byte) 0x53;

                  recibido = new byte[6];
                  recibido[0] = (byte) 0x44;
                  recibido[1] = (byte) 0x45;
                  recibido[2] = (byte) 0x41;
                  recibido[3] = (byte) 0x44;
                  recibido[4] = (byte) 0x44;
                  recibido[5] = (byte) 0x40;
                  */


                int idx = 0;
                int resultado_xor = (byte) 0x0;
                for (idx = 0; idx < recibido.length - 1; idx++) {
                    resultado_xor = resultado_xor ^ recibido[idx];
                }
                if (resultado_xor != recibido[idx]) return;

                int num_bytes_datos = recibido[0] & 0xF;
                int direccion_s88;
                int datos;
                int suma_por_nibble = 0;
                int linea_cola;
                int posicion_cola;
                boolean tabla_lado_derecho;

              /*  Bit 5,6: 00: Accesorio sin información (DCC_Gen lo da para desvíos)
                01: Accesorio con información (no soportado)
                10: Retromodulo
                Bit 4: Nibble alto si 1, Nibble bajo si 0
                Bit 0..3: Estado de las entradas. */

                for (idx = 1; idx < num_bytes_datos; idx += 2) {
                    direccion_s88 = recibido[idx];
                    datos = recibido[idx + 1];
                    //40 en hexadecimal es 1000000. si es informacion de retromodulo los bist 5 y 6 deben ser 10
                    if ((datos & 0x40) != 0x40) return;
                    if ((datos & 0x10) == 0x10) {
                        suma_por_nibble = 4;
                    } //nibble = 1 es el nibble alto
                    direccion_s88 = direccion_s88 - 0x40;

                    tabla_lado_derecho = false;
                    if (direccion_s88 % 2 == 1) tabla_lado_derecho = true;
                    linea_cola = (direccion_s88 / 2) + 1;

                    posicion_cola = (linea_cola * columnas_cola);

                    if (tabla_lado_derecho) {
                        posicion_cola += 9;
                    }

                    int estado = 0;
                    for (int idx_aux = 1; idx_aux < 5; idx_aux++) {
                        if (idx_aux == 4) {
                            estado = datos & 8;
                        }
                        if (idx_aux == 3) {
                            estado = datos & 4;
                        }
                        if (idx_aux == 2) {
                            estado = datos & 2;
                        }
                        if (idx_aux == 1) {
                            estado = datos & 1;
                        }

                        int posicion_grid = posicion_cola + idx_aux + suma_por_nibble;

                        TextView v = (TextView) gridview_s88.getChildAt(posicion_grid);
                        if (v != null) {
                            if (estado > 0) {
                                v.setBackgroundColor(Color.parseColor("#ff0000"));
                                v.setTextColor(Color.parseColor("#ff0000"));
                            } else {
                                v.setBackgroundColor(Color.parseColor("#eeeeee"));
                                v.setTextColor(Color.parseColor("#eeeeee"));
                            }
                        }
                    }
                    stop_recibir = false;
                    //   estado_s88_List.set(posicion_cola, "1".toString());
                    //   adaptador_s88.notifyDataSetChanged();
                }

            }
        });
    }

    private void actualizar_log(String text) {
        Items_log.add(text.toString());
        if (Items_log.size() > 40) {
            Items_log.remove(0);
        }
        adaptador1.notifyDataSetChanged();
        lv1.smoothScrollToPosition(Items_log.size());
    }

    private void enviar_orden(final byte[] orden) {
        if (device != null && device.isOpen() == true) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] enviado;
                        OutputStream out = device.getOutputStream();
                        out.write(orden);
                        out.flush();
                        textSent(orden);

                    } catch (Exception e) {
                        disconnect();
                        //						e.printStackTrace();
                    }
                }
            });
        }

        // dejo que corra esto en el mismo thread de la aplicacion

    /*        Runnable conRun = new Runnable() {
                public void run() {
                    try {
                        byte[] enviado;
                        OutputStream out = device.getOutputStream();
                        out.write(orden);
                        out.flush();
                        textSent(orden);

                    } catch (Exception e) {
                        disconnect();
//						e.printStackTrace();
                    }
                }
            };
            new Thread(conRun).start();
        }*/
    }

    private void marcador_numerico(View v) {
        TextView txt = (TextView) v;
        String valor_anterior = "";
        if (checkBox_accesorio.isChecked()) {
            if (accesorio_lanzado) {
                valor_anterior = "";
            } else {
                valor_anterior = accesorio.getText().toString();
            }
            accesorio_lanzado = false;
            if (valor_anterior.equals("0")) {
                valor_anterior = "".toString();
            }
            if (Integer.parseInt(valor_anterior + txt.getText()) > 999) {
                accesorio.setText(txt.getText().toString());
            } else {
                accesorio.setText(valor_anterior + txt.getText().toString());
            }

        } else {

            loco.setBackgroundColor(Color.RED);
            if (locomotora == 0) {
                valor_anterior = loco.getText().toString();
            }
            if (valor_anterior.equals("0")) {
                valor_anterior = "".toString();
            }

            if (Integer.parseInt(valor_anterior + txt.getText()) > 99) {
                loco.setText(txt.getText().toString());
            } else {
                loco.setText(valor_anterior + txt.getText().toString());
                if (loco.getText().toString().length() == 2){
                    cambiolocoBt();
                    return;

                }

            }
            locomotora = 0;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
     //   connectClick(connectBt);
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
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
        if (id == R.id.action_settings) {
            ayuda();
            return true;
        }
        if (id == R.id.puerto_settings) {
            elegir_portname();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initPreferences() {
        if (userPrefs == null) {
            userPrefs = new UserPrefs(getSharedPreferences(MainActivity.sharedPreferencesName, Context.MODE_PRIVATE));
        }
        portName = userPrefs.getString("puerto");
    }



    private void connectClick(View v) {

        TextView txt = (TextView) v;
        if (!(txt.getText().toString().equals(this.getResources().getString(connect)))) {
            disconnect();
            return;
        }
        if (portName == null) {
           elegir_portname();
       }
        if (portName == null)  {
            return;
        }
        if ( !activar_bluetooth()){
            return;
        };
        //portName = "BOLUTEK";
       connect();
       inicializar_conexion();
       estado_s88();



    }


    private void inicializar_conexion() {
        if (device != null && device.isOpen() == true) {
            Runnable conRun = new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                        while (true) {
                            byte[] enviado;
                            OutputStream out = device.getOutputStream();
                       /* enviado = new byte[]{(byte) 0x52, (byte) 0x02, (byte) 0x8F, (byte) 0xDF}; */
                            enviado = new byte[]{(byte) 0x21, (byte) 0x24, (byte) 0x05};
                            out.write(enviado);
                            out.flush();
                            textSent(enviado);
                            Thread.sleep(1000);
                            if (Integer.toHexString(recibido[0]).equals("62")) {
                                break;
                            } else {
                                enviado = new byte[]{(byte) 0xF0, (byte) 0xF0};
                                out.write(enviado);
                                out.flush();
                                textSent(enviado);
                                Thread.sleep(1000);
                                if (recibido[0] == (byte) 0x02) {
                                    break;
                                } else {
                                    enviado = new byte[]{(byte) 0x21, (byte) 0x21, (byte) 0x00};
                                    out.write(enviado);
                                    out.flush();
                                    textSent(enviado);
                                    Thread.sleep(1000);
                                    if (recibido[0] == (byte) 0x02) {
                                        break;
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        disconnect();
                        //						e.printStackTrace();
                    }
                }
            };
            new Thread(conRun).start();
        }
    }

//  /*      private void mostrar_estado(final String message) {
//                Runnable conRun = new Runnable() {
//                    public void run() {
//                        try {
//                            statusText.setText(message);
//                        } catch (Exception e) {
//                            //e.printStackTrace();
//                        }
//                    }
//        };
//        execute(conRun);
//    }
//*/








    private void connectError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(message);
                connectBt.setText(connect);
            }
        });
        deviceUnselect();
    }


    private void notFound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(R.string.device_not_found);
                connectBt.setText(connect);
            }
        });
        deviceUnselect();
    }

    private void connect() {
        deviceUnselect();
        deviceSelect();


        if (device != null) {
            Runnable conRun = new Runnable() {
                public void run() {
                    try {
//                        device.setParameters(userPrefs.getInt("0"), (byte) userPrefs.getInt("0"), (byte) userPrefs.getInt("0"), (byte) userPrefs.getInt("0"));
//                       // device.setParameters(userPrefs.getInt("configBaudrate"), (byte) userPrefs.getInt("configDatabits"), (byte) userPrefs.getInt("configStopbits"), (byte) userPrefs.getInt("configParity"));
                        device.open();
                    } catch (IOException e) {
                      connectError(e.getMessage());
                    } catch (Exception e) {
                    }
                }
            };
            execute(conRun);

          while (thread != null && thread.isAlive()) {
            }
            if (device == null) {
                return;
            }

            if (device.isOpen() == true) {
                //Open inStream
                try {
                    final InputStream inStream = device.getInputStream();
                    //Start read loop
                    Runnable r = new Runnable() {
                        public void run() {
                            try {
                                while (inStream != null && device.isOpen()) {
                                    try {
                                        byte[] buffer = new byte[1024];
                                        int leido;
                                        int num_bytes;
                                        if (inStream.available() > 0) {
                                            leido = inStream.read();
                                            if (leido != -1) {
                                                buffer[0] = (byte) leido;
                                                num_bytes = buffer[0] & 0xF;
                                                int idx = 0;
                                                while (true) {
                                                    if (inStream.available() > 0) {
                                                        leido = inStream.read();
                                                        buffer[idx + 1] = (byte) leido;
                                                        idx++;
                                                        if (idx > num_bytes) {
                                                            break;
                                                        }
                                                    }
                                                }
                                                recibido = new byte[num_bytes + 2];
                                                System.arraycopy(buffer, 0, recibido, 0, num_bytes + 2);
                                                textReceived(recibido);

                                                if (recibido[0] == 0x42) {
                                                    stop_recibir = true;
                                                    respuesta_s88();
                                                    while (stop_recibir) {

                                                    }
                                                }

                                                if (esperando_ok) {
                                                    if (recibido[0] == (byte) 0x01 && recibido[1] == (byte) 0x04 && recibido[2] == (byte) 0x05) {
                                                        esperando_ok = false;
                                                    }
                                                }

                                            }
                                        }
                                    } catch (IOException e) {
                                        err++;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    execute(r);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectBt.setText(R.string.disconnect);

                        }
                    });

                } catch (Exception e) {
                    disconnect();
                    e.printStackTrace();
                }


            }
        }
        if (device == null || device.isOpen() == false) {
            notFound();
        }
    }

    private void disconnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(R.string.disconnected);
                connectBt.setText(connect);
            }
        });
        deviceUnselect();
    }


    private void deviceSelect() {
        try {
            UartBluetooth uart = new UartBluetooth(portName);
            this.device = uart;

        } catch (Exception e) {
//					e.printStackTrace();
        }
    }


    private void deviceUnselect() {
        cancel();
        Runnable conRun = new Runnable() {
            public void run() {
                try {
                    device.close();
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        };
        execute(conRun);
        while (thread != null && thread.isAlive()) {
        }
        device = null;
    }

    private void execute(Runnable r) {
        cancel();
        thread = new Thread(r);
        thread.start();
    }

    private void cancel() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client.connect();

    }

    @Override
    public void onStop() {
        super.onStop();
        //client.disconnect();
    }
    public void ayuda() {
        // con este tema personalizado evitamos los bordes por defecto
        customDialog = new Dialog(this);
       //deshabilitamos el título por defecto
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //obligamos al usuario a pulsar los botones para cerrarlo
        customDialog.setCancelable(false);
        //establecemos el contenido de nuestro dialog
        customDialog.setContentView(R.layout.dialogo_ayuda);

        TextView titulo = (TextView) customDialog.findViewById(R.id.titulo);
        titulo.setText("- - - -    A  y  u  d  a    - - - -   ");

        TextView contenido = (TextView) customDialog.findViewById(R.id.contenido);

        String text =  "\n 1 - Antes de conectar debe indicar el dispositivo Bluetooth al que se va a conectar. Este debe estar pareado con el telefono para que aparezca en la lista de dispositivos";
               text += "\n\n 2 - Elegir Accesorio o Locomotora y teclear el nº del mismo. En locomotora despues de teclear pulsar la tecla OK para aceptarlo.El fondo se pondra verde ";
               text += "\n\n 3 - El programa recuerda la velocidad y estado de funciones de hasta 30 Locomotiras.";
               text += "\n\n 4 - El boton STOP realiza un stop de emergencia de todas las locomotoras.";
               text += "\n ";

        contenido.setText(text);

        ((Button) customDialog.findViewById(R.id.aceptar)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {
                customDialog.dismiss();
            }
        });

        customDialog.show();
    }


    public boolean activar_bluetooth() {

        try {
            if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
                Toast.makeText(getApplicationContext(), R.string.bt_enable, Toast.LENGTH_LONG).show();
                while (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) ;
                {
                }
               // statusText.setText("Puerto : " + portName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
        return true;
    }

    public void elegir_portname() {

       if (!activar_bluetooth()){return;};


        List<String> listItems = new ArrayList<String>();

        Set<BluetoothDevice> pariedDevices = mBluetoothAdapter.getBondedDevices();
        if (pariedDevices.size() > 0) {
            for (BluetoothDevice device : pariedDevices) {
                listItems.add(device.getName());
            }
        }
        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Elige el dispositivo bluetooth");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                portName = items[item].toString();
                userPrefs.saveString("puerto",portName);
                statusText.setText(portName);
                Toast toast = Toast.makeText(getApplicationContext(), "Haz elegido la opcion: " + items[item], Toast.LENGTH_SHORT);
                toast.show();
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
     }

}