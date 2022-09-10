import java.util.ArrayList;
import java.util.*;
import java.lang.*;

class Student implements Comparable<Student> {
	private int id;
	private String name;
	private int marks;

	public Student(int id, String name, int marks) {
		super();
		this.id = id;
		this.name = name;
		this.marks = marks;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMarks() {
		return marks;
	}

	public void setMarks(int marks) {
		this.marks = marks;
	}

	@Override
	public int compareTo(Student s) {
		if (marks == s.marks) {
			return 0;
		} else if (marks > s.marks) {
			return 1;
		} else {
			return -1;
		}
	}

}

public class ComparableExample {
	public static void main(String[] args) {
		ArrayList<Student> students = new ArrayList<Student>();
		students.add(new Student(11, "Abhijit", 88));
		students.add(new Student(12, "Suraj", 78));
		students.add(new Student(13, "Pranav", 68));
		students.add(new Student(15, "Bhushan", 98));
     // students.stream().sorted(Comparator.comparing(Student::getMarks).reversed()).forEach(s->System.out.print(" "+s.getMarks()));
		Collections.sort(students);
		students.forEach(s -> System.out.println(s.getMarks()));
		
	}
}
