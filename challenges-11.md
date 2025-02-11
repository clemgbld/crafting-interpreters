# Challenges 11

## 1

Why is it safe to eagerly define the variable bound to a function’s name when other variables must wait until after they are initialized before they can be used?

--

Because as mentioned earlier in the book spliting the declaration of the variable in two steps is necessary to handle
funny edge cases like that (a variable that reference itself in its own initializer):

var a = "outer";
{
var a = a;
}

and to show an error to the user saying that is can't read a local variable in its own initializer.
function does not have this problem it is even mandatory to define them eagerly to allow a function
to reference itself in its own body like the fibonacci for example:

fn fib (n){
if(n < 2) return n;
return fib(n - 1) + fib(n - 2);
}

## 2

How do other languages you know handle local variables that refer to the same name in their initializer, like:

var a = "outer";
{
var a = a;
}

Is it a runtime error? Compile error? Allowed? Do they treat global variables differently? Do you agree with their choices? Justify your answer.

--

in C it is allowed but the variable is initialized with itself so the result is a garbage value so it is allowed but could result in a Runtime error (undefined behavior).

in Java it is not allowed and would result in a compile time error.

in Javascript it depends of the syntax if you use var it will be ok because the variable will be hoisted and 
if you use let it will result in a runtime error because the a variable will be in the temporal dead zone so you will have a runtime error.

in Scheme will throw an error at runtime telling you that the variable a is unassigned.
but it is valid with the let syntax
(let ((a 5))
(let ((a a)) ; This is valid: outer `a` (5) initializes inner `a`.
a)) ; Returns 5

i think Scheme does the right thing because with the let syntax there is not a lot of chances that you did reassigned a variable in the same name by mistake.

## 3

Extend the resolver to report an error if a local variable is never used.

--

Implemented in the Resolver class






