# Challenges 11

## 1

Why is it safe to eagerly define the variable bound to a function’s name when other variables must wait until after they are initialized before they can be used?

--

Because as mentioned earlier in the book spliting the declaration of the variable in two steps is necessary to handle
funny edge cases like that (a variable that reference itself in its own initializer):

var a = "outer";
{
var a = a;
}

and to show an error to the user saying that is can't read a local variable in its own initializer.
function does not have this problem it is even mandatory to define them eagerly to allow a function
to reference itself in its own body like the fibonacci for example:

fn fib (n){
if(n < 2) return n;
return fib(n - 1) + fib(n - 2);
}
