# Challenges 16

## 1
Many newer languages support string interpolation. Inside a string literal, you have some sort of special delimiters—most commonly ${ at the beginning and } at the end. Between those delimiters, any expression can appear. When the string literal is executed, the inner expression is evaluated, converted to a string, and then merged with the surrounding string literal.

For example, if Lox supported string interpolation, then this ...

```Lox
var drink = "Tea";
var steep = 4;
var cool = 2;
print "${drink} will be ready in ${steep + cool}" minutes.";
```
```
```


...would print

"Tea will be ready in 6 minutes."

What token types would you define to implement a scanner for string interpolation? What sequence of tokens would you emit for the above string literal?

For the above string literal i would not great special token and just leverage the syntactic sugar concept because less is more:

TOKEN_IDENTIFER (drink)
TOKEN_STRING (" will be ready in ")
TOKEN_ADD
TOKEN_IDENTIFER (steep)
TOKEN_IDENTIFER (cool)
TOKEN_ADD
TOKEN_ADD

What tokens would you emit for:

"Nested ${"interpolation?! Are you ${"mad?!"}"}"

I would emit:
TOKEN_STRING ("Nested ")
TOKEN_STRING ("Interpolation! Are you ")
TOKEN_STRING ("mad?!")
TOKEN_ADD
TOKEN_ADD

Consider looking at other language implementations that support interpolation to see how they handle it.

I looked at Javascript (the V8 implementation) and they handle that totally differently they emit
TEMPLATE_SPAN
TEMPLATE_TAIL
and of course they can regonize that they will need interpolation because the string start with `.

It's tradeoff i can see that their approach keep the scanner stupid and simple by offloading more work to the compiler while the approach i come up with would make the scanner more complex and the compiler simpler.

## 2
Several languages use angle brackets for generics and also have a >> right shift operator. This led to a classic problem in early versions of C++:

```c++
vector<vector<string>> nestedVectors;
```
```
```

This would produce a compile error because the >> was lexed to a single right shift token, not two > tokens. Users were forced to avoid this by putting a space between the closing angle brackets.

Later versions of C++ are smarter and can handle the above code. Java and C# never had the problem. How do those languages specify and implement this?

From what i see in a open JDK implementation they've got separate tokens:
GT(">")
keeping this token for generic and split >> to two separate token when needed.
GTGT(">>")
keeping this token for right bit shift
even
GTGTGT(">>>")

that is how they are able to handle this edge case.

## 3

Many languages, especially later in their evolution, define “contextual keywords”. These are identifiers that act like reserved words in some contexts but can be normal user-defined identifiers in others

For example, await is a keyword inside an async method in C#, but in other methods, you can use await as your own identifier.

Name a few contextual keywords from other languages, and the context where they are meaningful.

There is async await in Javascript as well (they borrow it from C#) that you use to handle Promises more conveniently.
There is yield in Javascript that can be used to yield a value in a generator function.
var is a contextual keyword in Java as well to declare a variable (without declaring its type)
record in Java which enable you to declare the equivalent  to an immutable class without much syntax.

What are the pros and cons of having contextual keywords? 

The pro is syntactic sugar most of the time it enable you to do more with less typing like async with async await for example


``` javascript

// without async await
const y = promise
.then((x) => call(x))
.then((x) =>  x + 1);

// with async await
const y = call(await promise) + 1; 


// there is still some cases where classic promise .then and .catch are preferable
```

As you can see we can hide some complexity behind some convenient keywords.
The cons i would say is that it can be confusing and a mental burden to have to keep in your head that some keywords are reserved in a certain context only, or cannot be used in a same way in another context.

How would you implement them in your language’s front end if you needed to?
I would probably emit not special tokens for them:
and let the parser handle their meaning.
