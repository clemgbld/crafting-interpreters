# Challenge 15

## 1

What bytecode instruction sequences would you generate for the following expressions:


1 * 2 + 3 = 5

OP_CONSTANT
1
OP_CONSTANT
2
OP_MULTIPLY
OP_CONSTANT
3
OP_ADD
OP_RETURN


1 + 2 * 3 = 9

OP_CONSTANT
1
OP_CONSTANT
2
OP_ADD
OP_CONSTANT
3
OP_MULTIPLY
OP_RETURN


3 - 2 - 1:

OP_CONSTANT
3
OP_CONSTANT
2
OP_SUBSTRACT
OP_CONSTANT
1
OP_SUBSTRACT
OP_RETURN


1 + 2 * 3 - 4 / -5 = -1

OP_CONSTANT
1
OP_CONSTANT
2
OP_ADD
OP_CONSTANT
3
OP_MULTIPLY
OP_CONSTANT
4
OP_SUBSTRACT
OP_CONSTANT
5
OP_NEGATE
OP_DIVIDE
OP_RETURN

## 2 

If we really wanted a minimal instruction set, we could eliminate either OP_NEGATE or OP_SUBTRACT. Show the bytecode instruction sequence you would generate for:

4 - 3 * -2 = -2

Without OP_NEGATE:

OP_CONSTANT
4
OP_CONSTANT
3
OP_SUBTRACT
OP_CONSTANT
2
OP_CONSTANT
-1
OP_MULTIPLY
OP_MULTIPLY
OP_RETURN

Without OP_SUBTRACT:

OP_CONSTANT
4
OP_CONSTANT
3
OP_NEGATE
OP_ADD
OP_CONSTANT
2
OP_NEGATE
OP_MULTIPLY
OP_RETURN

Given the above, do you think it makes sense to have both instructions? Why or why not? Are there any other redundant instructions you would consider including

I would say that it's still make sense to have both instruction since it is easier to reason hence readability win over minimalism and reusability in that case.

For the second question i guess we can strip off multiplication and instead use addition only because it is what multiplication is under the hood but i would generate more bytecode:

For example:

5 * 3 looks like that with OP_MULTIPLY

OP_CONSTANT
5
OP_CONSTANT
3
OP_MULTIPLY
OP_RETURN

And without OP_MULTIPLY:
OP_CONSTANT
5
OP_CONSTANT
5
OP_ADD
OP_CONSTANT
5
OP_ADD
OP_RETURN

OP_DIVIDE could be eliminated with the same concept by replacing it with multiple OP_SUBTRACT

## 3
Our VM’s stack has a fixed size, and we don’t check if pushing a value overflows it. This means the wrong series of instructions could cause our interpreter to crash or go into undefined behavior. Avoid that by dynamically growing the stack as needed.

What are the costs and benefits of doing so?

The benefits is that you can use as much memory that your machine let you borrow and the downside i would say is more code and the performance hit that you take when you need to grow the stack dynamically.

## 4
To interpret OP_NEGATE, we pop the operand, negate the value, and then push the result. That’s a simple implementation, but it increments and decrements stackTop unnecessarily, since the stack ends up the same height in the end. It might be faster to simply negate the value in place on the stack and leave stackTop alone. Try that and see if you can measure a performance difference.

In a small program like this we can already that this optimization make us gamin roughly 10ms:

```c
int main() {
  double start = clock();
  initVM();
  Chunk chunk;
  initChunk(&chunk);

  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, addConstant(&chunk, 4), 123);
  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, addConstant(&chunk, 3), 123);
  writeChunk(&chunk, OP_NEGATE, 123);
  writeChunk(&chunk, OP_ADD, 123);
  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, addConstant(&chunk, 2), 123);
  writeChunk(&chunk, OP_NEGATE, 123);
  writeChunk(&chunk, OP_MULTIPLY, 123);
  writeChunk(&chunk, OP_RETURN, 123);

  interpret(&chunk);
  freeVM();
  freeChunk(&chunk);
  double end = clock();

  printf("time elapsed %f\n", end - start);

  return 0;
}
```
```
```


Are there other instructions where you can do a similar optimization?

Yes we can use the same technique on the binary operation as well:
i implemented it as well for fun.
