package com.assignment.studentapi;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Import this
import org.springframework.http.*; // Import this
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

    // This annotation reads the secret key from the environment variable we set in Render
    @Value("${REPLICATE_API_TOKEN}")
    private String replicateApiToken;


    @PostMapping
    public ResponseEntity<?> createStudent(@Valid @RequestBody Student student) {
        Optional<Student> createdStudentOpt = studentService.createStudent(student);
        if (createdStudentOpt.isPresent()) {
            return new ResponseEntity<>(createdStudentOpt.get(), HttpStatus.CREATED);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "A student with this email already exists."));
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchCreationResult> createMultipleStudents(@Valid @RequestBody List<Student> students) {
        BatchCreationResult result = studentService.createMultipleStudents(students);
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(result);
    }

    @GetMapping
    public Collection<Student> getAllStudents() { return studentService.getAllStudents(); }

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
     * MODIFIED to call the Replicate cloud API instead of local Ollama.
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
            // This is Replicate's API endpoint for Llama3-8B-Instruct
            String replicateApiUrl = "https://api.replicate.com/v1/models/meta/meta-llama-3-8b-instruct/predictions";

            // Set up the authorization headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + replicateApiToken);
            headers.set("Content-Type", "application/json");

            // Create the request body in the format Replicate expects
            // NOTE: The version hash might change over time. You can find the latest on Replicate's website.
            String modelVersion = "f5a31d58143248f731818b76a75f87b8f04403b0d2fdcf11b22e176b653f563e";
            ReplicateRequest requestBody = new ReplicateRequest(modelVersion, Map.of("prompt", prompt));

            HttpEntity<ReplicateRequest> entity = new HttpEntity<>(requestBody, headers);

            // Make the API call
            // NOTE: Replicate's API is asynchronous. This is a simplified call.
            // A full production app would poll the "status" URL. For this assignment,
            // we'll get an intermediate response. A better approach for sync would be to
            // use a faster model/provider like Groq, but this demonstrates the principle.
            ReplicateResponse response = restTemplate.postForObject(replicateApiUrl, entity, ReplicateResponse.class);

            // For now, let's return the status. A more complex client would poll the result URL.
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to connect to AI service: " + e.getMessage()));
        }
    }
}