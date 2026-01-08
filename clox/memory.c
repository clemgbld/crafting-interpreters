#include <stdlib.h>

#include "chunk.h"
#include "compiler.h"
#include "memory.h"
#include "object.h"
#include "table.h"
#include "value.h"
#include "vm.h"

#ifdef DEBUG_LOG_GC
#include "compiler.h"
#include "debug.h"
#endif

static void markRoots() {
  for (Value *slot = vm.stack; slot < vm.stackTop; slot++) {
    markValue(*slot);
  }
  for (int i = 0; i < vm.frameCount; i++) {
    markObject((Obj *)vm.frames[i].closure);
  }
  for (ObjUpvalue *upvalue = vm.openUpvalues; upvalue != NULL;
       upvalue = upvalue->next) {
    markObject((Obj *)upvalue);
  }
  markTable(&vm.globals);
  markCompilerRoots();
}

static void markArray(ValueArray *array) {
  for (int i = 0; i < array->count; i++) {
    markValue(array->values[i]);
  }
}

static void blackenObject(Obj *object) {
#ifdef DEBUG_LOG_GC
  printf("%p blacken ", (void *)object);
  printValue(OBJ_VAL(object));
  printf("\n");
#endif
  switch (object->type) {
  case OBJ_CLOSURE: {
    ObjClosure *closure = (ObjClosure *)object;
    markObject((Obj *)closure->function);
    for (int i = 0; i < closure->upvalueCount; i++) {
      markObject((Obj *)closure->upvalues[i]);
    }
    break;
  }
  case OBJ_FUNCTION: {
    ObjFunction *function = (ObjFunction *)object;
    markObject((Obj *)function->name);
    markArray(&function->chunk.constants);
    break;
  }
  case OBJ_UPVALUE:
    markValue(((ObjUpvalue *)object)->closed);
    break;
  case OBJ_NATIVE:
  case OBJ_STRING:
    break;
  }
}

static void traceReferences() {
  while (vm.grayCount > 0) {
    Obj *object = vm.grayStack[--vm.grayCount];
    blackenObject(object);
  }
}

static void freeObject(Obj *object) {
#ifdef DEBUG_LOG_GC
  printf("%p free type %d\n", (void *)object, object->type);
#endif
  switch (object->type) {
  case OBJ_CLOSURE: {
    ObjClosure *closure = (ObjClosure *)object;
    FREE_ARRAY(ObjUpvalue *, closure->upvalues, closure->upvalueCount);
    FREE(ObjClosure, object);
    break;
  }
  case OBJ_FUNCTION: {
    ObjFunction *function = (ObjFunction *)object;
    freeChunk(&function->chunk);
    FREE(ObjFunction, object);
    break;
  }
  case OBJ_UPVALUE: {
    FREE(ObjUpvalue, object);
    break;
  }
  case OBJ_NATIVE: {
    FREE(ObjNative, object);
    break;
  }

  case OBJ_STRING: {
    ObjString *string = (ObjString *)object;
    FREE_ARRAY(char, string->chars, string->length + 1);
    FREE(ObjString, object);
    break;
  }
  }
}

size_t getBytesByObj(Obj *object) {
  switch (object->type) {
  case OBJ_FUNCTION:
    return sizeof(ObjFunction);
  case OBJ_STRING:
    return sizeof(ObjString);
  case OBJ_NATIVE:
    return sizeof(ObjNative);
  case OBJ_CLOSURE:
    return sizeof(ObjClosure);
  case OBJ_UPVALUE:
    return sizeof(ObjUpvalue);

  default:
    // Unreachable
    return 0;
  }
}

void promote(Obj *object) {
  object->age++;
  object->next = vm.longLive;
  object->isMarked = false;
  object->isTraversed = true;
  vm.longLive = object;
  vm.bytesAllocatedLongLive += getBytesByObj(object);
}

static bool shouldPromote(Obj *object) {
  return object->isMarked && object->age == LONG_LIVE_AGE;
}

static void sweep() {
  Obj *previous = NULL;
  Obj *object = vm.objects;
  while (object != NULL) {
    if (shouldPromote(object) || !object->isMarked) {
      Obj *unreached = object;
      object = object->next;
      if (previous != NULL) {
        previous->next = object;
      } else {
        vm.objects = object;
      }
      if (shouldPromote(unreached)) {
        promote(unreached);
      } else {
        freeObject(unreached);
      }
    } else {
      object->isMarked = false;
      object->isTraversed = false;
      object->age++;
      previous = object;
      object = object->next;
    }
  }
}

static void sweepLongLive() {
  Obj *previous = NULL;
  Obj *object = vm.longLive;
  while (object != NULL) {
    if (object->isMarked) {
      object->isMarked = false;
      object->isTraversed = false;
      previous = object;
      object = object->next;
    } else {
      Obj *unreached = object;
      object = object->next;
      if (previous != NULL) {
        previous->next = object;
      } else {
        vm.longLive = object;
      }

      vm.bytesAllocatedLongLive -= getBytesByObj(unreached);
      freeObject(unreached);
    }
  }
}

void collectGarbage() {
#ifdef DEBUG_LOG_GC
  printf("-- gc begin short lived objects\n");
  size_t before = vm.bytesAllocated - vm.bytesAllocatedLongLive;
#endif
  markRoots();
  traceReferences();
  tableRemoveWhite(&vm.strings);
  sweep();
  vm.nextGC =
      (vm.bytesAllocated - vm.bytesAllocatedLongLive) * GC_HEAP_GROW_FACTOR;

#ifdef DEBUG_LOG_GC
  printf("-- gc end short lived objects\n");
  size_t after = vm.bytesAllocated - vm.bytesAllocatedLongLive;
  printf("  colllected %zu bytes (from %zu to %zu) next at %zu", before - after,
         before, after, vm.nextGC);
#endif
}

void collectLongLiveGarbage() {
#ifdef DEBUG_LOG_GC
  printf("-- gc begin long live objects\n");
  size_t before = vm.bytesAllocatedLongLive;
#endif
  vm.isLongLiveGarbageCollection = true;
  markRoots();
  traceReferences();
  tableRemoveWhite(&vm.strings);
  sweepLongLive();
  vm.isLongLiveGarbageCollection = false;

#ifdef DEBUG_LOG_GC
  printf("-- gc end short lived objects\n");
  size_t after = vm.bytesAllocatedLongLive;
  printf("  colllected %zu bytes (from %zu to %zu) next at %zu", before - after,
         before, after, vm.bytesAllocated * GC_HEAP_LONG_LIVE_MAX_FACTOR);
#endif
}

void *reallocate(void *pointer, size_t oldSize, size_t newSize) {
  vm.bytesAllocated += newSize - oldSize;
  if (newSize > oldSize) {
#ifdef DEBUG_STRESS_GC
    collectGarbage();
#endif
#ifdef DEBUG_STRESS_GC
    collectLongLiveGarbage();
#endif

    if ((vm.bytesAllocated - vm.bytesAllocatedLongLive) > vm.nextGC) {
      collectGarbage();
    }
    if (vm.bytesAllocated > 0 &&
        ((double)vm.bytesAllocatedLongLive / (double)vm.bytesAllocated) >
            GC_HEAP_LONG_LIVE_MAX_FACTOR) {
      collectLongLiveGarbage();
    }
  }
  if (newSize == 0) {
    free(pointer);
    return NULL;
  }

  void *result = realloc(pointer, newSize);
  return result;
}

void markObject(Obj *object) {
  if (object == NULL)
    return;
  if (object->isTraversed)
    return;

#ifdef DEBUG_LOG_GC
  printf("%mark p ", (void *)object);
  printValue(OBJ_VAL(object));
  printf("\n");
#endif

  if ((vm.isLongLiveGarbageCollection && object->age > LONG_LIVE_AGE) ||
      !vm.isLongLiveGarbageCollection && object->age <= LONG_LIVE_AGE) {
    object->isMarked = true;
  }

  object->isTraversed = true;

  if (vm.grayCapacity < vm.grayCount + 1) {
    vm.grayCapacity = GROW_CAPACITY(vm.grayCapacity);
    vm.grayStack =
        (Obj **)realloc(vm.grayStack, sizeof(Obj *) * vm.grayCapacity);
  }

  if (vm.grayStack == NULL)
    exit(1);

  vm.grayStack[vm.grayCount++] = object;
}

void markValue(Value value) {
  if (IS_OBJ(value)) {
    markObject(AS_OBJ(value));
  }
}

static void freeObjs(Obj *objects) {
  Obj *object = objects;
  while (object != NULL) {
    Obj *next = object->next;
    freeObject(object);
    object = next;
  }
}

void freeObjects() {
  freeObjs(vm.objects);
  freeObjs(vm.longLive);
}
