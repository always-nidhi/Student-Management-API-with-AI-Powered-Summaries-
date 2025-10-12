package com.assignment.studentapi;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    /**
     * Creates a single new student.
     * Returns 409 Conflict if a student with the same email already exists.
     */
    @PostMapping
    public ResponseEntity<?> createStudent(@Valid @RequestBody Student student) {
        Optional<Student> createdStudentOpt = studentService.createStudent(student);

        if (createdStudentOpt.isPresent()) {
            return new ResponseEntity<>(createdStudentOpt.get(), HttpStatus.CREATED);
        } else {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "A student with this email already exists."));
        }
    }

    /**
     * Creates multiple students in a single batch operation.
     * Returns 207 Multi-Status with a body detailing successes and failures.
     */
    @PostMapping("/batch")
    public ResponseEntity<BatchCreationResult> createMultipleStudents(@Valid @RequestBody List<Student> students) {
        BatchCreationResult result = studentService.createMultipleStudents(students);
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(result);
    }

    @GetMapping
    public Collection<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable int id) {
        return studentService.getStudentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable int id, @Valid @RequestBody Student student) {
        return studentService.updateStudent(id, student)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable int id) {
        if (studentService.deleteStudent(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<?> getStudentSummary(@PathVariable int id) {
        Optional<Student> studentOpt = studentService.getStudentById(id);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Student student = studentOpt.get();

        String prompt = String.format(
                "Provide a brief, one-paragraph professional summary for a student with the following profile. " +
                        "Focus on their potential. Profile -> Name: %s, Age: %d.",
                student.getName(), student.getAge()
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