package com.assignment.studentapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// Represents the JSON we get back from Replicate
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReplicateResponse(String status, List<String> output) {}