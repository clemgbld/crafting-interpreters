# Challenges 18

## 1

We could reduce our binary operators even further than we did here. Which other instructions can you eliminate, and how would the compiler cope with their absence?

We could eliminate: 

OP_SUBSTRACT  TOKEN_MINUS: emitBytes(OP_NEGATE,OP_ADD);
or
OP_ADD  TOKEN_MINUS: emitBytes(OP_NEGATE,OP_SUBSTRACT);

## 2

Conversely, we can improve the speed of our bytecode VM by adding more specific instructions that correspond to higher-level operations. What instructions would you define to speed up the kind of user code we added support for in this chapter?

I would probably add OP_NOT_EQUAL, OP_GREATER_EQUAL and OP_LESS_EQUAL as they are use often it would end up saving a lot of push and pop on the stack.


