# Challenges 7

## 1

Allowing comparisons on types other than numbers could be useful. The operators might have a reasonable interpretation for strings. Even comparisons among mixed types, like 3 < "pancake" could be handy to enable things like ordered collections of heterogeneous types. Or it could simply lead to bugs and confusion.

Would you extend Lox to support comparing other types? If so, which pairs of types do you allow and how do you define their ordering? Justify your choices and compare them to other languages.

--

i would not let the user compare different types but would design specific operation to compare each types. 

for example for strings i would something like scheme does (string<? "1" "2")

allowing every type to be compared together like in javascript would be a mess 

## 2

Many languages define + such that if either operand is a string, the other is converted to a string and the results are then concatenated. For example, "scone" + 4 would yield scone4. Extend the code in visitBinaryExpr() to support that.

--

implementation in the java class Interpreter

## 3

What happens right now if you divide a number by zero? What do you think should happen? Justify your choice. How do other languages you know handle division by zero, and why do they make the choices they do?

Change the implementation in visitBinaryExpr() to detect and report a runtime error for this case.

--

I would do it like scheme and java and c, etc... throw for integers but let divide by zero for floating-point numbers to respect  IEEE 754 standards.

in js it let you divide by zero

implementation in the java class Interpreter
