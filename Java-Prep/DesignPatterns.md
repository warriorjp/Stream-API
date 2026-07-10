# 🏗️ Java Design Patterns — Complete Guide

A comprehensive reference covering all 15 must-know design patterns with explanations, use cases, and Java code examples.

---

## Table of Contents

**Creational Patterns**
1. [Singleton](#1-singleton-pattern)
2. [Factory Method](#2-factory-method-pattern)
3. [Abstract Factory](#3-abstract-factory-pattern)
4. [Prototype](#4-prototype-pattern)
5. [Builder](#5-builder-pattern)

**Structural Patterns**

6. [Adapter](#6-adapter-pattern)
7. [Decorator](#7-decorator-pattern)
8. [Facade](#8-facade-pattern)
9. [Proxy](#9-proxy-pattern)
10. [Composite](#10-composite-pattern)

**Behavioral Patterns**

11. [Observer](#11-observer-pattern)
12. [Strategy](#12-strategy-pattern)
13. [Command](#13-command-pattern)
14. [Iterator](#14-iterator-pattern)
15. [State](#15-state-pattern)
16. [Template Method](#16-template-method-pattern)
17. [Chain of Responsibility](#17-chain-of-responsibility-pattern)

---

# 🟢 Creational Patterns
> Deal with object creation mechanisms — making creation more flexible and reusable.

---

## 1. Singleton Pattern

**Purpose:** Ensure a class has only **one instance** and provide a global access point to it.

**When to use:**
- Logging
- Configuration manager
- Database connection pool

```java
public class Singleton {
    // volatile ensures visibility across threads
    private static volatile Singleton instance;

    private Singleton() {} // Private constructor prevents external instantiation

    // Thread-safe lazy initialization (Double-Checked Locking)
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }

    public void printMessage() {
        System.out.println("I am a Singleton!");
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Singleton s1 = Singleton.getInstance();
        Singleton s2 = Singleton.getInstance();
        System.out.println(s1 == s2); // true — same instance
    }
}
```

> ⚠️ The basic `if (instance == null)` without `synchronized` is **not thread-safe**. Always use Double-Checked Locking with `volatile` in multithreaded environments.

---

## 2. Factory Method Pattern

**Purpose:** Define an interface for creating an object, but let subclasses decide which class to instantiate. Client code doesn't know class names.

**When to use:**
- When object creation logic is complex
- When client code should be decoupled from concrete classes

```java
interface Payment {
    void pay(double amount);
}

class CreditCardPayment implements Payment {
    public void pay(double amount) {
        System.out.println("Paid ₹" + amount + " using Credit Card");
    }
}

class UpiPayment implements Payment {
    public void pay(double amount) {
        System.out.println("Paid ₹" + amount + " using UPI");
    }
}

class NetBankingPayment implements Payment {
    public void pay(double amount) {
        System.out.println("Paid ₹" + amount + " using Net Banking");
    }
}

// Factory — client calls this; doesn't know concrete class names
class PaymentFactory {
    public static Payment getPayment(String type) {
        return switch (type.toLowerCase()) {
            case "card"        -> new CreditCardPayment();
            case "upi"         -> new UpiPayment();
            case "netbanking"  -> new NetBankingPayment();
            default -> throw new IllegalArgumentException("Invalid payment type: " + type);
        };
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Payment payment = PaymentFactory.getPayment("upi");
        payment.pay(500.00); // Paid ₹500.0 using UPI
    }
}
```

---

## 3. Abstract Factory Pattern

**Purpose:** Provide an interface for creating **families of related objects** without specifying their concrete classes.

**Difference from Factory Method:** Factory Method creates one product; Abstract Factory creates a *family* of related products.

**When to use:**
- UI toolkits that support multiple themes (Mac, Windows)
- Cross-platform component generation

```java
// Product interfaces
interface Button {
    void click();
}

interface Checkbox {
    void check();
}

// Mac products
class MacButton implements Button {
    public void click() { System.out.println("Mac Button Clicked"); }
}

class MacCheckbox implements Checkbox {
    public void check() { System.out.println("Mac Checkbox Checked"); }
}

// Windows products
class WindowsButton implements Button {
    public void click() { System.out.println("Windows Button Clicked"); }
}

class WindowsCheckbox implements Checkbox {
    public void check() { System.out.println("Windows Checkbox Checked"); }
}

// Abstract Factory
interface GUIFactory {
    Button createButton();
    Checkbox createCheckbox();
}

// Concrete factories
class MacFactory implements GUIFactory {
    public Button createButton()     { return new MacButton(); }
    public Checkbox createCheckbox() { return new MacCheckbox(); }
}

class WindowsFactory implements GUIFactory {
    public Button createButton()     { return new WindowsButton(); }
    public Checkbox createCheckbox() { return new WindowsCheckbox(); }
}

// Usage
public class Main {
    public static void main(String[] args) {
        GUIFactory factory = new MacFactory(); // Swap to WindowsFactory anytime
        Button btn = factory.createButton();
        btn.click(); // Mac Button Clicked
    }
}
```

---

## 4. Prototype Pattern

**Purpose:** Create new objects by **cloning** an existing object instead of constructing from scratch.

**When to use:**
- Object creation is expensive (e.g., DB fetch, heavy computation)
- You need many similar objects with slight differences

```java
class Document implements Cloneable {
    private String title;
    private String content;

    public Document(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void setTitle(String title)     { this.title = title; }
    public void setContent(String content) { this.content = content; }

    public void show() {
        System.out.println("Title: " + title + " | Content: " + content);
    }

    @Override
    public Document clone() {
        try {
            return (Document) super.clone(); // Shallow clone
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Cloning failed", e);
        }
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Document original = new Document("Report", "Q1 Results...");
        Document copy = original.clone();
        copy.setTitle("Report Copy");

        original.show(); // Title: Report       | Content: Q1 Results...
        copy.show();     // Title: Report Copy  | Content: Q1 Results...
    }
}
```

> ⚠️ `super.clone()` performs a **shallow copy**. For objects with mutable fields (lists, arrays), manually deep-copy those fields inside `clone()`.

---

## 5. Builder Pattern

**Purpose:** Construct complex objects **step by step** — avoids telescoping constructors with too many parameters.

**When to use:**
- Objects with many optional parameters
- Immutable objects with complex construction

```java
public class Computer {
    // Required
    private final String cpu;
    private final String ram;
    // Optional
    private final String storage;
    private final String graphicsCard;
    private final boolean wifi;

    private Computer(Builder builder) {
        this.cpu          = builder.cpu;
        this.ram          = builder.ram;
        this.storage      = builder.storage;
        this.graphicsCard = builder.graphicsCard;
        this.wifi         = builder.wifi;
    }

    @Override
    public String toString() {
        return "Computer{CPU='" + cpu + "', RAM='" + ram +
               "', Storage='" + storage + "', GPU='" + graphicsCard +
               "', WiFi=" + wifi + "}";
    }

    // Static nested Builder class
    public static class Builder {
        private final String cpu;
        private final String ram;
        private String storage      = "256GB HDD";
        private String graphicsCard = "Integrated";
        private boolean wifi        = true;

        public Builder(String cpu, String ram) {
            this.cpu = cpu;
            this.ram = ram;
        }

        public Builder setStorage(String storage) {
            this.storage = storage;
            return this;
        }

        public Builder setGraphicsCard(String graphicsCard) {
            this.graphicsCard = graphicsCard;
            return this;
        }

        public Builder setWifi(boolean wifi) {
            this.wifi = wifi;
            return this;
        }

        public Computer build() {
            return new Computer(this);
        }
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Computer gamingPC = new Computer.Builder("Intel i9", "32GB")
                .setStorage("1TB SSD")
                .setGraphicsCard("NVIDIA RTX 4080")
                .setWifi(true)
                .build();

        System.out.println(gamingPC);
    }
}
```

---

# 🔵 Structural Patterns
> Deal with object composition — how classes and objects are assembled into larger structures.

---

## 6. Adapter Pattern

**Purpose:** Bridge two **incompatible interfaces** so they can work together without modifying existing code.

**When to use:**
- Integrating legacy code with new systems
- Using third-party libraries with a different interface

```java
// Existing interface
interface RoundHole {
    void insertRound();
}

// Incompatible class (legacy / third-party)
class SquarePeg {
    public void insertSquare() {
        System.out.println("Square peg inserted");
    }
}

// Adapter — wraps SquarePeg, exposes RoundHole interface
class SquarePegAdapter implements RoundHole {
    private SquarePeg squarePeg;

    public SquarePegAdapter(SquarePeg squarePeg) {
        this.squarePeg = squarePeg;
    }

    @Override
    public void insertRound() {
        squarePeg.insertSquare(); // Adapts the call
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        SquarePeg peg = new SquarePeg();
        RoundHole hole = new SquarePegAdapter(peg);
        hole.insertRound(); // Square peg inserted
    }
}
```

---

## 7. Decorator Pattern

**Purpose:** **Dynamically add behaviour** to an object without modifying its class — wraps objects in decorator layers.

**When to use:**
- Adding features to objects at runtime
- Avoiding class explosion from subclassing

```java
interface Coffee {
    String getDescription();
    double getCost();
}

class SimpleCoffee implements Coffee {
    public String getDescription() { return "Simple Coffee"; }
    public double getCost()        { return 30.0; }
}

// Base decorator
abstract class CoffeeDecorator implements Coffee {
    protected Coffee coffee;
    public CoffeeDecorator(Coffee coffee) { this.coffee = coffee; }
}

class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) { super(coffee); }
    public String getDescription() { return coffee.getDescription() + ", Milk"; }
    public double getCost()        { return coffee.getCost() + 10.0; }
}

class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(Coffee coffee) { super(coffee); }
    public String getDescription() { return coffee.getDescription() + ", Sugar"; }
    public double getCost()        { return coffee.getCost() + 5.0; }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Coffee coffee = new SimpleCoffee();
        coffee = new MilkDecorator(coffee);
        coffee = new SugarDecorator(coffee);

        System.out.println(coffee.getDescription()); // Simple Coffee, Milk, Sugar
        System.out.println("₹" + coffee.getCost());  // ₹45.0
    }
}
```

---

## 8. Facade Pattern

**Purpose:** Provide a **simple unified interface** to a complex subsystem — hides internal complexity from the client.

**When to use:**
- Simplifying a complex library or framework
- Layered architecture (controller → service → repo)

```java
// Complex subsystems
class DVDPlayer {
    public void on()   { System.out.println("DVD Player ON"); }
    public void play() { System.out.println("DVD Playing"); }
    public void off()  { System.out.println("DVD Player OFF"); }
}

class Projector {
    public void on()  { System.out.println("Projector ON"); }
    public void off() { System.out.println("Projector OFF"); }
}

class SoundSystem {
    public void on()       { System.out.println("Sound System ON"); }
    public void setVolume(int vol) { System.out.println("Volume set to " + vol); }
    public void off()      { System.out.println("Sound System OFF"); }
}

// Facade — single simple interface for the client
class HomeTheaterFacade {
    private DVDPlayer dvd;
    private Projector projector;
    private SoundSystem sound;

    public HomeTheaterFacade() {
        this.dvd       = new DVDPlayer();
        this.projector = new Projector();
        this.sound     = new SoundSystem();
    }

    public void watchMovie() {
        System.out.println("--- Starting Movie ---");
        projector.on();
        sound.on();
        sound.setVolume(20);
        dvd.on();
        dvd.play();
    }

    public void endMovie() {
        System.out.println("--- Ending Movie ---");
        dvd.off();
        sound.off();
        projector.off();
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        HomeTheaterFacade theater = new HomeTheaterFacade();
        theater.watchMovie();
        theater.endMovie();
    }
}
```

---

## 9. Proxy Pattern

**Purpose:** Provide a **surrogate or placeholder** for another object to control access to it.

**When to use:**
- Lazy loading (virtual proxy)
- Access control (protection proxy)
- Logging, caching (smart proxy)

```java
interface Image {
    void display();
}

// Real object — expensive to create
class RealImage implements Image {
    private String filename;

    public RealImage(String filename) {
        this.filename = filename;
        loadFromDisk(); // Simulates expensive operation
    }

    private void loadFromDisk() {
        System.out.println("Loading image from disk: " + filename);
    }

    public void display() {
        System.out.println("Displaying: " + filename);
    }
}

// Proxy — controls access, loads lazily
class ProxyImage implements Image {
    private String filename;
    private RealImage realImage;

    public ProxyImage(String filename) {
        this.filename = filename;
    }

    public void display() {
        if (realImage == null) {
            realImage = new RealImage(filename); // Lazy load
        }
        realImage.display();
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Image image = new ProxyImage("photo.jpg");
        image.display(); // Loads from disk, then displays
        image.display(); // Just displays — no reload
    }
}
```

---

## 10. Composite Pattern

**Purpose:** Treat **individual objects and compositions** of objects uniformly — works as a tree structure.

**When to use:**
- File system (file vs folder)
- UI component hierarchies
- Organizational charts

```java
import java.util.*;

// Component
interface FileSystemComponent {
    void display(String indent);
}

// Leaf
class File implements FileSystemComponent {
    private String name;
    public File(String name) { this.name = name; }

    public void display(String indent) {
        System.out.println(indent + "📄 " + name);
    }
}

// Composite
class Folder implements FileSystemComponent {
    private String name;
    private List<FileSystemComponent> children = new ArrayList<>();

    public Folder(String name) { this.name = name; }

    public void add(FileSystemComponent component)    { children.add(component); }
    public void remove(FileSystemComponent component) { children.remove(component); }

    public void display(String indent) {
        System.out.println(indent + "📁 " + name);
        for (FileSystemComponent child : children) {
            child.display(indent + "  ");
        }
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Folder root = new Folder("root");
        root.add(new File("readme.txt"));

        Folder src = new Folder("src");
        src.add(new File("Main.java"));
        src.add(new File("App.java"));

        root.add(src);
        root.display("");
    }
}
```

**Output:**
```
📁 root
  📄 readme.txt
  📁 src
    📄 Main.java
    📄 App.java
```

---

# 🟣 Behavioral Patterns
> Deal with communication and responsibility between objects.

---

## 11. Observer Pattern

**Purpose:** When one object (Subject) changes state, all its **dependents (Observers) are automatically notified**.

**When to use:**
- Event listeners / UI frameworks
- Pub-Sub messaging
- Stock price updates, notifications

```java
import java.util.*;

interface Observer {
    void update(String event);
}

interface Subject {
    void subscribe(Observer o);
    void unsubscribe(Observer o);
    void notifyObservers(String event);
}

class EventManager implements Subject {
    private List<Observer> observers = new ArrayList<>();

    public void subscribe(Observer o)   { observers.add(o); }
    public void unsubscribe(Observer o) { observers.remove(o); }

    public void notifyObservers(String event) {
        for (Observer o : observers) {
            o.update(event);
        }
    }
}

class EmailNotifier implements Observer {
    public void update(String event) {
        System.out.println("Email sent for event: " + event);
    }
}

class SMSNotifier implements Observer {
    public void update(String event) {
        System.out.println("SMS sent for event: " + event);
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        EventManager manager = new EventManager();
        manager.subscribe(new EmailNotifier());
        manager.subscribe(new SMSNotifier());

        manager.notifyObservers("Order Placed");
        // Email sent for event: Order Placed
        // SMS sent for event: Order Placed
    }
}
```

---

## 12. Strategy Pattern

**Purpose:** Define a family of algorithms, encapsulate each one, and make them **interchangeable at runtime**.

**When to use:**
- Multiple sorting algorithms
- Different payment strategies
- Compression algorithms (zip, rar, gzip)

```java
// Strategy interface
interface SortStrategy {
    void sort(int[] arr);
}

class BubbleSort implements SortStrategy {
    public void sort(int[] arr) {
        System.out.println("Sorting using Bubble Sort");
        // bubble sort logic
    }
}

class QuickSort implements SortStrategy {
    public void sort(int[] arr) {
        System.out.println("Sorting using Quick Sort");
        // quick sort logic
    }
}

// Context — uses a strategy
class Sorter {
    private SortStrategy strategy;

    public Sorter(SortStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(SortStrategy strategy) {
        this.strategy = strategy; // Swap at runtime
    }

    public void sort(int[] arr) {
        strategy.sort(arr);
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        int[] data = {5, 3, 8, 1};
        Sorter sorter = new Sorter(new BubbleSort());
        sorter.sort(data); // Sorting using Bubble Sort

        sorter.setStrategy(new QuickSort());
        sorter.sort(data); // Sorting using Quick Sort
    }
}
```

---

## 13. Command Pattern

**Purpose:** **Encapsulate a request as an object** — supports undo/redo, queuing, and logging of operations.

**When to use:**
- Undo/Redo functionality
- Task queuing
- Remote controls, transactional operations

```java
// Command interface
interface Command {
    void execute();
    void undo();
}

// Receiver
class Light {
    public void turnOn()  { System.out.println("Light is ON"); }
    public void turnOff() { System.out.println("Light is OFF"); }
}

// Concrete commands
class TurnOnCommand implements Command {
    private Light light;
    public TurnOnCommand(Light light) { this.light = light; }
    public void execute() { light.turnOn(); }
    public void undo()    { light.turnOff(); }
}

class TurnOffCommand implements Command {
    private Light light;
    public TurnOffCommand(Light light) { this.light = light; }
    public void execute() { light.turnOff(); }
    public void undo()    { light.turnOn(); }
}

// Invoker
class RemoteControl {
    private Command lastCommand;

    public void pressButton(Command command) {
        command.execute();
        lastCommand = command;
    }

    public void pressUndo() {
        if (lastCommand != null) lastCommand.undo();
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Light light = new Light();
        RemoteControl remote = new RemoteControl();

        remote.pressButton(new TurnOnCommand(light));  // Light is ON
        remote.pressUndo();                            // Light is OFF (undo)
    }
}
```

---

## 14. Iterator Pattern

**Purpose:** Provide a way to **sequentially access elements** of a collection without exposing its underlying structure.

**When to use:**
- Custom collection traversal
- Hiding internal representation of data structures

```java
import java.util.*;

// Java's Iterable is a built-in implementation of this pattern
// Custom example:

interface Iterator<T> {
    boolean hasNext();
    T next();
}

class NameCollection {
    private String[] names;
    private int size;

    public NameCollection(String[] names) {
        this.names = names;
        this.size = names.length;
    }

    public Iterator<String> getIterator() {
        return new NameIterator();
    }

    private class NameIterator implements Iterator<String> {
        private int index = 0;

        public boolean hasNext() { return index < size; }
        public String next()     { return hasNext() ? names[index++] : null; }
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        NameCollection collection = new NameCollection(new String[]{"Alice", "Bob", "Charlie"});
        Iterator<String> it = collection.getIterator();

        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }
}
```

> Java's `java.util.Iterator` and enhanced for-each loop (`for (T item : collection)`) are built on this pattern.

---

## 15. State Pattern

**Purpose:** Allow an object to **alter its behaviour when its internal state changes** — appears to change its class.

**When to use:**
- Traffic lights, vending machines
- Order lifecycle (Pending → Processing → Shipped → Delivered)
- Connection states (Connected, Disconnected, Connecting)

```java
// State interface
interface State {
    void handle(TrafficLight light);
}

// Concrete states
class RedState implements State {
    public void handle(TrafficLight light) {
        System.out.println("🔴 RED — Stop");
        light.setState(new GreenState());
    }
}

class GreenState implements State {
    public void handle(TrafficLight light) {
        System.out.println("🟢 GREEN — Go");
        light.setState(new YellowState());
    }
}

class YellowState implements State {
    public void handle(TrafficLight light) {
        System.out.println("🟡 YELLOW — Slow Down");
        light.setState(new RedState());
    }
}

// Context
class TrafficLight {
    private State currentState;

    public TrafficLight() { this.currentState = new RedState(); }

    public void setState(State state) { this.currentState = state; }

    public void change() { currentState.handle(this); }
}

// Usage
public class Main {
    public static void main(String[] args) {
        TrafficLight light = new TrafficLight();
        light.change(); // 🔴 RED — Stop
        light.change(); // 🟢 GREEN — Go
        light.change(); // 🟡 YELLOW — Slow Down
        light.change(); // 🔴 RED — Stop
    }
}
```

---

## 16. Template Method Pattern

**Purpose:** Define the **skeleton of an algorithm** in a base class, letting subclasses override specific steps without changing the overall structure.

**When to use:**
- Data parsing pipelines (parse → process → output)
- Game loops (initialize → play → finish)
- Report generation

```java
// Abstract class defines the template
abstract class DataProcessor {
    // Template method — defines the skeleton
    public final void process() {
        readData();
        processData();
        writeOutput();
    }

    abstract void readData();
    abstract void processData();

    void writeOutput() {
        System.out.println("Writing output to default destination");
    }
}

class CSVProcessor extends DataProcessor {
    void readData()    { System.out.println("Reading CSV file"); }
    void processData() { System.out.println("Processing CSV data"); }
}

class JSONProcessor extends DataProcessor {
    void readData()    { System.out.println("Reading JSON file"); }
    void processData() { System.out.println("Processing JSON data"); }

    @Override
    void writeOutput() { System.out.println("Writing output to JSON sink"); }
}

// Usage
public class Main {
    public static void main(String[] args) {
        DataProcessor csv = new CSVProcessor();
        csv.process();

        System.out.println("---");

        DataProcessor json = new JSONProcessor();
        json.process();
    }
}
```

> The `final` keyword on the template method prevents subclasses from changing the algorithm's structure.

---

## 17. Chain of Responsibility Pattern

**Purpose:** Pass a request along a **chain of handlers** — each handler decides to process it or pass it to the next.

**When to use:**
- Request filtering / middleware (Spring filters)
- Logging levels (DEBUG → INFO → ERROR)
- Approval workflows (Manager → Director → CEO)

```java
// Handler interface
abstract class ApprovalHandler {
    protected ApprovalHandler next;

    public ApprovalHandler setNext(ApprovalHandler next) {
        this.next = next;
        return next;
    }

    public abstract void handleRequest(double amount);
}

// Concrete handlers
class Manager extends ApprovalHandler {
    public void handleRequest(double amount) {
        if (amount <= 10_000) {
            System.out.println("Manager approved ₹" + amount);
        } else if (next != null) {
            next.handleRequest(amount);
        }
    }
}

class Director extends ApprovalHandler {
    public void handleRequest(double amount) {
        if (amount <= 50_000) {
            System.out.println("Director approved ₹" + amount);
        } else if (next != null) {
            next.handleRequest(amount);
        }
    }
}

class CEO extends ApprovalHandler {
    public void handleRequest(double amount) {
        if (amount <= 200_000) {
            System.out.println("CEO approved ₹" + amount);
        } else {
            System.out.println("Amount ₹" + amount + " exceeds approval limit");
        }
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        ApprovalHandler manager  = new Manager();
        ApprovalHandler director = new Director();
        ApprovalHandler ceo      = new CEO();

        // Build the chain: Manager → Director → CEO
        manager.setNext(director).setNext(ceo);

        manager.handleRequest(5_000);   // Manager approved ₹5000.0
        manager.handleRequest(30_000);  // Director approved ₹30000.0
        manager.handleRequest(150_000); // CEO approved ₹150000.0
        manager.handleRequest(300_000); // Amount exceeds approval limit
    }
}
```
