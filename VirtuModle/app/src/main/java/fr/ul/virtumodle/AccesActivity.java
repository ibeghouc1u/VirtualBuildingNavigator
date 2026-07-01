package fr.ul.virtumodle;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.ul.virtumodle.modele.Acces;
import fr.ul.virtumodle.modele.AccesAdapter;
import fr.ul.virtumodle.modele.Modele;
import fr.ul.virtumodle.modele.Piece;
import fr.ul.virtumodle.modele.Chargerjson;

public class AccesActivity extends AppCompatActivity {

    private ImageView ivPhoto;
    private View selectionRectangle;
    private List<Acces> accesList = new ArrayList<>();
    private AccesAdapter adapter;
    private Button btnTerminer;

    private Uri modeleUri;
    private String nomPiece;
    private String direction;

    private float startIntrinsicX, startIntrinsicY, endIntrinsicX, endIntrinsicY;
    private float startDisplayX, startDisplayY, endDisplayX, endDisplayY;
    private boolean isDrawing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acces);

        // Récupération des extras
        String imageUriString = getIntent().getStringExtra("imageUri");
        String modeleUriString = getIntent().getStringExtra("modeleUri");
        nomPiece = getIntent().getStringExtra("nomPiece");
        direction = getIntent().getStringExtra("direction");

        if (modeleUriString != null) {
            modeleUri = Uri.parse(modeleUriString);
        } else {
            Toast.makeText(this, "Fichier modèle non trouvé", Toast.LENGTH_SHORT).show();
            finish();
        }

        ivPhoto = findViewById(R.id.ivPhoto);
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            ivPhoto.setImageURI(imageUri);
        }

        selectionRectangle = findViewById(R.id.selectionRectangle);
        selectionRectangle.setVisibility(View.GONE);

        btnTerminer = findViewById(R.id.btnTerminer);
        btnTerminer.setOnClickListener(v -> sauvegarderAccesDansModele());

        RecyclerView recyclerView = findViewById(R.id.recyclerViewAccess);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AccesAdapter(accesList, this::modifierAcces, this::supprimerAcces);
        recyclerView.setAdapter(adapter);


        Modele modele = Chargerjson.chargerModele(this, modeleUri);
        if (modele != null) {
            Piece piece = modele.getPiece(nomPiece);
            if (piece != null) {
                List<Acces> savedAcces = piece.getAccesParDirection(direction);
                if (savedAcces != null && !savedAcces.isEmpty()) {
                    accesList.addAll(savedAcces);
                    adapter.notifyDataSetChanged();
                }
            }
        }

        ivPhoto.setOnTouchListener((v, event) -> {
            float touchX = event.getX();
            float touchY = event.getY();

            float[] intrinsicCoords = convertirCoordonnees(ivPhoto, touchX, touchY);
            float intrinsicX = intrinsicCoords[0];
            float intrinsicY = intrinsicCoords[1];

            float[] scaleAndOffset = getScaleAndOffset(ivPhoto);
            float scale = scaleAndOffset[0];
            float offsetX = scaleAndOffset[1];
            float offsetY = scaleAndOffset[2];

            float currentDisplayX = intrinsicX * scale + offsetX;
            float currentDisplayY = intrinsicY * scale + offsetY;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    isDrawing = true;
                    startIntrinsicX = intrinsicX;
                    startIntrinsicY = intrinsicY;
                    startDisplayX = currentDisplayX;
                    startDisplayY = currentDisplayY;
                    selectionRectangle.setVisibility(View.VISIBLE);
                    updateRectangle(startDisplayX, startDisplayY, currentDisplayX, currentDisplayY);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (isDrawing) {
                        endIntrinsicX = intrinsicX;
                        endIntrinsicY = intrinsicY;
                        endDisplayX = currentDisplayX;
                        endDisplayY = currentDisplayY;
                        updateRectangle(startDisplayX, startDisplayY, endDisplayX, endDisplayY);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (isDrawing) {
                        isDrawing = false;
                        updateRectangle(startDisplayX, startDisplayY, endDisplayX, endDisplayY);

                        float leftNormalized = startIntrinsicX / ivPhoto.getDrawable().getIntrinsicWidth();
                        float topNormalized = startIntrinsicY / ivPhoto.getDrawable().getIntrinsicHeight();
                        float rightNormalized = endIntrinsicX / ivPhoto.getDrawable().getIntrinsicWidth();
                        float bottomNormalized = endIntrinsicY / ivPhoto.getDrawable().getIntrinsicHeight();

                        demanderNomAcces(leftNormalized, topNormalized, rightNormalized, bottomNormalized);
                    }
                    return true;
            }
            return false;
        });

        Button btnRetour = findViewById(R.id.btnRetour);
        btnRetour.setOnClickListener(v -> {
            Intent intent = new Intent(AccesActivity.this, PieceActivity.class);
            intent.putExtra("modeleUri", modeleUri.toString());
            intent.putExtra("nomPiece", nomPiece);
            startActivity(intent);
            finish(); // Pour ne pas empiler les activités
        });

    }
    private void sauvegarderAccesDansModele() {
        Modele modele = Chargerjson.chargerModele(this, modeleUri);
        if (modele == null) {
            Toast.makeText(this, "⚠️ Impossible de charger le modèle.", Toast.LENGTH_SHORT).show();
            return;
        }
        Piece piece = modele.getPiece(nomPiece);
        if (piece == null) {
            Toast.makeText(this, "⚠️ Pièce introuvable.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Ajouter chaque accès de la liste à la pièce pour la direction spécifiée
        for (Acces acces : accesList) {
            piece.ajouterAcces(direction, acces);
        }
        Chargerjson.sauvegarderModele(this, modele, modeleUri);
        Toast.makeText(this, "✅ Accès sauvegardés dans le modèle !", Toast.LENGTH_SHORT).show();
        finish();
    }
    private float[] convertirCoordonnees(ImageView imageView, float touchX, float touchY) {
        Drawable drawable = imageView.getDrawable();
        if (drawable == null)
            return new float[]{ touchX, touchY };

        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();

        float[] scaleAndOffset = getScaleAndOffset(imageView);
        float scale = scaleAndOffset[0];
        float offsetX = scaleAndOffset[1];
        float offsetY = scaleAndOffset[2];

        // Calculer les coordonnées intrinsèques.
        float intrinsicX = (touchX - offsetX) / scale;
        float intrinsicY = (touchY - offsetY) / scale;

        // Clamp pour rester dans l'image.
        if (intrinsicX < 0) intrinsicX = 0;
        if (intrinsicY < 0) intrinsicY = 0;
        if (intrinsicX > intrinsicWidth) intrinsicX = intrinsicWidth;
        if (intrinsicY > intrinsicHeight) intrinsicY = intrinsicHeight;

        return new float[]{ intrinsicX, intrinsicY };
    }
    private float[] getScaleAndOffset(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable == null)
            return new float[]{ 1f, 0f, 0f };

        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        int viewWidth = imageView.getWidth();
        int viewHeight = imageView.getHeight();

        float imageRatio = (float) intrinsicWidth / intrinsicHeight;
        float viewRatio = (float) viewWidth / viewHeight;
        float scale, offsetX = 0f, offsetY = 0f;

        if (imageRatio >= viewRatio) {
            // L'image occupe toute la largeur de l'ImageView, centrée verticalement.
            scale = (float) viewWidth / intrinsicWidth;
            float scaledHeight = intrinsicHeight * scale;
            offsetY = (viewHeight - scaledHeight) / 2f;
        } else {
            // L'image occupe toute la hauteur de l'ImageView, centrée horizontalement.
            scale = (float) viewHeight / intrinsicHeight;
            float scaledWidth = intrinsicWidth * scale;
            offsetX = (viewWidth - scaledWidth) / 2f;
        }
        return new float[]{ scale, offsetX, offsetY };
    }
    private void updateRectangle(float left, float top, float right, float bottom) {
        float minX = Math.min(left, right);
        float minY = Math.min(top, bottom);
        float maxX = Math.max(left, right);
        float maxY = Math.max(top, bottom);


        selectionRectangle.setTranslationX(minX);
        selectionRectangle.setTranslationY(minY);

        ViewGroup.LayoutParams params = selectionRectangle.getLayoutParams();
        params.width = (int) (maxX - minX);
        params.height = (int) (maxY - minY);
        selectionRectangle.setLayoutParams(params);
        selectionRectangle.setVisibility(View.VISIBLE);
    }
    private void demanderNomAcces(float left, float top, float right, float bottom) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Créer un accès");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputNom = new EditText(this);
        inputNom.setHint("Nom de l'accès");
        layout.addView(inputNom);

        final Spinner spinnerDest = new Spinner(this);
        List<String> piecesDisponibles = getPiecesDisponibles();

        

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, piecesDisponibles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDest.setAdapter(adapter);

        layout.addView(spinnerDest);

        builder.setView(layout);

        builder.setPositiveButton("Valider", (dialog, which) -> {
            String nomAcces = inputNom.getText().toString().trim();
            String destination = (String) spinnerDest.getSelectedItem();

            if (nomAcces.isEmpty()) {
                Toast.makeText(this, "⚠️ Veuillez saisir le nom de l'accès.", Toast.LENGTH_SHORT).show();
            } else if (destination == null) {
                Toast.makeText(this, "⚠️ Veuillez sélectionner une destination.", Toast.LENGTH_SHORT).show();
            } else {
                ajouterAcces(nomAcces, left, top, right, bottom, destination);
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private List<String> getPiecesDisponibles() {
        Modele modele = Chargerjson.chargerModele(this, modeleUri);
        List<String> nomsPieces = new ArrayList<>();
        if (modele != null) {
            for (Piece piece : modele.getPieces()) {
                nomsPieces.add(piece.getNom());
            }
        }
        return nomsPieces;
    }

    private void ajouterAcces(String nom, float left, float top, float right, float bottom, String destination) {
        Acces acces = new Acces(accesList.size(), nom, left, top, right, bottom, destination);
        accesList.add(acces);
        adapter.notifyDataSetChanged();
    }

    private void supprimerAcces(Acces acces) {
        accesList.remove(acces);
        adapter.notifyDataSetChanged();
        selectionRectangle.setVisibility(View.GONE);
    }

    private void modifierAcces(Acces acces) {

        float leftIntrinsic = acces.getLeft();
        float topIntrinsic = acces.getTop();
        float rightIntrinsic = acces.getRight();
        float bottomIntrinsic = acces.getBottom();

        float[] scaleAndOffset = getScaleAndOffset(ivPhoto);
        float scale = scaleAndOffset[0];
        float offsetX = scaleAndOffset[1];
        float offsetY = scaleAndOffset[2];

        float displayLeft = leftIntrinsic * scale + offsetX;
        float displayTop = topIntrinsic * scale + offsetY;
        float displayRight = rightIntrinsic * scale + offsetX;
        float displayBottom = bottomIntrinsic * scale + offsetY;

        updateRectangle(displayLeft, displayTop, displayRight, displayBottom);
        selectionRectangle.setVisibility(View.VISIBLE);
    }
}

