package com.example.surmeet.csihack;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.Spinner;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {

   // Spinner states_spin,cat_spin;
    TextView user,logout;
    Button view,update,button;
    EditText number;
    Intent in;
    ArrayList<String> xlist;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    private final int PICK_IMAGE = 1;
    private ProgressDialog detectionProgressDialog;

    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("1cc1bc4c1f1a430e99159b5782da3dd3");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Button button1 = (Button)findViewById(R.id.button1);
       // states_spin=(Spinner)findViewById(R.id.spinner);
       // cat_spin=(Spinner)findViewById(R.id.spinner2);
        user=(TextView)findViewById(R.id.textView6);
     //   view=(Button)findViewById(R.id.view);
      //  update=(Button)findViewById(R.id.update);
      //  button=(Button)findViewById(R.id.button);
       // number=(EditText)findViewById(R.id.number);
        logout=(TextView)findViewById(R.id.textView8);



       // cat_spin.setVisibility(View.INVISIBLE);
//        button.setVisibility(View.INVISIBLE);
 //       number.setVisibility(View.INVISIBLE);

        firebaseAuth=FirebaseAuth.getInstance();

        Log.i("ACTIVITY 2","CREATED");

//        Snackbar.make(view,"Logged in as:"+firebaseAuth.getCurrentUser().getEmail().toString(),Snackbar.LENGTH_SHORT).show();

        if(firebaseAuth.getCurrentUser()==null)
        {
            startActivity(new Intent(this,MainActivity.class));
        }

        firebaseUser=firebaseAuth.getCurrentUser();

        user.setText(firebaseUser.getEmail());
        Log.i("USER", firebaseUser.getEmail());

        logout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
                builder.setMessage("Are you Sure?").setTitle("Log Out");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Toast.makeText(MainActivity2.this,"Logging out",Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        Log.i("LOG OUT","log out on logout");
                        startActivity(new Intent(getBaseContext(),MainActivity.class));
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });

        in=new Intent(this,MainActivity.class);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);
                gallIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(gallIntent, "Select Picture"), PICK_IMAGE);
            }
        });

        detectionProgressDialog = new ProgressDialog(this);

    }

    private void detectAndFrame(final Bitmap imageBitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    null           // returnFaceAttributes: a string like "age, gender"
                            );
                            if (result == null)
                            {
                                publishProgress("Detection Finished. Nothing detected");
                                return null;
                            }
                            publishProgress(
                                    String.format("Detection Finished. %d face(s) detected",
                                            result.length));
                            return result;
                        } catch (Exception e) {
                            publishProgress("Detection failed");
                            return null;
                        }
                    }
                    @Override
                    protected void onPreExecute() {
                        detectionProgressDialog.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        detectionProgressDialog.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
                        detectionProgressDialog.dismiss();
                        if (result == null) return;
                        ImageView imageView = (ImageView)findViewById(R.id.imageView1);
                        imageView.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, result));
                        imageBitmap.recycle();
                    }
                };
        detectTask.execute(inputStream);
    }
    private static Bitmap drawFaceRectanglesOnBitmap(Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        int stokeWidth = 2;
        paint.setStrokeWidth(stokeWidth);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
            }
        }
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ImageView imageView = (ImageView) findViewById(R.id.imageView1);
                imageView.setImageBitmap(bitmap);
                detectAndFrame(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this,"Logging out",Toast.LENGTH_SHORT).show();
        user.setText("");
        Snackbar.make(view,"Logged out",Snackbar.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();
        Log.i("LOG OUT","log out on back press");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("ACTIVITY 2","DESTROYED");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("ACTIVITY 2","PAUSED");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("ACTIVITY 2","RESTARTED");
        Log.i("ACTIVITY 2",firebaseAuth.getCurrentUser().getEmail().toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("ACTIVITY 2","RESUMED");
        Log.i("ACTIVITY 2",firebaseAuth.getCurrentUser().getEmail().toString());

    }

}
