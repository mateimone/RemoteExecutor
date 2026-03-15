package com.executor.api;

import com.executor.common.Job;
import com.executor.common.JobRequest;
import com.executor.common.JobStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<Job> submit(@RequestBody JobRequest request) {
        Job job = jobService.submit(request.command(), request.cpuCount());

        if (job.getId() == null)
            return ResponseEntity.unprocessableEntity().build();

        return ResponseEntity.accepted().body(job);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobStatusResponse> status(@PathVariable("id") String id) {
        Job job = jobService.getJob(id);
        if (job == null)
            return ResponseEntity.ok(new JobStatusResponse(null, null, null, null, "Either the job id is wrong, or the job queue was full and the job couldn't be queued! If the id is correct, please try again later!"));

        JobStatusResponse resp = new JobStatusResponse(
                job.getId(), job.getStatus(),
                job.getResult(), job.getOutput(),
                "Job was queued!"
        );
        return ResponseEntity.ok(resp);
    }
}
