package com.assignment.studentapi;

import java.util.Map;

// Represents the correct JSON format for Replicate's /predictions endpoint
public record ReplicateRequest(String model, Map<String, Object> input) {}