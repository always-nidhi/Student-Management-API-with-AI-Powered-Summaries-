package com.assignment.studentapi;

import java.util.Map;

// Represents the JSON we send to Replicate
public record ReplicateRequest(String version, Map<String, String> input) {}