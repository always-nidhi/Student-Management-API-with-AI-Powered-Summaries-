package com.assignment.studentapi;

import java.util.List;

public record BatchDeletionResult(
        List<Integer> deletedIds,
        List<Integer> notFoundIds
) {}