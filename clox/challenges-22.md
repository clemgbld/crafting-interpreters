# CHALLENGES 22

## 1
Our simple local array makes it easy to calculate the stack slot of each local variable. But it means that when the compiler resolves a reference to a variable, we have to do a linear scan through the array.

Come up with something more efficient.

I would maintain a hash table with the key of which would be the variable name.
And the value would be an array of struct that would have look like that
{
  int index;
  int depth;
}

To be able to do a linear search for finding the correct index.

Another idea would be just to cache the current scope in a hash table with name of the variable as key and index as value.

Do you think the additional complexity is worth it?

No i don't think the additional complexity is worth it the memory variable and the shadowing of the variables would be hard to maintain.

## 2

How do other languages handle code like this:

```lox
var a = a;
```

What would you do if it was your language? Why?

It is usually not allowed in other languages and i would not allow it too since it is really confusing and could have the edge cases of trying to access a variable not yet initialized.
I think that allowing that when a variable in the upper scope already exist and reporting an error when there is no variables in an upper scope that is name the same would be very much confusing of the language user.

## 3

Many languages make a distinction between variables that can be reassigned and those that can’t. In Java, the final modifier prevents you from assigning to a variable. In JavaScript, a variable declared with let can be assigned, but one declared using const can’t. Swift treats let as single-assignment and uses var for assignable variables. Scala and Kotlin use val and var.

Pick a keyword for a single-assignment variable form to add to Lox. Justify your choice, then implement it. An attempt to assign to a variable declared using your new keyword should cause a compile error.

I choose "const" as a keyword because it is very obvious by the name that the value shouldn't be reassigned and you can see that this is the
choice of many programming language as well.
I implemented it only for local:
most of the work is in the compiler

In scanner.h

```c

TOKEN_CONST,
```

In scanner.c

```c
case 'c':
    if (scanner.current - scanner.start > 1) {
      switch (scanner.start[1]) {
      case 'l':
        return checkKeyword(2, 3, "ass", TOKEN_CLASS);
      case 'o':
        return checkKeyword(2, 3, "nst", TOKEN_CONST);
      }
    }
    break;
```

In compiler.c

```c
typedef struct {
  Token name;
  int depth;
  bool isConst;
} Local;

static void addLocal(Token name, bool isConst) {
  if (current->localCount == UINT8_MAX) {
    error("Too many local variables in function.");
    return;
  }
  Local *local = &current->locals[current->localCount++];
  local->name = name;
  local->depth = -1;
  local->isConst = isConst;
}

static void declareVariable(bool isConst) {
  if (current->scopeDepth == 0)
    return;
  Token *name = &parser.previous;
  for (int i = current->localCount - 1; i >= 0; i--) {
    Local *local = &current->locals[i];
    if (local->depth != -1 && local->depth < current->scopeDepth) {
      break;
    }
    if (identifiersEqual(name, &local->name)) {
      error("Already a variable with this name in this scope.");
    }
  }
  addLocal(*name, isConst);
}

static uint8_t parseVariable(const char *errorMessage, bool isConst) {
  consume(TOKEN_IDENTIFIER, errorMessage);
  declareVariable(isConst);
  if (current->scopeDepth > 0)
    return 0;
  return identifierConstant(&parser.previous);
}

static void varDeclaration(bool isConst) {
  uint8_t global = parseVariable("Expect variable name", isConst);

  if (match(TOKEN_EQUAL)) {
    expression();
  } else {
    emitByte(OP_NIL);
  }
  consume(TOKEN_SEMICOLON, "Expect ';' after variable declaration.");
  defineVariable(global);
}

static void synchronize() {
  parser.panicMode = false;
  while (parser.current.type != TOKEN_EOF) {
    if (parser.previous.type == TOKEN_SEMICOLON)
      return;
    switch (parser.current.type) {
    case TOKEN_CLASS:
    case TOKEN_FUN:
    case TOKEN_VAR:
    case TOKEN_CONST:
    case TOKEN_FOR:
    case TOKEN_IF:
    case TOKEN_WHILE:
    case TOKEN_PRINT:
    case TOKEN_RETURN:
      return;
    default:; // do nothing
    }
    advance();
  }
}
static void declaration() {
  if (match(TOKEN_VAR)) {
    varDeclaration(false);
  } else if (match(TOKEN_CONST)) {
    varDeclaration(true);
  } else {
    statement();
  }
  if (parser.panicMode)
    synchronize();
}
static void namedVariable(Token name, bool canAssign) {
  bool isGlobal = false;
  uint8_t getOp, setOp;
  int arg = resolveLocal(current, &name);

  if (arg != -1) {
    getOp = OP_GET_LOCAL;
    setOp = OP_SET_LOCAL;
  } else {
    isGlobal = true;
    arg = identifierConstant(&name);
    getOp = OP_GET_GLOBAL;
    setOp = OP_SET_GLOBAL;
  }

  if (canAssign && match(TOKEN_EQUAL)) {
    if (!isGlobal && current->locals[arg].isConst) {
      error("Cannot reassign constant");
      return;
    }
    expression();
    emitBytes(setOp, (uint8_t)arg);
  } else {
    emitBytes(getOp, (uint8_t)arg);
  }
}
ParseRule rules[] = {
    [TOKEN_LEFT_PAREN] = {grouping, NULL, PREC_NONE},
    [TOKEN_RIGHT_PAREN] = {NULL, NULL, PREC_NONE},
    [TOKEN_LEFT_BRACE] = {NULL, NULL, PREC_NONE},
    [TOKEN_RIGHT_BRACE] = {NULL, NULL, PREC_NONE},
    [TOKEN_COMMA] = {NULL, NULL, PREC_NONE},
    [TOKEN_DOT] = {NULL, NULL, PREC_NONE},
    [TOKEN_MINUS] = {unary, binary, PREC_TERM},
    [TOKEN_PLUS] = {NULL, binary, PREC_TERM},
    [TOKEN_SEMICOLON] = {NULL, NULL, PREC_NONE},
    [TOKEN_SLASH] = {NULL, binary, PREC_FACTOR},
    [TOKEN_STAR] = {NULL, binary, PREC_FACTOR},
    [TOKEN_BANG] = {unary, NULL, PREC_NONE},
    [TOKEN_BANG_EQUAL] = {NULL, binary, PREC_EQUALITY},
    [TOKEN_EQUAL] = {NULL, NULL, PREC_NONE},
    [TOKEN_EQUAL_EQUAL] = {NULL, binary, PREC_EQUALITY},
    [TOKEN_GREATER] = {NULL, binary, PREC_COMPARISON},
    [TOKEN_GREATER_EQUAL] = {NULL, binary, PREC_COMPARISON},
    [TOKEN_LESS] = {NULL, binary, PREC_COMPARISON},
    [TOKEN_LESS_EQUAL] = {NULL, binary, PREC_COMPARISON},
    [TOKEN_IDENTIFIER] = {variable, NULL, PREC_NONE},
    [TOKEN_STRING] = {string, NULL, PREC_NONE},
    [TOKEN_NUMBER] = {number, NULL, PREC_NONE},
    [TOKEN_AND] = {NULL, NULL, PREC_NONE},
    [TOKEN_CLASS] = {NULL, NULL, PREC_NONE},
    [TOKEN_ELSE] = {NULL, NULL, PREC_NONE},
    [TOKEN_FALSE] = {literal, NULL, PREC_NONE},
    [TOKEN_FOR] = {NULL, NULL, PREC_NONE},
    [TOKEN_FUN] = {NULL, NULL, PREC_NONE},
    [TOKEN_IF] = {NULL, NULL, PREC_NONE},
    [TOKEN_NIL] = {literal, NULL, PREC_NONE},
    [TOKEN_OR] = {NULL, NULL, PREC_NONE},
    [TOKEN_PRINT] = {NULL, NULL, PREC_NONE},
    [TOKEN_RETURN] = {NULL, NULL, PREC_NONE},
    [TOKEN_SUPER] = {NULL, NULL, PREC_NONE},
    [TOKEN_THIS] = {NULL, NULL, PREC_NONE},
    [TOKEN_TRUE] = {literal, NULL, PREC_NONE},
    [TOKEN_VAR] = {NULL, NULL, PREC_NONE},
    [TOKEN_CONST] = {NULL, NULL, PREC_NONE},
    [TOKEN_WHILE] = {NULL, NULL, PREC_NONE},
    [TOKEN_ERROR] = {NULL, NULL, PREC_NONE},
    [TOKEN_EOF] = {NULL, NULL, PREC_NONE},
};
```

## 4

Extend clox to allow more than 256 local variables to be in scope at a time.

In common.h

```c

#define UINT16_COUNT (UINT16_MAX + 1)
```

In compiler.h

```c
typedef struct {
  Local locals[UINT16_COUNT];
  int localCount;
  int scopeDepth;
} Compiler;

static void namedVariable(Token name, bool canAssign) {
  uint8_t getOp, setOp;
  int arg = resolveLocal(current, &name);
  bool isLocal = false;

  if (arg != -1) {
    isLocal = true;
    if (arg > UINT8_MAX) {
      getOp = OP_GET_LOCAL_LONG;
      setOp = OP_SET_LOCAL_LONG;
    } else {
      getOp = OP_GET_LOCAL;
      setOp = OP_SET_LOCAL;
    }
  } else {
    arg = identifierConstant(&name);
    getOp = OP_GET_GLOBAL;
    setOp = OP_SET_GLOBAL;
  }

  if (canAssign && match(TOKEN_EQUAL)) {
    expression();
    if (isLocal && arg > UINT8_MAX) {
      emitBytes(setOp,  (uint8_t)(arg >> 8));
      emitByte(arg & 255);
    } else {
      emitBytes(setOp, (uint8_t)arg);
    }
  } else {
    if (isLocal && arg > UINT8_MAX) {
      emitBytes(getOp, (uint8_t)(arg >> 8));
      emitByte(arg & 255);
    } else {
      emitBytes(getOp, (uint8_t)arg);
    }
  }
}
```

In chunk.h

```c
typedef enum {
  OP_CONSTANT,
  OP_NIL,
  OP_TRUE,
  OP_FALSE,
  OP_POP,
  OP_GET_LOCAL,
  OP_GET_LOCAL_LONG,
  OP_GET_GLOBAL,
  OP_DEFINE_GLOBAL,
  OP_SET_LOCAL,
  OP_SET_LOCAL_LONG,
  OP_SET_GLOBAL,
  OP_EQUAL,
  OP_GREATER,
  OP_LESS,
  OP_ADD,
  OP_SUBTRACT,
  OP_MULTIPLY,
  OP_DIVIDE,
  OP_NOT,
  OP_NEGATE,
  OP_PRINT,
  OP_RETURN
} OpCode;

```

In vm.h

```c

#define STACK_MAX UINT16_COUNT
```

In vm.c

```c
case OP_GET_LOCAL_LONG: {
      uint8_t firstByte = READ_BYTE();
      uint8_t secondByte = READ_BYTE();
      uint16_t slot = ((uint16_t) firstByte << 8) | secondByte;
      push(vm.stack[slot]);
      break;
    }

case OP_SET_LOCAL_LONG: {
      uint8_t firstByte = READ_BYTE();
      uint8_t secondByte = READ_BYTE();
      uint16_t slot = ((uint16_t) firstByte << 8) | secondByte;
      vm.stack[slot] = peek(0);
      break;
    }
```

In debug.c

```c
static int byteLongInstruction(const char *name, Chunk *chunk, int offset) {
  uint8_t firstByte = chunk->code[offset + 1];
  uint8_t secondByte = chunk->code[offset + 2];
  uint16_t slot = ((uint16_t) firstByte << 8) | secondByte;
  printf("%-16s %4d\n", name, slot);
  return offset + 3;
}


  case OP_GET_LOCAL_LONG:
    return byteLongInstruction("OP_GET_LOCAL_LONG", chunk, offset);
  case OP_SET_LOCAL_LONG:
    return byteLongInstruction("OP_SET_LOCAL_LONG", chunk, offset);

```
