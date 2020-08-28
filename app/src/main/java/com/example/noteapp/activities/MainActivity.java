package com.example.noteapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.noteapp.R;
import com.example.noteapp.adapters.NotesAdapter;
import com.example.noteapp.database.NotesDatabase;
import com.example.noteapp.entities.Note;
import com.example.noteapp.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    private int noteClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        );


        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);

//      the getNotes() method is called from the conCreate() method of an activity. It means the
//      application is just started and we need to display all notes from the database and that's why we are passing request_code_show_notes to that method.
        getNotes(REQUEST_CODE_SHOW_NOTES, false);
    }

    @Override
    public void onNotesClicked(Note note, int position) {
            noteClickedPosition = position;
            Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
            intent.putExtra("isViewOrUpdate", true);
            intent.putExtra("note", note);
            startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }
//  getting the request code as method parameter
    private void getNotes(final int requestCode, final boolean isNoteDeleted){

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                    // Request code is REQUEST_CODE_SHOW_NOTE,
                    // so we are adding all notes from database to noteList and notify adapter about the new data set.
                if (requestCode == REQUEST_CODE_SHOW_NOTES){
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }else if (requestCode == REQUEST_CODE_ADD_NOTE){
                    // request code is REQUEST_CODE_ADD_NOTE, so
                    // we are adding an only newly added note from the database to noteList and to notify
                    // the adapter for the newly inserted item and scrolling recycler view to the top
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                }else if (requestCode == REQUEST_CODE_UPDATE_NOTE){
                    // Request code is REQUEST_CODE_UPDATE_NOTE, so we are removing note from the
                    // clicked position and adding the latest updated note from the same position from
                    // the database and notify the adapter for item changed at the position.
                    noteList.remove(noteClickedPosition);
                    // if note is deleted the notify adapter about it. if not then it must be updated
                    // that is why we are adding a newly updated note tot hat same position where we removed
                    // and notifying adapter about it.
                    if(isNoteDeleted){
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    }else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);

                    }

                }
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
//          this getNotes() method is called from the onActivityResult() method of an activity and we
//          checked the current request code is for add and the result is RESULT_OK. It means a new note is
//          added from CreateNote activity and its result is sent back to this activity that's why we are passing
//          REQUEST_CODE_ADD_NOTE to that method.
            getNotes(REQUEST_CODE_ADD_NOTE, false);

//          this getNotes() method is called from the onActivityResult() method of an activity and we
//          checked the current request code is for update and the result is RESULT_OK. It means already
//          available note is updated from CreateNote activity and its result is sent back to this
//          activity that's why we are passing REQUEST_CODE_ADD_NOTE to that method.
        }else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK)
        getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));{
        }
    }
}

















