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
