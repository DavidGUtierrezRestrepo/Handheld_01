package com.example.handheld;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "scanned_data.db";
    private static final int DATABASE_VERSION = 36;

    // Nombre de la tabla para los productos escaneados
    public static final String TABLE_TREFILACION_CODIGO_2= "Trefilación_codigo_dos ";
    public static final String TABLE_TREFILACION_CODIGO_3= "Trefilación_codigo_tres";

    public static final String TABLE_TREFILACION_CODIGO_2_NO_CONFORME= "Trefilación_codigo_dos_no_conforme";
    public static final String TABLE_TREFILACION_CODIGO_3_NO_CONFORME= "Trefilación_codigo_tres_no_conforme";

    // Nombre de la tabla para los productos escaneados de galvanizado
    public static final String TABLE_DESTINO_GALVANIZADO_CODIGO_2= "Destino_galvanizado_codigo_dos";

    public static final String TABLE_PUNTILLERIA_CODIGO_2= "Puntilleria_codigo_dos";


    public static final String TABLE_GALVANIZADO_CODIGO_2= "Galvanizado_codigo_dos";

    public static final String TABLE_GALVANIZADO_CODIGO_2_BODEGA_12= "Galvanizado_codigo_dos_bodega_12";


    public static final String TABLE_RECOCIDO_CODIGO_2= "Recocido_codigo_dos";

    public static final String TABLE_RECOCIDO_CODIGO_2_NO_CONFORME= "Recocido_codigo_dos_bodega_cuatro";

    public static final String TABLE_RECOCIDO_CODIGO_3_NO_CONFORME= "Recocido_codigo_tres_bodega_cuatro";

    public static final String TABLE_RECOCIDO_CODIGO_3= "Recocido_codigo_tres";

    public static final String TABLE_ALAMBRON_CODIGO_1= "Alambron_codigo_uno";

    public static final String TABLE_ALAMBRON_CODIGO_2= "Alambron_codigo_dos";










    // Columnas de la tabla trefilacion codigo 2
    public static final String COLUMN_CODIGO = "codigo";
    public static final String COLUMN_ID_DETALLE = "id_detalle";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_CONSECUTIVO = "consecutivo";
    public static final String COLUMN_ID_ROLLO = "id_rollo";
    public static final String COLUMN_OPERARIO = "operario";
    public static final String COLUMN_DIAMETRO = "diametro";
    public static final String COLUMN_MATERIA_PRIMA = "materia_prima";
    public static final String COLUMN_COLADA = "colada";
    public static final String COLUMN_TRACCION = "traccion";
    public static final String COLUMN_PESO = "peso";
    public static final String COLUMN_COD_ORDEN = "cod_orden";
    public static final String COLUMN_FECHA_HORA = "fecha_hora";
    public static final String COLUMN_CLIENTE = "cliente";
    public static final String COLUMN_MANUAL = "manual";
    public static final String COLUMN_ANULADO = "anulado";
    public static final String COLUMN_DESTINO = "destino";


    // Columnas de la tabla trefilacion codigo 2 bodega 4
    public static final String COLUMN_CODIGO_F = "codigo";
    public static final String COLUMN_ID_DETALLE_F = "id_detalle";
    public static final String COLUMN_NOMBRE_F = "nombre";
    public static final String COLUMN_CONSECUTIVO_F = "consecutivo";
    public static final String COLUMN_ID_ROLLO_F = "id_rollo";
    public static final String COLUMN_OPERARIO_F = "operario";
    public static final String COLUMN_DIAMETRO_F = "diametro";
    public static final String COLUMN_MATERIA_PRIMA_F = "materia_prima";
    public static final String COLUMN_COLADA_F = "colada";
    public static final String COLUMN_TRACCION_F = "traccion";
    public static final String COLUMN_PESO_F = "peso";
    public static final String COLUMN_COD_ORDEN_F = "cod_orden";
    public static final String COLUMN_FECHA_HORA_F = "fecha_hora";
    public static final String COLUMN_CLIENTE_F = "cliente";
    public static final String COLUMN_MANUAL_F = "manual";
    public static final String COLUMN_ANULADO_F = "anulado";
    public static final String COLUMN_DESTINO_F = "destino";

    public static final String COLUMN_NO_CONFORME_F="no_conforme";


    // Columnas de la tabla trefilacion codigo 3
    public static final String COLUMN_CODIGO_T = "codigo";
    public static final String COLUMN_ID_DETALLE_T = "id_detalle";
    public static final String COLUMN_NOMBRE_T = "nombre";
    public static final String COLUMN_CONSECUTIVO_T = "consecutivo";
    public static final String COLUMN_ID_ROLLO_T = "id_rollo";
    public static final String COLUMN_OPERARIO_T = "operario";
    public static final String COLUMN_DIAMETRO_T = "diametro";
    public static final String COLUMN_MATERIA_PRIMA_T = "materia_prima";
    public static final String COLUMN_COLADA_T = "colada";
    public static final String COLUMN_TRACCION_T = "traccion";
    public static final String COLUMN_PESO_T = "peso";
    public static final String COLUMN_COD_ORDEN_T = "cod_orden";
    public static final String COLUMN_FECHA_HORA_T = "fecha_hora";
    public static final String COLUMN_CLIENTE_T = "cliente";
    public static final String COLUMN_MANUAL_T = "manual";
    public static final String COLUMN_ANULADO_T = "anulado";
    public static final String COLUMN_DESTINO_T = "destino";


    // Columnas de la tabla trefilacion codigo 3 bodega 4
    public static final String COLUMN_CODIGO_FT = "codigo";
    public static final String COLUMN_ID_DETALLE_FT = "id_detalle";
    public static final String COLUMN_NOMBRE_FT = "nombre";
    public static final String COLUMN_CONSECUTIVO_FT = "consecutivo";
    public static final String COLUMN_ID_ROLLO_FT = "id_rollo";
    public static final String COLUMN_OPERARIO_FT = "operario";
    public static final String COLUMN_DIAMETRO_FT = "diametro";
    public static final String COLUMN_MATERIA_PRIMA_FT = "materia_prima";
    public static final String COLUMN_COLADA_FT = "colada";
    public static final String COLUMN_TRACCION_FT = "traccion";
    public static final String COLUMN_PESO_FT = "peso";
    public static final String COLUMN_COD_ORDEN_FT = "cod_orden";
    public static final String COLUMN_FECHA_HORA_FT = "fecha_hora";
    public static final String COLUMN_CLIENTE_FT = "cliente";
    public static final String COLUMN_MANUAL_FT = "manual";
    public static final String COLUMN_ANULADO_FT = "anulado";
    public static final String COLUMN_DESTINO_FT = "destino";

    public static final String COLUMN_NO_CONFORME_FT = "no_conforme";

    // Columnas de la tabla destino galvanizado codigo 2
    public static final String COLUMN_CODIGO_G = "codigo";
    public static final String COLUMN_NOMBRE_G = "nombre";
    public static final String COLUMN_ID_DETALLE_G = "id_detalle";
    public static final String COLUMN_ID_ROLLO_G = "id_rollo";

    public static final String COLUMN_TRASLADO_G = "traslado";
    public static final String COLUMN_PESO_G = "peso";
    public static final String COLUMN_COD_ORDEN_G = "cod_orden";
    public static final String COLUMN_FECHA_HORA_G = "fecha_hora";

    public static final String  COLUMN_MANUAL_G="manual";
    public static final String COLUMN_ANULADO_G = "anulado";
    public static final String COLUMN_DESTINO_G = "destino";

    // Columnas de la tabla puntilleria codigo 2
    public static final String COLUMN_CODIGO_P = "codigo";
    public static final String COLUMN_NOMBRE_p = "nombre";
    public static final String COLUMN_ID_DETALLE_P = "id_detalle";
    public static final String COLUMN_ID_ROLLO_P = "id_rollo";
    public static final String COLUMN_TRASLADO_P = "traslado";
    public static final String COLUMN_PESO_P = "peso";
    public static final String COLUMN_COD_ORDEN_P = "cod_orden";
    public static final String COLUMN_FECHA_HORA_P = "fecha_hora";

    public static final String  COLUMN_MANUAL_P="manual";
    public static final String COLUMN_ANULADO_P = "anulado";
    public static final String COLUMN_DESTINO_P = "destino";





    // Columnas de la tabla galvanizado codigo 2
    public static final String COLUMN_CODIGO_C = "codigo";
    public static final String COLUMN_NOMBRE_C = "nombre";
    public static final String COLUMN_NRO_ORDEN_C = "cod_orden";
    public static final String COLUMN_NRO_ROLLO_C = "id_rollo";

    public static final String COLUMN_TIPO_TRANS_C= "tipo_trans";

    public static final String COLUMN_TRASLADO_C = "traslado";
    public static final String COLUMN_PESO_C = "peso";
    public static final String COLUMN_FECHA_HORA_C = "fecha_hora";




    // Columnas de la tabla Recocido codigo 2
    public static final String COLUMN_CODIGO_R = "codigo";
    public static final String COLUMN_NOMBRE_R = "nombre";
    public static final String COLUMN_COD_ORDEN_R = "cod_orden";
    public static final String COLUMN_ID_DETALLE_R = "id_detalle";
    public static final String COLUMN_ID_ROLLO_R = "id_rollo";

    public static final String COLUMN_PESO_R = "peso";

    // Columnas de la tabla Recocido codigo 2
    public static final String COLUMN_CODIGO_RT = "codigo";
    public static final String COLUMN_NOMBRE_RT = "nombre";
    public static final String COLUMN_COD_ORDEN_RT = "cod_orden";
    public static final String COLUMN_ID_DETALLE_RT = "id_detalle";
    public static final String COLUMN_ID_ROLLO_RT = "id_rollo";

    public static final String COLUMN_PESO_RT = "peso";

    // Columnas de la tabla Recocido codigo 3
    public static final String COLUMN_CODIGO_D = "codigo";
    public static final String COLUMN_NOMBRE_D = "nombre";
    public static final String COLUMN_COD_ORDEN_D = "cod_orden";
    public static final String COLUMN_ID_DETALLE_D = "id_detalle";
    public static final String COLUMN_ID_ROLLO_D = "id_rollo";
    public static final String COLUMN_PESO_D = "peso";

    // Columnas de la tabla Recocido codigo 3
    public static final String COLUMN_CODIGO_DT = "codigo";
    public static final String COLUMN_NOMBRE_DT = "nombre";
    public static final String COLUMN_COD_ORDEN_DT = "cod_orden";
    public static final String COLUMN_ID_DETALLE_DT = "id_detalle";
    public static final String COLUMN_ID_ROLLO_DT = "id_rollo";
    public static final String COLUMN_PESO_DT = "peso";

    // Columnas de la tabla Alambron codigo 1
    public static final String COLUMN_NIT_PROVEEDOR_X = "nit_proveedor";
    public static final String COLUMN_NUM_IMPORTACION_X = "num_importacion";
    public static final String COLUMN_ID_SOLICITUD_DET_X= "id_solicitud_det";
    public static final String COLUMN_NUMERO_ROLLO_X = "numero_rollo";
    public static final String COLUMN_PESO_X= "peso";
    public static final String COLUMN_CODIGO_X = "codigo";
    public static final String COLUMN_COSTO_KILO_X = "costo_kilo";

    // Columnas de la tabla Alambron codigo 2
    public static final String COLUMN_NIT_PROVEEDOR_Y = "nit_proveedor";
    public static final String COLUMN_NUM_IMPORTACION_Y = "num_importacion";
    public static final String COLUMN_ID_SOLICITUD_DET_Y= "id_solicitud_det";
    public static final String COLUMN_NUMERO_ROLLO_Y = "numero_rollo";
    public static final String COLUMN_PESO_Y= "peso";
    public static final String COLUMN_CODIGO_Y = "codigo";
    public static final String COLUMN_COSTO_KILO_Y = "costo_kilo";








    // Sentencia SQL para crear la tabla de trefilacion codigo 2
    private static final String CREATE_TABLE_TREFILACION_CODIGO_2 = "CREATE TABLE " +
            TABLE_TREFILACION_CODIGO_2 + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +  // Autoincrement para id
            COLUMN_CODIGO + " TEXT NOT NULL, " +
            COLUMN_NOMBRE + " TEXT, " +
            COLUMN_ID_DETALLE + " TEXT, " +
            COLUMN_CONSECUTIVO + " TEXT, " +
            COLUMN_ID_ROLLO + " TEXT, " +
            COLUMN_OPERARIO + " TEXT, " +
            COLUMN_DIAMETRO + " TEXT, " +
            COLUMN_MATERIA_PRIMA + " TEXT, " +
            COLUMN_COLADA + " TEXT, " +
            COLUMN_TRACCION + " TEXT, " +
            COLUMN_PESO + " REAL, " +
            COLUMN_COD_ORDEN + " TEXT, " +
            COLUMN_FECHA_HORA + " TEXT, " +
            COLUMN_CLIENTE + " TEXT, " +
            COLUMN_MANUAL + " TEXT, " +
            COLUMN_ANULADO + " TEXT, " +
            COLUMN_DESTINO + " TEXT)";


    private static final String CREATE_TABLE_TREFILACION_CODIGO_2_NO_CONFORME = "CREATE TABLE " +
            TABLE_TREFILACION_CODIGO_2_NO_CONFORME + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +  // Autoincrement para id
            COLUMN_CODIGO_F + " TEXT NOT NULL, " +
            COLUMN_NOMBRE_F + " TEXT, " +
            COLUMN_ID_DETALLE_F + " TEXT, " +
            COLUMN_CONSECUTIVO_F + " TEXT, " +
            COLUMN_ID_ROLLO_F + " TEXT, " +
            COLUMN_OPERARIO_F + " TEXT, " +
            COLUMN_DIAMETRO_F + " TEXT, " +
            COLUMN_MATERIA_PRIMA_F + " TEXT, " +
            COLUMN_COLADA_F + " TEXT, " +
            COLUMN_TRACCION_F + " TEXT, " +
            COLUMN_PESO_F + " REAL, " +
            COLUMN_COD_ORDEN_F + " TEXT, " +
            COLUMN_FECHA_HORA_F + " TEXT, " +
            COLUMN_CLIENTE_F + " TEXT, " +
            COLUMN_MANUAL_F + " TEXT, " +
            COLUMN_ANULADO_F + " TEXT, " +
            COLUMN_DESTINO_F + " TEXT, " +
            COLUMN_NO_CONFORME_F + " TEXT)";

    // Sentencia SQL para crear la tabla trefilacion codigo 3

    private static final String CREATE_TABLE_TREFILACION_CODIGO_3 = "CREATE TABLE " +
            TABLE_TREFILACION_CODIGO_3 + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +  // Autoincrement para id
            COLUMN_CODIGO_T + " TEXT NOT NULL, " +
            COLUMN_NOMBRE_T + " TEXT, " +
            COLUMN_ID_DETALLE_T + " TEXT, " +
            COLUMN_CONSECUTIVO_T + " TEXT, " +
            COLUMN_ID_ROLLO_T + " TEXT, " +
            COLUMN_OPERARIO_T + " TEXT, " +
            COLUMN_DIAMETRO_T + " TEXT, " +
            COLUMN_MATERIA_PRIMA_T + " TEXT, " +
            COLUMN_COLADA_T + " TEXT, " +
            COLUMN_TRACCION_T + " TEXT, " +
            COLUMN_PESO_T + " REAL, " +
            COLUMN_COD_ORDEN_T + " TEXT, " +
            COLUMN_FECHA_HORA_T + " TEXT, " +
            COLUMN_CLIENTE_T + " TEXT, " +
            COLUMN_MANUAL_T + " TEXT, " +
            COLUMN_ANULADO_T + " TEXT, " +
            COLUMN_DESTINO_T + " TEXT)";

    private static final String CREATE_TABLE_TREFILACION_CODIGO_3_NO_CONFORME = "CREATE TABLE " +
            TABLE_TREFILACION_CODIGO_3_NO_CONFORME + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +  // Autoincrement para id
            COLUMN_CODIGO_FT + " TEXT NOT NULL, " +
            COLUMN_NOMBRE_FT + " TEXT, " +
            COLUMN_ID_DETALLE_FT + " TEXT, " +
            COLUMN_CONSECUTIVO_FT + " TEXT, " +
            COLUMN_ID_ROLLO_FT + " TEXT, " +
            COLUMN_OPERARIO_FT + " TEXT, " +
            COLUMN_DIAMETRO_FT + " TEXT, " +
            COLUMN_MATERIA_PRIMA_FT + " TEXT, " +
            COLUMN_COLADA_FT + " TEXT, " +
            COLUMN_TRACCION_FT + " TEXT, " +
            COLUMN_PESO_FT + " REAL, " +
            COLUMN_COD_ORDEN_FT + " TEXT, " +
            COLUMN_FECHA_HORA_FT + " TEXT, " +
            COLUMN_CLIENTE_FT + " TEXT, " +
            COLUMN_MANUAL_FT + " TEXT, " +
            COLUMN_ANULADO_FT + " TEXT, " +
            COLUMN_DESTINO_FT + " TEXT, " +
            COLUMN_NO_CONFORME_FT + " TEXT)";


    // Sentencia SQL para crear la tabla de destino galvanizado codigo 2
    private static final String CREATE_TABLE_DESTINO_GALVANIZADO_CODIGO_2 = "CREATE TABLE " +
            TABLE_DESTINO_GALVANIZADO_CODIGO_2 + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +  // Autoincrement para id
            COLUMN_CODIGO_G + " TEXT NOT NULL, " +
            COLUMN_NOMBRE_G + " TEXT, " +
            COLUMN_ID_DETALLE_G + " TEXT, " +
            COLUMN_ID_ROLLO_G + " TEXT, " +
            COLUMN_TRASLADO_G + " TEXT, " +
            COLUMN_PESO_G + " REAL, " +
            COLUMN_COD_ORDEN_G + " TEXT, " +
            COLUMN_FECHA_HORA_G + " TEXT, " +
            COLUMN_MANUAL_G + " TEXT, " +
            COLUMN_ANULADO_G + " TEXT, " +
            COLUMN_DESTINO_G + " TEXT)";



    // Sentencia SQL para crear la tabla de  galvanizado codigo 2
    private static final String CREATE_TABLE_GALVANIZADO_CODIGO_2 = "CREATE TABLE " +
            TABLE_GALVANIZADO_CODIGO_2 + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +  // Autoincrement para id
            COLUMN_CODIGO_C + " TEXT NOT NULL, " +
            COLUMN_NOMBRE_C + " TEXT, " +
            COLUMN_NRO_ORDEN_C + " TEXT, " +
            COLUMN_NRO_ROLLO_C + " TEXT, " +
            COLUMN_TIPO_TRANS_C + " TEXT, " +
            COLUMN_TRASLADO_C + " TEXT, " +
            COLUMN_PESO_C + " REAL, " +
            COLUMN_FECHA_HORA_C + " TEXT)";




    private static final String CREATE_TABLE_GALVANIZADO_CODIGO_2_BODEGA_12 = "CREATE TABLE " +
            TABLE_GALVANIZADO_CODIGO_2_BODEGA_12 + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +  // Autoincrement para id
            COLUMN_CODIGO_C + " TEXT NOT NULL, " +
            COLUMN_NOMBRE_C + " TEXT, " +
            COLUMN_NRO_ORDEN_C + " TEXT, " +
            COLUMN_NRO_ROLLO_C + " TEXT, " +
            COLUMN_TIPO_TRANS_C + " TEXT, " +
            COLUMN_TRASLADO_C + " TEXT, " +
            COLUMN_PESO_C + " REAL, " +
            COLUMN_FECHA_HORA_C + " TEXT)";


    // Sentencia SQL para crear la tabla de puntilleria codigo 2
    private static final String CREATE_TABLE_PUNTILLERIA_CODIGO_2 = "CREATE TABLE " +
            TABLE_PUNTILLERIA_CODIGO_2 + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_CODIGO_P + " TEXT NOT NULL, " +
            COLUMN_NOMBRE_p + " TEXT, " +
            COLUMN_ID_DETALLE_P + " TEXT, " +
            COLUMN_ID_ROLLO_P + " TEXT, " +
            COLUMN_TRASLADO_P + " TEXT, " +
            COLUMN_PESO_P + " REAL, " +
            COLUMN_COD_ORDEN_P + " TEXT, " +
            COLUMN_FECHA_HORA_P + " TEXT, " +
            COLUMN_MANUAL_P + " TEXT, " +
            COLUMN_DESTINO_P + " TEXT, " +
            COLUMN_ANULADO_P + " TEXT)";



    private static final String CREATE_TABLE_RECOCIDO_CODIGO_2 = "CREATE TABLE " +
            TABLE_RECOCIDO_CODIGO_2 + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_CODIGO_R + " TEXT NOT NULL, " +
            COLUMN_NOMBRE_R + " TEXT, " +
            COLUMN_COD_ORDEN_R + " TEXT, " +
            COLUMN_ID_DETALLE_R + " TEXT, " +
            COLUMN_ID_ROLLO_R + " TEXT, " +
            COLUMN_PESO_R + " REAL)";

    private static final String CREATE_TABLE_RECOCIDO_CODIGO_2_NO_CONFORME = "CREATE TABLE " +
            TABLE_RECOCIDO_CODIGO_2_NO_CONFORME + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_CODIGO_RT + " TEXT NOT NULL, " +
            COLUMN_NOMBRE_RT + " TEXT, " +
            COLUMN_COD_ORDEN_RT + " TEXT, " +
            COLUMN_ID_DETALLE_RT + " TEXT, " +
            COLUMN_ID_ROLLO_RT + " TEXT, " +
            COLUMN_PESO_RT + " REAL)";

    private static final String CREATE_TABLE_RECOCIDO_CODIGO_3 = "CREATE TABLE " +
            TABLE_RECOCIDO_CODIGO_3 + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_CODIGO_D + " TEXT NOT NULL, " +
            COLUMN_NOMBRE_D + " TEXT, " +
            COLUMN_COD_ORDEN_D + " TEXT, " +
            COLUMN_ID_DETALLE_D + " TEXT, " +
            COLUMN_ID_ROLLO_D + " TEXT, " +
            COLUMN_PESO_D + " REAL)";

    private static final String CREATE_TABLE_RECOCIDO_CODIGO_3_NO_CONFORME = "CREATE TABLE " +
            TABLE_RECOCIDO_CODIGO_3_NO_CONFORME + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_CODIGO_DT + " TEXT NOT NULL, " +
            COLUMN_NOMBRE_DT + " TEXT, " +
            COLUMN_COD_ORDEN_DT + " TEXT, " +
            COLUMN_ID_DETALLE_DT + " TEXT, " +
            COLUMN_ID_ROLLO_DT + " TEXT, " +
            COLUMN_PESO_DT + " REAL)";



    // Sentencia SQL para crear la tabla de alambron codigo 1
    private static final String CREATE_TABLE_ALAMBRON_CODIGO_1= "CREATE TABLE " +
            TABLE_ALAMBRON_CODIGO_1 + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +  // Autoincrement para id
            COLUMN_NIT_PROVEEDOR_X + " TEXT NOT NULL, " +
            COLUMN_NUM_IMPORTACION_X + " TEXT, " +
            COLUMN_ID_SOLICITUD_DET_X + " TEXT, " +
            COLUMN_NUMERO_ROLLO_X + " TEXT, " +
            COLUMN_PESO_X + " REAL, " +
            COLUMN_CODIGO_X + " TEXT, " +
            COLUMN_COSTO_KILO_X + " TEXT)";


    private static final String CREATE_TABLE_ALAMBRON_CODIGO_2= "CREATE TABLE " +
            TABLE_ALAMBRON_CODIGO_2 + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +  // Autoincrement para id
            COLUMN_NIT_PROVEEDOR_Y + " TEXT NOT NULL, " +
            COLUMN_NUM_IMPORTACION_Y + " TEXT, " +
            COLUMN_ID_SOLICITUD_DET_Y + " TEXT, " +
            COLUMN_NUMERO_ROLLO_Y + " TEXT, " +
            COLUMN_PESO_Y + " REAL, " +
            COLUMN_CODIGO_Y + " TEXT, " +
            COLUMN_COSTO_KILO_Y + " TEXT)";





    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_TREFILACION_CODIGO_2);
        db.execSQL(CREATE_TABLE_TREFILACION_CODIGO_3);
        db.execSQL(CREATE_TABLE_TREFILACION_CODIGO_2_NO_CONFORME);
        db.execSQL(CREATE_TABLE_TREFILACION_CODIGO_3_NO_CONFORME);

        db.execSQL(CREATE_TABLE_DESTINO_GALVANIZADO_CODIGO_2);
        db.execSQL(CREATE_TABLE_PUNTILLERIA_CODIGO_2 );

        db.execSQL(CREATE_TABLE_GALVANIZADO_CODIGO_2);
        db.execSQL(CREATE_TABLE_GALVANIZADO_CODIGO_2_BODEGA_12);

        db.execSQL(CREATE_TABLE_RECOCIDO_CODIGO_2);
        db.execSQL(CREATE_TABLE_RECOCIDO_CODIGO_2_NO_CONFORME);

        db.execSQL(CREATE_TABLE_RECOCIDO_CODIGO_3);
        db.execSQL(CREATE_TABLE_RECOCIDO_CODIGO_3_NO_CONFORME);




        db.execSQL(CREATE_TABLE_ALAMBRON_CODIGO_1);
        db.execSQL(CREATE_TABLE_ALAMBRON_CODIGO_2);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TREFILACION_CODIGO_2);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TREFILACION_CODIGO_3);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TREFILACION_CODIGO_2_NO_CONFORME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TREFILACION_CODIGO_3_NO_CONFORME);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DESTINO_GALVANIZADO_CODIGO_2);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUNTILLERIA_CODIGO_2);
        ;

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GALVANIZADO_CODIGO_2);


        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GALVANIZADO_CODIGO_2_BODEGA_12);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECOCIDO_CODIGO_2);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECOCIDO_CODIGO_2_NO_CONFORME);



        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECOCIDO_CODIGO_3);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECOCIDO_CODIGO_3_NO_CONFORME);



        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALAMBRON_CODIGO_1);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALAMBRON_CODIGO_2);


        onCreate(db); // Vuelve a crear la tabla
    }


    public void deleteAllRowsFromTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TREFILACION_CODIGO_2, null, null);
        db.delete(TABLE_TREFILACION_CODIGO_3, null, null);
        db.delete(TABLE_TREFILACION_CODIGO_2_NO_CONFORME, null, null);
        db.delete(TABLE_TREFILACION_CODIGO_3_NO_CONFORME, null, null);

        db.delete(TABLE_DESTINO_GALVANIZADO_CODIGO_2, null, null);
        db.delete(TABLE_PUNTILLERIA_CODIGO_2,null, null);

        db.delete(TABLE_GALVANIZADO_CODIGO_2, null, null);

        db.delete(TABLE_GALVANIZADO_CODIGO_2_BODEGA_12, null, null);
        db.delete(TABLE_RECOCIDO_CODIGO_2, null, null);
        db.delete(TABLE_RECOCIDO_CODIGO_2_NO_CONFORME, null, null);


        db.delete(TABLE_RECOCIDO_CODIGO_3, null, null);

        db.delete(TABLE_RECOCIDO_CODIGO_3_NO_CONFORME, null, null);



        db.delete(TABLE_ALAMBRON_CODIGO_1, null, null);

        db.delete(TABLE_ALAMBRON_CODIGO_2, null, null);


        db.close();
    }


}
