package com.executor.api;

import com.executor.common.Job;
import com.executor.common.JobDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobService {
    private final JobRepository repo;
    private final Client client;

    public JobService(JobRepository repo, Client client) {
        this.repo = repo;
        this.client = client;
    }

    public Job submit(String command, int cpuCount) {
        Job job = new Job(UUID.randomUUID().toString(), command, cpuCount, "Job was queued!");
        repo.save(job);
        JobDTO dto = new JobDTO(job.getId(), job.getCommand(), job.getCpuCount());
        boolean accepted = client.send(dto);

        if (!accepted) {
            repo.delete(job);
            return new Job(null, null, -1, "The job queue was full and the job couldn't be queued! Please try again later!");
        }

//        producer.send(new Job(job.getId(), command, cpuCount));

        return job;
    }

    public Job getJob(String id) {
        return repo.findById(id).orElse(null);
    }
}
