import java.util.ArrayList;
import java.util.*;
import java.lang.*;

class Students {
	private int id;
	private String name;
	private int marks;

	public Students(int id, String name, int marks) {
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

}

class SortByName implements Comparator<Student>{

	@Override
	public int compare(Student s1, Student s2) {
	return s1.getName().compareTo(s2.getName());
//	return s2.getName().compareTo(s1.getName());      //Descending Order
	}
	
}

class SortbyMark implements Comparator<Student> {

	@Override
    public int compare(Student s1, Student s2)
    {
 
       // return s1.getMarks() -s2.getMarks();
        return s2.getMarks() - s1.getMarks();    //For Descending Order
    }
}

public class ComaparatorExample  {
	public static void main(String[] args) {
		ArrayList<Student> students = new ArrayList<Student>();
		students.add(new Student(11, "Abhijit", 88));
		students.add(new Student(12, "Suraj", 78));
		students.add(new Student(13, "Pranav", 68));
		students.add(new Student(15, "Bhushan", 98));
				
		Collections.sort(students,new SortByName());
		students.forEach(s -> System.out.println(s.getName()));
		
		Collections.sort(students,new SortbyMark());
		students.forEach(s -> System.out.println(s.getMarks()));

	}
}


