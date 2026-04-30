package entities;

import OSPAnimator.AnimShapeItem;

public class Nurse {
    private int id;
    private String aktualnaPozicia;
    private AnimShapeItem animItem;

    public Nurse(int id) {
        this.id = id;
        this.aktualnaPozicia = "Vstup Sanitka";
    }

    public int getId() {
        return id;
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
