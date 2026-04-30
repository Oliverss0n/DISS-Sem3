package entities;

import OSPAnimator.AnimShapeItem;

public class Doctor {
    private int id;
    private String aktualnaPozicia; // napr. "Vstup Sanitka", "Amb A1", "Amb B2"
    private OSPAnimator.AnimShapeItem animItem;

    public Doctor(int id) {
        this.id = id;
        this.aktualnaPozicia = "Vstup Sanitka"; // Štartovacia pozícia podľa zadania
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAktualnaPozicia() {
        return aktualnaPozicia;
    }

    public void setAktualnaPozicia(String aktualnaPozicia) {
        this.aktualnaPozicia = aktualnaPozicia;
    }

    public AnimShapeItem getAnimItem() {
        return animItem;
    }

    public void setAnimItem(AnimShapeItem animItem) {
        this.animItem = animItem;
    }
}
