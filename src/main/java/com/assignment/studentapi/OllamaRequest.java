package com.assignment.studentapi;

// A simple record to represent the request body for the Ollama API
public record OllamaRequest(String model, String prompt, boolean stream) {}