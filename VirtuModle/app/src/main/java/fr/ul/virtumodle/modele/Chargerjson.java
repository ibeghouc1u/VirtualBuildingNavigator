package fr.ul.virtumodle.modele;

import android.content.Context;
import android.net.Uri;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

public class Chargerjson {

    public static Modele chargerModele(Context context, Uri modeleUri) {
        try (InputStreamReader reader = new InputStreamReader(context.getContentResolver().openInputStream(modeleUri))) {
            Gson gson = new Gson();
            Type modeleType = new TypeToken<Modele>() {}.getType();
            return gson.fromJson(reader, modeleType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sauvegarderModele(Context context, Modele modele, Uri modeleUri) {
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(modeleUri, "wt")) {
            if (outputStream != null) {
                Gson gson = new Gson();
                outputStream.write(gson.toJson(modele).getBytes());
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
