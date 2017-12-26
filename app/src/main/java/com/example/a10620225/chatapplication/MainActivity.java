package com.example.a10620225.chatapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a10620225.chatapplication.model.ChatMessage;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import static android.text.TextUtils.*;

public class MainActivity extends AppCompatActivity {

    final static int SIGN_IN_REQUEST_CODE = 2;

    private FloatingActionButton fab;
    private ListView listOfMessages;
    private FirebaseListAdapter<ChatMessage> adapter;

    private FirebaseAuth firebaseAuth;
   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        } else {
            Toast.makeText(this,
                    "Welcome " + FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName(),
                    Toast.LENGTH_LONG)
                    .show();
            listOfMessages = findViewById(R.id.list_of_messages);
            // Load chat room contents
            displayChatMessages();

            fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v("FAB ", "Onclick called");
                    EditText input = (EditText)findViewById(R.id.input);
                    String message = input.getText().toString();
                    String user = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .push()
                            .setValue(new ChatMessage(message, user));
                    input.setText("");
                }
            });
        }
        listOfMessages = findViewById(R.id.list_of_messages);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Successfully signed in!",
                        Toast.LENGTH_LONG).show();
                displayChatMessages();
            } else {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        }
    }
    void displayChatMessages(){
        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView user = (v.findViewById(R.id.message_user));
                TextView message = v.findViewById(R.id.message_text);
                TextView date = v.findViewById(R.id.message_time);

                user.setText(model.getMessageUser());
                message.setText(model.getMessageText());
                date.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };
        listOfMessages.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out){
            AuthUI.getInstance().signOut(MainActivity.this);
            finish();
        }
        return true;
    }
}
