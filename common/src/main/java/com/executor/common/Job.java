package com.executor.common;


import jakarta.persistence.*;

@Entity
@Table(name="jobs_db")
public class Job {

    public enum Status { QUEUED, IN_PROGRESS, FINISHED }
    public enum Result { SUCCESS, FAILED }

    @Id
    private String id;

    @Column(columnDefinition = "TEXT")
    private String command;
    private int cpuCount;

    @Enumerated(EnumType.STRING)
    private volatile Status status;

    @Enumerated(EnumType.STRING)
    private volatile Result result;

    @Column(columnDefinition = "TEXT")
    private volatile String output;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Column(columnDefinition = "TEXT")
    private String message;

    public Job() {}

    public Job(String id, String command, int cpuCount, String message) {
        this.id = id;
        this.command = command;
        this.cpuCount = cpuCount;
        this.status = Status.QUEUED;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}