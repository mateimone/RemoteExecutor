package com.executor.worker;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ContainerAutoscaler {

    private final ContainerPool pool;
    private final double SCALE_UP_FACTOR = 2.0 / 3.0;
    private final double SCALE_DOWN_FACTOR = 1.0 / 3.0;
    private final int INITIAL_CAPACITY = 3;
    private final int MAX_TOTAL = 20;

    public ContainerAutoscaler(ContainerPool pool) {
        this.pool = pool;
    }

    @Scheduled(fixedRate = 2000)
    public void evaluateScale() {
        int total = pool.getTotalCount();
        int idle = pool.getIdleCount();
        int busy = total - idle;

        if (total == 0) {
            System.out.println("Pool empty. Initializing with " + INITIAL_CAPACITY + " containers...");
            for (int i = 0; i < INITIAL_CAPACITY; i++) {
                startNewContainer();
            }
            return;
        }

        double currentLoad = (double) busy / total;

        if (currentLoad >= SCALE_UP_FACTOR && total < MAX_TOTAL) {
            int containersToAdd = Math.max(1, total / 2);
            containersToAdd = Math.min(containersToAdd, MAX_TOTAL - total);

            System.out.println("Load is " + currentLoad + " (Busy: " + busy + ", Total: " + total + "). Exceeds " + SCALE_UP_FACTOR);

            for (int i = 0; i < containersToAdd; i++) {
                startNewContainer();
            }
        }
        else if (currentLoad <= SCALE_DOWN_FACTOR && total > INITIAL_CAPACITY) {
            System.out.println("Load is " + currentLoad + " (Busy: " + busy + ", Total: " + total + "). Below " + SCALE_DOWN_FACTOR);
            String containerId = pool.removeContainer();
            if (containerId != null) {
                destroyContainer(containerId);
            }
        }
    }

    private void startNewContainer() {
        try {
            String containerName = "worker-pool-" + UUID.randomUUID().toString().substring(0, 8);
            Process p = new ProcessBuilder(
                    "docker", "run", "-d", "--rm", "--name", containerName,
                    "alpine", "tail", "-f", "/dev/null"
            ).start();

            if (p.waitFor() == 0) {
                pool.addBrandNewContainer(containerName);
                System.out.println("Started container: " + containerName);
            } else {
                System.err.println("Failed to start container.");
            }
        } catch (Exception ignored) {}
    }

    private void destroyContainer(String containerName) {
        try {
            new ProcessBuilder("docker", "rm", "-f", containerName).start().waitFor();
            System.out.println("Destroyed container: " + containerName);
        } catch (Exception ignored) {}
    }
}
