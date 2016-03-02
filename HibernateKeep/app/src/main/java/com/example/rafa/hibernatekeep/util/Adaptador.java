package com.example.rafa.hibernatekeep.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.rafa.hibernatekeep.R;
import com.example.rafa.hibernatekeep.pojo.Keep;

import java.util.ArrayList;
import java.util.List;

public class Adaptador extends ArrayAdapter<Keep> {

    private Context contexto;
    private int resources;
    private LayoutInflater inflator;
    private List<Keep> lista;

    static class ViewHolder {
        public TextView tv1, tv2;
    }

    public Adaptador(Context context, int resource, List<Keep> objects) {
        super(context, resource, objects);
        this.contexto = context;
        this.resources = resource;
        this.lista = objects;
        this.inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public boolean borrar(int position) {
        try {
            lista.remove(position);
            this.notifyDataSetChanged();
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //1
        ViewHolder gv = new ViewHolder();
        if(convertView==null){
            convertView = inflator.inflate(resources, null);
            TextView tv = (TextView) convertView.findViewById(R.id.textView2);
            gv.tv1 = tv;
            tv = (TextView) convertView.findViewById(R.id.textView3);
            gv.tv2 = tv;

            convertView.setTag(gv);
        } else {
            gv = (ViewHolder) convertView.getTag();
        }
        gv.tv1.setText(lista.get(position).getContenido());
        gv.tv2.setText(lista.get(position).isEstado()+"");
        return convertView;
    }
}
