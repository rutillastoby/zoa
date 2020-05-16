package com.rutillastoby.zoria;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * CLASE PARA ADMINISTRAR LA BASE DE DATOS DE REPALDO DE INFORMACION EN CASO DE DESCONEXIÓN
 */
public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    /**
     * CONSTRUCTOR PARAMETRIZADO
     */
    public AdminSQLiteOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Sentencia para la creacion de la base de datos
        db.execSQL("create table data(id integer primary key autoincrement, path text, value text, typevalue integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
