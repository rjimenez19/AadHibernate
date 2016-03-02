package controlador;

import gestion.GestorKeep;
import gestion.GestorUsuario;
import hibernate.Keep;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

@WebServlet(name = "Controlador", urlPatterns = {"/go"})
public class Controlador extends HttpServlet {

    enum Camino {
        forward, redirect, print;
    }

    class Destino {

        public Camino camino;
        public String url;
        public String texto;

        public Destino() {
        }

        public Destino(Camino camino, String url, String texto) {
            this.camino = camino;
            this.url = url;
            this.texto = texto;
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException {
        String tabla = request.getParameter("tabla");
        String op = request.getParameter("op");
        String accion = request.getParameter("accion");
        String origen = request.getParameter("origen");
        Destino destino = handle(request, response, tabla, op, accion, origen);
        if (destino == null) {
            destino = new Destino(Camino.forward, "/WEB-INF/index.jsp", "");
        }
        if (destino.camino == Camino.forward) {
            request.getServletContext().
                    getRequestDispatcher(destino.url).forward(request, response);
        } else if (destino.camino == Camino.redirect) {
            response.sendRedirect(destino.url);
        } else {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println(destino.texto);
            }
        }
    }

    private Destino handle(HttpServletRequest request, HttpServletResponse response,String tabla, String op, String accion, String origen) throws JSONException {
        if (origen == null) {
            origen = "";
        }
        if (tabla == null || op == null || accion == null) {
            tabla = "usuario";
            op = "read";
            accion = "view";
        }
        switch (tabla) {
            case "usuario":
                return handleUsuario(request, response, op, accion, origen);
            case "keep":
                return handleKeep(request, response, op, accion, origen);
            default:
        }
        return null;
    }
    
    private Destino handleUsuario(HttpServletRequest request, HttpServletResponse response,String op, String accion, String origen) throws JSONException {
        switch (op) {
            case "login":
                if (origen.equals("android")) {
                    JSONObject obj = GestorUsuario.getLogin(request.getParameter("login"),request.getParameter("pass"));
                    return new Destino(Camino.print, "", obj.toString());
                }
                if (origen.equals("web")) {
                    JSONObject obj = GestorUsuario.getLogin(request.getParameter("login"),request.getParameter("pass"));
                    if (obj.getBoolean("r")) {
                        List<Keep> keeps = GestorKeep.listaKeep(request.getParameter("login"));
                        request.setAttribute("listado", keeps);
                        request.setAttribute("login", GestorUsuario.usuarioPorNombre(request.getParameter("login")));
                        return new Destino(Camino.forward, "/WEB-INF/notas.jsp", keeps.toString());
                    } else {
                        return new Destino(Camino.print, "index.html", obj.toString());
                    }
                }
        }
        return null;
    }

    private Destino handleKeep(HttpServletRequest request, HttpServletResponse response,String op, String accion, String origen) throws JSONException {
        switch (op) {
            case "create":
                if (origen.equals("android")) {
                    Keep k = new Keep(null, request.getParameter("idAndroid"),request.getParameter("contenido"), null, "estable");
                    JSONObject obj = GestorKeep.añadir(k,request.getParameter("login"));
                    return new Destino(Camino.print, "", obj.toString());
                }
                if (origen.equals("web")) {
                    List<Keep> keeps = GestorKeep.listaKeep(request.getParameter("login"));
                    int id = 0;
                    for (Keep k : keeps) {
                        if (k.getIdAndroid() >= id) {
                            id = k.getIdAndroid() + 1;
                        }
                    }
                    Keep keep = new Keep(null, id, request.getParameter("contenido"), null, "estable");
                    JSONObject obj = GestorKeep.añadir(keep, request.getParameter("login"));
                    keeps = GestorKeep.listaKeep(request.getParameter("login"));
                    request.setAttribute("listado", keeps);
                    return new Destino(Camino.forward, "/WEB-INF/notas.jsp", keeps.toString());
                }
            case "read":
                if (origen.equals("android")) {
                    JSONObject obj = GestorKeep.obtenerLista(request.getParameter("login"));
                    return new Destino(Camino.print, "", obj.toString());
                }
            case "delete":
                if (origen.equals("android")) {
                    Keep k = new Keep(null, request.getParameter("idAndroid"),request.getParameter("contenido"), null, "estable");
                    JSONObject obj = GestorKeep.borrar(k, request.getParameter("login"));
                    return new Destino(Camino.print, "", obj.toString());
                }
                if (origen.equals("web")){
                    GestorKeep.borrarPagina(Integer.parseInt(request.getParameter("id")));
                    List<Keep> keeps = GestorKeep.listaKeep(request.getParameter("login"));
                    request.setAttribute("listado", keeps);
                    return new Destino(Camino.forward, "/WEB-INF/notas.jsp", keeps.toString());
                }
            case "update":
                if(origen.equals("web")){
                    GestorKeep.actualizar(Integer.parseInt(request.getParameter("id")), request.getParameter("contenido"));
                    List<Keep> keeps = GestorKeep.listaKeep(request.getParameter("login"));
                    request.setAttribute("listado", keeps);
                    return new Destino(Camino.forward, "/WEB-INF/notas.jsp", keeps.toString());
                }
        }
        return null;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
