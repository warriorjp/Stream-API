import java.util.Date;

public class Employe {
   private String name;
   private Integer age;
   private Integer salary;
   private String dept;
   private Date doj;
   
   public String getName() {
	return name;    
}
public void setName(String name) {
	this.name = name;
}
public Integer getAge() {
	return age;
}
public void setAge(Integer age) {
	this.age = age;
}
public Integer getSalary() {
	return salary;
}
public void setSalary(Integer salary) {
	this.salary = salary;
}
public String getDept() {
	return dept;
}
public void setDept(String dept) {
	this.dept = dept;
}
public Date getDoj() {
	return doj;
}
public void setDoj(Date doj) {
	this.doj = doj;
}
public Employe(String name, Integer age, Integer salary, String dept, Date doj) {
	super();
	this.name = name;
	this.age = age;
	this.salary = salary;
	this.dept = dept;
	this.doj = doj;
}

}

