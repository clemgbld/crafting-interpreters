# Challenges 4

## 1 The lexical grammars of Python and Haskell are not regular. What does that mean, and why aren’t they?

That means that you can't recognize all of the different lexemes using regex.
And they aren't because Python and Haskell both use indentation-sensitve syntax.

## 2 Aside from separating tokens—distinguishing print foo from printfoo—spaces aren’t used for much in most languages. However, in a couple of dark corners, a space does affect how code is parsed in CoffeeScript, Ruby, and the C preprocessor. Where and what effect does it have in each of those languages?

in Coffeescript and Ruby (because parenthesis for functional call are optional) forgetting the space when you are trying to do print foo will result into the interpreter thinking that you are calling the function printfoo with no parameters

and with the c preprocessor #define X 5 will work and #define X5 won't because it will think that X5 is a variable name without any value which is resulting in a syntax error

## 3 Our scanner here, like most, discards comments and whitespace since those aren’t needed by the parser. Why might you want to write a scanner that does not discard those? What would it be useful for ? 

i might not want to discard them if i would use comments in my programing language to specify some metadata for example to suppress a warning like you can do with es-lint. 

