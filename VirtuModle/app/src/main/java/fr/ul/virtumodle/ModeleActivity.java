package fr.ul.virtumodle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.gson.Gson;

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

import fr.ul.virtumodle.modele.Modele;
import fr.ul.virtumodle.modele.Piece;
import fr.ul.virtumodle.modele.Chargerjson;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ModeleActivity extends AppCompatActivity {

    private EditText etModelName;
    private Button btnSave, btnAddRoom;
    private ListView listViewPieces;
    private TextView tvEmptyMessage;
    private Modele modele;
    private Uri modeleUri;

    private final ActivityResultLauncher<Intent> createFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    modeleUri = result.getData().getData();
                    sauvegarderModele();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_modele);

        etModelName = findViewById(R.id.etModelName);
        btnSave = findViewById(R.id.btnSave);
        btnAddRoom = findViewById(R.id.btnAddRoom);
        listViewPieces = findViewById(R.id.listViewPieces);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        btnSave.setOnClickListener(v -> demanderEmplacementSauvegarde());
        btnAddRoom.setOnClickListener(v -> {
            if (modeleUri == null) {
                Toast.makeText(this, "⚠️ Vous devez d'abord sauvegarder le modèle avant d'ajouter des pièces.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(ModeleActivity.this, PieceActivity.class);
            intent.putExtra("modeleUri", modeleUri.toString());
            startActivityForResult(intent, 1);
        });
        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            Intent intent = new Intent(ModeleActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        chargerModele();
    }
    private void demanderEmplacementSauvegarde() {
        String nomModele = etModelName.getText().toString().trim();
        if (nomModele.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un nom pour le modèle.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, nomModele + ".json");

        createFileLauncher.launch(intent);
    }
    private void sauvegarderModele() {
        if (modeleUri == null) {
            Toast.makeText(this, "Erreur : emplacement non sélectionné.", Toast.LENGTH_SHORT).show();
            return;
        }

        modele = new Modele(etModelName.getText().toString().trim());

        try (OutputStream outputStream = getContentResolver().openOutputStream(modeleUri)) {
            if (outputStream != null) {
                Gson gson = new Gson();
                String json = gson.toJson(modele);
                outputStream.write(json.getBytes());
                outputStream.close();

                Toast.makeText(this, "✅ Modèle sauvegardé avec succès !", Toast.LENGTH_LONG).show();

                chargerModele();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Erreur lors de la sauvegarde.", Toast.LENGTH_SHORT).show();
        }
    }
    private void chargerModele() {
        if (modeleUri != null) {
            modele = Chargerjson.chargerModele(this, modeleUri);
            if (modele != null && modele.getPieces() != null) {
                afficherListePieces();
            } else {
                Toast.makeText(this, "⚠️ Impossible de charger le modèle.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void afficherListePieces() {
        if (modele == null || modele.getPieces().isEmpty()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            listViewPieces.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            listViewPieces.setVisibility(View.VISIBLE);

            List<String> nomsPieces = modele.getPieces().stream()
                    .map(Piece::getNom)
                    .toList();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    nomsPieces
            );

            listViewPieces.setAdapter(adapter);

            listViewPieces.setOnItemClickListener((parent, view, position, id) -> {
                String nomPiece = nomsPieces.get(position);
                ouvrirPiece(nomPiece);
            });

            adapter.notifyDataSetChanged();
        }
    }


    private void ouvrirPiece(String nomPiece) {
        if (modeleUri != null) {
            Intent intent = new Intent(ModeleActivity.this, PieceActivity.class);
            intent.putExtra("modeleUri", modeleUri.toString());
            intent.putExtra("nomPiece", nomPiece);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            chargerModele();
        }
    }
}
