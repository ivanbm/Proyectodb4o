package com.izv.android.proyectodb4o;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;

import java.util.ArrayList;
import java.util.List;


public class Principal extends Activity {

    private List<Disco> datos;
    private Adaptador ad;
    private ObjectContainer bd=null;
    private ListView ls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        //setTheme(android.R.style.Theme_Holo_Light);

        initComponents();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if (id == R.id.action_anadir) {
            anadir();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;

        switch (item.getItemId()){

            case R.id.editar:
                editar(index);
                ad.notifyDataSetChanged();

                return true;
            case R.id.elimiar:
                eliminar(index);
                tostada(getString(R.string.msgeliminar));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextual, menu);
    }

    public void initComponents(){



        datos = new ArrayList<Disco>();
        select();
        ad = new Adaptador(this, R.layout.lista_detalle, datos);
        ls = (ListView)findViewById(R.id.lvLista);
        ls.setAdapter(ad);
        registerForContextMenu(ls);
    }

    private void tostada(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public boolean anadir(){


        final AlertDialog.Builder alert= new AlertDialog.Builder(this);
        alert.setTitle(R.string.tituloAnadir);

        LayoutInflater inflater = LayoutInflater.from(this);
        final View vista = inflater.inflate(R.layout.anadir, null);
        alert.setView(vista);


        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText et1,et2, et3;
                et1 = (EditText) vista.findViewById(R.id.etAlbum);
                et2 = (EditText) vista.findViewById(R.id.etAutor);
                et3 = (EditText) vista.findViewById(R.id.etDiscografica);


                    Disco d =new Disco(et1.getText().toString(),et2.getText().toString(), et3.getText().toString(),"caratula");
                    bd.store(d);
                    bd.commit();
                    //select();
                    ad.notifyDataSetChanged();
                    tostada("Album añadido!");
            }
        });
        alert.setNegativeButton(android.R.string.no ,null);
        alert.show();

        return true;
    }


public void eliminar(int index){

    Disco d = new Disco(datos.get(index).getAlbum(),datos.get(index).getAutor(),datos.get(index).getDiscografica(),"caratula");
    bd.delete(d);

}

    public boolean editar(final int index){
        final String aut = datos.get(index).getAutor();
        final String alb = datos.get(index).getAlbum();
        final String dis = datos.get(index).getDiscografica();

        final AlertDialog.Builder alert= new AlertDialog.Builder(this);
        alert.setTitle(R.string.tituloEditar);

        LayoutInflater inflater = LayoutInflater.from(this);
        final View vista = inflater.inflate(R.layout.anadir, null);
        alert.setView(vista);

        final EditText et1,et2, et3;
        et1 = (EditText) vista.findViewById(R.id.etAlbum);
        et2 = (EditText) vista.findViewById(R.id.etAutor);
        et3 = (EditText) vista.findViewById(R.id.etDiscografica);

        et1.setText(alb);
        et2.setText(aut);
        et3.setText(dis);

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Disco old = new Disco(alb, aut, dis, null);
                Disco nuevo = new Disco(et1.getText().toString(),et2.getText().toString(), et3.getText().toString(),"caratula");

                    bd.delete(old);
                    bd.store(nuevo);

                ad.notifyDataSetChanged();
                tostada("Album editado!");
            }
        });
        alert.setNegativeButton(android.R.string.no ,null);
        alert.show();

        return true;
    }


    /*----------------------------------------------------*/
    /*                  SELECCIONAR IMAGENES              */
    /*----------------------------------------------------*/
    final int REQ_CODE_PICK_IMAGE =1;
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(
                            selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();


                    Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
                }
        }
    }

    /*********************************/
    /*               DB4O            */
    /*********************************/

    private ObjectContainer bd() {
        try {
            if (bd == null) {
                bd = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), getExternalFilesDir(null) + "/discos.db4o");
                //Log.d(TAG, "opened new database connection");
            }

            return bd;
        } catch (Exception e) {
            //Log.e(TAG, "unable to open database");
            return null;
        }
    }


    public void select(){
        List<Disco> res = bd().query(Disco.class);
        for (Disco s : res) {
            Disco d = new Disco(s.getAlbum(), s.getAutor(), s.getDiscografica(), s.getImagen());
            datos.add(d);
        }
    }

}
