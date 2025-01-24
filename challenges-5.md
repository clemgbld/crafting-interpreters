## 1
Earlier, I said that the |, *, and + forms we added to our grammar metasyntax were just syntactic sugar. Take this grammar:

expr → expr ( "(" ( expr ( "," expr )* )? ")" | "." IDENTIFIER )+
| IDENTIFIER
| NUMBER

Produce a grammar that matches the same language but does not use any of that notational sugar.

Bonus: What kind of expression does this bit of grammar encode?

--

expr -> expr subexpr
expr -> IDENTIFIER
expr -> NUMBER

subexpr -> x x
subexpr -> z z
subexpr -> x z
subexpr -> z x
subexpr -> subexpr x
subexpr -> subexpr z

x -> "()" 
x -> "(" expr")"
x -> "(" expr y ")"


y-> "," expr
y -> "," expr y

z -> "." IDENTIFIER

Bonus: this expression seems to encode a parser that generate an AST 






