# Challenges 17

## 1


To really understand the parser, you need to see how execution threads through the interesting parsing functions—parsePrecedence() and the parser functions stored in the table. Take this (strange) expression:

(-1 + 2) * 3 - -4

Write a trace of how those functions are called. Show the order they are called, which calls which, and the arguments passed to them.

parsePrecedence(PREC_ASSIGNMENT)
getRule(TOKEN_LEFT_PAREN)
grouping()
parsePrecedence(PREC_ASSIGNMENT)
getRule(TOKEN_MINUS)
unary()
parsePrecedence(PREC_UNARY)
getRule(TOKEN_NUMBER)
number()
getRule(TOKEN_PLUS)
getRule(TOKEN_PLUS)
getRule(TOKEN_PLUS)
binary()
getRule(TOKEN_PLUS)
parsePrecedence(PREC_FACTOR)
getRule(TOKEN_NUMBER)
number()
getRule(TOKEN_RIGHT_PAREN)
getRule(TOKEN_RIGHT_PAREN)
consume(TOKEN_RIGHT_PAREN)
getRule(TOKEN_STAR)
getRule(TOKEN_STAR)
binary()
getRule(TOKEN_STAR)
parsePrecedence(PREC_UNARY)
getRule(TOKEN_NUMBER)
number()
getRule(TOKEN_MINUS)
getRule(TOKEN_MINUS)
getRule(TOKEN_MINUS)
binary()
getRule(TOKEN_MINUS)
parsePrecedence(PREC_FACTOR)
getRule(TOKEN_MINUS)
unary()
parsePrecedence(PREC_UNARY)
getRule(TOKEN_NUMBER)
number()
getRule(TOKEN_EOF)
getRule(TOKEN_EOF)
getRule(TOKEN_EOF)
consume(TOKEN_EOF)

## 2

The ParseRule row for TOKEN_MINUS has both prefix and infix function pointers. That’s because - is both a prefix operator (unary negation) and an infix one (subtraction).

In the full Lox language, what other tokens can be used in both prefix and infix positions? 

the "!" token can be use as prefix as bang "!someBoolean" or as not equal "!=" for the infix.

but "!" and "!=" are not really the same token so no other tokens can be used both as prefix and infix in Lox.

What about in C or in another language of your choice?

In Javascript you can use + as prefix for parsing an int as well as infix for addition.

## 3

You might be wondering about complex “mixfix” expressions that have more than two operands separated by tokens. C’s conditional or “ternary” operator, ?:, is a widely known one.

Add support for that operator to the compiler. You don’t have to generate any bytecode, just show how you would hook it up to the parser and handle the operands.
