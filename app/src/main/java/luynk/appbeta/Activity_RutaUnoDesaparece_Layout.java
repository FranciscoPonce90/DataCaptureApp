package luynk.appbeta;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import model.Configuracion;


public class Activity_RutaUnoDesaparece_Layout extends View {
    private ArrayList<Posicion> ruta = new ArrayList<>();
    Bitmap elemento;
    int elemento_x, elemento_y, x_dir, y_dir, contador;
    int elementoHeight, elementoWidth;
    boolean flag;
    String corX="NaN", corY="NaN";

    private Paint mPaint;
    private Path mPath;

    Configuracion config;
    String idUsuario;

    FileOutputStream fos;

    private final static int interval = 5;
    Handler mHandler = new Handler();

    //Hanbler que corre cada 5 milisegundos en el background
    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            ruta.add(new Posicion(corX, corY));
            mHandler.postDelayed(mHandlerTask, interval);
        }
    };

    //inicializar handler
    void startRepeatingTask()
    {
        mHandlerTask.run();
    }

    //detener handler
    void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mHandlerTask);
    }



    public Activity_RutaUnoDesaparece_Layout(Context context) throws FileNotFoundException {
        super(context);

        elemento_x = 0;
        elemento_y = getScreenHeight()/2;
        x_dir = 2;
        y_dir = -2;
        contador = 0;
        flag = true;
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);
        mPath = new Path();

        config = ((RutaUnoDesaparece)getContext()).getConfig();
        idUsuario = ((RutaUnoDesaparece)getContext()).getIdUsuario();

        String rootPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/PruebaApp/";
        File root = new File(rootPath);
        if (!root.exists()) {
            root.mkdirs();
        }

        File f = new File(rootPath + "coorDesaparece.txt");
        if (f.exists()) {
            f.delete();
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fos = new FileOutputStream(f);

        //inicio handler
        startRepeatingTask();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
        super.onDraw(canvas);

        //flag para controlar ejecucion de metodo onDraw
        if(flag) {
            //Crear objeto para obtener ancho y largo
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.cuadrado_peque, options);
            elementoHeight = options.outHeight;
            elementoWidth = options.outWidth;

            //finalizar metodo si se llega al final de la pantalla
            if (elemento_x >= canvas.getWidth() - elementoWidth * 2) {
                x_dir = 0;
                y_dir = 0;
                flag = false;

                stopRepeatingTask();

                //Guardar ruta de usuario en archivo
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(ruta);
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //guardar imagen
                this.setDrawingCacheEnabled(true);
                this.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                Bitmap bitmap = this.getDrawingCache();
                String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/PruebaApp/";
                File file = new File(path+"/imagenDesaparece.png");
                FileOutputStream ostream;
                try {
                    file.createNewFile();
                    ostream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                    ostream.flush();
                    ostream.close();
                    Toast.makeText(this.getContext(), "Imagen Guardada", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }

                // nueva activity
                Intent newIntent = new Intent(this.getContext(), SeleccionaConfiguracion.class);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.getContext().startActivity(newIntent);

            } else {
                //Elegir ruta
                if (contador == 200) {
                    y_dir = 2;
                } else if (contador == 400) {
                    y_dir = -2;
                    contador = 0;
                }
            }

            //Calcular movimiento del objeto en base a x_dir e y_dir
            elemento_x = elemento_x + x_dir;
            elemento_y = elemento_y + y_dir;

            //Para clase desaparece
            String porcentaje = config.getDesaparece();

            int inferior;
            double superior;
            if(porcentaje.contains("2550")){
                inferior=1;
                superior = 1.5;
            }else if (porcentaje.contains("5075")){
                inferior=2;
                superior=2.5;
            }else{
                inferior=3;
                superior = 3.5;
            }

            int aux = canvas.getWidth()/4;
            if (elemento_x>=aux*inferior && elemento_x<aux*superior)
            {
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                        elemento, 1, 1, false);
                canvas.drawBitmap(resizedBitmap, elemento_x, elemento_y, null);
            }else{
                //Dibujar el objeto
                if (config.getFigura().contains("cuadrado"))
                    elemento = BitmapFactory.decodeResource(getResources(), R.drawable.cuadrado_peque);
                else if (config.getFigura().contains("circulo"))
                    elemento = BitmapFactory.decodeResource(getResources(), R.drawable.circulo_peque);
                else if (config.getFigura().contains("triangulo"))
                    elemento = BitmapFactory.decodeResource(getResources(), R.drawable.triangulo_peque);
                else
                    elemento = BitmapFactory.decodeResource(getResources(), R.drawable.rombo_peque);
                canvas.drawBitmap(elemento, elemento_x, elemento_y, null);
            }

            contador++;
            invalidate();
        }
    }

    //metodo que se ejecuta en cada touch
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){

            case MotionEvent.ACTION_DOWN:
                corX = String.valueOf(Math.round(event.getX()));
                corY = String.valueOf(Math.round(event.getY()));
                mPath.moveTo(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_MOVE:
                corX = String.valueOf(Math.round(event.getX()));
                corY = String.valueOf(Math.round(event.getY()));
                mPath.lineTo(event.getX(), event.getY());
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                corX = "NaN";
                corY = "NaN";
                break;
        }

        return true;
    }
   /* public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }*/

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}

