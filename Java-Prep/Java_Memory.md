## JVM Memory

      ├── Heap
      │     ├── Objects (new Student(), new Employee())
      │     ├── Instance Variables (id, name, salary, etc.)
      │     ├── Arrays (int[], String[], Object[])
      │     ├── String Pool ("Hello", "Java", etc.)
      │     └── Other Heap Objects (Collections, Wrapper objects, etc.)
      │
      ├── Stack (One Stack per Thread)
      │     ├── Method Calls / Stack Frames
      │     ├── Local Variables
      │     ├── Method Parameters
      │     ├── Primitive Variables
      │     │      (int, double, boolean, char, etc.)
      │     └── Object References
      │            (Student s, Employee emp)
      │
      └── Method Area (Metaspace)
            ├── Class Metadata
            ├── Method Bytecode
            ├── Runtime Constant Pool
            ├── Static Variables
            └── Static Methods Information
---

## Mehtod Area

      class Student {
      
          static String college = "ABC";
      
          static void show() {
              System.out.println("Hello");
          }
      
          int id;   //instance varible store in heap
      }

**Stored in Method Area:**
      
      Class Student Metadata

      Static Variable
      college = "ABC"
      
      Static Method
      show()
      Method bytecode


Only one copy exists regardless of how many objects you create.

 ---
 
 ## Heap Memory
 
Stores Objects and Instance Variables.

      Student s1 = new Student();
      Student s2 = new Student();
      
Garbage Collector cleans unused heap objects.   

---

## Stack Memory

            public static void main(String[] args) {
            
                Student s = new Student();
            
                int x = 10;   //local varible
            }
            
  The reference variable (s) is on Stack.          

  The actual object is on Heap.          


