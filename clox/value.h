#ifndef clox_value_h
#define clox_value_h

#include "common.h"

typedef double Value;

typedef struct {
  int capacity;
  int count;
  Value *values;
  Value *stackTop;
} ValueArray;

void initValueArray(ValueArray *array);
void freeValueArray(ValueArray *array);
void writeValueArray(ValueArray *array, Value value);
void printValue(Value value);

void pushStack(ValueArray *array, Value value);
Value popStack(ValueArray *array);

#endif
