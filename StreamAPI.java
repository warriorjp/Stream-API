import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;



public class StreamAPI {
	public static void main(String[] args) {

	System.out.println(">>>>>>>>>>>Remove Duplicate>>>>>>>>>>>>>>>>>>>>>>.");
        ArrayList<Integer> arr=new ArrayList<>(Arrays.asList(12,44,76,11,8,9,9));	
       List<Integer> temp=  arr.stream().distinct().collect(Collectors.toList());
        temp.forEach(s->System.out.println(s));
        
        System.out.println(">>>>>>>>>>>Find First>>>>>>>>>>>>>>>>>>>>>>.");
        arr.stream()
        .findFirst()
        .ifPresent(System.out::println);
        
        System.out.println(">>>>>>>>>>>>>>>>find duplicate element>>>>>>>>>>>>>>>>>.");
        List<Integer> myList = Arrays.asList(10,15,8,49,15,32,8);
        Set<Integer> set1 = new HashSet();
        myList.stream()  .filter(n -> !set1.add(n)) .forEach(System.out::println);
        
        System.out.println(">>>>>>>>>>>>>>>>>>return char with its count>>>>>>>>>>>>>>>.");
        //return char with its count
        String str = "Better Butter";
        String ch=String.valueOf(str.replace(" ",""));
        Map<String,Long> map=Arrays.stream(ch.split("")).collect(Collectors.groupingBy(c->c,Collectors.counting()));
        map.forEach((k,v)->System.out.println(k+"  "+v));

       System.out.println(">>>>>>>>>>>>>>>>>>non repeating character from string >>>>>>>>>>>>>>>.");
       String s="AAHJAKTMJ";
       Map<String, Long> counts = Arrays.stream(s.split(""))
            .collect(Collectors.groupingBy(i -> i, Collectors.counting()));

      // Display repeating elements and their counts
       counts.forEach((element, count) -> {
        if (count == 1) {
            System.out.println("Non Repeating Character From String: " +element);
        }
        });
        
        System.out.println(">>>>>>>>>>>>>>return nth highest>>>>>>>>>>>>>>>>>>>.");
        int n=3;
        int highest=arr.stream().sorted(Collections.reverseOrder())
        		.collect(Collectors.toList()).get(n-1);
        System.out.println("nth highest "+highest);
       
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.");
        String n1="hi guys welcome to The team";
      Arrays.stream(n1.toLowerCase().split(" ")).filter(e->e.startsWith("t")).forEach(e->System.out.println(" "+e));
      
      System.out.println(">>>>>>>>>>>>>>Count Department with Name>>>>>>>>>>>>>>>>>>>.");
      List<Employe> emp=new ArrayList<>();
      emp.add(new Employe("Jay",24 , 25000,"IT",new Date(2009,01,22)));
      emp.add(new Employe("Rehman",45 , 55000,"Testing",new Date(2021,01,02)));
      emp.add(new Employe("Rajan",34 , 120000,"Prodcution",new Date(2012,11,12)));
      emp.add(new Employe("Sagar",46 , 320000,"Testing",new Date(2016,03,02)));
      emp.add(new Employe("Abhijit",26 , 50000,"IT",new Date(2010,06,23)));
      
      System.out.println(">>>>>>>>>>>>>>Count Employee By Dept>>>>>>>>>>>>>>>>>>>.");
      Map<String, Long> re=emp.stream().collect(Collectors.groupingBy(Employe::getDept,Collectors.counting()));
       re.forEach((k,v)->{System.out.println(k+" "+v);});


       System.out.println(">>>>>>>>>>>>>>Sum of All Salaries>>>>>>>>>>>>>>>>>>>.");
        Integer a= emp.stream().collect(Collectors.summingInt(Employe::getSalary));
	Integer a= arr.stream().collect(Collectors.summingInt(Integer::intValue));
		
       System.out.println(">>>>>>>>>>>>>>Max Salary By Dept>>>>>>>>>>>>>>>>>>>.");
       Map<String, Employe> maxEmp=emp.stream().collect(Collectors.toMap(
    		   e->e.getDept(),e->e,BinaryOperator.maxBy(Comparator.comparingInt(e->e.getSalary()))));
        maxEmp.forEach((k,v)->System.out.println("Max Salary By Dept: "+k+" "+v.getSalary()));

       System.out.println(">>>>>>>>>>>>>>Digit Start With 1 >>>>>>>>>>>>>>>>>>>.");
        List<Integer> lsDigit=Arrays.asList(11,20,45,60,10,16,100);
        List<String> strings = lsDigit.stream().map(Object::toString).collect(Collectors.toList());             
    	strings.stream().filter(s->s.startsWith("1")).forEach(System.out::println);

	System.out.println(">>>>>>>>>>>>>>String Start With R >>>>>>>>>>>>>>>>>>>.");
       emp.stream().filter(e->e.getName().startsWith("R")).forEach(e->System.out.println(e.getName()));
       
       emp.stream().filter(e->e.getDoj().getYear()>2010).forEach(e->System.out.println(e.getName()));
       
       long n11=emp.stream().filter(e->e.getAge()>30).count();
       System.out.println(n11);

       System.out.println(">>>>>>>>>>>>>>Concat List Of String >>>>>>>>>>>>>>>>>>>.");
		List<Integer> things = Arrays.asList("Welcome","Guys","Bye");
		 String joined = things.stream().map(Object::toString).collect(Collectors.joining(", "));
		
		String joined = emp.stream().map(Person::getName).collect(Collectors.joining(", "));
		
         System.out.println(">>>>>>>>>>>>>>Find most repatative element >>>>>>>>>>>>>>>>>>>.");
		List<Integer> arrList = new ArrayList<>(Arrays.asList(3, 7, 5, 1, 3, 6, 7, 7));
		Entry<Integer, Long>result=arrList.stream().collect(Collectors.groupingBy(c->c,Collectors.counting()))
				.entrySet().stream().max(Map.Entry.comparingByValue()).get();
	    System.out.print("______"+result.getKey());
       //**************************************************************************************************************
       emp.stream().sorted(Comparator.comparingInt(Employe::getSalary)).collect(Collectors.toList()).forEach(e->System.out.print("-"+e.getSalary()));;
    
       emp.stream().sorted(Comparator.comparingInt(Employe::getSalary).reversed()).collect(Collectors.toList()).forEach(e->System.out.println("-"+e.getSalary()));;
   
       emp.stream().sorted(Comparator.comparing(Employe::getAge).thenComparing(Employe::getDept)).
       collect(Collectors.toList()).forEach(s->System.out.println(s.getAge()+"::"+s.getDept()));
       
       //Increases salary of employee whose age greater than 25 
       emp.stream().map(e->{
    	   if(e.getAge()>25) {
    		   e.setSalary(e.getSalary()+10000);
    		   return e;
    	   }
    	   return e;
       }).forEach(e1->System.out.print(e1.getName()+" <::>"+e1.getSalary()));
       System.out.println();
       
       //Find Vowels
        String dem0="Java Is Good For APP Devleopment";
        String s=dem0.replace(" " , "");
        List<String> vowel=Arrays.asList("a","i","o","u","e");
       Arrays.stream(s.toLowerCase().split("")).filter(vowel::contains) .forEach(System.out::println );  
       
       //Find Odd Number Cube
       List<Integer> listOfNumbers = Arrays.asList(1, 2, 3, 4, 5, 6, 12, 18); 
      listOfNumbers.stream() .filter(i -> i % 2 != 0) .forEach(e->System.out.print("-"+e*e*e));
      
      //Find Top 3 max salary
      emp.stream().sorted(Comparator.comparing(Employe::getSalary).reversed()).limit(3).forEach(ss->System.out.println("<>"+ss.getSalary()));
     
      //Return Duplicate character from string
      String dump="Characters Duplicate";
      HashSet<String> hashSet=new HashSet<>();
      Arrays.stream(dump.replaceAll(" " , "").toLowerCase().split("")).filter(x-> !hashSet.add(x)).forEach(System.out::println);
      
     System.out.println(" //find special character");
      String dump2="!Characters@1)&";
      for(int i=0;i<dump2.length();i++) {
    	  if(!Character.isDigit(dump2.charAt(i)) || !Character.isLetter(dump2.charAt(i))) {
    		  System.out.println(dump2.charAt(i));
    	  }
      }
      
      //Group Employee by Department
      
      Map<String, List<Employe>>  emp1=emp.stream().collect(Collectors.groupingBy(Employe::getDept));
     emp1.forEach((k,v)->{
    	 System.out.print(k+"<>");
    	 v.forEach(e->System.out.print(" "+e.getName()+","));
     });
      //Find Duplicate
     ArrayList<String> name=new ArrayList<>();
     name.add("Jay"); name.add("Naman");name.add("Rajan");
     name.add("Rajan");name.add("Jay"); name.add("Abhijit");
   HashSet<String> set=new HashSet<>();
   for(String s1:name) {
	   if(set.add(s1)==false)
	   {
		   System.out.println("Duplicate :"+s1);
	   }
   }
     }
	
}

