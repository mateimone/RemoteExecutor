# RemoteExecutor

## How to run
Inside `/api/src/main/java/resources/application.yml`, change the base-url of the worker to whatever ip address 
your local executor machine has on the network. The executor machine must be on the same network as the API machine!

Then, on the API machine run:
- `docker compose up -d` (this starts up the database on your API machine)
- `mvn -pl api spring-boot:run`

And on the executor machine, run:
- `mvn -pl worker spring-boot:run`

Now, both parts of the application are ready to communicate.
A user can submit a job by sending a POST request to the API:
```bash
curl -X POST http://192.168.2.24:8080/jobs \
  -H "Content-Type: application/json" \
  -d '{"command":"echo hello","cpuCount":1}'
```
If the job is accepted, the API returns a response containing the job identifier:
{
"jobId": "ee72fd8a-2e79-4a58-b5eb-22df6f717932",
"status": "Queued"
}

To check the status of a job:
```bash
curl -X GET http://192.168.2.24:8080/jobs/ee72fd8a-2e79-4a58-b5eb-22df6f717932
```


## Overview

This app allows a user to submit command scripts for remote execution inside containers.

A user can:
- send a command script to be executed
- specify CPU count
- check execution status
- view execution output

The system consists of an API service, a remote executor, an autoscaler for container pooling, and a database.

---

## Architecture

### API

The user interacts with the API.

The API communicates with the Executor through synchronous requests.  
The connection is closed as soon as a job is queued or rejected. A job is rejected if the job queue is full (can happen with many concurrent users).
The user is then prompted to try again later.

After submission, the user can perform another request to check job status.  
After execution finishes, the user can retrieve the output.

---

### Remote Executor

Receives job requests from the API.

- maintains a queue of submitted jobs
- uses a thread pool to execute jobs
- jobs are executed inside containers

Execution logic:

- if no container is available: a new container is started
- otherwise: a pre-warmed container is reused

Containers are reset after execution.  
Temporary files created by scripts are removed.

---

### Autoscaler / Container Pool

A dedicated Autoscaler service maintains a pool of pre-warmed containers.

The pool is resized dynamically based on system load (2/3 load factor).

This reduces container startup latency.

---

### Database

Accepted jobs are stored in a database.

Job lifecycle:

- **Queued** – job accepted but waiting in queue
- **In Progress** – job is being executed
- **Finished** – execution completed

Final execution result:
- **Success**
- **Failed**

---

## Failure / rejection cases

- job rejected when queue is full
- timeout: job marked as failed
- execution error: job marked as failed

---

## System Limits

- maximum worker threads: **10**
- maximum concurrent containers: **10**
- maximum queued jobs: **20**
- execution timeout: **30 seconds**

Timeout protects against scripts that do not terminate.

---

## Production considerations (not implemented)

### Scalability / availability

- request submission should be asynchronous
- message queues should be used for persistence

### Recoverability
- database should act as point of recovery if executor fails
- jobs that are queued and in progress should be recoverable after failure

### Security

Currently, any user can query any job if they know its UUID. The simplest solution would be an API-key per user and user accounts.

