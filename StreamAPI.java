// Stream API Questions and Answers

// ✅ 1. Find the first element in a list
arr.stream().findFirst().ifPresent(System.out::println);

// ✅ 2. Sort an array
int[] numbers = {5, 2, 8, 1, 3};
numbers = Arrays.stream(numbers).sorted().toArray();

// ✅ 3. Remove duplicates from a list
List<Integer> arr = Arrays.asList(12, 44, 76, 11, 8, 9, 9);
List<Integer> temp = arr.stream().distinct().collect(Collectors.toList());

// ✅ 4. Find duplicate elements in a list
List<Integer> myList = Arrays.asList(10, 15, 8, 49, 15, 32, 8);
Set<Integer> set1 = new HashSet<>();
myList.stream().filter(n -> !set1.add(n)).forEach(System.out::println);

// ✅ 5. Keep 0s on left, 1s on right
List<Integer> list = List.of(1, 0, 1, 0, 1, 0, 0, 1, 0, 1);
List<Integer> sortedList = list.stream().sorted().collect(Collectors.toList());

// ✅ 6. Count occurrences of each character in a string
String str = "Better Butter";
String ch = str.replace(" ", "");
Map<String, Long> map = Arrays.stream(ch.split(""))
        .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

// ✅ 7. Divide a list into even and odd
List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
Map<Boolean, List<Integer>> result = nums.stream()
        .collect(Collectors.partitioningBy(n -> n % 2 == 0));

// ✅ 8. Print non-duplicate characters in a string
String s = "Better";
HashSet<String> set = new HashSet<>();
Arrays.stream(s.split(""))
      .filter(set::add)
      .forEach(System.out::print);

// ✅ 9. First non-repeating character in a string
String str1 = "AAHJAKTMJ";
LinkedHashMap<String, Long> map1 = Arrays.stream(str1.split(""))
        .collect(Collectors.groupingBy(c -> c, LinkedHashMap::new, Collectors.counting()));
map1.entrySet().stream().filter(e -> e.getValue() == 1).findFirst().ifPresent(e -> System.out.println(e.getKey()));

// ✅ 10. Concatenate list of strings
List<String> words = Arrays.asList("Java", "8", "String", "Concatenation");
String resultStr = String.join(" ", words);

// ✅ 11. Most repetitive element in a string
String str2 = "Hello Jay";
Map<String, Long> hm = Arrays.stream(str2.toLowerCase().replace(" ", "").split(""))
        .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
hm.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(System.out::println);

// ✅ 12. Find 2nd highest number in array
int[] arr2 = {22, 55, 66, 11, 77, 33};
int n = 2;
Integer secondHighest = Arrays.stream(arr2).boxed().sorted(Collections.reverseOrder()).skip(n - 1).findFirst().orElse(null);

// ✅ 13. Find words starting with 't'
String sentence = "hi guys welcome to The team";
Arrays.stream(sentence.toLowerCase().split(" ")).filter(e -> e.startsWith("t")).forEach(System.out::println);

// ✅ 14. Find numbers starting with 1
List<Integer> digits = Arrays.asList(11, 20, 45, 60, 10, 16, 100);
List<String> digitStrings = digits.stream().map(Object::toString).collect(Collectors.toList());
digitStrings.stream().filter(s1 -> s1.startsWith("1")).forEach(System.out::println);

// ✅ 15. Find vowels in a string
String demo = "Java Is Good For APP Development";
List<String> vowels = List.of("a", "e", "i", "o", "u");
Arrays.stream(demo.replace(" ", "").toLowerCase().split(""))
        .filter(vowels::contains).forEach(System.out::println);

// ✅ 16. Find cube of odd numbers
List<Integer> listOfNumbers = Arrays.asList(1, 2, 3, 4, 5, 6, 12, 18);
listOfNumbers.stream().filter(i -> i % 2 != 0).forEach(i -> System.out.print("-" + i * i * i));

// ✅ 17. Find duplicate characters in a string
String dump = "Characters Duplicate";
HashSet<String> hashSet = new HashSet<>();
Arrays.stream(dump.replace(" ", "").toLowerCase().split(""))
        .filter(c -> !hashSet.add(c)).forEach(System.out::println);

// ✅ 18. Filter common elements from two arrays
Integer[] array1 = {1, 2, 3, 4, 5, 6};
Integer[] array2 = {4, 5, 6, 7, 8, 9};
List<Integer> list2 = Arrays.asList(array2);
List<Integer> common = Arrays.stream(array1).filter(list2::contains).collect(Collectors.toList());

// ✅ 19. Employee-Based Stream Operations
List<Employe> emp = new ArrayList<>();
// Add employee objects here...

// ✅ Sort employees by dept and age
List<Employe> empList = emp.stream()
        .sorted(Comparator.comparing(Employe::getDept).thenComparing(Employe::getAge))
        .collect(Collectors.toList());

// ✅ Count employees in each department
Map<String, Long> deptCount = emp.stream()
        .collect(Collectors.groupingBy(Employe::getDept, Collectors.counting()));

// ✅ Get employee with max salary
emp.stream().max(Comparator.comparing(Employe::getSalary)).ifPresent(e -> System.out.println(e.getName()));

// ✅ Sum of all salaries
int totalSalary = emp.stream().collect(Collectors.summingInt(Employe::getSalary));

// ✅ Max salary in each department
Map<String, Employe> maxSalaryEmp = emp.stream()
        .collect(Collectors.toMap(Employe::getDept, e -> e,
                BinaryOperator.maxBy(Comparator.comparingInt(Employe::getSalary))));

// ✅ Average salary by department
Map<String, Double> avgSalary = emp.stream()
        .collect(Collectors.groupingBy(Employe::getDept, Collectors.averagingDouble(Employe::getSalary)));

// ✅ Increase salary by 10%
emp.forEach(e -> e.setSalary(e.getSalary() * 1.10));

// ✅ Find employees whose names start with 'R'
emp.stream().filter(e -> e.getName().startsWith("R")).forEach(e -> System.out.println(e.getName()));

// ✅ Count employees older than 30
long olderThan30 = emp.stream().filter(e -> e.getAge() > 30).count();

// ✅ Sort employees by salary ascending and descending
emp.stream().sorted(Comparator.comparingInt(Employe::getSalary)).forEach(e -> System.out.print("-" + e.getSalary()));
emp.stream().sorted(Comparator.comparingInt(Employe::getSalary).reversed()).forEach(e -> System.out.println("-" + e.getSalary()));

// ✅ Increase salary for employees older than 25
emp.stream().map(e -> {
    if (e.getAge() > 25) e.setSalary(e.getSalary() + 10000);
    return e;
}).forEach(e -> System.out.println(e.getName() + " <::> " + e.getSalary()));

// ✅ Top 3 highest salaries
emp.stream().sorted(Comparator.comparing(Employe::getSalary).reversed()).limit(3).forEach(e -> System.out.println("<>" + e.getSalary()));
