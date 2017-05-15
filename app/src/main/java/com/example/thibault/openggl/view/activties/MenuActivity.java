package com.example.thibault.openggl.view.activties;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.thibault.openggl.R;

/**
 * Created by thibault on 03/05/17.
 */

public class MenuActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_menu);

        Button play = (Button) findViewById(R.id.buttonPlay);
        Switch swic = (Switch) findViewById(R.id.powerSettings);
        swic.setChecked(false);
        TextView text = (TextView) findViewById(R.id.isActivated);
        text.setText(R.string.switch_off);

        //Quand on clique sur le bouton jouer cela nous envoi vers l'activité de jeu
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent play = new Intent(getApplicationContext(),GameActivity.class);
                Switch swic = (Switch) findViewById(R.id.powerSettings);
                play.putExtra("powerAtivated",swic.isChecked());
                startActivityForResult(play,1);
            }
        });

        //Quand on clique sur le switch change le texte
        swic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView text = (TextView) findViewById(R.id.isActivated);
                if (isChecked){
                    text.setText(R.string.switch_activated);
                }else{
                    text.setText(R.string.switch_off);
                }
            }
        });
    }
}
