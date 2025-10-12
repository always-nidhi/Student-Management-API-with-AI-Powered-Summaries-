package com.assignment.studentapi;

import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StudentService {
    // Using ConcurrentHashMap for thread-safe in-memory storage
    private final ConcurrentHashMap<Integer, Student> students = new ConcurrentHashMap<>();
    // Using AtomicInteger for thread-safe ID generation
    private final AtomicInteger idCounter = new AtomicInteger();

    public Student createStudent(Student student) {
        int newId = idCounter.incrementAndGet();
        student.setId(newId);
        students.put(newId, student);
        return student;
    }

    public Collection<Student> getAllStudents() {
        return students.values();
    }

    public Optional<Student> getStudentById(int id) {
        return Optional.ofNullable(students.get(id));
    }



    public Optional<Student> updateStudent(int id, Student updatedStudent) {
        return Optional.ofNullable(students.computeIfPresent(id, (key, existingStudent) -> {
            existingStudent.setName(updatedStudent.getName());
            existingStudent.setAge(updatedStudent.getAge());
            existingStudent.setEmail(updatedStudent.getEmail());
            return existingStudent;
        }));
    }

    public boolean deleteStudent(int id) {
        return students.remove(id) != null;
    }
}