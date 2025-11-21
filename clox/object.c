#include "object.h"
#include <stdio.h>
#include <string.h>

#include "memory.h"
#include "value.h"
#include "vm.h"

#define ALLOCATE_OBJ(type, objectType)                                         \
  (type *)allocateObject(sizeof(type), objectType)

static Obj *allocateObject(size_t size, ObjType type) {
  Obj *object = (Obj *)reallocate(NULL, 0, size);
  object->type = type;
  object->next = vm.objects;
  vm.objects = object;
  return object;
}

static ObjString *allocateContantString(const char *chars, int length) {
  ObjString *string = ALLOCATE_OBJ(ObjString, OBJ_STRING);
  string->length = length;
  string->stringType = CONSTANT;
  string->as.constant = chars;
  return string;
}

static ObjString *allocateString(char *chars, int length) {
  ObjString *string = ALLOCATE_OBJ(ObjString, OBJ_STRING);
  string->length = length;
  string->stringType = OWNED;
  string->as.chars = chars;
  return string;
}

ObjString *takeString(char *chars, int length) {
  return allocateString(chars, length);
}

ObjString *createConstantString(const char *chars, int length) {
  return allocateContantString(chars, length);
}

const char *getString(ObjString *string) {
  switch (string->stringType) {
  case CONSTANT:
    return string->as.constant;
  case OWNED:
    return string->as.chars;
  }
}

void printObject(Value value) {
  switch (OBJ_TYPE(value)) {
  case OBJ_STRING: {
    ObjString *string = AS_STRING(value);
    printf("\"%.*s\"", string->length, getString(string));
    break;
  }
  }
}
