# 1.Constructor Injection 
- Most recomended approch for depedency injection
- Spring injects the dependency through the constructor.

### Advantages
  - Immutable dependencies (final)
  - Easy unit testing
  - Prevents NullPointerException

```java
         @Service
         public class EmployeeService {
        
             private final EmployeeRepository repository;
        
             public EmployeeService(EmployeeRepository repository) {
                 this.repository = repository;
             }
         }
```
---

# 2.Setter Injection

 ### Advantages

- Dependency can be changed later
- Useful for optional dependencies

### Disadvantages
- Object can be created without dependency
- More chances of runtime errors

```java
    @Service
    public class EmployeeService {
    
        private EmployeeRepository repository;
    
        @Autowired
        public void setRepository(EmployeeRepository repository) {
            this.repository = repository;
        }
    }
```

---

# 3. Field Injection

### Advantages
- Less code

### Disadvantages
- unit testing becomes harder since dependencies cannot be passed directly
- Hidden dependencies : You cannot immediately tell what dependencies are required to create the object.
- Not recommended for production code

```java
@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repository;
}
```
