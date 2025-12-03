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
