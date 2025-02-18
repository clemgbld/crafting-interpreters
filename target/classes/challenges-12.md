# Challenges 12

## 1

We have methods on instances, but there is no way to define “static” methods that can be called directly on the class object itself. 
Add support for them. 
Use a class keyword preceding the method to indicate a static method that hangs off the class object.

class Math {
class square(n) {
return n * n;
}
}

print Math.square(3); // Prints "9".
You can solve this however you like, but the “metaclasses” used by Smalltalk and Ruby are a particularly elegant approach. Hint: Make LoxClass extend LoxInstance and go from there.

--
implemented in Parser, Resolver and Interpreter

## 2

Most modern languages support “getters” and “setters”—members on a class that look like field reads and writes but that actually execute user-defined code. 
Extend Lox to support getter methods. 
These are declared without a parameter list. 
The body of the getter is executed when a property with that name is accessed.

class Circle {
init(radius) {
this.radius = radius;
}

area {
return 3.141592653 * this.radius * this.radius;
}
}

var circle = Circle(4);
print circle.area; // Prints roughly "50.2655".

--

implemented in Parser and Interpreter

## 3

Python and JavaScript allow you to freely access an object’s fields from outside of its own methods. Ruby and Smalltalk encapsulate instance state. Only methods on the class can access the raw fields, and it is up to the class to decide which state is exposed. Most statically typed languages offer modifiers like private and public to control which parts of a class are externally accessible on a per-member basis.

What are the trade-offs between these approaches and why might a language prefer one or the other?



