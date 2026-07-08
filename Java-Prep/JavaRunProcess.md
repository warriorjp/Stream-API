# JVM Class Loading Process and Core Java Platform Concepts

## How JVM Starts a Java Application (java ClassName)

When we start a Java application using java ClassName, the JVM follows a structured process that involves class loading, linking, and initialization before executing the main method.

### Step 1: Class Loading

The JVM needs to load the .class file (bytecode) into memory.
The ClassLoader is responsible for dynamically finding and loading the required classes when needed.

Java has three main types of ClassLoaders:

- Bootstrap ClassLoader, loads core Java classes from rt.jar (Java standard library classes like java.lang.*)
- Extension ClassLoader, loads classes from the ext directory (java.ext.dirs)
- Application (System) ClassLoader, loads application classes from the classpath

### Step 2: Linking

Once the class is loaded, it goes through three linking phases:

- Verification, ensures bytecode validity
- Preparation, allocates memory for static variables
- Resolution, converts symbolic references to actual memory references

### Step 3: Initialization

- The static variables are assigned values, and static blocks are executed
- This is where the main method is identified

### Step 4: JVM Calls main Method

- The JVM starts executing the main method from the loaded class
- Since main is static, it is called without creating an object
- The statements inside main run sequentially
- If other classes or methods are required, the ClassLoader loads them dynamically

### Example

```java
public class Test {
    static {
        System.out.println("Class Loaded!");
    }

    public static void main(String[] args) {
        System.out.println("Main method executed!");
    }
}
```

---

## JRE, JVM, JDK

### JVM (Java Virtual Machine)

- JVM is the engine that runs Java bytecode
- It interprets or compiles .class files (Java bytecode) into machine code so your program can run on any platform
- JVM is responsible for memory management, garbage collection, security, and thread execution

### JRE (Java Runtime Environment)

- JRE provides the runtime environment required to run Java applications
- It includes the JVM, core libraries, and other supporting files. However, it does not include development tools like compilers or debuggers
- It is used by users who only need to run Java programs, not develop them

### JDK (Java Development Kit)

- JDK is a software development kit used to develop Java applications
- It includes the JRE, JVM, and developer tools like the javac compiler, java runner, javadoc, and debugging tools
- It is essential for Java developers

---

## Maven vs Gradle

Maven and Gradle are build automation tools used to manage dependencies and automate tasks like compiling code, running tests, packaging applications, and deployment.

- Maven uses XML (pom.xml) and follows a convention over configuration approach, making it simple and standardised
- Gradle uses Groovy or Kotlin DSL (build.gradle) and provides more flexibility, faster builds through incremental compilation and caching, and easier customisation
- Maven is widely used for its simplicity, while Gradle is often preferred for large projects where build performance and customisation are important
