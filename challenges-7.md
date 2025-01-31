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