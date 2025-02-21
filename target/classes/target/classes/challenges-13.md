# Challenges 13

## 1

Lox supports only single inheritance—a class may have a single superclass and that’s the only way to reuse methods across classes.
Other languages have explored a variety of ways to more freely reuse and share capabilities across classes: mixins, traits, multiple inheritance, virtual inheritance, extension methods, etc.

If you were to add some feature along these lines to Lox, which would you pick and why? If you’re feeling courageous (and you should be at this point), go ahead and add it.

--

For Lox i would pick multiple inheritance (even though i wouldn't use it that much) because since you can implement mixin without special syntax,
i feel trait are more for language that are statically typed.
And it shouldn't be that hard to implement multiple inheritance in Lox.

Implemented in Parser, Resolver and Interpreter