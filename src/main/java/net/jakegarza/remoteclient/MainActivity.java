package net.jakegarza.remoteclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    public static String ip = "";
    private static Context context;

    private Button btnLMB;
    private Button btnRMB;
    private Button btnBackspace;
    private EditText txtToType;
    private ImageView imgScreen;

    private int mouseX = 0;
    private int mouseY = 0;

    private int scrollDrag = 8;
    private int lastX = 0, lastY = 0;
    private int lastUpX = 0, lastUpY = 0;
    private int lastDownX = 0, lastDownY;
    private long lastDownTime = -1;

    private int x = 0, y = 0;

    private static Queue<String> messageQueue = null;
    private static TCPClient tcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        btnLMB = findViewById(R.id.btnLMB);
        btnRMB = findViewById(R.id.btnRMB);
        btnBackspace = findViewById(R.id.btnBackspace);
        txtToType = findViewById(R.id.txtToType);
        imgScreen = findViewById(R.id.imgScreen);

        messageQueue = new LinkedList<String>();

        Thread networkThread = new Thread(new Runnable() {
            public void run() {
                try {
                    tcpClient = new TCPClient(new Socket(ip, 25566));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        });
        networkThread.start();


        final ConstraintLayout.LayoutParams txtToTypeLayoutParams = (ConstraintLayout.LayoutParams) txtToType.getLayoutParams();

        txtToType.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH ||
                        i == EditorInfo.IME_ACTION_DONE ||
                        keyEvent != null &&
                                keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                                keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (keyEvent == null || !keyEvent.isShiftPressed()) {
                        Log.e("Done Typing", "Done Typing");

                        final String keysToSend = txtToType.getText().toString();
                        messageQueue.add("KEYS:" + keysToSend);

                        //When user is done typing, set the edit text and button to original settings
                        txtToType.setLayoutParams(txtToTypeLayoutParams);
                        btnBackspace.setVisibility(View.VISIBLE);
                        txtToType.setText("T");
                    }
                }
                return false;
            }
        });

        txtToType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Make edit text bigger when typing and make backspace button invisible
                txtToType.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 0));
                btnBackspace.setVisibility(View.INVISIBLE);
                txtToType.setText("");
            }
        });

        btnLMB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageQueue.add("CLICK LMB");
            }
        });

        btnRMB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageQueue.add("CLICK RMB");
            }
        });

        btnBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageQueue.add("BACKSPACE");
            }
        });

        Thread messageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (messageQueue.size() != 0) {
                        String nextMessage = messageQueue.peek();
                        try {
                            tcpClient.print(nextMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        messageQueue.remove();
                    }
                }
            }
        });
        messageThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        int pointerCount = event.getPointerCount();

        if (pointerCount == 1) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                lastUpX = (int)event.getX();
                lastUpY = (int)event.getY();

                messageQueue.add("RELEASE LMB");
            }
            else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastDownX = (int)event.getX();
                lastDownY = (int)event.getY();

                if (lastDownTime == -1) {
                    lastDownTime = System.currentTimeMillis();
                }
                //If the user double taps, send a left mouse button click
                else {
                    if (System.currentTimeMillis()-lastDownTime <= 300) {
                        messageQueue.add("HOLD LMB");

                        Log.e("Double tap", "Double tap");
                    }
                }

                lastDownTime = System.currentTimeMillis();
            }
            else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                this.x = (int)event.getX();
                y = (int)event.getY();

                if (lastDownX < this.x)
                    mouseX += (this.x -lastDownX)/scrollDrag;
                else
                    mouseX -= (lastDownX- this.x)/scrollDrag;

                if (lastDownY < y)
                    mouseY += (y-lastDownY)/scrollDrag;
                else
                    mouseY -= (lastDownY-y)/scrollDrag;

                if (mouseX < 0) mouseX = 0;
                if (mouseY < 0) mouseY = 0;

                lastX = this.x;
                lastY = y;

                messageQueue.add("MOUSE: " + mouseX + ", " + mouseY);
            }

            Log.e("TouchTest", "x: " + this.x + " mouseX: " + mouseX + " upX: " + lastUpX);
        }
        //Only if we lift two fingers up, click right mouse button
        else if (pointerCount == 2 && event.getAction() == 262) {
                messageQueue.add("CLICK RMB");
        }
        //Only if we are moving two fingers, scroll the mouse wheel
        else if (pointerCount == 2 && event.getAction() == MotionEvent.ACTION_MOVE) {

            if (lastDownY < (int)event.getY())
                messageQueue.add("WHEEL DOWN");
            else
                messageQueue.add("WHEEL UP");
        }
        return true;
    }

    public void updateImage(final byte imageBytes[]) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imgScreen.setImageBitmap(bmp);
            }
        });
    }

    public static Context getContext() { return context; }
}
