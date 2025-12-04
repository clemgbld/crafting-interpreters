# CHALLENGES 21

## 1

The compiler adds a global variable’s name to the constant table as a string every time an identifier is encountered. It creates a new constant each time, even if that variable name is already in a previous slot in the constant table. That’s wasteful in cases where the same variable is referenced multiple times by the same function. That, in turn, increases the odds of filling up the constant table and running out of slots since we allow only 256 constants in a single chunk.
Optimize this.

In chunk.h:

```c
typedef struct {
  int count;
  int capacity;
  uint8_t *code;
  ValueArray constants;
  Table stringConstantIndexes;
  int *lines;
} Chun


```
```
```

In chunk.c:

```c
#include "table.h"

void initChunk(Chunk *chunk) {
  chunk->count = 0;
  chunk->capacity = 0;
  chunk->code = NULL;
  chunk->lines = NULL;
  initValueArray(&chunk->constants);
  initTable(&chunk->stringConstantIndexes);
};

void freeChunk(Chunk *chunk) {
  FREE_ARRAY(u_int8_t, chunk->code, chunk->capacity);
  FREE_ARRAY(int, chunk->lines, chunk->capacity);
  initChunk(chunk);
  freeValueArray(&chunk->constants);
  freeTable(&chunk->stringConstantIndexes);
}
```
```
```

In compiler.c:

```c

#include "table.h"
#include "object.h"
#include "value.h"

static uint8_t makeConstant(Value value) {

  Value index;
  bool isString = IS_OBJ(value) && IS_STRING(value);
  if (isString && tableGet(&compilingChunk->stringConstantIndexes,
                           AS_STRING(value), &index)) {
    return (uint8_t)AS_NUMBER(index);
  }
  int constant = addConstant(compilingChunk, value);
  if (isString) {
    tableSet(&compilingChunk->stringConstantIndexes, AS_STRING(value),
             NUMBER_VAL((double)constant));
  }
  if (constant > UINT8_MAX) {
    error("Too many constants in one chunk");
    return 0;
  }
  return (uint8_t)constant;
}
```
```
```

How does your optimization affect the performance of the compiler compared to the runtime? Is this the right trade-off?

My optimization affect performance in a sense that it's slower to verify if the index of a same constant string already exist and  set it if it doesn't in the hash table.
I cold have do less performance by iterating on the constant pool and check if the string already exists and give the index if it does (that would have impact performance critically).

Overall i think the trade-off is ok because it avoid us to hit the maximum of 256 quickly with the same constant string but it is slower to add hash table lookup some there is runtime performance degradation as well as memory overhead.

Slicing the program in multiple chunk and a bigger constant pool maybe we like 2 op code to have a bigger range than 256 constants would be better.

## 2

Looking up a global variable by name in a hash table each time it is used is pretty slow, even with a good hash table. Can you come up with a more efficient way to store and access global variables without changing the semantics?

In object.h:

```c
struct ObjString {
  Obj obj;
  int length;
  char *chars;
  uint32_t hash;
  Value value;
  bool hasValue;
};
```
```
```

In object.c:

```c
static ObjString *allocateString(char *chars, int length, uint32_t hash) {
  ObjString *string = ALLOCATE_OBJ(ObjString, OBJ_STRING);
  string->length = length;
  string->chars = chars;
  string->hash = hash;
  string->value = NIL_VAL;
  string->hasValue = false;
  tableSet(&vm.strings, string, NIL_VAL);
  return string;
}
```
```
```


In vm.h:

```c
typedef struct {
  Chunk *chunk;
  uint8_t *ip;
  Value stack[STACK_MAX];
  Value *stackTop;
  Table strings;
  Obj *objects;
} VM;
```
```
```

In vm.c:

```c
void initVM() {
  resetStack();
  vm.objects = NULL;
  initTable(&vm.strings);
};

void freeVM() {
  freeObjects();
  freeTable(&vm.strings);
};

case OP_GET_GLOBAL: {
      ObjString *name = READ_STRING();
      if (!name->hasValue) {
        runtimeError("Undefined variable '%s'.", name->chars);
        return INTERPRET_RUNTIME_ERROR;
      }
      push(name->value);
      break;
    }
    case OP_DEFINE_GLOBAL: {
      ObjString *name = READ_STRING();
      name->value = peek(0);
      name->hasValue = true;
      pop();
      break;
    }
    case OP_SET_GLOBAL: {
      ObjString *name = READ_STRING();
      if (!name->hasValue) {
        runtimeError("Undefined variable '%s'.", name->chars);
        return INTERPRET_RUNTIME_ERROR;
      }
      name->value = peek(0);
      break;
    }
```
```
```


Since every string is internalized in the string pool in Lox we can simply store directly the value associated to the global variable name in the string itself. It is a trade-off because it is faster than the hash table lookup but we increase memory for each string.

## 3

When running in the REPL, a user might write a function that references an unknown global variable. Then, in the next line, they declare the variable. Lox should handle this gracefully by not reporting an “unknown variable” compile error when the function is first defined.

But when a user runs a Lox script, the compiler has access to the full text of the entire program before any code is run. Consider this program:

```lox
```

fun useVar() {
  print oops;
}

var ooops = "too many o's!";
```


```
Here, we can tell statically that oops will not be defined because there is no declaration of that global anywhere in the program. Note that useVar() is never called either, so even though the variable isn’t defined, no runtime error will occur because it’s never used either.

We could report mistakes like this as compile errors, at least when running from a script. Do you think we should? Justify your answer. What do other scripting languages you know do?

I don't think we should it would break the coherence between the REPL and a Lox script.
Javascript for example doesn't report an error in that case and most other scripting language behave the same.
