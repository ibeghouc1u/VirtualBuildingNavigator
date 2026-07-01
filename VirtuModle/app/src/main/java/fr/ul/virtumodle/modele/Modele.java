package fr.ul.virtumodle.modele;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Modele implements Serializable {
    private String nom;
    private List<Piece> pieces;

    public Modele(String nom) {
        this.nom = nom;
        this.pieces = new ArrayList<>();
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public void ajouterPiece(Piece piece) {
        pieces.add(piece);
    }

    public Piece getPiece(String nomPiece) {
        for (Piece piece : pieces) {
            if (piece.getNom().equalsIgnoreCase(nomPiece)) {
                return piece;
            }
        }
        return null;
    }

    public void supprimerPiece(String nom) {
        pieces.removeIf(p -> p.getNom().equalsIgnoreCase(nom));
    }

}
