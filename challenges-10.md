# Challenges 10

# 1

Our interpreter carefully checks that the number of arguments passed to a function matches the number of parameters it expects. 
Since this check is done at runtime on every call, it has a performance cost. 
Smalltalk implementations don’t have that problem. 
Why not?

--

In small talk the number of arguments of a method (its arity) is baked into the name of this mehtod or more precisely into
its method selector.

Calculator >> add: x
^x + 1

Calculator >> add: x and: y
^x + y

calc := Calculator new.
result1 := calc add: 3.           "Calls add: (arity 1), result1 = 4"
result2 := calc add: 3 and: 4.    "Calls add:and: (arity 2), result2 = 7"

you see if i call calc add and i don't provide any arguments the method does not need to check the number of parameters it expects
since the arity is into the method selector calc add or calc add: 3 and 4 and 7 for example won't match any methods at all and you will have
an error because of that.

## 2

Lox’s function declaration syntax performs two independent operations. It creates a function and also binds it to a name. 
This improves usability for the common case where you do want to associate a name with the function. 
But in functional-styled code, you often want to create a function to immediately pass it to some other function or return it. 
In that case, it doesn’t need a name.

Languages that encourage a functional style usually support anonymous functions or lambdas—an expression syntax that creates a function without binding it to a name. 
Add anonymous function syntax to Lox so that this works:

fun thrice(fn) {
for (var i = 1; i <= 3; i = i + 1) {
fn(i);
}
}

thrice(fun (a) {
print a;
});
// "1".
// "2".
// "3".

How do you handle the tricky case of an anonymous function expression occurring in an expression statement:

fun () {};

--

Implemented in Parser and Interpreter classes

the trick is to report an error when there is no name in a statement but not in an expression.
I wrapped the function statement in an expression that i called lambda.

## 3

Is this program valid?

fun scope(a) {
var a = "local";
}
In other words, are a function’s parameters in the same scope as its local variables, or in an outer scope? What does Lox do? 

-- 

yes they are in the same scope.
What about other languages you are familiar with? 
it the programming languages i'm familiar they are in the same scope but redeclaring it would cause a syntax error.
What do you think a language should do?
i expect the language to report a syntax error.



