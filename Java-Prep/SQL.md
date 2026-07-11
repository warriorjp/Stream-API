# SQL Interview Notes

## Table of Contents

- [SQL Command Categories](#sql-command-categories)
- [DELETE vs TRUNCATE vs DROP](#delete-vs-truncate-vs-drop)
- [PRIMARY KEY vs UNIQUE KEY](#primary-key-vs-unique-key)
- [Cursor](#cursor)
- [Trigger](#trigger)
- [JOINS](#joins)
- [WHERE vs GROUP BY vs HAVING](#where-vs-group-by-vs-having)
- [LIMIT and OFFSET](#limit-and-offset)
- [ALTER vs UPDATE](#alter-vs-update)
- [Common Interview Queries](#common-interview-queries)
- [Important Notes](#important-notes)
- [N+1 Query Problem](#n1-query-problem)

---

### SQL Command Categories


| Category | Full Form                         | Commands                        |
|----------|-----------------------------------|---------------------------------|
| DDL      | Data Definition Language          | CREATE, ALTER, DROP, TRUNCATE   |
| DML      | Data Manipulation Language        | INSERT, UPDATE, DELETE          |
| DQL      | Data Query Language               | SELECT                          |
| TCL      | Transaction Control Language      | COMMIT, ROLLBACK, SAVEPOINT     |
| DCL      | Data Control Language             | GRANT, REVOKE                   |

---

## DELETE vs TRUNCATE vs DROP

| Command  | Removes Data?       | Can Be Rolled Back? | Affects Structure?       |
|----------|---------------------|---------------------|--------------------------|
| DELETE   | Yes (selected rows) | Yes (before COMMIT) | No                       |
| TRUNCATE | Yes (all rows)      | No                  | No                       |
| DROP     | Yes (entire table)  | No                  | Yes (removes structure)  |

---

## PRIMARY KEY vs UNIQUE KEY

| Constraint  | Uniqueness | NULL Allowed?             | Usage              |
|-------------|------------|---------------------------|--------------------|
| PRIMARY KEY | Yes        | No                        | One per table      |
| UNIQUE KEY  | Yes        | Yes (one NULL in most DBs)| Multiple per table |

---

## Cursor

A **Cursor** is used when rows need to be processed **one at a time** instead of as a complete set.

---

## Trigger

A **Trigger** is a stored procedure that automatically executes when an event occurs on a table or view.

```sql
CREATE TRIGGER trg_AuditInsert
ON Employee
AFTER INSERT
AS
BEGIN
    INSERT INTO Employee_Audit(emp_id, action_time)
    SELECT emp_id, GETDATE() FROM inserted;
END;
```

---

## JOINS

| Join Type      | Description                                 |
|----------------|---------------------------------------------|
| INNER JOIN     | Returns rows with matching values in both tables |
| LEFT JOIN      | All rows from the left table + matching rows from the right table |
| RIGHT JOIN     | All rows from the right table + matching rows from the left table |
| FULL OUTER JOIN| Returns all rows from both tables           |
| NATURAL JOIN   | Joins tables using columns with the same name |
| SELF JOIN      | Joins a table with itself                   |

```sql
SELECT C.OrderID, C.CustomerName, O.OrderDate
FROM Orders O
INNER JOIN Customers C
ON O.CustomerID = C.CustomerID;
```

---

## WHERE vs GROUP BY vs HAVING

| Clause | Filters | Applied On |
| Clause   | Purpose                     | Execution Order    |
|----------|-----------------------------|--------------------|
| WHERE    | Filters individual rows     | Before `GROUP BY`  |
| GROUP BY | Groups rows by column       | After `WHERE`      |
| HAVING   | Filters groups after aggregation | After `GROUP BY` |


```sql
-- WHERE: filter rows
SELECT * FROM employees WHERE salary > 50000;

-- GROUP BY: group rows
SELECT department, AVG(salary)
FROM employees
GROUP BY department;

-- HAVING: filter groups
SELECT department, AVG(salary)
FROM employees
GROUP BY department
HAVING AVG(salary) > 60000;
```

---

## LIMIT and OFFSET

```sql
SELECT *
FROM employees
ORDER BY id
LIMIT 5 OFFSET 10;
-- skips first 10 rows, returns next 5
```

---

## ALTER vs UPDATE

| Command | Purpose                                                        |
|---------|----------------------------------------------------------------|
| ALTER   | Changes table structure (rename, add/drop columns, change datatype) |
| UPDATE  | Changes existing data in rows                                  |

---

## Common Interview Queries

### Employees Earning More Than Average Salary

```sql
SELECT name
FROM employees
WHERE salary > (SELECT AVG(salary) FROM employees);
```

### Second Highest Salary

```sql
SELECT MAX(salary)
FROM employees
WHERE salary < (SELECT MAX(salary) FROM employees);
```

### Third Highest Salary

```sql
SELECT DISTINCT salary
FROM employees
ORDER BY salary DESC
LIMIT 1 OFFSET 2;
```

### Find Duplicate Records

```sql
SELECT name, COUNT(*)
FROM employee
GROUP BY name
HAVING COUNT(*) > 1;
```

### Maximum Salary Department-wise

```sql
SELECT department, MAX(salary) AS max_salary
FROM employees
GROUP BY department;
```

### Employee with Maximum Salary

```sql
SELECT emp_name
FROM employee
WHERE salary = (SELECT MAX(salary) FROM employee);
```

### Employee Count by Department

```sql
SELECT dept, COUNT(*) AS employee_count
FROM employee
GROUP BY dept;
```

### Departments with Fewer Than 2 Employees

```sql
SELECT dept
FROM employee
GROUP BY dept
HAVING COUNT(*) < 2;
```

### Employees in Departments Having Fewer Than 2 Employees

```sql
SELECT emp_name
FROM employee
WHERE dept IN (
    SELECT dept
    FROM employee
    GROUP BY dept
    HAVING COUNT(*) < 2
);
```

### Highest Paid Employee in Each Department

```sql
SELECT e.emp_name, e.dept, e.salary
FROM employee e
WHERE e.salary = (
    SELECT MAX(salary)
    FROM employee
    WHERE dept = e.dept
);
```

### Customer Who Ordered More Than One Product

```sql
SELECT customer_id, COUNT(*) AS total_count
FROM OrderTable
GROUP BY customer_id
HAVING COUNT(*) > 1;
```

### Employees by Joining Month (Number)

```sql
SELECT MONTH(join_date) AS joining_month,
       COUNT(*) AS employee_count
FROM employee
GROUP BY MONTH(join_date)
ORDER BY joining_month;
```

### Employees by Joining Month (Name)

```sql
SELECT MONTHNAME(join_date) AS joining_month,
       COUNT(*) AS employee_count
FROM employee
GROUP BY MONTHNAME(join_date)
ORDER BY MONTH(join_date);
```

---

## Important Notes

- Every column in `SELECT` must either be in `GROUP BY` or use an aggregate function.
- If a subquery returns multiple values, use `IN` instead of `=`.

```sql
-- Correct: multiple values -> use IN
SELECT Name
FROM Employees
WHERE DepartmentID IN (
    SELECT DepartmentID
    FROM Departments
    WHERE Location = 'NY'
);
```

---

## N+1 Query Problem

Occurs when an application fetches a list of parent records and then fires one additional query per parent to get related data.

**Bad (1 + N Queries)**
- Query 1: Fetch all employees
- Query 2 to N+1: Fetch salary separately for each employee

**Good (Single Query with JOIN)**

```sql
SELECT e.employee_name, s.salary
FROM employee e
JOIN salary s ON e.emp_id = s.emp_id;
```

> Always prefer JOINs or eager loading over repeated individual queries in loops.