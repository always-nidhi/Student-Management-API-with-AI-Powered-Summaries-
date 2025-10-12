package com.assignment.studentapi;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StudentService {

    private final ConcurrentHashMap<Integer, Student> students = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger();

    /**
     * Checks if an email already exists in the student list (case-insensitive).
     * @param email The email to check.
     * @return true if the email exists, false otherwise.
     */
    private boolean emailExists(String email) {
        return students.values().stream()
                .anyMatch(student -> student.getEmail().equalsIgnoreCase(email));
    }

    /**
     * Creates a single new student if the email is not already in use.
     * @param student The student object to create.
     * @return An Optional containing the created student, or an empty Optional if the email already exists.
     */
    public Optional<Student> createStudent(Student student) {
        if (emailExists(student.getEmail())) {
            return Optional.empty(); // Email is a duplicate
        }
        int newId = idCounter.incrementAndGet();
        student.setId(newId);
        students.put(newId, student);
        return Optional.of(student);
    }

    /**
     * Attempts to create multiple students from a list.
     * It processes the entire list and returns a result detailing successes and failures.
     * @param newStudents A list of students to create.
     * @return A BatchCreationResult object with lists of created and failed students.
     */
    public BatchCreationResult createMultipleStudents(List<Student> newStudents) {
        List<Student> successfullyCreated = new ArrayList<>();
        List<Map<String, Object>> failedToCreate = new ArrayList<>();

        for (Student student : newStudents) {
            // Check for duplicates in the main list AND within the current batch
            boolean isDuplicate = emailExists(student.getEmail()) ||
                    successfullyCreated.stream().anyMatch(s -> s.getEmail().equalsIgnoreCase(student.getEmail()));

            if (isDuplicate) {
                failedToCreate.add(Map.of(
                        "student", student,
                        "reason", "A student with this email already exists."
                ));
            } else {
                int newId = idCounter.incrementAndGet();
                student.setId(newId);
                students.put(newId, student);
                successfullyCreated.add(student);
            }
        }
        return new BatchCreationResult(successfullyCreated, failedToCreate);
    }

    public Collection<Student> getAllStudents() {
        return students.values();
    }

    public Optional<Student> getStudentById(int id) {
        return Optional.ofNullable(students.get(id));
    }

    public Optional<Student> updateStudent(int id, Student updatedStudent) {
        if (!students.containsKey(id)) {
            return Optional.empty();
        }
        updatedStudent.setId(id);
        students.put(id, updatedStudent);
        return Optional.of(updatedStudent);
    }

    public boolean deleteStudent(int id) {
        return students.remove(id) != null;
    }
}