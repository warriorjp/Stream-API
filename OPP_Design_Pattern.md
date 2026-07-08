# Design Patterns - All 23 GoF Patterns (Creational, Structural, Behavioral)

---

## Creational Patterns

### 1. Singleton Pattern

Purpose: Ensure a class has only one instance, and provide a global point of access to it.

When to use:
- Logging
- Configuration manager
- Database connections

Java Example:

```java
public class Singleton {
    private static Singleton instance;

    private Singleton() {}  // Private constructor

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();  // Lazy initialization
        }
        return instance;
    }

    public void printMessage() {
        System.out.println("I am a Singleton!");
    }
}
```

---

### 2. Factory Method Pattern

Purpose: Define an interface for creating an object, but let subclasses decide which class to instantiate.

When to use:
- When the creation process is complex
- When client code should not know class names

Java Example:

```java
interface Payment {
    void pay(double amount);
}

class CreditCardPayment implements Payment {
    public void pay(double amount) {
        System.out.println("Paid Rs " + amount + " using Credit Card");
    }
}

class UpiPayment implements Payment {
    public void pay(double amount) {
        System.out.println("Paid Rs " + amount + " using UPI");
    }
}

class PaymentFactory {
    public static Payment getPayment(String type) {
        if ("card".equalsIgnoreCase(type)) return new CreditCardPayment();
        if ("upi".equalsIgnoreCase(type)) return new UpiPayment();
        throw new IllegalArgumentException("Invalid payment type");
    }
}
```

---

### 3. Abstract Factory Pattern

Purpose: Provide an interface for creating families of related or dependent objects without specifying their concrete classes.

Java Example:

```java
interface Button {
    void click();
}

class MacButton implements Button {
    public void click() { System.out.println("Mac Button Clicked"); }
}

class WindowsButton implements Button {
    public void click() { System.out.println("Windows Button Clicked"); }
}

interface GUIFactory {
    Button createButton();
}

class MacFactory implements GUIFactory {
    public Button createButton() { return new MacButton(); }
}

class WindowsFactory implements GUIFactory {
    public Button createButton() { return new WindowsButton(); }
}
```

---

### 4. Prototype Pattern

Purpose: Create new objects by copying an existing object (clone).

Java Example:

```java
class Document implements Cloneable {
    private String content;

    public Document(String content) {
        this.content = content;
    }

    public void show() {
        System.out.println("Content: " + content);
    }

    public Document clone() {
        try {
            return (Document) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
```

---

### 5. Builder Pattern

Purpose: Separate the construction of a complex object from its representation, allowing values to be set step by step rather than through a long constructor.

Java Example:

```java
public class Computer {
    // Required parameters
    private String CPU;
    private String RAM;

    // Optional parameters
    private String storage;
    private String graphicsCard;
}

public class Main {
    public static void main(String[] args) {
        Computer gamingPC = new Computer.Builder("Intel i9", "32GB")
            .setStorage("1TB SSD")
            .setGraphicsCard("NVIDIA RTX 4080")
            .build();
    }
}
```

---

## Structural Patterns

### 6. Adapter Pattern

Purpose: Convert the interface of a class into another interface clients expect, allowing incompatible interfaces to work together.

When to use:
- Integrating a third party library with a mismatched interface
- Making legacy code work with new code

Java Example:

```java
interface MediaPlayer {
    void play(String fileName);
}

class AdvancedMediaPlayer {
    void playMp4(String fileName) {
        System.out.println("Playing mp4 file: " + fileName);
    }
}

class MediaAdapter implements MediaPlayer {
    private AdvancedMediaPlayer advancedPlayer;

    public MediaAdapter() {
        advancedPlayer = new AdvancedMediaPlayer();
    }

    public void play(String fileName) {
        advancedPlayer.playMp4(fileName);
    }
}
```

---

### 7. Bridge Pattern

Purpose: Decouple an abstraction from its implementation so the two can vary independently.

When to use:
- When both the abstraction and implementation need to be extended independently
- Avoiding a class explosion from combining variants (for example shape types times colors)

Java Example:

```java
interface Color {
    String fill();
}

class RedColor implements Color {
    public String fill() { return "Red"; }
}

class BlueColor implements Color {
    public String fill() { return "Blue"; }
}

abstract class Shape {
    protected Color color;

    Shape(Color color) {
        this.color = color;
    }

    abstract void draw();
}

class Circle extends Shape {
    Circle(Color color) {
        super(color);
    }

    void draw() {
        System.out.println("Circle filled with " + color.fill());
    }
}
```

---

### 8. Composite Pattern

Purpose: Compose objects into tree structures to represent part whole hierarchies, letting clients treat individual objects and compositions uniformly.

When to use:
- File and folder structures
- Organization hierarchies
- UI component trees

Java Example:

```java
interface Employee {
    void showDetails();
}

class Developer implements Employee {
    private String name;

    Developer(String name) {
        this.name = name;
    }

    public void showDetails() {
        System.out.println("Developer: " + name);
    }
}

class Manager implements Employee {
    private String name;
    private List<Employee> subordinates = new ArrayList<>();

    Manager(String name) {
        this.name = name;
    }

    public void add(Employee e) {
        subordinates.add(e);
    }

    public void showDetails() {
        System.out.println("Manager: " + name);
        for (Employee e : subordinates) {
            e.showDetails();
        }
    }
}
```

---

### 9. Decorator Pattern

Purpose: Attach additional responsibilities to an object dynamically, without modifying its structure, as a flexible alternative to subclassing.

When to use:
- Adding features to objects at runtime (for example, adding toppings to a coffee order)
- Avoiding a large number of subclasses for every feature combination

Java Example:

```java
interface Coffee {
    double cost();
}

class SimpleCoffee implements Coffee {
    public double cost() { return 50; }
}

abstract class CoffeeDecorator implements Coffee {
    protected Coffee decoratedCoffee;

    CoffeeDecorator(Coffee coffee) {
        this.decoratedCoffee = coffee;
    }
}

class MilkDecorator extends CoffeeDecorator {
    MilkDecorator(Coffee coffee) {
        super(coffee);
    }

    public double cost() {
        return decoratedCoffee.cost() + 10;
    }
}
```

---

### 10. Facade Pattern

Purpose: Provide a simplified, unified interface to a set of interfaces in a subsystem, making the subsystem easier to use.

When to use:
- Simplifying a complex library or set of services for client code
- Hiding internal complexity of subsystems (for example, an order processing facade hiding payment, inventory, and shipping subsystems)

Java Example:

```java
class InventoryService {
    boolean checkStock(String item) {
        System.out.println("Checking stock for " + item);
        return true;
    }
}

class PaymentService {
    boolean processPayment(double amount) {
        System.out.println("Processing payment of " + amount);
        return true;
    }
}

class ShippingService {
    void shipOrder(String item) {
        System.out.println("Shipping " + item);
    }
}

class OrderFacade {
    private InventoryService inventory = new InventoryService();
    private PaymentService payment = new PaymentService();
    private ShippingService shipping = new ShippingService();

    public void placeOrder(String item, double amount) {
        if (inventory.checkStock(item) && payment.processPayment(amount)) {
            shipping.shipOrder(item);
        }
    }
}
```

---

### 11. Flyweight Pattern

Purpose: Use sharing to support large numbers of fine grained objects efficiently, by separating intrinsic (shared) state from extrinsic (context specific) state.

When to use:
- Rendering large numbers of similar objects (for example, characters in a text editor, or trees in a game map)
- Reducing memory footprint when many objects share common data

Java Example:

```java
class TreeType {
    String name;
    String color;

    TreeType(String name, String color) {
        this.name = name;
        this.color = color;
    }

    void draw(int x, int y) {
        System.out.println("Drawing " + name + " at (" + x + "," + y + ") in " + color);
    }
}

class TreeFactory {
    private static Map<String, TreeType> treeTypes = new HashMap<>();

    static TreeType getTreeType(String name, String color) {
        String key = name + color;
        TreeType type = treeTypes.get(key);
        if (type == null) {
            type = new TreeType(name, color);
            treeTypes.put(key, type);
        }
        return type;
    }
}
```

---

### 12. Proxy Pattern

Purpose: Provide a surrogate or placeholder for another object to control access to it.

When to use:
- Lazy loading of expensive objects
- Access control or permission checks
- Logging or caching before delegating to the real object

Java Example:

```java
interface Image {
    void display();
}

class RealImage implements Image {
    private String fileName;

    RealImage(String fileName) {
        this.fileName = fileName;
        loadFromDisk();
    }

    private void loadFromDisk() {
        System.out.println("Loading " + fileName);
    }

    public void display() {
        System.out.println("Displaying " + fileName);
    }
}

class ProxyImage implements Image {
    private RealImage realImage;
    private String fileName;

    ProxyImage(String fileName) {
        this.fileName = fileName;
    }

    public void display() {
        if (realImage == null) {
            realImage = new RealImage(fileName);
        }
        realImage.display();
    }
}
```

---

## Behavioral Patterns

### 13. Chain of Responsibility Pattern

Purpose: Pass a request along a chain of handlers until one of them handles it, decoupling sender from receiver.

When to use:
- Approval workflows (for example, expense approval by level)
- Middleware or filter chains in web frameworks

Java Example:

```java
abstract class Approver {
    protected Approver next;

    void setNext(Approver next) {
        this.next = next;
    }

    abstract void approve(double amount);
}

class Manager extends Approver {
    void approve(double amount) {
        if (amount <= 10000) {
            System.out.println("Manager approved: " + amount);
        } else if (next != null) {
            next.approve(amount);
        }
    }
}

class Director extends Approver {
    void approve(double amount) {
        System.out.println("Director approved: " + amount);
    }
}
```

---

### 14. Command Pattern

Purpose: Encapsulate a request as an object, allowing parameterization of clients with queues, requests, and operations, and support for undo.

When to use:
- Implementing undo or redo functionality
- Queuing or logging requests (for example, task scheduling systems)

Java Example:

```java
interface Command {
    void execute();
}

class Light {
    void turnOn() {
        System.out.println("Light is ON");
    }
}

class TurnOnCommand implements Command {
    private Light light;

    TurnOnCommand(Light light) {
        this.light = light;
    }

    public void execute() {
        light.turnOn();
    }
}

class RemoteControl {
    private Command command;

    void setCommand(Command command) {
        this.command = command;
    }

    void pressButton() {
        command.execute();
    }
}
```

---

### 15. Interpreter Pattern

Purpose: Given a language, define a representation for its grammar along with an interpreter that uses the representation to interpret sentences.

When to use:
- Parsing simple expression languages (for example, SQL like query parsers, rule engines)
- Rarely used in typical application code, more common in compilers or DSL tools

Java Example:

```java
interface Expression {
    int interpret();
}

class NumberExpression implements Expression {
    private int number;

    NumberExpression(int number) {
        this.number = number;
    }

    public int interpret() {
        return number;
    }
}

class AddExpression implements Expression {
    private Expression left;
    private Expression right;

    AddExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public int interpret() {
        return left.interpret() + right.interpret();
    }
}
```

---

### 16. Iterator Pattern

Purpose: Provide a way to access elements of a collection sequentially without exposing its underlying representation.

When to use:
- Traversing custom collection types
- Java's built in Iterator and Iterable interfaces are direct implementations of this pattern

Java Example:

```java
class NameRepository implements Iterable<String> {
    private List<String> names = new ArrayList<>();

    void addName(String name) {
        names.add(name);
    }

    public Iterator<String> iterator() {
        return names.iterator();
    }
}
```

---

### 17. Mediator Pattern

Purpose: Define an object that encapsulates how a set of objects interact, promoting loose coupling by keeping objects from referring to each other directly.

When to use:
- Chat room applications, where users communicate through a central mediator instead of directly
- Complex UI components that need coordinated communication

Java Example:

```java
class ChatRoom {
    void sendMessage(String message, String user) {
        System.out.println(user + " sends: " + message);
    }
}

class User {
    private String name;
    private ChatRoom chatRoom;

    User(String name, ChatRoom chatRoom) {
        this.name = name;
        this.chatRoom = chatRoom;
    }

    void send(String message) {
        chatRoom.sendMessage(message, name);
    }
}
```

---

### 18. Memento Pattern

Purpose: Without violating encapsulation, capture and externalize an object's internal state so it can be restored later.

When to use:
- Implementing undo functionality
- Saving checkpoints or snapshots of application state

Java Example:

```java
class EditorMemento {
    private final String content;

    EditorMemento(String content) {
        this.content = content;
    }

    String getContent() {
        return content;
    }
}

class Editor {
    private String content = "";

    void write(String text) {
        content += text;
    }

    EditorMemento save() {
        return new EditorMemento(content);
    }

    void restore(EditorMemento memento) {
        content = memento.getContent();
    }
}
```

---

### 19. Observer Pattern

Purpose: Define a one to many dependency between objects so that when one object changes state, all its dependents are notified automatically.

When to use:
- Event driven systems (for example, Kafka consumers reacting to topic events)
- UI frameworks reacting to model changes

Java Example:

```java
interface Observer {
    void update(String event);
}

class EmailSubscriber implements Observer {
    public void update(String event) {
        System.out.println("Email notification: " + event);
    }
}

class EventPublisher {
    private List<Observer> observers = new ArrayList<>();

    void subscribe(Observer o) {
        observers.add(o);
    }

    void notifyAll(String event) {
        for (Observer o : observers) {
            o.update(event);
        }
    }
}
```

---

### 20. State Pattern

Purpose: Allow an object to alter its behavior when its internal state changes, appearing as if the object changed its class.

When to use:
- Order status workflows (for example, Placed, Shipped, Delivered)
- Any system with clearly defined states and state specific behavior

Java Example:

```java
interface OrderState {
    void next(OrderContext context);
}

class PlacedState implements OrderState {
    public void next(OrderContext context) {
        System.out.println("Order shipped");
        context.setState(new ShippedState());
    }
}

class ShippedState implements OrderState {
    public void next(OrderContext context) {
        System.out.println("Order delivered");
    }
}

class OrderContext {
    private OrderState state = new PlacedState();

    void setState(OrderState state) {
        this.state = state;
    }

    void next() {
        state.next(this);
    }
}
```

---

### 21. Strategy Pattern

Purpose: Define a family of algorithms, encapsulate each one, and make them interchangeable at runtime.

When to use:
- Choosing between multiple algorithms at runtime (for example, different sorting or pricing strategies)
- Avoiding large conditional blocks for algorithm selection

Java Example:

```java
interface DiscountStrategy {
    double applyDiscount(double amount);
}

class NoDiscount implements DiscountStrategy {
    public double applyDiscount(double amount) {
        return amount;
    }
}

class TenPercentDiscount implements DiscountStrategy {
    public double applyDiscount(double amount) {
        return amount * 0.9;
    }
}

class Checkout {
    private DiscountStrategy strategy;

    Checkout(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    double getFinalAmount(double amount) {
        return strategy.applyDiscount(amount);
    }
}
```

---

### 22. Template Method Pattern

Purpose: Define the skeleton of an algorithm in a base class, letting subclasses override specific steps without changing the overall structure.

When to use:
- Standardizing a process while allowing customization of specific steps (for example, a data processing pipeline with a fixed read, process, write flow)

Java Example:

```java
abstract class DataProcessor {
    final void process() {
        readData();
        processData();
        writeData();
    }

    abstract void readData();
    abstract void processData();

    void writeData() {
        System.out.println("Writing data to output");
    }
}

class CsvDataProcessor extends DataProcessor {
    void readData() {
        System.out.println("Reading CSV data");
    }

    void processData() {
        System.out.println("Processing CSV data");
    }
}
```

---

### 23. Visitor Pattern

Purpose: Represent an operation to be performed on elements of an object structure, letting you define a new operation without changing the classes of the elements it operates on.

When to use:
- Adding new operations to a stable set of classes without modifying them
- Compilers and AST processing tools commonly use this pattern

Java Example:

```java
interface Visitor {
    void visit(Book book);
    void visit(Electronics electronics);
}

interface Item {
    void accept(Visitor visitor);
}

class Book implements Item {
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

class Electronics implements Item {
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

class PriceVisitor implements Visitor {
    public void visit(Book book) {
        System.out.println("Calculating price for Book");
    }

    public void visit(Electronics electronics) {
        System.out.println("Calculating price for Electronics");
    }
}
```

---

## Quick Interview Reference

- Creational patterns (5): Singleton, Factory Method, Abstract Factory, Prototype, Builder
- Structural patterns (7): Adapter, Bridge, Composite, Decorator, Facade, Flyweight, Proxy
- Behavioral patterns (11): Chain of Responsibility, Command, Interpreter, Iterator, Mediator, Memento, Observer, State, Strategy, Template Method, Visitor
- Most commonly asked in interviews across all 23: Singleton, Factory Method, Builder, Observer, Strategy, Decorator, Proxy, and Adapter
- For Kafka or event driven system discussions, Observer pattern is a natural talking point since consumer notification models mirror it directly
- For microservices discussions, Facade maps well to API Gateway style design, and Strategy maps well to pluggable business rules across services