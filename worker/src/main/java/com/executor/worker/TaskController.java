package com.executor.worker;

import com.executor.common.JobDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final RemoteExecutionService executionService;

    public TaskController(RemoteExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping
    public ResponseEntity<Void> runTask(@RequestBody JobDTO job) {
        boolean res = executionService.execute(job);

        if (res)
            return ResponseEntity.ok().build();
        else
            return ResponseEntity.unprocessableEntity().build();
    }
}
