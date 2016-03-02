package com.example.rafa.hibernatekeep.bd;

import android.provider.BaseColumns;

public class Contrato {

    private Contrato(){

    }

    public static abstract class TablaKeep implements BaseColumns {
        public static final String TABLA = "keep";
        public static final String NOTA ="nota";
        public static final String ESTADO = "estado";
    }
}
