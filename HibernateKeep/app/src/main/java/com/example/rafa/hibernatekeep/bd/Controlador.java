package com.example.rafa.hibernatekeep.bd;


import android.content.Context;
import android.util.Log;

import com.example.rafa.hibernatekeep.gestion.GestorKeep;
import com.example.rafa.hibernatekeep.pojo.Keep;
import com.example.rafa.hibernatekeep.pojo.Usuario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Controlador {

    private GestorKeep gk;
    private String urlDireccion = "http://192.168.56.1:8080/Keep/go";
    private BufferedReader in = null;
    private String login, res ="";
    private URL url = null;

    public Controlador(Context context) {
        this.gk = new GestorKeep(context);
    }

    public Controlador() {
    }

    public List<Keep> obtenerNotas(Usuario u) {
        List<Keep> lista = new ArrayList<>();
        try {
            login = URLEncoder.encode(u.getEmail(), "UTF-8");
            String destino = urlDireccion + "?tabla=keep&op=read&login=" + login + "&origen=android&accion=";
            url = new URL(destino);
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            String linea;
            while ((linea = in.readLine()) != null) {
                res += linea;
            }
            in.close();
            JSONObject obj = new JSONObject(res);
            JSONArray array = (JSONArray) obj.get("r");
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = (JSONObject) array.get(i);
                Keep keep = new Keep(o.getInt("idAndroid"), o.getString("contenido"), true);
                lista.add(keep);
            }
            return lista;
        } catch (MalformedURLException e) {
            Log.v("error", e.toString());
        } catch (IOException e) {
            Log.v("error2", e.toString());
        } catch (JSONException e) {
            Log.v("error3", e.toString());
        }
        return null;
    }

    public long getNextAndroidId(List<Keep> lista) {
        long next = -1;
        for (Keep k : lista) {
            if (k.getId() > next) {
                next = k.getId();
            }
        }
        return next + 1;
    }

    public List<Keep> cargarNota(List<Keep> l, Usuario u) {
        gk.open();
        List<Keep> lista, lista2 = new ArrayList<>();
        lista = obtenerNotas(u);
        try {
            login = URLEncoder.encode(u.getEmail(), "UTF-8");
            for (Keep k : l) {
                if(!k.isEstado()) {
//                    if(lista.contains(k)){
//                        String destino = urlDireccion + "?tabla=keep&op=delete&login=" + login + "&origen=android&idAndroid=" + k.getId() + "&contenido=" + k.getContenido() + "&accion=";
//                        url = new URL(destino);
//                        in = new BufferedReader(new InputStreamReader(url.openStream()));
//                    }
                    String destino = this.urlDireccion + "?tabla=keep&op=create&login=" + login + "&origen=android&idAndroid=" + k.getId() + "&contenido=" + k.getContenido().replace(" ", "+") + "&accion=";
                    url = new URL(destino);
                    in = new BufferedReader(new InputStreamReader(url.openStream()));
                    String linea;
                    while ((linea = in.readLine()) != null) {
                        res += linea;
                    }
                    in.close();
                    k.setEstado(true);
                    gk.changeState(k);
                }
                lista2.add(k);
            }
            gk.close();
            return lista2;
        } catch (MalformedURLException e) {
            Log.v("error4", e.toString());
        } catch (IOException e) {
            Log.v("error5", e.toString());
        }
        gk.close();
        return null;
    }

    public void borrarNota(Keep k, Usuario u){
        try {
            login = URLEncoder.encode(u.getEmail(), "UTF-8");
            String destino = this.urlDireccion + "?tabla=keep&op=delete&login=" + login + "&origen=android&idAndroid=" + k.getId() + "&contenido=" + k.getContenido() + "&accion=";
            url = new URL(destino);
            in = new BufferedReader(new InputStreamReader(url.openStream()));
        } catch (MalformedURLException e) {
            Log.v("error6", e.toString());
        } catch (IOException e) {
            Log.v("error7", e.toString());
        }
    }
}
