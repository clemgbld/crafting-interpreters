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

