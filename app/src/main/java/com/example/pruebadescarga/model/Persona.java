package com.example.pruebadescarga.model;
import java.util.Date;
public class Persona {
    private String nombre;
    private long numer_cuenta;
    private Date fechaNacimeinto;

    public Persona() {
    }

    /*
            Getters and setters
         * */
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public long getNumer_cuenta() {
        return numer_cuenta;
    }

    public void setNumer_cuenta(long numer_cuenta) {
        this.numer_cuenta = numer_cuenta;
    }

    public Date getFechaNacimeinto() {
        return fechaNacimeinto;
    }

    public void setFechaNacimeinto(Date fechaNacimeinto) {
        this.fechaNacimeinto = fechaNacimeinto;
    }



}
