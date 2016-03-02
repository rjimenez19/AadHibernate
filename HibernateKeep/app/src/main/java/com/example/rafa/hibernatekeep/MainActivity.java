package com.example.rafa.hibernatekeep;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.rafa.hibernatekeep.bd.Controlador;
import com.example.rafa.hibernatekeep.gestion.GestorKeep;
import com.example.rafa.hibernatekeep.pojo.Keep;
import com.example.rafa.hibernatekeep.pojo.Usuario;
import com.example.rafa.hibernatekeep.util.Adaptador;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView lv;
    private Adaptador adp;

    private Usuario usuario;
    private Controlador controlador = new Controlador(this);
    private GestorKeep gestorKeep= new GestorKeep(this);

    private boolean conexion = false;
    private List<Keep> listaNotas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogoAñadir();
            }
        });

        componentes();
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sincronizar){
            sincronizar sinc = new sincronizar();
            sinc.execute();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gestorKeep.close();
    }

    @Override
    protected void onResume() {
        gestorKeep.open();
        listaNotas = gestorKeep.select(null);
        adp = new Adaptador(MainActivity.this, R.layout.item, listaNotas);
        lv.setAdapter(adp);
        super.onResume();
    }

    public void componentes(){
        lv = (ListView) findViewById(R.id.listView);
        adp = new Adaptador(MainActivity.this, R.layout.item, listaNotas);
        usuario = getIntent().getParcelableExtra("usuario");
    }

    public void init() {

        //Comprobar el usuario
        if (usuario != null) {
            Toast.makeText(this, usuario.getEmail(), Toast.LENGTH_LONG).show();
            conexion = true;
        }

        //OnClick para editar
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View view = inflater.inflate(R.layout.aniadir, null);

                final EditText ed = (EditText) view.findViewById(R.id.editText);
                adb.setView(view).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Keep k = listaNotas.get(position);
                        k.setContenido(ed.getText().toString());
                        k.setEstado(false);
                        gestorKeep.updateContenido(k);
                        if (isInternet()) {
                            añadir hebra = new añadir();
                            hebra.execute();

                            sincronizar sinc = new sincronizar();
                            sinc.execute();
                        }
                        adp.notifyDataSetChanged();
                }
            }).setNegativeButton("Cancelar", null).show();
            }
        });

        //Long click para el delete
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setMessage("¿Estas seguro de que quieres borrar?").setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Keep k = listaNotas.get(position);
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                controlador.borrarNota(k, usuario);
                            }
                        };
                        Thread t = new Thread(r);
                        t.start();

                        sincronizar sinc = new sincronizar();
                        sinc.execute();
                        gestorKeep.delete(k);
                        listaNotas.remove(position);
                        adp.notifyDataSetChanged();
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
                return false;
            }
        });
    }

    //Comprobar si hay internet
    public boolean isInternet() {
        ConnectivityManager m = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean a3g = m.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean wifi = m.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        if (a3g || wifi) {
            return true;
        }
        return false;
    }

    //Asynctask añadir
    private class añadir extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            listaNotas = controlador.cargarNota(listaNotas, usuario);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    //Asynctask sincronizar
    private class sincronizar extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            List<Keep>listaNotasBD = controlador.obtenerNotas(usuario);
            List<Keep> ambas = new ArrayList<>();
            ambas.addAll(listaNotasBD);

            for(Keep k: listaNotas){
                if(!k.isEstado()){
                    ambas.add(k);
                }
            }
            listaNotas = ambas;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adp.notifyDataSetChanged();
            añadir hebra = new añadir();
            hebra.execute();
        }
    }

    //Añadir una nota
    public void dialogoAñadir(){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.aniadir, null);
        final EditText ed = (EditText) view.findViewById(R.id.editText);
        adb.setView(view).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Keep k= new Keep(controlador.getNextAndroidId(listaNotas), ed.getText().toString(), false);
                listaNotas.add(new Keep(controlador.getNextAndroidId(listaNotas), ed.getText().toString(), false));
                adp.notifyDataSetChanged();
                if (isInternet()) {
                    añadir a = new añadir();
                    a.execute();

                    sincronizar sinc = new sincronizar();
                    sinc.execute();
                }
                gestorKeep.insert(k);
                adp.notifyDataSetChanged();
            }
        }).setNegativeButton("Cancelar", null).show();
        adp.notifyDataSetChanged();
    }
}

