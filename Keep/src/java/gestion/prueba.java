package gestion;

import hibernate.Keep;
import org.json.JSONException;

public class prueba {
    
    
    public static void main(String[] args) throws JSONException {
        Keep k = new Keep(null, 317, "contenido", null, "inestable");
        GestorKeep.añadir(k, "pepe");
    }
}
