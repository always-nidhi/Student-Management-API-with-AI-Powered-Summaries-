package com.assignment.studentapi;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // POST /students - Create a new student
    @PostMapping
    public ResponseEntity<Student> createStudent(@Valid @RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    // GET /students - Get all students
    @GetMapping
    public Collection<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    // GET /students/{id} - Get a student by ID
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable int id) {
        return studentService.getStudentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /students/{id} - Update a student by ID
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable int id, @Valid @RequestBody Student student) {
        return studentService.updateStudent(id, student)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /students/{id} - Delete a student by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable int id) {
        if (studentService.deleteStudent(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /students/{id}/summary - Generate summary using Ollama
    @GetMapping("/{id}/summary")
    public ResponseEntity<?> getStudentSummary(@PathVariable int id) {
        Optional<Student> studentOpt = studentService.getStudentById(id);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Student student = studentOpt.get();

        // Prompt Engineering: Create a clear prompt for the AI
        String prompt = String.format(
                "Generate a brief, one-paragraph professional summary for the following student profile. " +
                        "Focus on their potential as a student or future professional. " +
                        "Profile -> Name: %s, Age: %d, Email: %s",
                student.getName(), student.getAge(), student.getEmail()
        );

        try {
            RestTemplate restTemplate = new RestTemplate();
            String ollamaApiUrl = "http://localhost:11434/api/generate";

            OllamaRequest request = new OllamaRequest("gemma:2b", prompt, false);

            OllamaResponse response = restTemplate.postForObject(ollamaApiUrl, request, OllamaResponse.class);

            return ResponseEntity.ok(Map.of("summary", response.response()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to connect to Ollama service: " + e.getMessage()));
        }
    }
}