#ifndef clox_object_h
#define clox_object_h

#include "chunk.h"
#include "common.h"
#include "value.h"

#define OBJ_TYPE(value) (AS_OBJ(value)->type)
#define IS_FUNCTION(value) isObjType(value, OBJ_FUNCTION)
#define IS_NATIVE(value) isObjType(value, OBJ_NATIVE)
#define IS_STRING(value) isObjType(value, OBJ_STRING)
#define IS_OBJ_TYPE(value, expectedType) isObjTypeLoose(value, (expectedType))

#define AS_FUNCTION(value) ((ObjFunction *)AS_OBJ(value))
#define AS_NATIVE(value) (((ObjNative *)AS_OBJ(value)))
#define AS_STRING(value) ((ObjString *)AS_OBJ(value))
#define AS_CSTRING(value) (((ObjString *)AS_OBJ(value))->chars)

typedef enum { OBJ_FUNCTION, OBJ_STRING, OBJ_NATIVE } ObjType;

struct Obj {
  ObjType type;
  struct Obj *next;
};

typedef struct {
  Obj obj;
  int arity;
  Chunk chunk;
  ObjString *name;
} ObjFunction;

typedef Value (*NativeFn)(int argCount, Value *args);

typedef struct {
  ValueType valueType;
  ObjType objType;
} ArgumentType;

#define ARGUMENT_TYPE_MAX 1

typedef struct {
  Obj obj;
  NativeFn function;
  int arity;
  ArgumentType argumentTypes[ARGUMENT_TYPE_MAX];
} ObjNative;

struct ObjString {
  Obj obj;
  int length;
  char *chars;
  uint32_t hash;
};

ObjFunction *newFunction();

ObjNative *newNative(NativeFn function, int arity,
                     ArgumentType argumentTypes[]);

ObjString *takeString(char *chars, int length);

static inline bool isObjType(Value value, ObjType type) {
  return IS_OBJ(value) && AS_OBJ(value)->type == type;
}

static inline bool isObjTypeLoose(Value value, ObjType type) {
  return isObjType(value, type) ||
         (IS_OBJ(value) &&
          ((AS_OBJ(value)->type == OBJ_FUNCTION && type == OBJ_NATIVE) ||
           (AS_OBJ(value)->type == OBJ_NATIVE && type == OBJ_FUNCTION)));
}

ObjString *copyString(const char *chars, int length);

void printObject(Value value);

#endif
