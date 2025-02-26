# Challenges 13

## 1

Challenges completed on the branch challenges-13

## 2

In Lox, as in most other object-oriented languages, when looking up a method, we start at the bottom of the class hierarchy and work our way up—a subclass’s method is preferred over a superclass’s. In order to get to the superclass method from within an overriding method, you use super.

The language BETA takes the opposite approach. When you call a method, it starts at the top of the class hierarchy and works down. A superclass method wins over a subclass method. In order to get to the subclass method, the superclass method can call inner, which is sort of like the inverse of super. It chains to the next method down the hierarchy.

The superclass method controls when and where the subclass is allowed to refine its behavior. If the superclass method doesn’t call inner at all, then the subclass has no way of overriding or modifying the superclass’s behavior.

Take out Lox’s current overriding and super behavior and replace it with BETA’s semantics. In short:

When calling a method on a class, prefer the method highest on the class’s inheritance chain.

Inside the body of a method, a call to inner looks for a method with the same name in the nearest subclass along the inheritance chain between the class containing the inner and the class of this. If there is no matching method, the inner call does nothing.

For example:

class Doughnut {
cook() {
print "Fry until golden brown.";
inner();
print "Place in a nice box.";
}
}

class BostonCream < Doughnut {
cook() {
print "Pipe full of custard and coat with chocolate.";
}
}

BostonCream().cook();
This should print:

Fry until golden brown.
Pipe full of custard and coat with chocolate.
Place in a nice box.

--

implemented in Scanner , Parser, Resolver and Interpreter

## 3

In the chapter where I introduced Lox, I challenged you to come up with a couple of features you think the language is missing. 
Now that you know how to build an interpreter, implement one of those features.

--

implemented list in the ast as: 

"[" expression* "]"



implemented in Parser, Resolver and Interpreter


