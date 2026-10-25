# JavaScript Interview Questions — Output-Based Notes

## 1. Truthy and Falsy Values

### Q1. Predict the output

```javascript
console.log(Boolean([]));
console.log(Boolean({}));
console.log(Boolean(""));
console.log(Boolean("0"));
console.log(Boolean(0));
```

**Answer:**
```text
true
true
false
true
false
```

**Explanation:**
- Empty arrays `[]` and empty objects `{}` are truthy.
- Empty string `""` is falsy.
- `"0"` is a non-empty string, so it is truthy.
- Number `0` is falsy.

### Q2. Predict the output

```javascript
if ([]) {
    console.log("Hello");
}
```

**Answer:**
```text
Hello
```

**Explanation:** An empty array is truthy, so the `if` block executes.

---

## 2. Type Coercion

Type coercion means converting a value from one data type to another.

### Q3. Predict the output

```javascript
console.log(1 + "2" + 3);
console.log(1 + 2 + "3");
console.log("5" - 2);
console.log("5" + 2);
```

**Answer:**
```text
123
33
3
52
```

**Explanation:**
- `1 + "2"` → `"12"`
- `"12" + 3` → `"123"`
- `1 + 2` → `3`, then `3 + "3"` → `"33"`
- `"5" - 2` → `3`
- `"5" + 2` → `"52"`

The `+` operator can perform addition or string concatenation. The `-` operator converts operands to numbers.

### Q4. Predict the output

```javascript
console.log(true + true);
console.log("10" - "5");
console.log("10" + "5");
console.log(null + 1);
console.log(undefined + 1);
```

**Answer:**
```text
2
5
105
1
NaN
```

**Explanation:**

| Expression | Explanation |
|---|---|
| `true + true` | `true` becomes `1`; result is `2` |
| `"10" - "5"` | Strings convert to numbers; result is `5` |
| `"10" + "5"` | String concatenation; result is `"105"` |
| `null + 1` | `null` becomes `0`; result is `1` |
| `undefined + 1` | `undefined` becomes `NaN` in numeric operations |

### Important conversion table

| Value | `Number(value)` |
|---|---:|
| `null` | `0` |
| `undefined` | `NaN` |
| `true` | `1` |
| `false` | `0` |
| `""` | `0` |
| `"10"` | `10` |
| `"hello"` | `NaN` |

### Q5. Predict the output

```javascript
console.log([] + 1);
console.log([1, 2] + 3);
console.log([] == 0);
console.log([] == "");
```

**Answer:**
```text
1
1,23
true
true
```

**Explanation:**

Arrays convert to strings in these expressions:

```javascript
[]       // ""
[1, 2]   // "1,2"
```

Therefore:

```javascript
[] + 1       // "1"
[1, 2] + 3   // "1,23"
[] == 0      // true
[] == ""     // true
```

---

## 3. Equality Operators

### Q6. Predict the output

```javascript
console.log(0 == false);
console.log(0 === false);
console.log(null == undefined);
console.log(null === undefined);
console.log([] == false);
```

**Answer:**
```text
true
false
true
false
true
```

**Explanation:**
- `==` allows type coercion.
- `===` checks both type and value without coercion.
- `null == undefined` is a special JavaScript rule.
- `[] == false` → `"" == 0` → `0 == 0` → `true`.

**Interview tip:** Prefer `===` unless you intentionally need loose equality.

---

## 4. typeof Operator and Arrays

### Q7. What is the type of `NaN`?

```javascript
console.log(typeof NaN);
```

**Answer:**
```text
number
```

**Explanation:** `NaN` means "Not-a-Number", but JavaScript represents it as a special numeric value.

### Q8. Is `NaN` equal to itself?

```javascript
console.log(NaN === NaN);
console.log(Number.isNaN(NaN));
```

**Answer:**
```text
false
true
```

**Explanation:** `NaN` is not equal to itself. Use `Number.isNaN()` to check whether a value is `NaN`.

### Q9. Predict the output

```javascript
console.log(typeof null);
console.log(typeof []);
console.log(typeof {});
console.log(typeof function() {});
console.log(Array.isArray([]));
```

**Answer:**
```text
object
object
object
function
true
```

**Explanation:**

| Expression | Output | Explanation |
|---|---|---|
| `typeof null` | `"object"` | Historical JavaScript behavior |
| `typeof []` | `"object"` | Arrays are objects |
| `typeof {}` | `"object"` | Object literal |
| `typeof function() {}` | `"function"` | Functions have a special `typeof` result |
| `Array.isArray([])` | `true` | Correct way to check for an array |

**Important interview trick:**

```javascript
console.log(typeof [] === typeof {});
```

Output:
```text
true
```

Both return `"object"`.

But:

```javascript
console.log(Array.isArray([])); // true
console.log(Array.isArray({})); // false
```

Never use `typeof` to distinguish arrays from normal objects. Use `Array.isArray()`.

---

## 5. Function Return Values

### Q10. Predict the output

```javascript
function app() {
    const a = 10;

    {
        name: "Rita"
    }
}

console.log(app());
```

**Answer:**
```text
undefined
```

**Explanation:**

The function has no `return` statement, so it returns `undefined` by default.

Also, this:

```javascript
{
    name: "Rita"
}
```

is interpreted as a block containing a labelled statement, not as an object literal being returned.

To return an object:

```javascript
function app() {
    return {
        name: "Rita"
    };
}

console.log(app());
```

**Output:**
```text
{ name: "Rita" }
```

---

## 6. Promises and async/await

### Q11. What does `Promise.resolve(10)` do?

```javascript
const promise = Promise.resolve(10);

promise.then(value => {
    console.log(value);
});
```

**Answer:**
```text
10
```

**Explanation:** `Promise.resolve(10)` creates a Promise fulfilled with the value `10`. The `.then()` callback runs as a microtask.

### Q12. Predict the output

```javascript
async function test() {
    console.log("A");

    await Promise.resolve();

    console.log("B");
}

console.log("Start");

test();

console.log("End");
```

**Answer:**
```text
Start
A
End
B
```

**Explanation:**
- `Start` prints first.
- Calling `test()` prints `A` synchronously.
- `await` pauses the async function.
- The main code prints `End`.
- The async function resumes and prints `B`.

**Important:** `await` pauses only the current async function, not the entire JavaScript program.

### Q13. What happens if a Promise is rejected?

```javascript
async function test() {
    try {
        const result = await Promise.reject("Failed");
        console.log(result);
    } catch (error) {
        console.log(error);
    }
}

test();
```

**Answer:**
```text
Failed
```

**Explanation:** A rejected Promise throws an error at the `await` expression. Use `try/catch` to handle it.

---

## 7. Promise Chaining

### Q14. Predict the output

```javascript
Promise.resolve(10)
    .then(value => {
        console.log(value);
        return value * 2;
    })
    .then(value => {
        console.log(value);
        return value + 5;
    })
    .then(value => {
        console.log(value);
    });
```

**Answer:**
```text
10
20
25
```

**Explanation:** Each `.then()` receives the value returned by the previous `.then()`.

```text
10 → 20 → 25
```

Every `.then()` returns a new Promise.

### Q15. What happens if we don't return a value?

```javascript
Promise.resolve(10)
    .then(value => {
        console.log(value);
        value * 2;
    })
    .then(value => {
        console.log(value);
    });
```

**Answer:**
```text
10
undefined
```

**Explanation:** The first callback calculates `20`, but doesn't return it. A function without an explicit `return` returns `undefined`.

---

## 8. Event Loop — Promise vs setTimeout

### Q16. Predict the output

```javascript
console.log("Start");

setTimeout(() => {
    console.log("Timer");
}, 0);

Promise.resolve().then(() => {
    console.log("Promise");
});

console.log("End");
```

**Answer:**
```text
Start
End
Promise
Timer
```

**Explanation:**
- Synchronous code executes first.
- Promise callbacks run as microtasks.
- `setTimeout` callbacks run as tasks.
- Microtasks are processed before the next timer task.

**Interview rule:** Synchronous code → Microtasks → Timer tasks (in this simplified example).

### Q17. Predict the output

```javascript
console.log("Start");

setTimeout(() => {
    console.log("Timer");

    Promise.resolve().then(() => {
        console.log("Promise inside Timer");
    });
}, 0);

Promise.resolve().then(() => {
    console.log("Promise 1");

    setTimeout(() => {
        console.log("Timer inside Promise");
    }, 0);
});

Promise.resolve().then(() => {
    console.log("Promise 2");
});

console.log("End");
```

**Answer:**
```text
Start
End
Promise 1
Promise 2
Timer
Promise inside Timer
Timer inside Promise
```

**Explanation:**
1. Synchronous code prints `Start` and `End`.
2. Promise microtasks run in registration order.
3. The original timer prints `Timer`.
4. Its Promise microtask prints `Promise inside Timer`.
5. The timer scheduled inside `Promise 1` prints `Timer inside Promise`.

---

## 9. this, call, apply, and bind

### Q18. Predict the output

```javascript
const user1 = { name: "Jay" };
const user2 = { name: "Rita" };

function greet() {
    console.log(this.name);
}

const greetJay = greet.bind(user1);

greetJay.call(user2);
```

**Answer:**
```text
Jay
```

**Explanation:** `bind()` fixes the `this` value for the new function. A later `call()` cannot override the bound `this`.

### Q19. Predict the output

```javascript
function greet(city) {
    console.log(this.name + " from " + city);
}

const user = { name: "Jay" };

greet.call(user, "Pune");
greet.apply(user, ["Mumbai"]);
```

**Answer:**
```text
Jay from Pune
Jay from Mumbai
```

**Explanation:**
- `call()` passes arguments individually.
- `apply()` passes arguments as an array (or array-like object).

---

## 10. Closures and Hoisting

### Q20. Predict the output

```javascript
console.log(a);
var a = 10;

console.log(b);
let b = 20;
```

**Answer:**
```text
undefined
ReferenceError
```

**Explanation:**
- `var` is hoisted and initialized with `undefined`.
- `let` is hoisted but remains in the Temporal Dead Zone (TDZ) until its declaration is evaluated.

### Q21. Predict the output

```javascript
for (var i = 0; i < 3; i++) {
    setTimeout(() => console.log(i), 1000);
}
```

**Answer:**
```text
3
3
3
```

**Explanation:** `var` is function-scoped. All callbacks share the same `i`, which is `3` when the timers execute.

Using `let`:

```javascript
for (let i = 0; i < 3; i++) {
    setTimeout(() => console.log(i), 1000);
}
```

**Output:**
```text
0
1
2
```

**Explanation:** `let` creates a separate binding for each loop iteration.

### Q22. Predict the output

```javascript
function outer() {
    let count = 0;

    return function inner() {
        count++;
        console.log(count);
    };
}

const fn1 = outer();

fn1();
fn1();
fn1();

const fn2 = outer();
fn2();
```

**Answer:**
```text
1
2
3
1
```

**Explanation:** Each call to `outer()` creates a new lexical environment. `fn1` shares its own `count`; `fn2` gets a separate `count`.

---

# Quick Revision Rules

1. Empty arrays and empty objects are truthy.
2. `typeof NaN` returns `"number"`.
3. `NaN === NaN` is `false`.
4. `typeof null` returns `"object"` due to a historical behavior.
5. Use `Array.isArray()` to check arrays.
6. `==` allows type coercion; `===` does not.
7. `null == undefined` is `true`.
8. `+` can perform addition or string concatenation.
9. `await` pauses only the async function.
10. Promise callbacks run as microtasks.
11. `setTimeout` callbacks run as tasks.
12. A function without `return` returns `undefined`.
13. Every `.then()` returns a new Promise.
14. `bind()` fixes the `this` value.
15. Closures retain access to their lexical environment.
