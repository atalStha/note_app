package com.example.noteapp.listeners;

import com.example.noteapp.entities.Note;

public interface NotesListener {
    void onNotesClicked(Note note,
                        int position);
}
