package com.example.rafa.hibernatekeep.pojo;

public class Keep {

    private long id;
    private String contenido;
    private boolean estado;

    public Keep() {
    }

    public Keep(long id, String contenido, boolean estado) {
        this.id = id;
        this.contenido = contenido;
        this.estado = estado;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Keep keep = (Keep) o;

        if (id != keep.id) return false;
        if (estado != keep.estado) return false;
        return !(contenido != null ? !contenido.equals(keep.contenido) : keep.contenido != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (contenido != null ? contenido.hashCode() : 0);
        result = 31 * result + (estado ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Keep{" +
                "id=" + id +
                ", contenido='" + contenido + '\'' +
                ", estado=" + estado +
                '}';
    }
}
