## 1. Second highest salary

```
SELECT emp_name, salary
FROM (SELECT *, DENSE_RANK() OVER ( PARTITION BY department
ORDER BY salary DESC ) AS rnk FROM employee) p
WHERE rnk = 2;
```

## 2. Find duplicate record
```
SELECT  * FROM product where category  IN 
(SELECT category FROM product GROUP BY category  
HAVING COUNT(*) > 1);
```
## 3. Remove duplicate element from table
```
DELETE FROM employee
WHERE emp_id IN (
    SELECT emp_id
    FROM (
        SELECT emp_id,
               ROW_NUMBER() OVER (
                   PARTITION BY emp_name, department, salary
                   ORDER BY emp_id
               ) AS rn
        FROM employee
    ) t
    WHERE rn > 1
);
```
## 4. Find top 5 salary
```
SELECT emp_name,
       department,
       salary
FROM (
    SELECT emp_name,
           department,
           salary,
           DENSE_RANK() OVER (
               ORDER BY salary DESC
           ) AS rnk
    FROM employee
) e
WHERE rnk <= 5;
```
## 5. Find salary spend by each department
```
SELECT department , SUM(salary) FROM employee GROUP BY department ;
```

## 6. Find employee who joined in last 6 month
```
SELECT * FROM employee where joining_date > DATE_SUB(CURDATE(), INTERVAL  6 MONTH) ;
```

## 7. Find employee who are not in any department 
```
SELECT e.*
FROM employee e
LEFT JOIN department d
    ON e.department_id = d.department_id
WHERE d.department_id IS NULL;
```
## 8. Find employee count from each department
```
SELECT  department  , count(*) as Toal_Employee FROM employee GROUP BY department ;
```
## 9. Pivot employee count by department
```
SELECT
    SUM(CASE WHEN department = 'IT' THEN 1 ELSE 0 END) AS IT_Count,
    SUM(CASE WHEN department = 'HR' THEN 1 ELSE 0 END) AS HR_Count,
    SUM(CASE WHEN department = 'Finance' THEN 1 ELSE 0 END) AS Finance_Count
FROM employee;
```
## 10 .Find customer with their recent order
```
WITH tmp AS (
    SELECT
        c.customer_name,
        o.product_name,
        o.created_at,
        ROW_NUMBER() OVER (
            PARTITION BY o.customer_id
            ORDER BY o.created_at, o.product_id
        ) AS rnk
    FROM customer c
    INNER JOIN orders o
        ON c.customer_id = o.customer_id
)


SELECT *
FROM tmp
WHERE rnk = 1;

```
## 11. Find employee with his probation end date
```
SELECT
    emp_id,DATE_ADD(joining_date, INTERVAL 3 MONTH) AS probation_period_end
FROM employee;
```
##
```
```
##
```
```
##
```
```


