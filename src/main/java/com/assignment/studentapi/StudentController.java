package com.assignment.studentapi;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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

    @Value("${REPLICATE_API_TOKEN}")
    private String replicateApiToken;

    // Creates a single student, checking for duplicates.
    @PostMapping
    public ResponseEntity<?> createStudent(@Valid @RequestBody Student student) {
        Optional<Student> createdStudentOpt = studentService.createStudent(student);
        if (createdStudentOpt.isPresent()) {
            return new ResponseEntity<>(createdStudentOpt.get(), HttpStatus.CREATED);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "A student with this email already exists."));
        }
    }

    // Creates multiple students in a single batch request.
    @PostMapping("/batch")
    public ResponseEntity<BatchCreationResult> createMultipleStudents(@Valid @RequestBody List<Student> students) {
        BatchCreationResult result = studentService.createMultipleStudents(students);
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(result);
    }

    // Deletes multiple students in a single batch request.
    @DeleteMapping
    public ResponseEntity<BatchDeletionResult> deleteMultipleStudents(@RequestBody List<Integer> ids) {
        BatchDeletionResult result = studentService.deleteMultipleStudents(ids);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public Collection<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable int id) {
        return studentService.getStudentById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable int id, @Valid @RequestBody Student student) {
        return studentService.updateStudent(id, student).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable int id) {
        return studentService.deleteStudent(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Generates a summary for a student using the Replicate cloud API.
     * This version uses the corrected request format.
     */
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
            String replicateApiUrl = "https://api.replicate.com/v1/deployments/meta/meta-llama-3-8b-instruct/predictions";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + replicateApiToken);
            headers.set("Content-Type", "application/json");

            // Create the request body using the corrected ReplicateRequest class (without 'version')
            ReplicateRequest requestBody = new ReplicateRequest(Map.of("prompt", prompt));

            HttpEntity<ReplicateRequest> entity = new HttpEntity<>(requestBody, headers);
            ReplicateResponse response = restTemplate.postForObject(replicateApiUrl, entity, ReplicateResponse.class);

            // Clean up the response to provide a simple summary string
            if (response != null && response.output() != null && !response.output().isEmpty()) {
                String summary = String.join("", response.output());
                return ResponseEntity.ok(Map.of("summary", summary));
            }

            // Fallback in case the output format is unexpected
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to connect to AI service: " + e.getMessage()));
        }
    }
}