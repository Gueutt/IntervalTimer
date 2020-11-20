package com.smapps.compteur;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private ConstraintLayout background;
    private LinearLayout sequencePicker;
    private LinearLayout arrowsContainer;
    private EditText sequenceCountDown;
    private TextView afficheurSecondes;
    private ImageView startButton;
    private ImageView upButton;
    private ImageView downButton;
    private ImageView cancelButton;

    private int detailSecondes = 0;
    private String bgStatus = "V";
    private boolean planColorChange = false;
    private Timer timer;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        background = findViewById(R.id.background);
        sequencePicker = findViewById(R.id.sequence_picker);
        arrowsContainer = findViewById(R.id.arrowsContainer);
        sequenceCountDown = findViewById(R.id.sequenceCountDown);
        afficheurSecondes = findViewById(R.id.countdownDisplayer);
        startButton = findViewById(R.id.startButton);
        upButton = findViewById(R.id.upButton);
        downButton = findViewById(R.id.downButton);
        cancelButton = findViewById(R.id.cancelButton);

        startButton.setOnClickListener(onClickStart());
        arrowsContainer.setOnClickListener(onClickArrows());
        cancelButton.setOnClickListener(onClickCancel());

        sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String previousSequence = sharedPreferences.getString(getString(R.string.SP_SEQUENCE), null);
        if (previousSequence != null && previousSequence.length() > 0) {
            sequenceCountDown.setText(previousSequence);
        }

        animerAfficheur();
    }

    private View.OnClickListener onClickStart() {
        return view -> {
            if(sequenceCountDown.getText() != null && !sequenceCountDown.getText().toString().isEmpty()){
                fermerSequencePicker();
                cancelButton.setVisibility(View.VISIBLE);
                fermerClavier(afficheurSecondes);
                sharedPreferences.edit().putString(getString(R.string.SP_SEQUENCE), sequenceCountDown.getText().toString()).apply();

                String[] strSequence = sequenceCountDown.getText().toString().split("/");

                List<Integer> sequence = new ArrayList<>();
                for (String str : strSequence) {
                    sequence.add(Integer.valueOf(str));
                }

                detailSecondes = sequence.get(0);
                enableStartButton(false);
                diminuerDecompteToutesLesSecondes(sequence);
            }
        };
    }

    private View.OnClickListener onClickArrows() {
        return view -> {
            if(sequencePicker.getVisibility() == View.GONE) {
                ouvrirSequencePicker();
            } else {
                fermerSequencePicker();
            }
        };
    }

    private View.OnClickListener onClickCancel() {
        return view -> {
            if (timer != null) {
                timer.cancel();
                afficheurSecondes.setText("00");
                cancelButton.setVisibility(View.GONE);
                if (!"V".equals(bgStatus)) {
                    changeBackgroundColor();
                }
                ouvrirSequencePicker();
                enableStartButton(true);
            }
        };
    }

    private void diminuerDecompteToutesLesSecondes(List<Integer> sequence){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                StringBuilder stringBuilder = new StringBuilder();
                if(detailSecondes < 10){
                    stringBuilder.append("0");
                }
                stringBuilder.append(detailSecondes);

                runOnUiThread(() -> {
                    afficheurSecondes.setText(stringBuilder);

                    if (planColorChange) {
                        changeBackgroundColor();
                    }
                    if (detailSecondes < 0) {
                        sequence.remove(0);

                        if(sequence.size() > 0){
                            detailSecondes = sequence.get(0);
                            planColorChange = true;
                        } else {
                            enableStartButton(true);
                            ouvrirSequencePicker();
                            cancelButton.setVisibility(View.GONE);
                            cancel();
                        }
                    }
                });

                if (detailSecondes < 4 && detailSecondes > 0) {
                    playWarningBeep();
                }
                if (detailSecondes == 0) {
                    playFinalBeep();
                }

                detailSecondes--;
            }
        }, 0, 1000);
    }

    private void changeBackgroundColor() {
        if ("V".equals(bgStatus)) {
            background.setBackgroundColor(getResources().getColor(R.color.rouge_3, null));
            bgStatus = "R";
        } else {
            background.setBackgroundColor(getResources().getColor(R.color.vert_3, null));
            bgStatus = "V";
        }
        planColorChange = false;
    }

    private void playFinalBeep(){
        new Thread(){
            @Override
            public void run() {
                MediaPlayer.create(MainActivity.this, R.raw.final_beep).start();
            }
        }.start();
    }

    private void playWarningBeep(){
        new Thread(){
            @Override
            public void run() {
                MediaPlayer.create(MainActivity.this, R.raw.warning_beep).start();
            }
        }.start();
    }

    private void fermerSequencePicker() {
        sequencePicker.setVisibility(View.GONE);
        downButton.setVisibility(View.GONE);
        upButton.setVisibility(View.VISIBLE);
    }

    private void ouvrirSequencePicker() {
        sequencePicker.setVisibility(View.VISIBLE);
        downButton.setVisibility(View.VISIBLE);
        upButton.setVisibility(View.GONE);
    }

    private void enableStartButton(boolean enable) {
        startButton.setEnabled(enable);
        if (enable) {
            startButton.setColorFilter(getColor(R.color.vert_7));
        } else {
            startButton.setColorFilter(getColor(R.color.gris_clair));
        }
    }

    private void fermerClavier(View view){
        InputMethodManager imm =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void animerAfficheur() {
        new Thread() {
            private String sens = "D";

            @Override
            public void run() {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(afficheurSecondes.getRotationY() == 60){
                            sens = "G";
                        }
                        if(afficheurSecondes.getRotationY() == -60){
                            sens = "D";
                        }

                        runOnUiThread(() -> {
                            if (sens.equals("D")) {
                                afficheurSecondes.setRotationY(afficheurSecondes.getRotationY() + 1);
                            } else {
                                afficheurSecondes.setRotationY(afficheurSecondes.getRotationY() - 1);
                            }
                        });
                    }
                }, 0, 22);
            }
        }.start();

        new Thread() {
            private String sens = "G";

            @Override
            public void run() {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(afficheurSecondes.getRotationX() == 60){
                            sens = "G";
                        }
                        if(afficheurSecondes.getRotationX() == -60){
                            sens = "D";
                        }

                        runOnUiThread(() -> {
                            if (sens.equals("D")) {
                                afficheurSecondes.setRotationX(afficheurSecondes.getRotationX() + 1);
                            } else {
                                afficheurSecondes.setRotationX(afficheurSecondes.getRotationX() - 1);
                            }
                        });
                    }
                }, 0, 17);
            }
        }.start();
    }
}