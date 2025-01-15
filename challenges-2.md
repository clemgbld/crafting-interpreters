# Challenges 2

## 1

Pick an open source implementation of a language you like. Download the source code and poke around in it. 
Try to find the code that implements the scanner and parser.
Are they handwritten, or generated using tools like Lex and Yacc? (.l or .y files usually imply the latter.)

--

i choose to dive in the repository of the go programming language
there is indeed a scanner and a parser written in an earlier version of go written in c (this is called bootstrapping)

## 2 

Just-in-time compilation tends to be the fastest way to implement dynamically typed languages, but not all of them use it.
What reasons are there to not JIT?

--

What i found is that JIT adds an extra step, it has to be compiled to IL and then it is compiled to machine code which is an extra performance overhead.
And this layer of abstraction does not always make sense for languages that want to be be close the "bare metal"

## 3

Most Lisp implementations that compile to C also contain an interpreter that lets them execute Lisp code on the fly as well. Why?

--

in my opinion an interpreter is more development friendly you can execute your code and see the results of change directly without needing to wait for the compilation.
