package com.example.bingo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Perfil extends AppCompatActivity {

    Button btnSeleccionar;
    ImageView imagenSel;
    private MediaPlayer music;
    TextView txtUsername;

    int SELECT_PICTURE = 200;

    //shared preferences
    private static final String PREFS_NAME = "BingoPrefs";
    private static final String PREF_USERNAME = "username";
    //uri de la imagen
    private static final String PREF_IMAGE_URI = "imageUri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        music = MediaPlayer.create(this, R.raw.menu);
        music.setLooping(true);

        btnSeleccionar = findViewById(R.id.btnSeleccionar);
        imagenSel = findViewById(R.id.imagenSel);
        txtUsername = findViewById(R.id.txtUsername);

        //cargar nombre del usuario
        loadUsername();

        //cargar imagen guardada anteriormente
        loadProfileImage();

        //seleccionar imagen
        btnSeleccionar.setOnClickListener(
                new View.OnClickListener() {
                    @Override public void onClick(View v)
                    {
                        imageChooser();
                    }
                });
    }

    private void loadUsername() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        //buscar el nombre guardado o sino poner invitado por default
        String username = prefs.getString(PREF_USERNAME, "Invitado");
        txtUsername.setText(username);
    }

    void imageChooser()
    {
        //obtener uri persistente
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");

        //permisos
        i.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivityForResult(
                Intent.createChooser(i, "Select Picture"),
                SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    try {
                        //permiso persistente
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);

                        //guardar uri en sharedpreferences
                        saveImageUri(selectedImageUri);

                        //mostrar img
                        imagenSel.setImageURI(selectedImageUri);

                    } catch (Exception e) {
                        Log.e("Perfil", "Error al tomar permiso o guardar URI", e);
                    }
                }
            }
        }
    }

    private void saveImageUri(Uri uri) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        //uri como string
        editor.putString(PREF_IMAGE_URI, uri.toString());
        editor.apply();
    }

    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        //obtener uri
        String uriString = prefs.getString(PREF_IMAGE_URI, null);

        if (uriString != null) {
            try {
                Uri savedUri = Uri.parse(uriString);
                getContentResolver().takePersistableUriPermission(savedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                imagenSel.setImageURI(savedUri);
            } catch (SecurityException e) {
                Log.w("Perfil", "No se pudo cargar la imagen, permiso denegado o archivo no encontrado.", e);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //iniciar musica
        if (music != null) {
            music.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //pausar musica
        if (music != null && music.isPlaying()) {
            music.pause();
            //reiniciar
            music.seekTo(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (music != null) {
            music.stop();
            music.release();
            music = null;
        }
    }
}