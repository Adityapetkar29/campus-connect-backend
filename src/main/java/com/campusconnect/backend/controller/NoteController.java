package com.campusconnect.backend.controller;

import com.campusconnect.backend.entity.Note;
import com.campusconnect.backend.repository.NoteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/notes")
public class NoteController {

    private final NoteRepository noteRepository;

    public NoteController(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @GetMapping
    public ResponseEntity<?> getStudentNotes() {

        List<Note> notes =
                noteRepository.findByTargetRole("STUDENT");

        return ResponseEntity.ok(notes);
    }
}