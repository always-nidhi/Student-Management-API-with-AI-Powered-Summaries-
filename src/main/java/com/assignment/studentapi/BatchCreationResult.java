package com.assignment.studentapi;

import java.util.List;
import java.util.Map;

// Using a record for a simple, immutable data-holder class
public record BatchCreationResult(
        List<Student> createdStudents,
        List<Map<String, Object>> failedStudents
) {}