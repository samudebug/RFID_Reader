package com.example.samuel.rfidreader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.example.samuel.rfidreader.database.RfidDAO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    private static final Pattern RFID_KEYCODE = Pattern.compile("KEYCODE_(\\d)");
    TextView mensagem;
    StringBuilder sb;
    boolean executed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mensagem = findViewById(R.id.mensagem);
        sb = new StringBuilder();
        executed = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        String key = KeyEvent.keyCodeToString(keyCode);

        if (executed) {
            sb = new StringBuilder();
        } else {
            executed = false;

        }

        Matcher matcher = RFID_KEYCODE.matcher(key);

        if (matcher.matches()) {

            sb.append(matcher.group(1));
        }

        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            String code = sb.toString();
            RfidDAO rfidDAO = RfidDAO.getInstance(this);
            rfidDAO.open();
            Funcionario funcionario = rfidDAO.getFuncionario(code);
            if(funcionario != null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://maker.ifttt.com/trigger/rfid_passed_teste/with/key/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ApiInterface api = retrofit.create(ApiInterface.class);

                RFIDJson json = new RFIDJson();
                json.setValue1(code);
                json.setValue2(funcionario.getNome());
                funcionario.setEntrou(!funcionario.isEntrou());
                rfidDAO.updateFuncionario(funcionario);
                json.setValue3(funcionario.isEntrou() ? "SAIU" : "ENTROU");
                Call<RFIDJson> call = api.enviarCode(json);

                call.enqueue(new Callback<RFIDJson>() {
                    @Override
                    public void onResponse(Call<RFIDJson> call, Response<RFIDJson> response) {

                    }

                    @Override
                    public void onFailure(Call<RFIDJson> call, Throwable t) {
                    }
                });
                executed = true;
            }
        }


        return super.onKeyDown(keyCode, event);
    }
}
