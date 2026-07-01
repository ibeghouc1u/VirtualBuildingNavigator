package fr.ul.virtumodle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import fr.ul.virtumodle.modele.Modele;
import fr.ul.virtumodle.modele.Piece;
import fr.ul.virtumodle.modele.Chargerjson;

import android.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;



import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;


public class PieceActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String selectedDirection = "";
    private HashMap<String, Uri> imagePaths = new HashMap<>();
    private Uri modeleUri;
    private EditText etPieceName;
    private Button btnValider;
    private Uri photoUri;
    private Piece piece;


    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private ImageView ivCompass;

    private final float[] gravity = new float[3];
    private final float[] geomagnetic = new float[3];
    private float currentDegree = 0f;

    private final ActivityResultLauncher<Intent> selectFromGalleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null && !selectedDirection.isEmpty()) {
                        imagePaths.put(selectedDirection, selectedImageUri);

                        Button button = getButtonForDirection(selectedDirection);
                        if (button != null) {
                            button.setText("✔️ " + selectedDirection);
                        }

                        Toast.makeText(this, "🖼️ Photo chargée depuis la galerie : " + selectedDirection, Toast.LENGTH_SHORT).show();
                    }
                }
            });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_piece);

        etPieceName = findViewById(R.id.etPieceName);
        btnValider = findViewById(R.id.btnValider);


        String modeleUriStr = getIntent().getStringExtra("modeleUri");
        String nomPiece = getIntent().getStringExtra("nomPiece");

        if (modeleUriStr != null) {
            modeleUri = Uri.parse(modeleUriStr);


            Modele modele = Chargerjson.chargerModele(this, modeleUri);
            if (modele != null) {
                piece = modele.getPiece(nomPiece);

                if (piece != null) {
                    etPieceName.setText(piece.getNom());
                    chargerPhotos();
                }
            }
        } else {
            Toast.makeText(this, "⚠️ Fichier modèle non trouvé.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.piece), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        setupPhotoButton(R.id.btnPhotoNord, "Nord");
        setupPhotoButton(R.id.btnPhotoSud, "Sud");
        setupPhotoButton(R.id.btnPhotoEst, "Est");
        setupPhotoButton(R.id.btnPhotoOuest, "Ouest");

        setupAccessButton(R.id.btnAddAccessNord, "Nord");
        setupAccessButton(R.id.btnAddAccessSud, "Sud");
        setupAccessButton(R.id.btnAddAccessEst, "Est");
        setupAccessButton(R.id.btnAddAccessOuest, "Ouest");

        setupDeleteButton(R.id.btnDeleteNord, "Nord");
        setupDeleteButton(R.id.btnDeleteSud, "Sud");
        setupDeleteButton(R.id.btnDeleteEst, "Est");
        setupDeleteButton(R.id.btnDeleteOuest, "Ouest");

        setupViewButton(R.id.btnViewNord, "Nord");
        setupViewButton(R.id.btnViewSud, "Sud");
        setupViewButton(R.id.btnViewEst, "Est");
        setupViewButton(R.id.btnViewOuest, "Ouest");


        btnValider.setOnClickListener(v -> validerPiece());
        Button btnRetour = findViewById(R.id.btnRetour);
        btnRetour.setOnClickListener(v -> {
            Intent intent = new Intent(PieceActivity.this, ModeleActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });



        // init bussole

        ivCompass = findViewById(R.id.ivCompass);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


    }


    private void chargerPhotos() {
        if (piece.getPhotos() != null) {
            for (String direction : piece.getPhotos().keySet()) {
                Uri imageUri = Uri.parse(piece.getPhotos().get(direction));
                imagePaths.put(direction, imageUri);


                Button button = getButtonForDirection(direction);
                if (button != null) {
                    button.setText("✔️ " + direction);
                }
            }
        }
    }

    private void setupPhotoButton(int buttonId, String direction) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            selectedDirection = direction;

            new AlertDialog.Builder(PieceActivity.this)
                    .setTitle("Ajouter une photo")
                    .setMessage("Souhaitez-vous prendre une photo ou en choisir une dans la galerie ?")
                    .setPositiveButton("📷 Appareil photo", (dialog, which) -> capturePhoto())
                    .setNegativeButton("🖼️ Galerie", (dialog, which) -> openGallery())
                    .show();
        });

        if (imagePaths.containsKey(direction)) {
            button.setText("✔️ " + direction);
        }
    }


    private void setupAccessButton(int buttonId, String direction) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            if (!imagePaths.containsKey(direction)) {
                Toast.makeText(this, "⚠️ Vous devez d'abord prendre une photo pour " + direction + ".", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pieceEstEnregistree()) {
                Toast.makeText(this, "⚠️ Vous devez d'abord enregistrer la pièce.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(PieceActivity.this, AccesActivity.class);
            intent.putExtra("modeleUri", modeleUri.toString());
            intent.putExtra("nomPiece", etPieceName.getText().toString().trim());
            intent.putExtra("direction", direction);

            Uri imageUri = imagePaths.get(direction);
            if (imageUri != null) {
                intent.putExtra("imageUri", imageUri.toString());
            }

            startActivity(intent);
        });
    }


    private void capturePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, "fr.ul.virtumodle.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "🚨 Erreur lors de la capture de la photo", Toast.LENGTH_SHORT).show();
        }
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile("IMG_" + timeStamp, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            if (photoUri != null) {
                imagePaths.put(selectedDirection, photoUri);
                Toast.makeText(this, "✅ Photo enregistrée pour " + selectedDirection, Toast.LENGTH_SHORT).show();


                Button button = getButtonForDirection(selectedDirection);
                if (button != null) {
                    button.setText("✔️ " + selectedDirection);
                }
            }
        }
    }

    private void validerPiece() {
        String nomPiece = etPieceName.getText().toString().trim();

        if (nomPiece.isEmpty() || !pieceComplete()) {
            Toast.makeText(this, "⚠️ Vous devez entrer un nom et prendre toutes les photos.", Toast.LENGTH_SHORT).show();
            return;
        }
        Modele modele = Chargerjson.chargerModele(this, modeleUri);
        if (modele == null) {
            Toast.makeText(this, "⚠️ Impossible de charger le modèle.", Toast.LENGTH_SHORT).show();
            return;
        }
        Piece nouvellePiece = new Piece(nomPiece);
        for (String direction : imagePaths.keySet()) {
            nouvellePiece.ajouterPhoto(direction, imagePaths.get(direction).toString());
        }
        if (modele.getPiece(nomPiece) != null) {
            modele.supprimerPiece(nomPiece);
        }
        modele.ajouterPiece(nouvellePiece);
        Chargerjson.sauvegarderModele(this, modele, modeleUri);

        Toast.makeText(this, "✅ Pièce sauvegardée !", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }


    private Button getButtonForDirection(String direction) {
        switch (direction) {
            case "Nord": return findViewById(R.id.btnPhotoNord);
            case "Sud": return findViewById(R.id.btnPhotoSud);
            case "Est": return findViewById(R.id.btnPhotoEst);
            case "Ouest": return findViewById(R.id.btnPhotoOuest);
            default: return null;
        }
    }
    private boolean pieceEstEnregistree() {
        Modele modele = Chargerjson.chargerModele(this, modeleUri);
        if (modele != null) {
            String nomPiece = etPieceName.getText().toString().trim();
            Piece piece = modele.getPiece(nomPiece);
            return piece != null;
        }
        return false;
    }

    private boolean pieceComplete() {

        return imagePaths.containsKey("Nord") &&
                imagePaths.containsKey("Sud") &&
                imagePaths.containsKey("Est") &&
                imagePaths.containsKey("Ouest");
    }
    private void setupDeleteButton(int buttonId, String direction) {
        Button btnDelete = findViewById(buttonId);
        btnDelete.setOnClickListener(v -> {
            if (imagePaths.containsKey(direction)) {
                imagePaths.remove(direction);


                Button photoBtn = getButtonForDirection(direction);
                if (photoBtn != null) {
                    photoBtn.setText("Photo " + direction);
                }

                Toast.makeText(this, "🗑️ Photo " + direction + " supprimée.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Aucune photo à supprimer pour " + direction, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupViewButton(int buttonId, String direction) {
        Button btnView = findViewById(buttonId);
        btnView.setOnClickListener(v -> {
            if (!imagePaths.containsKey(direction)) {
                Toast.makeText(this, "⚠️ Aucune photo pour " + direction + ".", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri imageUri = imagePaths.get(direction);
            Intent intent = new Intent(PieceActivity.this, VoirPhotoActivity.class);
            intent.putExtra("imageUri", imageUri.toString());
            startActivity(intent);
        });
    }






    // bussole
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener);
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            final float alpha = 0.97f;

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0];
                geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1];
                geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2];
            }

            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                RotateAnimation rotateAnimation = new RotateAnimation(
                        currentDegree,
                        -azimuth,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(250);
                rotateAnimation.setFillAfter(true);

                ivCompass.startAnimation(rotateAnimation);
                currentDegree = -azimuth;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };



    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        selectFromGalleryLauncher.launch(intent);
    }






}
