package com.assignment.studentapi;

import java.util.Map;

// Represents the simplified JSON we send to Replicate
public record ReplicateRequest(Map<String, Object> input) {}