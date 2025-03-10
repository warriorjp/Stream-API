import java.io.InputStream;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class StreamAPI {
    public static void main(String[] args) {

        // Remove Duplicates from the List
        List<Integer> arr = Arrays.asList(12, 44, 76, 11, 8, 9, 9);
        List<Integer> temp = arr.stream().distinct().collect(Collectors.toList());

        // Find the first element in the list
        arr.stream().findFirst().ifPresent(System.out::println);

	//Sort Array     
	int[] numbers = {5, 2, 8, 1, 3};
        numbers = Arrays.stream(numbers)
                        .sorted() // Sorts in ascending order
                        .toArray();

        // Find duplicate elements in the list
        List<Integer> myList = Arrays.asList(10, 15, 8, 49, 15, 32, 8);
        Set<Integer> set1 = new HashSet<>();
        myList.stream().filter(n -> !set1.add(n)).forEach(System.out::println);

        // Count occurrences of each character in a string
        String str = "Better Butter";
        String ch = str.replace(" ", "");
        Map<String, Long> map = Arrays.stream(ch.split(""))
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        //Print non duplicate element
        String s="Better";
		HashSet set=new HashSet();
		Arrays.stream(s.split("")).filter(e->set.add(e))
		.forEach(System.out::print);

        // Find the first non-repeating character in a string
        String s = "AAHJAKTMJ";
        Map<String, Long> counts = Arrays.stream(s.split(""))
                .collect(Collectors.groupingBy(i -> i, Collectors.counting()));
        counts.forEach((element, count) -> {
            if (count == 1) {
                System.out.println("Non Repeating Character From String: " + element);
            }
        });

        // Find the nth highest number in the list
        int n = 3;
        int highest = arr.stream().sorted(Collections.reverseOrder())
                .collect(Collectors.toList()).get(n - 1);

        // Find words starting with 't' in a sentence
        String n1 = "hi guys welcome to The team";
        Arrays.stream(n1.toLowerCase().split(" ")).filter(e -> e.startsWith("t")).forEach(System.out::println);

        // Count employees in each department
        List<Employe> emp = new ArrayList<>();
        emp.add(new Employe("Jay", 24, 25000, "IT", new Date(2009, 1, 22)));
        emp.add(new Employe("Rehman", 45, 55000, "Testing", new Date(2021, 1, 2)));
        emp.add(new Employe("Rajan", 34, 120000, "Production", new Date(2012, 11, 12)));
        emp.add(new Employe("Sagar", 46, 320000, "Testing", new Date(2016, 3, 2)));
        emp.add(new Employe("Abhijit", 26, 50000, "IT", new Date(2010, 6, 23)));

        Map<String, Long> re = emp.stream().collect(Collectors.groupingBy(Employe::getDept, Collectors.counting()));

        // Sum of all salaries
        Integer totalSalary = emp.stream().collect(Collectors.summingInt(Employe::getSalary));

        // Find max salary in each department
        Map<String, Employe> maxEmp = emp.stream().collect(Collectors.toMap(
                Employe::getDept, e -> e, BinaryOperator.maxBy(Comparator.comparingInt(Employe::getSalary))));

        // Find numbers that start with 1
        List<Integer> lsDigit = Arrays.asList(11, 20, 45, 60, 10, 16, 100);
        List<String> strings = lsDigit.stream().map(Object::toString).collect(Collectors.toList());
        strings.stream().filter(s -> s.startsWith("1")).forEach(System.out::println);

        // Find employees whose names start with 'R'
        emp.stream().filter(e -> e.getName().startsWith("R")).forEach(e -> System.out.println(e.getName()));

        // Count employees older than 30
        long countOlderThan30 = emp.stream().filter(e -> e.getAge() > 30).count();

        // Concatenate list of strings
        List<String> words = Arrays.asList("Java", "8", "String", "Concatenation");
        String result = String.join(" ", words);

        // Find most repetitive element in a list
        List<Integer> arrList = new ArrayList<>(Arrays.asList(3, 7, 5, 1, 3, 6, 7, 7));
        Map.Entry<Integer, Long> mostRepeated = arrList.stream()
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).get();

        // Sort employees by salary (ascending and descending)
        emp.stream().sorted(Comparator.comparingInt(Employe::getSalary)).forEach(e -> System.out.print("-" + e.getSalary()));
        emp.stream().sorted(Comparator.comparingInt(Employe::getSalary).reversed()).forEach(e -> System.out.println("-" + e.getSalary()));

        // Increase salary of employees older than 25
        emp.stream().map(e -> {
            if (e.getAge() > 25) {
                e.setSalary(e.getSalary() + 10000);
            }
            return e;
        }).forEach(e1 -> System.out.print(e1.getName() + " <::>" + e1.getSalary()));

        // Find vowels in a string
        String demo = "Java Is Good For APP Development";
        List<String> vowels = Arrays.asList("a", "i", "o", "u", "e");
        Arrays.stream(demo.replace(" ", "").toLowerCase().split(""))
                .filter(vowels::contains).forEach(System.out::println);

        // Find cube of odd numbers
        List<Integer> listOfNumbers = Arrays.asList(1, 2, 3, 4, 5, 6, 12, 18);
        listOfNumbers.stream().filter(i -> i % 2 != 0).forEach(e -> System.out.print("-" + e * e * e));

        // Find top 3 highest salaries
        emp.stream().sorted(Comparator.comparing(Employe::getSalary).reversed()).limit(3).forEach(ss -> System.out.println("<>" + ss.getSalary()));

        // Find duplicate characters in a string
        String dump = "Characters Duplicate";
        HashSet<String> hashSet = new HashSet<>();
        Arrays.stream(dump.replaceAll(" ", "").toLowerCase().split(""))
                .filter(x -> !hashSet.add(x)).forEach(System.out::println);

        // Use Streams to filter common elements
        Integer[] array1 = {1, 2, 3, 4, 5, 6};
        Integer[] array2 = {4, 5, 6, 7, 8, 9};

        List<Integer> list = Arrays.asList(array2);
        
        List<Integer> commonElements = Arrays.stream(array1)
                .filter(list::contains) // Check if element exists in list
                .collect(Collectors.toList());
    }
}
