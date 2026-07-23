## 1. An API suddenly becomes slow after a release, but CPU and memory look normal. Where do you start?

Since CPU and memory are normal, I wouldn't assume infrastructure is the issue. I'd first compare the release changes,
identify whether the slowdown affects all APIs or a specific endpoint, then use metrics and APM to break down response time 
into database, external service, and business logic. I'd also review slow SQL queries, thread dumps, and logs to isolate the
bottleneck before applying a fix.

---
## 2. A Spring Boot service returns 500 errors randomly without clear logs. What will you check?

Random 500 errors usually indicate an intermittent issue rather than a permanent bug. My approach is to isolate where the request is
failing by collecting as much information as possible.

**1. Reproduce the issue**
- Check whether it happens for a specific API, user, payload, or randomly.
- Try reproducing it in lower environments with the same request.
- Verify if it started after a recent deployment or configuration change.

Reason: If I can reproduce it, debugging becomes much easier.

**2. Check Application Logs**
  
**3. Check External Dependencies**

If the API calls:

- Database
- Kafka
- Redis
- Another microservice
- Third-party API

Check whether they are occasionally failing.

**Examples:**
- Database connection timeout
- Feign client timeout
- HTTP 503 from another service
- Kafka broker unavailable

**4. Check Thread Dumps**
   Look for:
  -  Blocked threads
   - Deadlocks
   - Thread starvation
   - Infinite loops

**5. Check Recent Deployment**
- What changed?
- New feature?
- Dependency upgrade?
- Configuration change?
- Environment variable change?

---
 ### 3.Response time keeps increasing over time, even though traffic is stable. What could be happening?

I would analyze the database for slow queries, missing indexes, lock contention, or long-running transactions
that may have degraded performance over time. I would also verify whether external dependencies such as other microservices,
Kafka, Redis, or third-party APIs are responding slowly, since their latency directly impacts my application's response time.

## 4. A database query works fine locally but is slow in production. How will you debug it?

my approach is to compare execution plans, verify indexes, analyze production data volume, check locks and database health, review ORM-generated SQL,
and use monitoring tools to identify the actual bottleneck before making any changes.

## 5. Multiple threads are causing inconsistent data updates. How will you fix it?

If multiple threads are causing inconsistent data updates, I would first identify whether it's a race condition, 
where multiple threads are reading and updating the same data simultaneously without proper synchronization.

If the issue is within the application, I would use thread-safe approaches such as synchronized blocks, ReentrantLock, 
or concurrent collections like ConcurrentHashMap to ensure that only one thread modifies the shared resource at a time.
However, I would be careful not to overuse synchronization because it can reduce performance by blocking other threads.

If the data is stored in a database, I would use proper transaction management and choose an appropriate locking strategy.
For most business applications, I prefer optimistic locking using a version field (@Version in JPA), where the update fails 
if another transaction has already modified the record. If conflicts are frequent and data consistency is critical, 
I would use pessimistic locking to prevent other transactions from updating the record until the current transaction completes.

In distributed systems where multiple application instances are running, application-level synchronization is not sufficient 
because each instance has its own memory. In that case, I would use a distributed locking mechanism, such as Redis or ZooKeeper,
or rely on database locking to coordinate updates across instances.

## 6.A memory issue crashes the application after a few hours. How will you identify the root cause?

If the application crashes after running for a few hours, my first suspicion would be a memory leak or resource exhaustion. 
Instead of immediately changing the code, I would collect evidence to identify the root cause.

First, I would review the application logs to check whether the crash is caused by an OutOfMemoryError, excessive garbage
collection, or another JVM-related issue. I would also monitor JVM metrics such as heap usage, non-heap memory, garbage collection 
frequency, and GC pause times using tools like Grafana, Prometheus, or JConsole.

Next, I would check for common causes of memory leaks, such as objects stored indefinitely in static collections, unbounded caches,
memory-heavy sessions, unreleased database connections, file streams, HTTP client connections, or large collections that continue growing over time.

I would also monitor thread count and capture thread dumps to ensure there isn't a thread leak, where threads continue increasing because they 
are never terminated. Similarly, I would verify that database connections and thread pools are being properly released and are not exhausted.


## 7.A service call blocks threads and leads to timeouts under load. What's your approach?

If a service call blocks threads and causes timeouts under load, my first goal is to identify why the threads are 
waiting and where the bottleneck is. I would start by checking application logs, response time metrics, thread pool 
utilization, and thread dumps to determine whether threads are blocked waiting for an external service, database, or some synchronized code.

Next, I would measure the latency of the downstream service. If that service is responding slowly, my application 
threads will remain blocked until a response is received, eventually exhausting the thread pool and causing new requests to time out.

I would then verify that proper connection timeouts and read timeouts are configured for the HTTP client, 
such as RestTemplate, WebClient, or Feign Client. Without timeouts, threads may wait indefinitely for a response.

I would also implement a Circuit Breaker (using Resilience4j) so that if the downstream service is unhealthy, 
requests fail fast instead of continuously waiting. Along with that, I would configure Retry with exponential backoff
only for transient failures and avoid excessive retries that could make the situation worse.

---
## 8.Our teammate want NoSQL for scalability for a service that need a strong consistency and join. How do you make the call?

Scalability alone isn't enough to justify NoSQL. Since this service requires strong consistency and joins, I'd choose a relational database because it provides ACID guarantees and efficient joins. If scalability becomes an issue, I'd scale the SQL database through indexing, caching, read replicas, or sharding. If different parts of the system have different requirements, I'd use SQL for transactional data and NoSQL for high-scale, non-relational workloads.

I would first understand the service requirements rather than choosing NoSQL just for scalability. If the service requires strong consistency, ACID transactions, and frequent joins across multiple entities, I would lean toward a relational database like PostgreSQL or MySQL.

NoSQL databases are excellent for horizontal scalability, flexible schemas, and high write throughput, but most do not support complex joins efficiently, and some prioritize availability or partition tolerance over strong consistency.

    Read replicas for read-heavy workloads
    Proper indexing
    Query optimization
    Caching (e.g., Redis)
    Partitioning or sharding if needed

---
## 9.If scalability is still a concern, I would first explore scaling the SQL database using##

I would use an adapter/facade pattern. REST controllers and SOAP endpoints (or SOAP client adapters) would only translate requests and responses. Both would invoke the same business service containing the business logic. This avoids duplication, keeps the code maintainable, and allows us to replace the SOAP integration in the future with minimal changes. 

                 Client
                   |
          REST Controller
                   |
          -----------------
          |               |
     SOAP Adapter    REST Adapter
          \               /
           \             /
        Business Service
               |
      Repository/Database

  - REST Controller handles HTTP requests/responses, JSON, validation, and authentication.
- SOAP Adapter/Endpoint handles SOAP XML, WSDL, and converts SOAP requests into internal DTOs.
- Both call the same business service, where all the business logic resides.
- The business service interacts with the database or downstream services.

## 10. Tell me about the most challenging situation you faced recently.

**Situation 1:** One of the most challenging issues I recently handled was a race condition in our event-driven microservices architecture. We had two Kafka topics publishing events for the same business entity, and occasionally both events arrived almost simultaneously. As a result, two consumers tried to update the same MongoDB document at the same time.

Task: My responsibility was to ensure that only the correct update was persisted without causing data inconsistency or losing any business information.

Action: I first analyzed the Kafka and application logs to confirm that the issue was caused by two messages being processed concurrently for the same document. We evaluated several approaches, including distributed locking and Kafka partitioning. Since the messages were coming from two independent Kafka topics, partitioning alone could not guarantee ordering across topics.

We implemented optimistic locking using a version field and added idempotency checks to ensure that only one update could be applied at a time. If another update encountered a version conflict, it was temporarily rejected, moved to a retry state, and retried after fetching the latest version of the document. This ensured that no valid update was lost and that both messages were eventually applied in a consistent manner. We also introduced retry logic for version conflicts and enhanced monitoring to detect and investigate similar concurrency issues in the future.

Result: The race condition was eliminated, data consistency improved significantly, and we no longer saw incorrect updates in production. The solution also made the service more resilient to concurrent events without impacting performance.

**Situation: 2** Another challenging issue I worked on was high application latency for our China manufacturing plants. Our microservice was deployed in the Europe region, while users and factories were located in China. Every request had to travel a long distance, resulting in high response times and a poor user experience.

**Task:** My responsibility was to reduce the latency without affecting data consistency or the existing application functionality.

**Action:** I analyzed the application metrics, API response times, and infrastructure topology to identify the root cause. The primary issue was network latency caused by cross-region communication. To address this, we deployed a dedicated instance of the microservice in the Asia region, closer to the China factories. We also configured it to use the MongoDB read-only (RO) node hosted in the Singapore region, significantly reducing the distance between the application and the database for read operations.

In addition, we optimized the service by:

* Routing China traffic to the Asia deployment through the load balancer.
* Ensuring read-heavy requests were served from the local read replica while write operations continued to go to the primary node.
* Monitoring latency, CPU utilization, and database response times before and after the deployment to validate the improvement.
* Verifying that indexes were in place for the frequently executed queries to minimize database execution time.

**Result:** After deploying the service in the Asia region and connecting it to the Singapore read replica, API response times for the China factories were significantly reduced, providing a much better user experience while maintaining data consistency.


---
## 11. Describe a production issue where you were involved. How did you handle it?

**Situation:** During one of our production releases for the merger project, we deployed a new microservice that introduced new business logic along with Liquibase database changes to create the required indexes. However, during deployment, the DevOps team accidentally deployed the application before executing the Liquibase migration scripts.

**Problem:** Because the required database indexes were missing, the new endpoint executed expensive queries that resulted in full collection scans. As traffic increased, the queries became very slow, causing request timeouts. The API Gateway eventually returned **502 Bad Gateway** responses because the backend service was not responding within the configured timeout.

**Action:** As soon as we noticed the increase in 502 errors, I joined the production incident bridge and started investigating. I reviewed the application logs, API Gateway logs, and database metrics to identify the root cause. We confirmed that the application deployment had completed successfully, but the Liquibase migration had not been executed. I coordinated with the DevOps team to immediately run the pending Liquibase scripts, which created the required indexes. After the indexes were created, we validated the query execution plans, monitored API response times, and confirmed that the endpoint was performing as expected. We also closely monitored the service for some time after the fix to ensure there were no recurring issues.

**Prevention:** After the incident, we improved our deployment process by making database migrations a mandatory prerequisite before application deployment. We also updated the deployment checklist and pipeline validation to ensure Liquibase migrations completed successfully before the new application version could go live. This reduced the risk of similar deployment issues in future releases.

**Result:** Once the indexes were created, query performance improved significantly, the 502 errors stopped, and the service returned to normal operation. The incident also led to a more robust deployment process and better coordination between the development and DevOps teams.



