**SOLID Principles in Java**

SOLID principles help write modular, flexible, and testable code. They are:

- Single Responsibility Principle (SRP)
- Open Closed Principle (OCP)
- Liskov Substitution Principle (LSP)
- Interface Segregation Principle (ISP)
- Dependency Inversion Principle (DIP)

**1. Single Responsibility Principle (SRP)**

A class should have only one reason to change.

**Bad Example**

One class handles both order processing and invoice generation.

```java
class OrderService {
    void processOrder() {
        // Order processing logic
    }

    void generateInvoice() {
        // Invoice logic, not order's responsibility
    }
}
```

**Good Example**

Separate classes for order processing and invoicing.

```java
class OrderService {
    void processOrder() {
        // Order processing logic
    }
}

class InvoiceService {
    void generateInvoice() {
        // Invoice logic
    }
}
```

**2. Open Closed Principle (OCP)**

Classes should be open for extension but closed for modification.

**Bad Example**

Modifying a class every time a new payment method is added.

```java
class PaymentProcessor {
    void pay(String type) {
        if (type.equals("CreditCard")) {
            // Credit card logic
        } else if (type.equals("PayPal")) {
            // PayPal logic
        }
    }
}
```

**Good Example**

Use abstractions so new payment types can be added without modifying existing code.

```java
interface PaymentMethod {
    void pay();
}

class CreditCard implements PaymentMethod {
    public void pay() {
        // Credit card logic
    }
}

class PayPal implements PaymentMethod {
    public void pay() {
        // PayPal logic
    }
}

class PaymentProcessor {
    void pay(PaymentMethod method) {
        method.pay();
    }
}
```

**3. Liskov Substitution Principle (LSP)**

Subclasses should be replaceable for their base class without breaking functionality.

**Bad Example**

`Penguin` inherits a `fly()` method it cannot use.

```java
class Bird {
    void fly() {
        // Not all birds fly
    }
}

class Penguin extends Bird {
    void fly() {
        // Cannot fly, breaks LSP
    }
}
```

**Good Example**

Separate behaviors using interfaces.

```java
interface Flyable {
    void fly();
}

class Sparrow implements Flyable {
    public void fly() {
        // Sparrow flies
    }
}

class Penguin {
    // Penguins don't fly, so no fly method here
}
```

**4. Interface Segregation Principle (ISP)**

Clients should not be forced to depend on methods they don't use.

**Bad Example**

A `Robot` class is forced to implement `eat()`, which is irrelevant.

```java
interface LivingBeing {
    void eat();
    void move();
}

class Robot implements LivingBeing {
    public void eat() {
        // Robots don't eat, unnecessary method
    }

    public void move() {
        // Robot movement
    }
}
```

**Good Example**

Create separate interfaces.

```java
interface Eatable {
    void eat();
}

interface Movable {
    void move();
}

class Robot implements Movable {
    public void move() {
        // Robot movement
    }
}

class Human implements Eatable, Movable {
    public void eat() {
        // Humans eat
    }

    public void move() {
        // Humans move
    }
}
```

**5. Dependency Inversion Principle (DIP)**

High-level modules should depend on abstractions, not concrete implementations.

**Bad Example**

`DataManager` is tightly coupled to `MySQLDatabase`.

```java
class MySQLDatabase {
    void connect() {
        // MySQL connection
    }
}

class DataManager {
    MySQLDatabase db = new MySQLDatabase();
    // Tightly coupled
}
```

**Good Example**

Use an interface to decouple the dependency.

```java
interface Database {
    void connect();
}

class MySQLDatabase implements Database {
    public void connect() {
        // MySQL connection
    }
}

class PostgreSQLDatabase implements Database {
    public void connect() {
        // PostgreSQL connection
    }
}

class DataManager {
    Database db;

    DataManager(Database db) {
        this.db = db;
    }
}
```

**Summary of SOLID Principles**

```
| Principle | Key Idea                                       | Example Fix                                      |
|-----------|------------------------------------------------|--------------------------------------------------|
| SRP       | One class should have only one responsibility  | Separate `OrderService` and `InvoiceService`     |
| OCP       | Open for extension, closed for modification    | Use a `PaymentMethod` interface                  |
| LSP       | Subclasses should be replaceable with parent   | Use a `Flyable` interface for birds              |
| ISP       | Clients should not depend on unused methods    | Separate `Eatable` and `Movable` interfaces      |
| DIP       | Depend on abstractions, not implementations    | Use a `Database` interface                       |
```

**Why Follow SOLID**

- Better maintainability
- Scalability and flexibility
- Less code duplication
- Easier debugging and testing

**Interview Questions and Answers**

**Q: What problem does SRP solve and how do you identify a violation?**

A: SRP prevents a class from taking on multiple unrelated responsibilities that could each change for different reasons. A common sign of violation is a class name that keeps growing in scope, like `OrderService` also handling invoicing, notifications, and logging. I split each responsibility into its own class so a change in invoicing logic never risks breaking order processing.

**Q: How does OCP help with adding new features without regression risk?**

A: OCP is achieved through abstraction, typically an interface or abstract class. New payment types, new notification channels, or new report formats can be added as new implementations without touching the existing tested code, which reduces regression risk during code review and deployment.

**Q: Can you give a real world LSP violation you have seen?**

A: A classic one is a `Rectangle` and `Square` inheritance relationship, where forcing `Square` to extend `Rectangle` and override both `setWidth()` and `setHeight()` breaks the expected behavior of the base class. The fix is to model them as separate shapes implementing a common `Shape` interface instead of forcing an inheritance relationship that does not hold in every case.

**Q: Why does ISP matter in microservices or API design?**

A: Fat interfaces force every implementing class or client to depend on methods it does not need, which increases coupling and makes future changes riskier. In an API context, this often means splitting a large service interface into smaller, role-specific interfaces so consumers only depend on the methods relevant to them.

**Q: How does DIP relate to Spring's dependency injection?**

A: Spring implements DIP directly. Instead of a class instantiating its own dependency with `new`, the dependency is defined as an interface and Spring injects the concrete implementation through the constructor or `@Autowired`. This makes unit testing easier since a mock implementation can be injected in place of the real one.