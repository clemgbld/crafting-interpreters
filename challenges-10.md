# Challenges 10

# 1

Our interpreter carefully checks that the number of arguments passed to a function matches the number of parameters it expects. 
Since this check is done at runtime on every call, it has a performance cost. 
Smalltalk implementations don’t have that problem. 
Why not?

--

In small talk the number of arguments of a method (its arity) is baked into the name of this mehtod or more precisely into
its method selector.

Calculator >> add: x
^x + 1

Calculator >> add: x and: y
^x + y

calc := Calculator new.
result1 := calc add: 3.           "Calls add: (arity 1), result1 = 4"
result2 := calc add: 3 and: 4.    "Calls add:and: (arity 2), result2 = 7"

you see if i call calc add and i don't provide any arguments the method does not need to check the number of parameters it expects
since the arity is into the method selector calc add or calc add: 3 and 4 and 7 for example won't match any methods at all and you will have
an error because of that.

## 2

