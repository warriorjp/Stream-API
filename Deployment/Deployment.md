 # Deployment stages:
 
       1. Developer Pushes Code
               ↓
       2. Jenkins CI/CD Triggered
               ↓
       3. Build, Test, Package Spring Boot App
               ↓
       4. Build Docker Image
               ↓
       5. Push to Azure Container Registry (ACR)
               ↓
       6. Deploy to Azure Kubernetes Service (AKS)
               ↓
       7. Expose App via LoadBalancer
               ↓
       8. Users Access the Application


# Deployment Strategies

## 1. Recreate Deployment
- Stop old version
- Deploy new version

```text
Old App ❌
New App ✅
```

**Pros:** Simple  
**Cons:** Downtime occurs

---

## 2. Rolling Deployment
Replace instances one by one.

```text
V1 V1 V1 V1

↓

V2 V1 V1 V1
V2 V2 V1 V1
V2 V2 V2 V1
V2 V2 V2 V2
```

**Pros:** No downtime  
**Cons:** Both versions run simultaneously during deployment

Commonly used in Kubernetes.

---

## 3. Blue-Green Deployment

Maintain two environments:

```text
Blue  → Live Production
Green → New Version
```

After testing:

```text
Blue  ❌
Green ✅
```

**Pros:**
- Near-zero downtime
- Easy rollback

**Cons:**
- Requires double infrastructure

---

## 4. Canary Deployment

Release to a small percentage of users first.

```text
90% → Version 1
10% → Version 2
```

If stable:

```text
50% → Version 2
100% → Version 2
```

**Pros:**
- Low risk
- Detect issues early

Used by Netflix, Amazon, and Google.

---

## 5. A/B Deployment

Different users see different versions.

```text
Group A → Old UI
Group B → New UI
```

Used for:
- Feature testing
- User experience experiments

---

## 6. Shadow Deployment

New version receives a copy of production traffic but doesn't respond to users.

```text
User Request
      ↓
   Version 1 (response)
      ↓
   Version 2 (test only)
```

**Pros:**
- Test under real traffic
- No customer impact

---

## 7. Feature Toggle (Feature Flag)

Deploy code but keep feature disabled.

```text
Feature deployed
Feature OFF
```

Enable later through configuration.

Used extensively in microservices.

---
# Deployment Strategies

## 1. Recreate Deployment
- Stop old version
- Deploy new version

```text
Old App ❌
New App ✅
```

**Pros:** Simple  
**Cons:** Downtime occurs

---

## 2. Rolling Deployment
Replace instances one by one.

```text
V1 V1 V1 V1

↓

V2 V1 V1 V1
V2 V2 V1 V1
V2 V2 V2 V1
V2 V2 V2 V2
```

**Pros:** No downtime  
**Cons:** Both versions run simultaneously during deployment

Commonly used in Kubernetes.

---

## 3. Blue-Green Deployment

Maintain two environments:

```text
Blue  → Live Production
Green → New Version
```

After testing:

```text
Blue  ❌
Green ✅
```

**Pros:**
- Near-zero downtime
- Easy rollback

**Cons:**
- Requires double infrastructure

---

## 4. Canary Deployment

Release to a small percentage of users first.

```text
90% → Version 1
10% → Version 2
```

If stable:

```text
50% → Version 2
100% → Version 2
```

**Pros:**
- Low risk
- Detect issues early

Used by Netflix, Amazon, and Google.

---

## 5. A/B Deployment

Different users see different versions.

```text
Group A → Old UI
Group B → New UI
```

Used for:
- Feature testing
- User experience experiments

---

## 6. Shadow Deployment

New version receives a copy of production traffic but doesn't respond to users.

```text
User Request
      ↓
   Version 1 (response)
      ↓
   Version 2 (test only)
```

**Pros:**
- Test under real traffic
- No customer impact

---

## 7. Feature Toggle (Feature Flag)

Deploy code but keep feature disabled.

```text
Feature deployed
Feature OFF
```

Enable later through configuration.

Used extensively in microservices.

---

# Deployment Strategies

## 1. Recreate Deployment
- Stop old version
- Deploy new version

```text
Old App ❌
New App ✅
```

**Pros:** Simple  
**Cons:** Downtime occurs

---

## 2. Rolling Deployment
Replace instances one by one.

```text
V1 V1 V1 V1

↓

V2 V1 V1 V1
V2 V2 V1 V1
V2 V2 V2 V1
V2 V2 V2 V2
```

**Pros:** No downtime  
**Cons:** Both versions run simultaneously during deployment

Commonly used in Kubernetes.

---

## 3. Blue-Green Deployment

Maintain two environments:

```text
Blue  → Live Production
Green → New Version
```

After testing:

```text
Blue  ❌
Green ✅
```

**Pros:**
- Near-zero downtime
- Easy rollback

**Cons:**
- Requires double infrastructure

---

## 4. Canary Deployment

Release to a small percentage of users first.

```text
90% → Version 1
10% → Version 2
```

If stable:

```text
50% → Version 2
100% → Version 2
```

**Pros:**
- Low risk
- Detect issues early

Used by Netflix, Amazon, and Google.

---

## 5. A/B Deployment

Different users see different versions.

```text
Group A → Old UI
Group B → New UI
```

Used for:
- Feature testing
- User experience experiments

---

## 6. Shadow Deployment

New version receives a copy of production traffic but doesn't respond to users.

```text
User Request
      ↓
   Version 1 (response)
      ↓
   Version 2 (test only)
```

**Pros:**
- Test under real traffic
- No customer impact

---

## 7. Feature Toggle (Feature Flag)

Deploy code but keep feature disabled.

```text
Feature deployed
Feature OFF
```

Enable later through configuration.

Used extensively in microservices.

---

# Docker, Kubernetes, and Azure for Spring Boot Applications

## 🐳 1. Docker – Containerization

Docker allows you to package your Spring Boot application along with all its dependencies into a single container image. This ensures that the application runs consistently across Development, Testing, and Production environments.

### Example Dockerfile

```dockerfile
FROM openjdk:17
COPY target/my-springboot-app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Benefits

- Consistent runtime environment
- Easy deployment and portability
- Dependency isolation
- Faster application delivery

---

## ☸️ 2. Kubernetes (K8s) – Container Orchestration

Kubernetes helps you deploy, manage, scale, and monitor containerized Spring Boot applications.

### Key Capabilities

- Automatic scaling of application instances
- Rolling updates with minimal downtime
- Self-healing by restarting failed containers
- Load balancing across application instances
- Resource management and scheduling

---

## ☁️ 3. Azure – Cloud Platform

Azure provides managed services for hosting and managing Kubernetes workloads.

### Azure Kubernetes Service (AKS)

A fully managed Kubernetes service that simplifies cluster deployment and operations.

### Azure Container Registry (ACR)

A private container registry used to securely store and manage Docker images.

### Azure DevOps / GitHub Actions

CI/CD tools used to automate:

- Build
- Testing
- Docker image creation
- Deployment to AKS

---

## Kubernetes Architecture Example

```text
AKS Cluster (Dev / Test / Prod)

├── Node 1 (Virtual Machine)
│   ├── Pod A (Spring Boot App)
│   ├── Pod B (Spring Boot App)
│   └── Pod C (Spring Boot App)
│
├── Node 2 (Virtual Machine)
│   ├── Pod D (Spring Boot App)
│   └── Pod E (Spring Boot App)
```

## Key Concepts

### Cluster

A collection of Nodes (Virtual Machines) managed by Kubernetes.

### Node

A worker machine (Virtual Machine) that runs application workloads.

### Pod

The smallest deployable unit in Kubernetes.

A Pod typically contains one or more containers.

For a Spring Boot application:

```text
Pod
 └── Docker Container
      └── Spring Boot Application
```

### Important Notes

- Multiple Pods can run on the same Node.
- A Node can host multiple Pods.
- Pods are distributed across Nodes for high availability.
- Kubernetes automatically recreates failed Pods.
- Traffic is distributed across Pods using Kubernetes Services.

---

## Quick Summary

```text
Spring Boot Application
        ↓
Docker Container
        ↓
Kubernetes Pod
        ↓
Node (VM)
        ↓
AKS Cluster
        ↓
Azure Cloud
```
