package com.br.frases;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import javax.annotation.Nullable;

import helpers.ConfiguracaoFirebase;
import helpers.UsuarioFirebase;
import model.Usuario;

public class HomeActivity extends AppCompatActivity {

    private static final int SELECAO_GALERIA = 200;

    private FirebaseAuth autenticacao;
    private ImageView imgPerfil;
    private String idUsuarioLogado;
    private TextView txtNome;


    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();

        storageReference = ConfiguracaoFirebase.getStorageReference();

        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //REFERENCIA PARA O CAMPO USUARIOS NO FIREBASE
        DatabaseReference database = ConfiguracaoFirebase.getFirebase().child("usuarios");

        //METODO DO FIREBASE PARA BUSCAR EM TEMPO REAL NO BANCO "UMA ESPECIE DE OBSERVADOR"
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot keyNode : snapshot.getChildren()){
                    Usuario usuario = keyNode.getValue(Usuario.class);
                    txtNome.setText(usuario.getNome());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        inicializaComponentes();


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Frases Nerd");
        setSupportActionBar(toolbar);

        //Configuração a imagem
        imgPerfil.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                );

                if (i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });


    }

    private void inicializaComponentes() {
        imgPerfil = findViewById(R.id.imgPerfil);
        txtNome = findViewById(R.id.txtNome);
    }


        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_usuario, menu);

            //MenuItem item = menu.findItem(R.id.toolbar);

            return super.onCreateOptionsMenu(menu);
        }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuSair:
                autenticacao.signOut();
                finish();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }



    //Função responsável por gerar a frase
    public void gerarNovaFrase(View view){

        String[] frases = {
                "Vida longa e próspera(Sr. Spock - StarTrek)", //posição 0
                "Eu tenho a força! (He-man)", // posição 1
                "Que a Força esteja com você! (StarWars)", // posição 2
                "Siga-me os bons! (Chapolin-Colorado)",// posição 3
                "Ao infinito e além! (BuzzLIghtyear)",// posição 4
                "Com grandes poderes, vem grandes responsabilidades!(Tio Ben - SpiderMAn)",// posição 5
                "Meu precioso! (Smeagol)",// posição 6
                "Bazinga! (Shedon Cooper - The Big Bang Theory)",// posição 7
                "Eu não vim do lixo para perder pra basculho! (Gil do Vigor - BBB21)"// posição 8
        };

        //Var resposável por gerar os valores aleatórios
        //que serão utilizados para indicar a posiçao no vetor frase(acima)
        int numero = new Random().nextInt(9);

        //Mostrando a frase com base na posição sorteada
        TextView texto = findViewById(R.id.txtFraseGerada);
        texto.setText(frases[numero]);

    }// Fechamento função GerarNovaFrase


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Bitmap imagem = null;

            try {
                switch (requestCode){
                    case SELECAO_GALERIA:
                        Uri localImagem = data.getData();
                        imagem = MediaStore.Images
                                .Media
                                .getBitmap(getContentResolver(),localImagem);
                        break;
                }

                //verifica se a imagem foi escolhida e já faz upload
                if (imagem != null){
                    imgPerfil.setImageBitmap(imagem);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //configurando o Storage
                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("usuarios")
                            .child(idUsuarioLogado + "jpeg");

                    //Tarefa de Upload
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);

                    // Em caso de falha no upload
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(HomeActivity.this,
                                    "Erro ao fazer o upload da imagem",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(HomeActivity.this,
                                    "Sucesso ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }






}