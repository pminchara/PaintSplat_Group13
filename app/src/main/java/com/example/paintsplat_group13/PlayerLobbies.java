package com.example.paintsplat_group13;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PlayerLobbies extends AppCompatActivity {

   ListView listView;
   Button button;

   List<String> roomsList;

   String playerName = "";
   String roomName = "";

   FirebaseDatabase database;
   DatabaseReference roomRef;
   DatabaseReference roomsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_lobbies);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Available Lobbies");
        database = FirebaseDatabase.getInstance();

        //get the player name and assign his room to thr player name
        SharedPreferences preferences = getSharedPreferences("PREFS", 0);
        playerName = preferences.getString("playerName", "");
        roomName = playerName;

        listView = findViewById(R.id.listOfLobbies);
        button = findViewById(R.id.createRoomButton);
        //all vailable rooms
        roomsList = new ArrayList<>();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create room and add yourself
                button.setText("Creating room");
                button.setEnabled(false);
                roomName = playerName;
                roomRef = database.getReference("rooms/" + roomName + "/player1");
                addRoomEventListener();
                roomRef.setValue(playerName);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // join an existing room
                roomName = roomsList.get(position);
                roomRef = database.getReference("rooms/" + roomName);

                roomRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snap) {

                        for(int i=2; i<5;i++) {
                            DataSnapshot player = snap.child("player" + String.valueOf(i));

                            if(player.getValue() == null) {
                                DatabaseReference setRef = roomRef = database.getReference("rooms/" + roomName + "/player" + String.valueOf(i));
                                addRoomEventListener();
                                setRef.setValue(playerName);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        //show if new room is available
        addRoomsEventListener();
    }

    private void addRoomEventListener(){
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //join yhe room
                button.setText("Create Room");
                button.setEnabled(true);
                Intent intent = new Intent(getApplicationContext(), WaitingRoom.class);
                intent.putExtra("roomName", roomName);
                intent.putExtra("playerName", playerName);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // error
                button.setText("Create Room");
                button.setEnabled(true);
                Toast.makeText(PlayerLobbies.this, "Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addRoomsEventListener(){
        roomsRef = database.getReference("rooms");
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //show list of rooms
                roomsList.clear();
                Iterable<DataSnapshot> rooms = dataSnapshot.getChildren();
                for (DataSnapshot snapshot : rooms){
                    roomsList.add(snapshot.getKey());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(PlayerLobbies.this,
                            android.R.layout.simple_list_item_1, roomsList);
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //nothing
            }
        });
    }
}