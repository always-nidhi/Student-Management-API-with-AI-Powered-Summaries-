package com.assignment.studentapi;

import java.util.List;

// A simple record to hold the results of a bulk delete operation
public record BatchDeletionResult(
        List<Integer> deletedIds,
        List<Integer> notFoundIds
) {}