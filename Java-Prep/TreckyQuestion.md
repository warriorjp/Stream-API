# 🧠 Java Tricky Questions & Concepts

A curated reference of commonly asked tricky Java interview questions with explanations and code examples.

---

## Table of Contents

1. [Method Overloading with `null`](#1-method-overloading-with-null)
2. [Thread `start()` vs `run()`](#2-thread-start-vs-run)
3. [Interface Method Conflict — Different Return Types](#3-interface-method-conflict--different-return-types)
4. [Static Method Accessing Non-Static Variable](#4-static-method-accessing-non-static-variable)
5. [Static Nested Class](#5-static-nested-class)
6. [Functional Interface & Lambda](#6-functional-interface--lambda)
7. [Built-in Functional Interfaces](#7-built-in-functional-interfaces)
8. [Exception Handling in Method Overriding](#8-exception-handling-in-method-overriding)
9. [Constructor Chaining](#9-constructor-chaining)
10. [Exception in `finally` Block](#10-exception-in-finally-block)
11. [Try-With-Resources](#11-try-with-resources)
12. [Deep Copy vs Shallow Copy](#12-deep-copy-vs-shallow-copy)
13. [Streams Are Consumed Once](#13-streams-are-consumed-once)
14. [Thread vs Runnable — `run()` Method](#14-thread-vs-runnable--run-method)
15. [Multiple Inheritance via Default Methods](#15-multiple-inheritance-via-default-methods)
16. [Anonymous Classes](#16-anonymous-classes)
17. [Circular Dependency](#17-circular-dependency)
18. [Preventing JVM Memory Leaks / OOM](#18-preventing-jvm-memory-leaks--oom)
19. [Optimising Slow SQL Queries](#19-optimising-slow-sql-queries)
20. [String Object Count — `concat()`](#20-string-object-count--concat)
21. [Resolving Slow API Responses](#21-resolving-slow-api-responses)
22. [String Pool vs Heap — `intern()`](#22-bonus-string-pool-vs-heap--intern)
23. [`==` vs `equals()` for Strings](#23-bonus--vs-equals-for-strings)
24. [`volatile` vs `synchronized`](#24-bonus-volatile-vs-synchronized)
25. [Default Rollback Behavior `@Transactional`](#25-default-rollback-behavior-transactional)
26. [Why are Passwords Hashed Instead of Encrypted?](#26-why-are-passwords-hashed-instead-of-encrypted)

---

## 1. Method Overloading with `null`

When calling `testMethod(null)`, both `testMethod(Object)` and `testMethod(String)` are candidates.
Since `String` is a subclass of `Object`, Java prefers the **most specific** type — so the `String` version is selected.

```java
public class TestClass {
    public void testMethod(Object obj) {
        System.out.println("Object method called");
    }

    public void testMethod(String str) {
        System.out.println("String method called");
    }

    public static void main(String[] args) {
        TestClass obj = new TestClass();
        obj.testMethod(null); // Output: String method called
    }
}
```

> ⚠️ **Ambiguity Error:** If both methods had equally specific parameters (e.g., `Integer` and `String`),
> Java throws a **compilation error** because neither is more specific than the other.

```java
// ❌ Ambiguous — compilation error
public void testMethod(Integer inte) { ... }
public void testMethod(String str)   { ... }
```

---

## 2. Thread `start()` vs `run()`

```
| Feature                  | `start()`             | `run()`               |
|--------------------------|-----------------------|-----------------------|
| Creates a new thread?    | ✅ Yes                | ❌ No                 |
| Execution context        | New thread            | Current (main) thread |
| Use case                 | True parallel execution | Normal method call  |

```

```java
class MyThread extends Thread {
    public void run() {
        System.out.println(Thread.currentThread().getName() + " is running");
    }
}

public class Test {
    public static void main(String[] args) {
        MyThread t1 = new MyThread();
        MyThread t2 = new MyThread();

        t1.start(); // Creates a new thread → "Thread-0 is running"
        t2.start(); // Creates a new thread → "Thread-1 is running"

        t1.run();   // Runs in main thread → "main is running"
        t2.run();   // Runs in main thread → "main is running"
    }
}
```

**Output:**
```
Thread-0 is running
Thread-1 is running
main is running
main is running
```

---

## 3. Interface Method Conflict — Different Return Types

If two interfaces declare a method with the **same name** but **different return types**, and a class implements both, it results in a **compilation error** — Java does not allow method overloading based solely on return type.

```java
interface A {
    int show();
}

interface B {
    String show();
}

// ❌ Compilation Error
class C implements A, B {
    @Override
    public int show() { // Cannot satisfy both return types
        return 10;
    }
}
```

**Error:**
```
C is not abstract and does not override the abstract method show() in B
```

---

## 4. Static Method Accessing Non-Static Variable

Static methods belong to the **class**, not to any instance. So they cannot directly access non-static (instance) variables.

```java
class Example {
    int instanceVar = 10; // Non-static

    static void staticMethod() {
        System.out.println(instanceVar); // ❌ Compilation Error
    }
}
```

**Fix — Create an instance inside the static method:**

```java
class Example {
    int instanceVar = 10;

    static void staticMethod() {
        Example obj = new Example();
        System.out.println(obj.instanceVar); // ✅ Works
    }

    public static void main(String[] args) {
        staticMethod();
    }
}
```

---

## 5. Static Nested Class

- An **inner class can be made static** (called a *static nested class*).
- A **top-level (outer) class cannot be static**.
- A static nested class can access **only static members** of the outer class directly.

```java
class Outer {
    static int staticVar = 100;
    int instanceVar = 200;

    static class StaticInner {
        void display() {
            System.out.println("Static variable: " + staticVar);   // ✅ Allowed
            // System.out.println(instanceVar);                     // ❌ Not allowed
        }
    }
}

public class Main {
    public static void main(String[] args) {
        // No need to create an Outer instance
        Outer.StaticInner innerObj = new Outer.StaticInner();
        innerObj.display();
    }
}
```

---

## 6. Functional Interface & Lambda

`@FunctionalInterface` ensures that the interface has **exactly one abstract method**.
A lambda expression provides its implementation concisely.

```java
@FunctionalInterface
interface MyFunctionalInterface {
    void showMessage(String message);
}

public class LambdaExample {
    public static void main(String[] args) {
        MyFunctionalInterface myFunc = message -> System.out.println("Message: " + message);
        myFunc.showMessage("Hello from Lambda!"); // Output: Message: Hello from Lambda!
    }
}
```

> ℹ️ A functional interface **can** have default and static methods — only one *abstract* method is the restriction.

---

## 7. Built-in Functional Interfaces

Java provides four core functional interfaces in the `java.util.function` package:

```

| Interface     | Input | Output    | Use Case                          |
|---------------|-------|-----------|-----------------------------------|
| `Predicate<T>` | T     | `boolean` | Filtering, validation             |
| `Consumer<T>`  | T     | `void`    | Printing, saving, side effects    |
| `Supplier<T>`  | None  | T         | Lazy initialization, value generation |
| `Function<T, R>` | T   | R         | Data transformation               |
```


```java
import java.util.function.*;

public class FunctionalInterfaceExample {
    public static void main(String[] args) {

        // Predicate — returns boolean
        Predicate<Integer> isEven = num -> num % 2 == 0;
        System.out.println("Is 4 even? " + isEven.test(4)); // true

        // Consumer — accepts input, no return
        Consumer<String> consumer = message -> System.out.println("Consumed: " + message);
        consumer.accept("Hello Consumer");

        // Supplier — no input, returns value
        Supplier<Double> randomValue = () -> Math.random();
        System.out.println("Random Value: " + randomValue.get());

        // Function — transforms input to output
        Function<Integer, String> function = num -> "Number is " + num;
        System.out.println(function.apply(10)); // Number is 10
    }
}
```

---

## 8. Exception Handling in Method Overriding

### When parent method **declares** a checked exception:

```

| Child method can… | Allowed? |
|---|---|
| Declare the **same** exception | ✅ Yes |
| Declare a **subclass** of the exception | ✅ Yes |
| Declare **no** exception | ✅ Yes |
| Declare a **broader/superclass** exception | ❌ No |
```

```java
class Parent {
    void show() throws Exception { }
}

class Child extends Parent {
    @Override
    void show() throws Exception { }            // ✅ Same exception

    // void show() throws ArithmeticException { } // ✅ Subclass exception (unchecked)
    // void show() throws Throwable { }           // ❌ Broader — Compilation Error
}
```

### When parent method declares **no** exception:

```java
class Parent {
    void show() { }
}

class Child extends Parent {
    @Override
    void show() throws IOException { }          // ❌ Checked exception — not allowed

    @Override
    void show() throws ArithmeticException { }  // ✅ Unchecked (runtime) — allowed
}
```

```
| Feature                  | Shallow Copy                                  | Deep Copy                                                        |
|--------------------------|-----------------------------------------------|------------------------------------------------------------------|
| What is copied           | Reference to the existing object              | A new independent object is created                              |
| Changes affect original? | ✅ Yes (for shared mutable objects)           | ❌ No                                                            |
| How                      | Assign or copy the reference                  | Create a new object and copy all nested objects                  |

```

---

## 9. Constructor Chaining

Constructor chaining calls one constructor from another using `this()` (same class) or `super()` (parent class).
`this()` / `super()` must always be the **first statement** in the constructor body.

```java
class Example {
    int x;
    String name;

    Example() {
        this(10);  // Calls single-parameter constructor
        System.out.println("Default Constructor");
    }

    Example(int x) {
        this(x, "John");  // Calls two-parameter constructor
        System.out.println("Single Parameter Constructor: x = " + x);
    }

    Example(int x, String name) {
        this.x = x;
        this.name = name;
        System.out.println("Two Parameter Constructor: x = " + x + ", name = " + name);
    }

    public static void main(String[] args) {
        new Example();
    }
}
```

**Output:**
```
Two Parameter Constructor: x = 10, name = John
Single Parameter Constructor: x = 10
Default Constructor
```

---

## 10. Exception in `finally` Block

If both `try` and `finally` blocks throw exceptions, the `finally` block's exception **overrides** the original one from `try`. The `try` block's exception is **suppressed**.

```java
public class FinallyExceptionExample {
    public static void main(String[] args) {
        try {
            System.out.println("Inside try block");
            throw new NullPointerException("Try block exception");
        } finally {
            System.out.println("Finally block executed.");
            throw new ArithmeticException("Finally block exception");
        }
    }
}
```

**Output:**
```
Inside try block
Finally block executed.
Exception in thread "main" java.lang.ArithmeticException: Finally block exception
```

> ⚠️ The `NullPointerException` from `try` is lost — only the `ArithmeticException` from `finally` propagates.

---

## 11. Try-With-Resources

Introduced in **Java 7**, try-with-resources automatically closes resources that implement `AutoCloseable` — no need for explicit `finally` blocks.

```java
import java.io.*;

public class TryWithResourcesExample {
    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader("test.txt"))) {
            System.out.println(br.readLine());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        // br is automatically closed here
    }
}
```

> Multiple resources can be declared, separated by semicolons. They are closed in **reverse order** of declaration.

---

## 12. Deep Copy vs Shallow Copy

```
| Feature                  | Shallow Copy                     | Deep Copy                     |
|--------------------------|----------------------------------|-------------------------------|
| What is copied           | Reference to the existing object | A new independent object      |
| Changes affect original? | ✅ Yes (shared mutable objects)  | ❌ No                         |
| How                      | Assign or copy the reference     | Copy all objects recursively  |
```

```java
import java.util.*;

public final class Person {
    private final String name;
    private final List<String> hobbies;

    // Shallow copy — shares the same list reference
    public Person(String name, List<String> hobbies) {
        this.name = name;
        this.hobbies = hobbies; // ⚠️ Shallow
    }

    // Deep copy — creates a new independent list
    public Person deepCopy(String name, List<String> hobbies) {
        return new Person(name, new ArrayList<>(hobbies)); // ✅ Deep
    }

    public String getName() {
        return name;
    }

    // Defensive copy on return — prevents external mutation
    public List<String> getHobbies() {
        return Collections.unmodifiableList(hobbies);
    }
}
```

---

## 13. Streams Are Consumed Once

In Java, a `Stream` can only be **consumed once**. Calling a terminal operation (e.g., `forEach`, `collect`, `count`) closes the stream. Any subsequent operation on it throws `IllegalStateException`.

```java
import java.util.*;
import java.util.stream.*;

public class StreamReuseExample {
    public static void main(String[] args) {
        List<Integer> numbers = List.of(5, 3, 9, 1, 4);

        Stream<Integer> stream = numbers.stream().sorted();

        stream.forEach(System.out::println); // ✅ Works fine

        stream.forEach(System.out::println); // ❌ IllegalStateException: stream has already been operated upon or closed
    }
}
```

> **Fix:** Create a new stream each time, or collect results into a `List` and reuse that.

---

## 14. Thread vs Runnable — `run()` Method

### Extending `Thread`

```java
class MyThread extends Thread {
    public void run() {
        System.out.println("Thread is running in: " + Thread.currentThread().getName());
    }
}

public class Main {
    public static void main(String[] args) {
        MyThread t1 = new MyThread();
        t1.start(); // ✅ Starts a new thread and calls run()
    }
}
```

### Implementing `Runnable`

```java
class MyRunnable implements Runnable {
    public void run() {
        System.out.println("Runnable is running in: " + Thread.currentThread().getName());
    }
}

public class Main {
    public static void main(String[] args) {
        Thread t1 = new Thread(new MyRunnable());
        t1.start(); // ✅ Starts a new thread and calls run()
    }
}
```

> **Prefer `Runnable`** over extending `Thread` — it allows the class to extend another class and promotes separation of task logic from thread management.

---

## 15. Multiple Inheritance via Default Methods

Java doesn't support multiple inheritance with classes, but **default methods in interfaces** can simulate it. When two interfaces have the same default method, the implementing class **must override** it to resolve the conflict.

```java
interface A {
    default void show() {
        System.out.println("A");
    }
}

interface B {
    default void show() {
        System.out.println("B");
    }
}

class C implements A, B {
    @Override
    public void show() {
        System.out.println("C: Resolving conflict between A and B");
        A.super.show(); // Explicitly call A's version
        B.super.show(); // Explicitly call B's version
    }
}

public class Main {
    public static void main(String[] args) {
        new C().show();
    }
}
```

**Output:**
```
C: Resolving conflict between A and B
A
B
```

---

## 16. Anonymous Classes

An anonymous class is an **inline, one-time implementation** of an interface or abstract class — no separate class file needed.

### Implementing an Interface

```java
interface Greeting {
    void sayHello();
}

public class Main {
    public static void main(String[] args) {
        Greeting greeting = new Greeting() {
            public void sayHello() {
                System.out.println("Hello from anonymous class!");
            }
        };
        greeting.sayHello();
    }
}
```

### Extending an Abstract Class

```java
abstract class Animal {
    abstract void sound();
}

public class Main {
    public static void main(String[] args) {
        Animal dog = new Animal() {
            void sound() {
                System.out.println("Bark!");
            }
        };
        dog.sound();
    }
}
```

> For single-method interfaces, prefer **lambda expressions** over anonymous classes (cleaner syntax, Java 8+).

---

## 17. Circular Dependency

A circular dependency occurs when two or more beans depend on each other, creating a **cycle** that prevents Spring from wiring them properly at startup.

```java
// ❌ Circular dependency
@Component
public class A {
    @Autowired
    private B b;
}

@Component
public class B {
    @Autowired
    private A a;
}
```

**Fix — Introduce a Mediator/Service:**

```java
@Component
public class A {
    @Autowired
    private MediatorService mediator;
}

@Component
public class B {
    @Autowired
    private MediatorService mediator;
}

@Component
public class MediatorService {
    public void handleAStuff() { }
    public void handleBStuff() { }
}
```

> Other fixes: use `@Lazy` on one of the dependencies, or refactor to use setter injection instead of constructor injection.

---

## 18. Preventing JVM Memory Leaks / OOM(Out Of Memory)

```
| Strategy                 | How                                                                  |
|--------------------------|----------------------------------------------------------------------|
| Make objects GC-eligible | Set unused large objects to `null`                                   |
| Close resources          | Use try-with-resources for I/O, databases, and sockets               |
| Avoid static collections | Don't hold data in long-lived `static List` or `static Map`          |
| Tune heap size           | Use `-Xms` and `-Xmx` JVM flags                                      |
| Profile memory           | Use VisualVM, Eclipse MAT, JProfiler, or YourKit                     |
```

```bash
# Increase JVM heap size
java -Xms512m -Xmx1024m MyApp
# -Xms = initial heap size
# -Xmx = maximum heap size
```

```java
// ✅ Make object eligible for GC
myLargeObject = null;

// ✅ Auto-close DB connection
try (Connection conn = dataSource.getConnection()) {
    // use conn
}
```

---

## 19. Optimising Slow SQL Queries

- Use **indexed columns** in `WHERE`, `JOIN`, `ORDER BY`, and `GROUP BY`.
- Avoid `SELECT *` — fetch only the columns you need.
- Use `LIMIT` when you only need a subset of rows.
- Check **execution plans** (`EXPLAIN`) to spot full table scans.
- Cache frequent read-only results using Redis or Memcached.

---

## 20. String Object Count — `concat()`

### Case 1 — Result is discarded

```java
String str = "hello";
str.concat("hi"); // New String "hellohi" created on heap but not assigned — immediately eligible for GC
```

**Objects created:** 2 — `"hello"` in String Pool, `"hellohi"` on Heap (discarded).

---

### Case 2 — Result is assigned

```java
String str = "hello";
str = str.concat("hi");
```

**Objects created:** 3:
1. `"hello"` — String Pool
2. `"hi"` — String Pool
3. `"hellohi"` — Heap (result of concat, now referenced by `str`)

> `String` is **immutable** — `concat()` always creates a new object; the original is never modified.

---

## 21. Resolving Slow API Responses

### 📌 Database Optimisation
- Add indexes on fields used in `WHERE`, `JOIN`, `ORDER BY`, `GROUP BY`.
- Avoid `SELECT *` — fetch only required columns.
- Optimise queries and review execution plans.
- Cache frequent or read-only data (Redis, Memcached).

### 📦 Payload Optimisation
- Avoid returning large payloads.
- Implement **pagination** for list APIs.
- Remove unnecessary metadata and deeply nested objects.

### ⚡ Code & Logic Optimisation
- Refactor complex or blocking code.
- Use **asynchronous / non-blocking** calls where possible.
- Optimise loops and eliminate redundant computations.
- Profile and monitor hotspots.

### 🛠️ Infrastructure & API Design
- Scale API servers horizontally (load balancing, auto-scaling).
- Enable **connection pooling** for DB and external API calls.
- Use circuit breakers, bulkheads, and retries for downstream calls.
- Apply **rate limiting** and request throttling.

### 🗂️ Caching Strategies
- Use application-level caching (in-memory or distributed).
- Use HTTP caching headers: `ETag`, `Last-Modified`, `Cache-Control`.
- Cache DB query results when freshness is not critical.

---

## 22. *(Bonus)* String Pool vs Heap — `intern()`

```java
String a = "hello";           // String Pool
String b = new String("hello"); // Heap — new object every time and refernce in StringPool
String c = b.intern();         // Returns String Pool reference

System.out.println(a == b);   // false — different references
System.out.println(a == c);   // true  — same pool reference
```

> Use `intern()` sparingly — it can cause memory pressure if overused.

---

## 23.`==` vs `equals()` for Strings

```java
String s1 = "Java";
String s2 = "Java";
String s3 = new String("Java");

System.out.println(s1 == s2);         // true  — same pool reference
System.out.println(s1 == s3);         // false — s3 is a heap object
System.out.println(s1.equals(s3));    // true  — same content

```

**Reason**

- == compares memory addresses.

- equals() compares the actual string content because String overrides equals().

```
StringBuilder sb1 = new StringBuilder("Java");
StringBuilder sb2 = new StringBuilder("Java");

System.out.println(sb1 == sb2);      // false
System.out.println(sb1.equals(sb2)); // false

```
**Reason**
- == compares references.

- StringBuilder does not override equals().

- Therefore, equals() behaves exactly like == (reference comparison).

```
StringBuffer sb1 = new StringBuffer("Java");
StringBuffer sb2 = new StringBuffer("Java");

System.out.println(sb1 == sb2);      // false
System.out.println(sb1.equals(sb2)); // false
```
**Reason**
- StringBuffer also does not override equals().

- It inherits Object.equals(), which compares references.
---

## 24.`volatile` vs `synchronized`

```

| Feature                 | `volatile`                     | `synchronized`                       |
|-------------------------|--------------------------------|--------------------------------------|
| Guarantees visibility   | ✅ Yes                         | ✅ Yes                               |
| Guarantees atomicity    | ❌ No                          | ✅ Yes                               |
| Locks / blocks threads  | ❌ No                          | ✅ Yes                               |
| Use case                | Simple flags, status variables | Compound operations, critical sections|
```

```java
// volatile — safe for a simple boolean flag
private volatile boolean running = true;

// synchronized — needed for compound check-then-act operations
public synchronized void increment() {
    count++;
}
```

---

## 25. Default Rollback Behavior @Transcational ##

By default, Spring rolls back transactions only for:

✅ RuntimeException

✅ Error

It does not roll back for checked exceptions.

```
@Transactional
public void saveEmployee() throws IOException {

    repository.save(employee);

    throw new IOException("Checked Exception");
}
```

The employee is saved.

**Why?**

Spring assumes checked exceptions are recoverable/business exceptions, so it commits the transaction unless configured otherwise.

**How to Roll Back for Checked Exceptions**

Use rollbackFor.

```
@Transactional(rollbackFor = Exception.class)
public void saveEmployee() throws Exception {

    repository.save(employee);

    throw new IOException();
}
```

**What if a RuntimeException is thrown?**

```
@Transactional
public void process() {

    repository.save(emp);

    throw new NullPointerException();
}
```
Transaction rolls back.

Nothing is saved.


**What if you catch the exception?**

```
@Transactional
public void process() {

    try {

        repository.save(emp);

        throw new RuntimeException();

    } catch (Exception e) {

        System.out.println(e.getMessage());

    }
}
```

The transaction commits.

**Why?**

Because the exception never leaves the transactional method. Spring sees the method complete normally and commits.

---

## 26. Why are passwords hashed instead of encrypted?##

**Hashing**
- Same input always produces the same hash.
- Even a tiny change in input creates a completely different hash.
- You cannot get the original password back from the hash.
- 
```
Password: "Java123"

Hash Function
      ↓
A7D92BC1F4...
```
**Make Hash More Secure By Adding Salt**

Salt is a random value added to each password before hashing. It ensures that even if two users choose the same password, their stored hashes are different. This prevents attackers from identifying shared passwords and protects against rainbow table attacks. In Spring Security, BCryptPasswordEncoder automatically generates, stores, and uses the salt as part of the hash, so developers don't need to manage it manually.

    Suppose the user enters:
    
    Password = Welcome@123
    
    BCrypt generates a random salt:
    
    Salt = XyZ123AbCdEf...
    
    It hashes:
    
    Welcome@123 + XyZ123AbCdEf...
    
    The stored value looks something like:
    
    $2a$10$XyZ123AbCdEfGhIjKlMnOuvQ9f7bR3mX6nW...

**How to compare hash with entered password**

- Reads the stored hash from the database.
- Extracts the salt embedded inside that hash.
- Combines the entered password with the extracted salt.
- Hashes it again using the same algorithm and cost factor.
- Compares the newly generated hash with the stored hash.

**Algorithm used**
- Spring Security typically uses the BCrypt algorithm for password hashing.
- Argon2  ✅ Best choice (modern)
    

**Encryption**

Encryption is like putting data in a locked box.

```
"Hello"

Encrypt with Key
      ↓
X9#P@L2

Decrypt with Key
      ↓
"Hello"
```

- Data can be restored using the correct key.
- Used when the original information must remain recoverable.

**Answer:**

Because the application never needs to know your original password. During login, it simply hashes the entered password and compares it with the stored hash. Even if the database is leaked, attackers cannot directly recover the original passwords from properly hashed values (especially when modern password-hashing algorithms with salts are used).

When to Use Which?

✅ Hashing: Password storage, file integrity checks, digital signatures, checksums.

✅ Encryption: Secure communication, payment information, confidential files, sensitive personal data.

---
## 27.How do you make sure your application does not consume duplicate Kafka messages?##

Make the Consumer Idempotent (Most Common)

Every message should have a unique identifier.

```
Receive Message

↓

Check if orderId already processed

↓

Yes → Ignore

No → Process
```


 | orderId | processed |
| ------- | --------- |
| ORD123  | Yes       |
| ORD124  | Yes       |





