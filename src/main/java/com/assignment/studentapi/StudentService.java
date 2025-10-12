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

    private boolean emailExists(String email) {
        return students.values().stream()
                .anyMatch(student -> student.getEmail().equalsIgnoreCase(email));
    }

    public Optional<Student> createStudent(Student student) {
        if (emailExists(student.getEmail())) {
            return Optional.empty();
        }
        int newId = idCounter.incrementAndGet();
        student.setId(newId);
        students.put(newId, student);
        return Optional.of(student);
    }

    public BatchCreationResult createMultipleStudents(List<Student> newStudents) {
        List<Student> successfullyCreated = new ArrayList<>();
        List<Map<String, Object>> failedToCreate = new ArrayList<>();

        for (Student student : newStudents) {
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


    public BatchDeletionResult deleteMultipleStudents(List<Integer> idsToDelete) {
        List<Integer> successfullyDeleted = new java.util.ArrayList<>();
        List<Integer> failedToDelete = new java.util.ArrayList<>();

        for (Integer id : idsToDelete) {
            if (students.remove(id) != null) {
                successfullyDeleted.add(id);
            } else {
                failedToDelete.add(id);
            }
        }
        return new BatchDeletionResult(successfullyDeleted, failedToDelete);
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