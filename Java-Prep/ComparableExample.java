import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class Student {

    private int id;
    private String name;
    private int marks;

    public Student(int id, String name, int marks) {
        this.id = id;
        this.name = name;
        this.marks = marks;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMarks() {
        return marks;
    }

    @Override
    public String toString() {
        return id + " " + name + " " + marks;
    }
}

// Sort by Name (Ascending)
class SortByName implements Comparator<Student> {

    @Override
    public int compare(Student s1, Student s2) {
        return s1.getName().compareTo(s2.getName());

        // Descending
        // return s2.getName().compareTo(s1.getName());
    }
}

// Sort by Marks (Descending)
class SortByMark implements Comparator<Student> {

    @Override
    public int compare(Student s1, Student s2) {
        return Integer.compare(s2.getMarks(), s1.getMarks());

        // Ascending
        // return Integer.compare(s1.getMarks(), s2.getMarks());
    }
}

public class ComparatorExample {

    public static void main(String[] args) {

        ArrayList<Student> students = new ArrayList<>();

        students.add(new Student(11, "Abhijit", 88));
        students.add(new Student(12, "Suraj", 78));
        students.add(new Student(13, "Pranav", 68));
        students.add(new Student(15, "Bhushan", 98));

        // Sort by Name
        Collections.sort(students, new SortByName());

        System.out.println("Sorted by Name:");
        students.forEach(System.out::println);

        // Sort by Marks
        Collections.sort(students, new SortByMark());

        System.out.println("\nSorted by Marks:");
        students.forEach(System.out::println);
    }
}
