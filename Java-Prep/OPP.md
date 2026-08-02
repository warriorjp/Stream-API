# Object-Oriented Programming (OOP) Concepts in Java

Java follows four main Object-Oriented Programming (OOP) principles:

- Encapsulation
- Abstraction
- Inheritance
- Polymorphism

---

# 1. Abstraction

## Definition

**Abstraction** is the process of hiding the implementation details and exposing only the essential functionality to the user. It allows the user to know **what an object does**, without knowing **how it does it**.

Abstraction can be achieved using:
- Abstract Classes
- Interfaces

## Example

```java
abstract class Animal {

    abstract void makeSound();

    public void display() {
        System.out.println("Animal Class");
    }
}

class Dog extends Animal {

    @Override
    public void makeSound() {
        System.out.println("Dog Barks");
    }

    @Override
    public void display() {
        System.out.println("Dog Class");
    }
}

public class Main {

    public static void main(String[] args) {

        Animal obj = new Dog();

        obj.makeSound();
        obj.display();
    }
}
```

### Output

```
Dog Barks
Dog Class
```

### Real-world Example

Driving a car.

You use:
- Steering
- Brake
- Accelerator

You don't need to know how the engine, gearbox, or fuel injection system works internally.

---

# 2. Encapsulation

## Definition

**Encapsulation** is the process of wrapping data (variables) and methods (functions) into a single class while restricting direct access to the data using the `private` access modifier.

Data is accessed through public getter and setter methods.

## Example

```java
class Student {

    private String name;
    private int rollNo;

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public int getRollNo() {
        return rollNo;
    }

    public void setRollNo(int newRollNo) {
        this.rollNo = newRollNo;
    }
}

public class Main {

    public static void main(String[] args) {

        Student obj = new Student();

        obj.setName("ABC");
        obj.setRollNo(12);

        System.out.println("Name : " + obj.getName());
        System.out.println("Roll No : " + obj.getRollNo());
    }
}
```

### Output

```
Name : ABC
Roll No : 12
```

### Why Encapsulation?

- Data hiding
- Controlled access to variables
- Validation before updating data
- Improved security and maintainability

### Real-world Example

ATM Machine

You cannot directly change your account balance.

Instead, you perform operations like:
- Deposit
- Withdraw
- Check Balance

The balance remains protected inside the system.

---

# 3. Polymorphism

## Definition

**Polymorphism** means **one interface, multiple implementations**.

The same method can behave differently depending on the object that invokes it.

There are two types of polymorphism:

- Compile-time Polymorphism (Method Overloading)
- Runtime Polymorphism (Method Overriding)

---

## 3.1 Compile-time Polymorphism (Method Overloading)

### Definition

Method Overloading occurs when multiple methods have:
- Same method name
- Different number or type of parameters
- Same class

The compiler decides which method to invoke.

### Example

```java
class Calculator {

    public int calculate(int a, int b) {
        return a + b;
    }

    public int calculate(int a, int b, int c) {
        return a + b + c;
    }
}

public class Main {

    public static void main(String[] args) {

        Calculator calc = new Calculator();

        System.out.println(calc.calculate(10, 20));
        System.out.println(calc.calculate(20, 10, 30));
    }
}
```

### Output

```
30
60
```

---

## 3.2 Runtime Polymorphism (Method Overriding)

### Definition

Method Overriding occurs when:
- Parent and child classes have the same method
- Same method name
- Same parameters
- Java decides which implementation to execute at runtime based on the object.

### Example

```java
class Animal {

    public void sound() {
        System.out.println("Animal makes a sound");
    }
}

class Dog extends Animal {

    @Override
    public void sound() {
        System.out.println("Dog Barks");
    }
}

class Cat extends Animal {

    @Override
    public void sound() {
        System.out.println("Cat Meows");
    }
}

public class Main {

    public static void main(String[] args) {

        Animal a1 = new Dog();
        Animal a2 = new Cat();

        a1.sound();
        a2.sound();
    }
}
```

### Output

```
Dog Barks
Cat Meows
```

### Real-world Example

A payment application.

```java
Payment payment = new CreditCardPayment();
payment.pay();

payment = new UpiPayment();
payment.pay();
```

The same `pay()` method behaves differently depending on the object.

---

# 4. Inheritance

## Definition

**Inheritance** is the process by which one class acquires the properties and behaviors of another class using the `extends` keyword.

It promotes code reuse and establishes an **IS-A** relationship.

## Example

```java
class Animal {

    public void eat() {
        System.out.println("Animal is eating");
    }
}

class Dog extends Animal {

    public void bark() {
        System.out.println("Dog is barking");
    }
}

public class Main {

    public static void main(String[] args) {

        Dog dog = new Dog();

        dog.eat();
        dog.bark();
    }
}
```

### Output

```
Animal is eating
Dog is barking
```

### Real-world Example

```
Animal
   ↑
 Dog
```

A Dog **is an** Animal.

---

# Difference Between Abstraction and Encapsulation

| Abstraction | Encapsulation |
|-------------|---------------|
| Hides implementation details | Hides data |
| Focuses on **what** an object does | Focuses on **how** data is protected |
| Achieved using Abstract Classes and Interfaces | Achieved using private fields and getters/setters |
| Reduces complexity | Improves security and maintainability |
| Example: Car driving | Example: ATM |

---

# Interview One-liners

### Encapsulation

> Encapsulation is the process of wrapping data and methods into a single class while restricting direct access to data using private variables and exposing it through getters and setters.

### Abstraction

> Abstraction is the process of hiding implementation details and exposing only the essential functionality to the user.

### Inheritance

> Inheritance allows one class to inherit the properties and methods of another class, promoting code reuse.

### Polymorphism

> Polymorphism allows the same method to perform different behaviors depending on the object that invokes it.
