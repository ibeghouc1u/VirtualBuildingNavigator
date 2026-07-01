package fr.ul.virtumodle.modele;

import java.io.Serializable;

public class Acces implements Serializable {
    private int id;
    private String nom;
    private float left, top, right, bottom;
    private String pieceDestination;

    public Acces(int id, String nom, float left, float top, float right, float bottom, String pieceDestination) {
        this.id = id;
        this.nom = nom;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.pieceDestination = pieceDestination;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public float getLeft() { return left; }
    public float getTop() { return top; }
    public float getRight() { return right; }
    public float getBottom() { return bottom; }
    public String getPieceDestination() { return pieceDestination; }

    public void setPieceDestination(String pieceDestination) { this.pieceDestination = pieceDestination; }
}
