# Challenges 9

# 1

A few chapters from now, when Lox supports first-class functions and dynamic dispatch, we technically won’t need branching statements built into the language. 
Show how conditional execution can be implemented in terms of those. 
Name a language that uses this technique for its control flow.

we can implement is with a hasMap or hasTable or any kind of object in js or with a classes
in js:

const  x = {true: () => "true",false:() => "false"}
x[true]()

in lox:

class Boolean {
// Abstract method
ifThenElse(thenBranch, elseBranch) {}
}

class True < Boolean {
ifThenElse(thenBranch, elseBranch) {
return thenBranch(); 
}
}

class False < Boolean {
ifThenElse(thenBranch, elseBranch) {
return elseBranch(); // 
}
}

var condition = (5 > 3); // Evaluates to a `True` object
condition.ifThenElse(
{ print("Condition is true!"); },   // Then branch
{ print("Condition is false!"); }   // Else branch
);


Smalltalk use dynamic dispatch for its control flow

--

## 2

Likewise, looping can be implemented using those same tools, provided our interpreter supports an important optimization.
What is it, and why is it necessary? 
Name a language that uses this technique for iteration.

--

This optimization is named tail call optimization, which is necessary for not running into a stackoverflow.

All most functional languages use that mechanism for looping like Scheme,Haskell, Ocaml etc...

## 3
Unlike Lox, most other C-style languages also support break and continue statements inside loops. 
Add support for break statements. 
The syntax is a break keyword followed by a semicolon. 
It should be a syntax error to have a break statement appear outside of any enclosing loop. At runtime, 
a break statement causes execution to jump to the end of the nearest enclosing loop and proceeds from there. 
Note that the break may be nested inside other blocks and if statements that also need to be exited.

--