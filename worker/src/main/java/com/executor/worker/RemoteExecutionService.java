package com.executor.worker;

import com.executor.common.Job;
import com.executor.common.JobDTO;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RemoteExecutionService {

    private final JobRepository repo;
    private final ContainerPool pool;
    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            8, 8,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(20)
    );

    public RemoteExecutionService(JobRepository repo, ContainerPool pool) {
        this.repo = repo;
        this.pool = pool;
    }

    public boolean execute(JobDTO dto) {
        try {
            threadPool.submit(() -> runJob(dto));
            return true;
        } catch (RejectedExecutionException e) {
            return false;
        }
    }

    private void runJob(JobDTO dto) {
        Job job = repo.findById(dto.id()).orElseThrow();
        job.setStatus(Job.Status.IN_PROGRESS);
        repo.save(job);
        
        String containerId = null;

        try {
            containerId = pool.acquireContainer();

            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", containerId,
                    "sh", "-c", job.getCommand()
            );

            pb.redirectErrorStream(true);
            Process p = pb.start();

            boolean finished = p.waitFor(30, TimeUnit.SECONDS);

            String output;
            if (!finished) {
                p.destroyForcibly();
                output = "Job lasted more than 30 seconds. Timed out.";
            } else {
                output = new BufferedReader(
                        new InputStreamReader(p.getInputStream()))
                        .lines().collect(Collectors.joining("\n"));
            }
            int exit = finished ? p.exitValue() : -1;

            job.setOutput(output);
            job.setResult(exit == 0 ? Job.Result.SUCCESS : Job.Result.FAILED);

            if (finished) {
                new ProcessBuilder("docker", "exec", containerId, "sh", "-c", "rm -rf /tmp/*").start().waitFor();
                pool.releaseContainer(containerId);
                containerId = null; // Mark as successfully returned
            }

        } catch (Exception e) {
            job.setOutput(e.getMessage());
            job.setResult(Job.Result.FAILED);
        } finally {
            if (containerId != null) {
                try {
                    new ProcessBuilder("docker", "rm", "-f", containerId).start().waitFor();
                } catch (Exception ignored) {}
                pool.reportContainerDestroyed();
            }
            
            job.setStatus(Job.Status.FINISHED);
            repo.save(job);
        }
    }

    public boolean allThreadsBusy() {
        return threadPool.getActiveCount() == threadPool.getPoolSize();
    }

    public boolean saturated() {
        return allThreadsBusy() && threadPool.getQueue().remainingCapacity() == 0;
    }
}