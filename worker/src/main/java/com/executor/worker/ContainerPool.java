package com.executor.worker;

import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ContainerPool {
    private final BlockingQueue<String> idleContainers = new LinkedBlockingQueue<>();
    private final AtomicInteger totalContainers = new AtomicInteger(0);

    public String acquireContainer() throws InterruptedException {
        return idleContainers.take();
    }

    public void releaseContainer(String containerId) {
        idleContainers.offer(containerId);
    }

    public void addBrandNewContainer(String containerId) {
        totalContainers.incrementAndGet();
        idleContainers.offer(containerId);
    }

    public String removeContainer() {
        if (idleContainers.isEmpty()) return null;
        String containerId = idleContainers.poll();
        if (containerId != null) {
            totalContainers.decrementAndGet();
        }
        return containerId;
    }

    public void reportContainerDestroyed() {
        totalContainers.decrementAndGet();
    }

    public int getIdleCount() { return idleContainers.size(); }
    public int getTotalCount() { return totalContainers.get(); }
}
