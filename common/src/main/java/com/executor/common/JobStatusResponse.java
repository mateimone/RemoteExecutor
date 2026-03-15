package com.executor.common;

public record JobStatusResponse(String id, Job.Status status, Job.Result result, String output, String message) {
}
