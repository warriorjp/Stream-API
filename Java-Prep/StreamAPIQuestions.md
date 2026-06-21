# 🌊 Java Stream API — Tricky Questions & Answers

A curated collection of practical Stream API problems with clean solutions and explanations.

---

## Table of Contents

**String & Character Operations**

1. [Count Occurrences of Each Character](#6-count-occurrences-of-each-character-in-a-string)

2. [First Non-Repeating Character](#9-first-non-repeating-character-in-a-string)

3. [Most Repetitive Element in a String](#11-most-repetitive-element-in-a-string)

4. [Print Non-Duplicate Characters](#8-print-non-duplicate-characters-in-a-string)

5. [Find Duplicate Characters](#17-find-duplicate-characters-in-a-string)

6. [Find Vowels in a String](#15-find-vowels-in-a-string)

7. [Concatenate List of Strings](#10-concatenate-list-of-strings)

**List & Array Operations**

8. [Find First Element](#1-find-the-first-element-in-a-list)

9. [Sort an Array](#2-sort-an-array)

10. [Remove Duplicates](#3-remove-duplicates-from-a-list)

11. [Find Duplicate Elements](#4-find-duplicate-elements-in-a-list)

12. [Keep 0s Left, 1s Right](#5-keep-0s-on-left-1s-on-right)

13. [Divide Into Even and Odd](#7-divide-a-list-into-even-and-odd)

14. [Find 2nd Highest Number](#12-find-nth-highest-number-in-an-array)

15. [Find Numbers Starting With 1](#14-find-numbers-starting-with-1)

16. [Cube of Odd Numbers](#16-find-cube-of-odd-numbers)

17. [Filter Common Elements from Two Arrays](#18-filter-common-elements-from-two-arrays)

18. [Find Words Starting With a Letter](#13-find-words-starting-with-a-specific-letter)

**Employee Stream Operations**

19. [Sort Employees by Dept & Age](#19-sort-employees-by-dept-and-age)

20. [Count Employees Per Department](#20-count-employees-in-each-department)

21. [Employee With Max Salary](#21-employee-with-max-salary)

22. [Sum of All Salaries](#22-sum-of-all-salaries)

23. [Max Salary Per Department](#23-max-salary-per-department)

24. [Average Salary by Department](#24-average-salary-by-department)

25. [Increase Salary by 10%](#25-increase-all-salaries-by-10)

26. [Filter Employees by Name](#26-find-employees-whose-names-start-with-r)

27. [Count Employees Older Than 30](#27-count-employees-older-than-30)

28. [Sort by Salary Asc & Desc](#28-sort-employees-by-salary-ascending-and-descending)

29. [Raise Salary for Employees Over 25](#29-increase-salary-for-employees-older-than-25)

30. [Top 3 Highest Salaries](#30-top-3-highest-salaries)

---

## Employee Model (used in Section 3)

```java
class Employee {
    private String name;
    private String dept;
    private int age;
    private double salary;

    public Employee(String name, String dept, int age, double salary) {
        this.name = name;
        this.dept = dept;
        this.age = age;
        this.salary = salary;
    }

    public String getName()       { return name; }
    public String getDept()       { return dept; }
    public int getAge()           { return age; }
    public double getSalary()     { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
}
```

---

## Section 1 — String & Character Operations

---

### 6. Count Occurrences of Each Character in a String

```java
String str = "Better Butter";
String cleaned = str.replace(" ", "");

Map<String, Long> charCount = Arrays.stream(cleaned.split(""))
        .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

System.out.println(charCount);
// {B=1, e=3, t=4, r=2, u=1}
```

> `groupingBy` groups elements by key; `counting()` is the downstream collector that counts each group.

---

### 9. First Non-Repeating Character in a String

```java
String str = "AAHJAKTMJ";

LinkedHashMap<String, Long> charCount = Arrays.stream(str.split(""))
        .collect(Collectors.groupingBy(c -> c, LinkedHashMap::new, Collectors.counting()));

charCount.entrySet().stream()
        .filter(e -> e.getValue() == 1)
        .findFirst()
        .ifPresent(e -> System.out.println(e.getKey())); // H
```

> `LinkedHashMap::new` preserves **insertion order** — critical here so we get the *first* non-repeating character, not just any.

---

### 11. Most Repetitive Element in a String

```java
String str = "Hello Jay";

Map<String, Long> charCount = Arrays.stream(
        str.toLowerCase().replace(" ", "").split(""))
        .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

charCount.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .ifPresent(System.out::println); // l=2
```

---

### 8. Print Non-Duplicate Characters in a String

```java
String s = "Better";
Set<String> seen = new HashSet<>();

Arrays.stream(s.split(""))
      .filter(seen::add)     // add() returns false if already present → filter keeps first occurrences only
      .forEach(System.out::print); // B e t r
```

> `set::add` returns `true` only on the first insertion — an elegant way to filter non-duplicates without extra logic.

---

### 17. Find Duplicate Characters in a String

```java
String str = "Characters Duplicate";
Set<String> seen = new HashSet<>();

Arrays.stream(str.replace(" ", "").toLowerCase().split(""))
      .filter(c -> !seen.add(c))   // !add() → true only when already present
      .forEach(System.out::println);
```

---

### 15. Find Vowels in a String

```java
String sentence = "Java Is Good For APP Development";
List<String> vowels = List.of("a", "e", "i", "o", "u");

Arrays.stream(sentence.replace(" ", "").toLowerCase().split(""))
      .filter(vowels::contains)
      .forEach(System.out::println);
```

---

### 10. Concatenate List of Strings

```java
List<String> words = Arrays.asList("Java", "8", "Stream", "Concatenation");

// Option 1 — String.join (simplest)
String result1 = String.join(" ", words); // "Java 8 Stream Concatenation"

// Option 2 — Stream with Collectors.joining
String result2 = words.stream()
        .collect(Collectors.joining(" ")); // same result
```

> `Collectors.joining(delimiter, prefix, suffix)` also accepts optional prefix/suffix — useful for formatting.

---

## Section 2 — List & Array Operations

---

### 1. Find the First Element in a List

```java
List<Integer> arr = Arrays.asList(5, 3, 9, 1, 4);

arr.stream()
   .findFirst()
   .ifPresent(System.out::println); // 5
```

---

### 2. Sort an Array

```java
int[] numbers = {5, 2, 8, 1, 3};
numbers = Arrays.stream(numbers).sorted().toArray();
// [1, 2, 3, 5, 8]
```

---

### 3. Remove Duplicates from a List

```java
List<Integer> arr = Arrays.asList(12, 44, 76, 11, 8, 9, 9);

List<Integer> unique = arr.stream()
        .distinct()
        .collect(Collectors.toList());
// [12, 44, 76, 11, 8, 9]
```

---

### 4. Find Duplicate Elements in a List

```java
List<Integer> myList = Arrays.asList(10, 15, 8, 49, 15, 32, 8);
Set<Integer> seen = new HashSet<>();

myList.stream()
      .filter(n -> !seen.add(n))   // false on second insert = duplicate
      .forEach(System.out::println); // 15, 8
```

---

### 5. Keep 0s on Left, 1s on Right

```java
List<Integer> list = List.of(1, 0, 1, 0, 1, 0, 0, 1, 0, 1);

List<Integer> sorted = list.stream()
        .sorted()
        .collect(Collectors.toList());
// [0, 0, 0, 0, 0, 1, 1, 1, 1, 1]
```

---

### 7. Divide a List into Even and Odd

```java
List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

Map<Boolean, List<Integer>> result = nums.stream()
        .collect(Collectors.partitioningBy(n -> n % 2 == 0));

System.out.println("Even: " + result.get(true));
System.out.println("Odd:  " + result.get(false));
```

> `partitioningBy` is a special case of `groupingBy` that always produces exactly two groups: `true` and `false`.

---

### 12. Find Nth Highest Number in an Array

```java
int[] arr = {22, 55, 66, 11, 77, 33};
int n = 2; // find 2nd highest

Integer nthHighest = Arrays.stream(arr)
        .boxed()
        .sorted(Collections.reverseOrder())
        .skip(n - 1)       // skip top (n-1) elements
        .findFirst()
        .orElse(null);

System.out.println(nthHighest); // 66
```

> Change `n` to find any Nth highest — works generically.

---

### 14. Find Numbers Starting With 1

```java
List<Integer> digits = Arrays.asList(11, 20, 45, 60, 10, 16, 100);

digits.stream()
      .map(Object::toString)
      .filter(s -> s.startsWith("1"))
      .forEach(System.out::println);
// 11, 10, 16, 100
```

---

### 16. Find Cube of Odd Numbers

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 12, 18);

numbers.stream()
       .filter(i -> i % 2 != 0)
       .map(i -> i * i * i)
       .forEach(cube -> System.out.print(cube + " "));
// 1 27 125
```

---

### 18. Filter Common Elements from Two Arrays

```java
Integer[] array1 = {1, 2, 3, 4, 5, 6};
Integer[] array2 = {4, 5, 6, 7, 8, 9};

Set<Integer> set2 = new HashSet<>(Arrays.asList(array2)); // O(1) lookup

List<Integer> common = Arrays.stream(array1)
        .filter(set2::contains)
        .collect(Collectors.toList());

System.out.println(common); // [4, 5, 6]
```

> Using a `Set` for the second array gives **O(1)** lookup vs `List::contains` which is **O(n)** — better performance.

---

### 13. Find Words Starting With a Specific Letter

```java
String sentence = "hi guys welcome to the team";

Arrays.stream(sentence.toLowerCase().split(" "))
      .filter(word -> word.startsWith("t"))
      .forEach(System.out::println);
// to, the, team
```

---

## Section 3 — Employee Stream Operations

Sample data setup:

```java
List<Employee> emp = Arrays.asList(
    new Employee("Ravi",   "IT",  28, 60000),
    new Employee("Meena",  "HR",  35, 45000),
    new Employee("Raj",    "IT",  32, 75000),
    new Employee("Priya",  "HR",  27, 50000),
    new Employee("Rohan",  "IT",  25, 55000),
    new Employee("Sneha",  "Finance", 30, 80000)
);
```

---

### 19. Sort Employees by Dept and Age

```java
List<Employee> sorted = emp.stream()
        .sorted(Comparator.comparing(Employee::getDept)
                          .thenComparing(Employee::getAge))
        .collect(Collectors.toList());
```

---

### 20. Count Employees in Each Department

```java
Map<String, Long> deptCount = emp.stream()
        .collect(Collectors.groupingBy(Employee::getDept, Collectors.counting()));

// {IT=3, HR=2, Finance=1}
```

---

### 21. Employee With Max Salary

```java
emp.stream()
   .max(Comparator.comparingDouble(Employee::getSalary))
   .ifPresent(e -> System.out.println(e.getName())); // Sneha
```

---

### 22. Sum of All Salaries

```java
double totalSalary = emp.stream()
        .collect(Collectors.summingDouble(Employee::getSalary));

// Or using reduce:
double totalSalary2 = emp.stream()
        .mapToDouble(Employee::getSalary)
        .sum();
```

---

### 23. Max Salary Per Department

```java
Map<String, Optional<Employee>> maxSalaryPerDept = emp.stream()
        .collect(Collectors.groupingBy(
                Employee::getDept,
                Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary))
        ));

maxSalaryPerDept.forEach((dept, e) ->
        e.ifPresent(employee ->
                System.out.println(dept + " → " + employee.getName() + " : " + employee.getSalary())));
```

---

### 24. Average Salary by Department

```java
Map<String, Double> avgSalary = emp.stream()
        .collect(Collectors.groupingBy(
                Employee::getDept,
                Collectors.averagingDouble(Employee::getSalary)
        ));

// {IT=63333.33, HR=47500.0, Finance=80000.0}
```

---

### 25. Increase All Salaries by 10%

```java
emp.forEach(e -> e.setSalary(e.getSalary() * 1.10));
```

> Avoid doing this inside a `stream().map()` — mutating objects inside streams is a side effect and goes against functional programming principles. Use `forEach` on the list directly.

---

### 26. Find Employees Whose Names Start With 'R'

```java
emp.stream()
   .filter(e -> e.getName().startsWith("R"))
   .map(Employee::getName)
   .forEach(System.out::println);
// Ravi, Raj, Rohan
```

---

### 27. Count Employees Older Than 30

```java
long count = emp.stream()
        .filter(e -> e.getAge() > 30)
        .count();

System.out.println("Employees older than 30: " + count); // 2
```

---

### 28. Sort Employees by Salary Ascending and Descending

```java
// Ascending
emp.stream()
   .sorted(Comparator.comparingDouble(Employee::getSalary))
   .forEach(e -> System.out.println(e.getName() + " : " + e.getSalary()));

System.out.println("---");

// Descending
emp.stream()
   .sorted(Comparator.comparingDouble(Employee::getSalary).reversed())
   .forEach(e -> System.out.println(e.getName() + " : " + e.getSalary()));
```

---

### 29. Increase Salary for Employees Older Than 25

```java
emp.stream()
   .filter(e -> e.getAge() > 25)
   .forEach(e -> e.setSalary(e.getSalary() + 10000));

emp.forEach(e -> System.out.println(e.getName() + " → " + e.getSalary()));
```

---

### 30. Top 3 Highest Salaries

```java
emp.stream()
   .sorted(Comparator.comparingDouble(Employee::getSalary).reversed())
   .limit(3)
   .forEach(e -> System.out.println(e.getName() + " : " + e.getSalary()));
// Sneha : 80000, Raj : 75000, Ravi : 60000
```

---

## Quick Reference — Common Stream Operations

| Operation | Method | Type |
|---|---|---|

| Filter elements | `filter(predicate)` | Intermediate |

| Transform elements | `map(function)` | Intermediate |

| Remove duplicates | `distinct()` | Intermediate |

| Sort | `sorted()` / `sorted(comparator)` | Intermediate |

| Limit results | `limit(n)` | Intermediate |

| Skip elements | `skip(n)` | Intermediate |

| Collect to List | `collect(Collectors.toList())` | Terminal |

| Group elements | `collect(Collectors.groupingBy(...))` | Terminal |

| Partition (true/false) | `collect(Collectors.partitioningBy(...))` | Terminal |

| Count | `count()` | Terminal |

| Find first | `findFirst()` | Terminal |

| Max / Min | `max(comparator)` / `min(comparator)` | Terminal |

| Sum | `mapToInt/Double/Long(...).sum()` | Terminal |

| Iterate | `forEach(consumer)` | Terminal |

---
