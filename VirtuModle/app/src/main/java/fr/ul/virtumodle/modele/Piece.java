package fr.ul.virtumodle.modele;

import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class Piece implements Serializable {
    private String nom;
    private HashMap<String, String> photos;
    private HashMap<String, List<Acces>> accesParDirection;

    public Piece(String nom) {
        this.nom = nom;
        this.photos = new HashMap<>();
        this.accesParDirection = new HashMap<>();
        accesParDirection.put("Nord", new ArrayList<>());
        accesParDirection.put("Sud", new ArrayList<>());
        accesParDirection.put("Est", new ArrayList<>());
        accesParDirection.put("Ouest", new ArrayList<>());
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public void ajouterPhoto(String direction, String cheminPhoto) {
        photos.put(direction, cheminPhoto);
    }

    public void supprimerPhoto(String direction) {
        photos.remove(direction);
        accesParDirection.get(direction).clear(); // Supprime les accès liés
    }

    public String getPhoto(String direction) {
        return photos.get(direction);
    }

    public boolean hasPhoto(String direction) {
        return photos.containsKey(direction);
    }

    public HashMap<String, String> getPhotos() {
        return photos;
    }

    public void ajouterAcces(String direction, Acces acces) {
        if (accesParDirection.containsKey(direction)) {
            accesParDirection.get(direction).add(acces);
        }
    }

    public List<Acces> getAccesParDirection(String direction) {
        return accesParDirection.getOrDefault(direction, new ArrayList<>());
    }
    public void setAcces(String direction, List<Acces> acces) {
        if (accesParDirection.containsKey(direction)) {
            accesParDirection.put(direction, new ArrayList<>(acces));
        }
    }

}
