# CHALLENGES 25

## 1
Wrapping every ObjFunction in an ObjClosure introduces a level of indirection that has a performance cost. That cost isn’t necessary for functions that do not close over any variables, but it does let the runtime treat all calls uniformly.

Change clox to only wrap functions in ObjClosures that need upvalues. How does the code complexity and performance compare to always wrapping functions? Take care to benchmark programs that do and do not use closures. 

In compiler.c

```c
// in function
ObjFunction *function = endCompiler();
  if (function->upvalueCount > 0) {
    emitBytes(OP_CLOSURE, makeConstant(OBJ_VAL(function)));

    for (int i = 0; i < function->upvalueCount; i++) {
      emitByte(compiler.upvalues[i].isLocal ? 1 : 0);
      emitByte(compiler.upvalues[i].index);
    }
  } else {
    emitBytes(OP_CONSTANT, makeConstant(OBJ_VAL(function)));
  }
```

In vm.h

```c
#define IS_CLOSURE_FRAME(frame) (frame->type == CLOSURE)
#define GET_FRAME_FUNCTION(frame)                                              \
  (IS_CLOSURE_FRAME(frame) ? frame->as.closure->function : frame->as.function)

typedef enum { CLOSURE, FUNCTION } CallFrameType;

typedef struct {
  CallFrameType type;
  union {
    ObjClosure *closure;
    ObjFunction *function;
  } as;
  uint8_t *ip;
  Value *slots;
} CallFrame;
```

In vm.c

```c

// in runtimeError
ObjFunction *function = GET_FRAME_FUNCTION(frame);
// call -> callClosure
static bool callClosure(ObjClosure *closure, int argCount) {
  if (argCount != closure->function->arity) {
    runtimeError("Expected %d arguments but got %d.", closure->function->arity,
                 argCount);
    return false;
  }

  if (vm.frameCount == FRAMES_MAX) {
    runtimeError("Stack overflow.");
    return false;
  }

  CallFrame *frame = &vm.frames[vm.frameCount++];
  frame->as.closure = closure;
  frame->ip = closure->function->chunk.code;
  frame->slots = vm.stackTop - argCount - 1;
  frame->type = CLOSURE;
  return true;
}

static bool callFunction(ObjFunction *function, int argCount) {
  if (argCount != function->arity) {
    runtimeError("Expected %d arguments but got %d.", function->arity,
                 argCount);
    return false;
  }
  if (vm.frameCount == FRAMES_MAX) {
    runtimeError("Stack overflow.");
    return false;
  }

  CallFrame *frame = &vm.frames[vm.frameCount++];
  frame->as.function = function;
  frame->ip = function->chunk.code;
  frame->slots = vm.stackTop - argCount - 1;
  frame->type = FUNCTION;
  return true;
}

static bool callValue(Value callee, int argCount) {
  if (IS_OBJ(callee)) {
    switch (OBJ_TYPE(callee)) {
    case OBJ_CLOSURE:
      return callClosure(AS_CLOSURE(callee), argCount);
    case OBJ_FUNCTION:
      return callFunction(AS_FUNCTION(callee), argCount);
    case OBJ_NATIVE: {
      NativeFn native = AS_NATIVE(callee);
      Value result = native(argCount, vm.stackTop - argCount);
      vm.stackTop -= argCount + 1;
      push(result);
      return true;
    }
    default:
      break;
    }
  }
  runtimeError("Can only call functinos and classes");
  return false;
}

// in run

#define READ_CONSTANT()                                                        \
  (GET_FRAME_FUNCTION(frame)->chunk.constants.values[READ_BYTE()])

    dissassembleInstruction(
        &GET_FRAME_FUNCTION(frame)->chunk,
        (int)(frame->ip - GET_FRAME_FUNCTION(frame)->chunk.code));

    case OP_GET_UPVALUE: {
      uint8_t slot = READ_BYTE();
      push(*frame->as.closure->upvalues[slot]->location);
      break;
    }
    case OP_SET_UPVALUE: {
      uint8_t slot = READ_BYTE();
      *frame->as.closure->upvalues[slot]->location = peek(0);
      break;
    }
 
    case OP_CLOSURE: {
      ObjFunction *function = AS_FUNCTION(READ_CONSTANT());
      ObjClosure *closure = newClosure(function);
      push(OBJ_VAL(closure));
      for (int i = 0; i < closure->upvalueCount; i++) {
        uint8_t isLocal = READ_BYTE();
        uint8_t index = READ_BYTE();
        if (isLocal) {
          closure->upvalues[i] = captureUpvalue(frame->slots + index);
        } else {
          closure->upvalues[i] = frame->as.closure->upvalues[index];
        }
      }
      break;
    }
 InterpretResult interpret(const char *source) {
  ObjFunction *function = compile(source);
  if (function == NULL)
    return INTERPRET_COMPILE_ERROR;
  push(OBJ_VAL(function));
  callFunction(function, 0);
  return run();
}


```

### When an object closure is always created

- benchmark-1.text (deeply nested closure): execution time 0.000383 s
- benchmark-2.text (no closure, just functions): execution time 0.000436 s

### When an object closure is not always created

- benchmark-1.text (deeply nested closure): execution time ? 0.000436s
- benchmark-2.text (no closure, just functions): execution time ? 0.000315 s

How should you weight the importance of each benchmark? 

I think that the benchmark with no closure should weight more because even though closures are really useful a lot of part of your codebase probably don't use it.

If one gets slower and one faster, how do you decide what trade-off to make to choose an implementation strategy?

I would say in a language like lox you are much more likely to have a function without a closure than with a closure, so i would optimize for the later if the language gain popularity of course if not i would pick the much simpler implementation which to always wrap every function in a closure.

## 2

Read the design note below. I’ll wait. Now, how do you think Lox should behave?

I think that lox should behave like Javascript behave when using let in a loop, a new variable should be created at each iteration.

Change the implementation to create a new variable for each loop iteration.

Solution from Robert Nystrom: https://github.com/munificent/craftinginterpreters/blob/master/note/answers/chapter25_closures/2.md

```c
static void forStatement() {
  beginScope();

  // 1: Grab the name and slot of the loop variable so we can refer to it later.
  int loopVariable = -1;
  Token loopVariableName;
  loopVariableName.start = NULL;
  // end.

  consume(TOKEN_LEFT_PAREN, "Expect '(' after 'for'.");
  if (match(TOKEN_VAR)) {
    // 1: Grab the name of the loop variable.
    loopVariableName = parser.current;
    // end.
    varDeclaration();
    // 1: And get its slot.
    loopVariable = current->localCount - 1;
    // end.
  } else if (match(TOKEN_SEMICOLON)) {
    // No initializer.
  } else {
    expressionStatement();
  }

  int loopStart = currentChunk()->count;

  int exitJump = -1;
  if (!match(TOKEN_SEMICOLON)) {
    expression();
    consume(TOKEN_SEMICOLON, "Expect ';' after loop condition.");

    // Jump out of the loop if the condition is false.
    exitJump = emitJump(OP_JUMP_IF_FALSE);
    emitByte(OP_POP); // Condition.
  }

  if (!match(TOKEN_RIGHT_PAREN)) {
    int bodyJump = emitJump(OP_JUMP);

    int incrementStart = currentChunk()->count;
    expression();
    emitByte(OP_POP);
    consume(TOKEN_RIGHT_PAREN, "Expect ')' after for clauses.");

    emitLoop(loopStart);
    loopStart = incrementStart;
    patchJump(bodyJump);
  }

  // 1: If the loop declares a variable...
  int innerVariable = -1;
  if (loopVariable != -1) {
    // 1: Create a scope for the copy...
    beginScope();
    // 1: Define a new variable initialized with the current value of the loop
    //    variable.
    emitBytes(OP_GET_LOCAL, (uint8_t)loopVariable);
    addLocal(loopVariableName);
    markInitialized();
    // 1: Keep track of its slot.
    innerVariable = current->localCount - 1;
  }
  // end.

  statement();

  // 3: If the loop declares a variable...
  if (loopVariable != -1) {
    // 3: Store the inner variable back in the loop variable.
    emitBytes(OP_GET_LOCAL, (uint8_t)innerVariable);
    emitBytes(OP_SET_LOCAL, (uint8_t)loopVariable);
    emitByte(OP_POP);

    // 4: Close the temporary scope for the copy of the loop variable.
    endScope();
  }

  emitLoop(loopStart);

  if (exitJump != -1) {
    patchJump(exitJump);
    emitByte(OP_POP); // Condition.
  }

  endScope();
}
```

## 3
A famous koan teaches us that “objects are a poor man’s closure” (and vice versa). Our VM doesn’t support objects yet, but now that we have closures we can approximate them. Using closures, write a Lox program that models two-dimensional vector “objects”. It should:

Define a “constructor” function to create a new vector with the given x and y coordinates.

Provide “methods” to access the x and y coordinates of values returned from that constructor.

Define an addition “method” that adds two vectors and produces a third.

```lox
fun makeVector(x,y){

    fun getX(){
        return x;
      }

    fun getY(){
        return y;
      }

    fun add(vector){
        return makeVector(x + vector("getX")(), y + vector("getY")());
      }

    fun methods(method){
      if(method == "getX"){
          return getX; 
        }

      if(method == "getY"){
          return getY; 
        }

      if(method == "add"){
          return add; 
        }
      print "wrong method name";
      }
    return methods;
  }

  var vector1 = makeVector(1,2);

  var vector2 = makeVector(3,4);

  var vector3 = vector1("add")(vector2);

  print vector3("getX")();
  print vector3("getY")();
```
