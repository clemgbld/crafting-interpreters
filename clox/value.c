#include "value.h"
#include "memory.h"
#include <stdio.h>
#include <stdlib.h>

void initValueArray(ValueArray *array) {
  array->values = NULL;
  array->capacity = 0;
  array->count = 0;
};

void freeValueArray(ValueArray *array) {
  FREE_ARRAY(Value, array->values, array->capacity);
  initValueArray(array);
}

void writeValueArray(ValueArray *array, Value value) {
  if (array->capacity < array->count + 1) {
    int oldCapacity = array->capacity;
    array->capacity = GROW_CAPACITY(oldCapacity);
    array->values =
        GROW_ARRAY(Value, array->values, oldCapacity, array->capacity);
  }

  array->values[array->count] = value;
  array->count++;
}

void printValue(Value value) { printf("%g", value); }

void pushStack(ValueArray *array, Value value) {
  if (array->capacity == 0) {
    array->capacity = GROW_CAPACITY(0);
    array->values = GROW_ARRAY(Value, array->values, 0, array->capacity);
    array->stackTop = array->values;
    *array->stackTop = value;
  } else if (array->capacity < (array->stackTop - array->values) + 1) {
    int oldCapacity = array->capacity;
    int stackSize = (array->stackTop - array->values);
    array->capacity = GROW_CAPACITY(oldCapacity);
    array->values =
        GROW_ARRAY(Value, array->values, oldCapacity, array->capacity);
    array->stackTop = array->values + stackSize;
    *array->stackTop = value;
  } else {
    *array->stackTop = value;
  }

  array->stackTop++;
}

Value popStack(ValueArray *array) {
  array->stackTop--;
  return *array->stackTop;
}
