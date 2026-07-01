package fr.ul.virtumodle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChargerPieceActivity extends AppCompatActivity {

    private LinearLayout containerPieces;
    private RadioGroup rgOrientation;
    private Button btnValider;

    private String selectedPiece = null;
    private String selectedOrientation = null;
    private JSONObject modeleData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charger_piece);

        containerPieces = findViewById(R.id.containerPieces);
        rgOrientation = findViewById(R.id.rgOrientation);
        btnValider = findViewById(R.id.btnValider);


        String jsonData = getIntent().getStringExtra("jsonData");

        if (jsonData != null) {
            try {
                modeleData = new JSONObject(jsonData);
                loadModel(modeleData);
            } catch (Exception e) {
                Log.e("ChargerPieceActivity", "Erreur lors de la lecture du JSON", e);
            }
        }


        rgOrientation.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbNord) selectedOrientation = "Nord";
            else if (checkedId == R.id.rbSud) selectedOrientation = "Sud";
            else if (checkedId == R.id.rbEst) selectedOrientation = "Est";
            else if (checkedId == R.id.rbOuest) selectedOrientation = "Ouest";
        });
        btnValider.setOnClickListener(v -> {
            if (selectedPiece != null && selectedOrientation != null) {
                launchExploitationModeleActivity();
            } else {
                Toast.makeText(this, "Veuillez sélectionner une pièce et une direction !", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnRetour = findViewById(R.id.btnRetour);
        btnRetour.setOnClickListener(v -> {
            Intent intent = new Intent(ChargerPieceActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

    }

    private void loadModel(JSONObject jsonObject) {
        try {

            JSONArray piecesArray = jsonObject.getJSONArray("pieces");

            for (int i = 0; i < piecesArray.length(); i++) {
                JSONObject pieceObject = piecesArray.getJSONObject(i);
                String pieceName = pieceObject.getString("nom");
                TextView pieceView = new TextView(this);
                pieceView.setText(pieceName);
                pieceView.setTextSize(18f);
                pieceView.setPadding(16, 16, 16, 16);
                pieceView.setBackgroundColor(getResources().getColor(R.color.purple_700));
                pieceView.setTextColor(getResources().getColor(android.R.color.white));


                pieceView.setOnClickListener(v -> {
                    selectPiece(pieceView, pieceName);
                });

                containerPieces.addView(pieceView);
            }
        } catch (Exception e) {
            Log.e("ChargerPieceActivity", "Erreur lors du chargement des pièces", e);
        }
    }

    private void selectPiece(TextView selectedView, String pieceName) {
        selectedPiece = pieceName;


        for (int i = 0; i < containerPieces.getChildCount(); i++) {
            TextView view = (TextView) containerPieces.getChildAt(i);
            if (view == selectedView) {
                view.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            } else {
                view.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
            }
        }
    }

    private void launchExploitationModeleActivity() {

        Intent intent = new Intent(this, ExploitationModeleActivity.class);
        intent.putExtra("piece", selectedPiece);
        intent.putExtra("orientation", selectedOrientation);


        if (modeleData != null) {
            intent.putExtra("jsonData", modeleData.toString());
        }

        startActivity(intent);
    }
}
