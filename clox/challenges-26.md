# CHALLENGES 26

## 1

The Obj header struct at the top of each object now has three fields: type, isMarked, and next. 
How much memory do those take up (on your machine)? 

the Obj struct takes 16 bytes on my machine.

```c
struct Obj {
  ObjType type; // 4 bytes
  // padding of 3 byte
  bool isMarked; // 1 byte
  struct Obj *next; // 8 bytes
};
```


Can you come up with something more compact? Is there a runtime cost to doing so?

I already know that the struct as it is is impossible to optimize by tweaking the type and isMarked fields since we can't avoid padding because the next field is a pointer so it takes 8 bytes.

That is about as far as i was able to get given my current system programming knowledge

(the author answer)[https://github.com/munificent/craftinginterpreters/blob/master/note/answers/chapter26_garbage/1.md]

The author came up with a way to pack everything in 8 bytes (which is 64 bits) with only 49 bits used and the rest used for alignment.

```c
00000000 00000000 01111111 11010110 01001111 01010000 00000000 01100000
Bit position:
66665555 55555544 44444444 33333333 33222222 22221111 11111100 00000000
32109876 54321098 76543210 98765432 10987654 32109876 54321098 76543210

Bits needed for pointer:
........ ........ |------- -------- -------- ------- --------- ----|...

Packing everything in:
.....TTT .......M NNNNNNNN NNNNNNNN NNNNNNNN NNNNNNNN NNNNNNNN NNNNNNNN

T = type enum, M = mark bit, N = next pointer.
```

So the author packed the 3 fields into one 64 bits field.

```c
struct Obj {
  uint64_t header;
}
```

Implemented some getter to extract each field from the sole 64 bytes header field.

```c
static inline ObjType objType(Obj* object) {
  // object->header extract the top 1 byte and with the logical & keep only the bits that match
 // 1111 1111
  return (ObjType)((object->header >> 56) & 0xff);
}
// object->header extract the top 2 bytes and with the logical & keep only the bits that match
 // 0000 0000 0000 0001
static inline bool isMarked(Obj* object) {
  return (bool)((object->header >> 48) & 0x01);
}
// object->header get the full 64 bits
 // 0000 0000 0000 0000 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111
static inline Obj* objNext(Obj* object) {
  return (Obj*)(object->header & 0x0000ffffffffffff);
}
```

Implemented the setters for the isMarked and the objType.

```c
static inline void setIsMarked(Obj* object, bool isMarked){
   object->header = (object->header & 0xff00ffffffffffff) |((uint64_t) isMarked) << 48;
}

static inline void setIsObjNext(Obj* object, Obj* next){
  object->header = (object->header & 0xffff000000000000) | (uint64_t) Obj* next;
}
```


When an object is first created we assign it its type;

```c

object-i>header = (unsigned long) vm.objects | (unsigned long) type << 56;

```
There is obviously a runtime cost now because of shifting and masking, but we cut the size of the Obj struct in half from 16 to 8.
The concerning part is the part when we get the type of the object since it is done quite frequently

## 2

When the sweep phase traverses a live object, it clears the isMarked field to prepare it for the next collection cycle. Can you come up with a more efficient approach?

A clean solution is to have a global markValue that will be toggled at each collect garbage cycle.

```c
// in memory.c
// in collectGarbage after the mark and sweep steps
  vm.markValue = !vm.markValue;

// in markObject
if (object->isMarked == vm.markValue)
    return;
//...
  object->isMarked = vm.markValue;

//in sweep
static void sweep() {
  Obj *previous = NULL;
  Obj *object = vm.objects;
  while (object != NULL) {
    if (object->isMarked == vm.markValue) {
      previous = object;
      object = object->next;
    } else {
      Obj *unreached = object;
      object = object->next;
      if (previous != NULL) {
        previous->next = object;
      } else {
        vm.objects = object;
      }
      freeObject(unreached);
    }
  }
}

// in object.c allocateObject
 object->isMarked = !vm.markValue;


// vm.c in init VM
  vm.markValue = true;

// vm.h in vm struct
bool markValue;

```
```
```

## 3

Mark-sweep is only one of a variety of garbage collection algorithms out there. Explore those by replacing or augmenting the current collector with another one. Good candidates to consider are reference counting, Cheney’s algorithm, or the Lisp 2 mark-compact algorithm.

I choose to augment the current mark and sweep algorithm by adding a generational aspect like mention in the design note.

It only gains time at the sweep phase though and i came up with it from first principle i'm sure we could speed up the mark phase in order to make the algorithm truly generational.

Here are your code changes organized by file, formatted for your documentation or a README:

object.h
Updated the base object structure to include the isTraversed flag for safe global tracing and an age counter for promotion logic.

```c
struct Obj {
   ObjType type;
   bool isMarked;
+  bool isTraversed;
+  int age;
   struct Obj *next;
 };
```

vm.h
Added the second heap partition (longLive), memory counters for the old generation, and the state flag for the collection type.

```c
typedef struct {
   Table globals;
   ObjUpvalue *openUpvalues;
   Obj *objects;
+  Obj *longLive;
   int grayCapacity;
   int grayCount;
   Obj **grayStack;
   size_t bytesAllocated;
+  size_t bytesAllocatedLongLive;
   size_t nextGC;
+  bool isLongLiveGarbageCollection;
 } VM;
```
 
memory.h
Defined constants to control when the long-lived generation is collected and how many cycles an object must survive to be promoted.

```c
 #define GC_HEAP_GROW_FACTOR 2
+
+#define GC_HEAP_LONG_LIVE_MAX_FACTOR 0.75
+
+#define LONG_LIVE_AGE 3

```
memory.c
The core of the "Segmented Sweep" logic. Includes object promotion, generation-aware marking, and partitioned collection cycles.

Promotion and Sweeping

```c
+size_t getBytesByObj(Obj *object) {
+  switch (object->type) {
+  case OBJ_FUNCTION: return sizeof(ObjFunction);
+  case OBJ_STRING:   return sizeof(ObjString);
+  case OBJ_NATIVE:   return sizeof(ObjNative);
+  case OBJ_CLOSURE:  return sizeof(ObjClosure);
+  case OBJ_UPVALUE:  return sizeof(ObjUpvalue);
+  default:           return 0;
+  }
+}
+
+void promote(Obj *object) {
+  object->age++;
+  object->next = vm.longLive;
+  object->isMarked = false;
+  object->isTraversed = true;
+  vm.longLive = object;
+  vm.bytesAllocatedLongLive += getBytesByObj(object);
+}
+
+static bool shouldPromote(Obj *object) {
+  return object->isMarked && object->age == LONG_LIVE_AGE;
+}
+
 static void sweep() {
   Obj *previous = NULL;
   Obj *object = vm.objects;
+  while (object != NULL) {
+    if (shouldPromote(object) || !object->isMarked) {
+      Obj *unreached = object;
+      object = object->next;
+      if (previous != NULL) {
+        previous->next = object;
+      } else {
+        vm.objects = object;
+      }
+      if (shouldPromote(unreached)) {
+        promote(unreached);
+      } else {
+        freeObject(unreached);
+      }
+    } else {
+      object->isMarked = false;
+      object->isTraversed = false;
+      object->age++;
+      previous = object;
+      object = object->next;
+    }
+  }
 }
+
+static void sweepLongLive() {
+  Obj *previous = NULL;
+  Obj *object = vm.longLive;
+  while (object != NULL) {
+    if (object->isMarked) {
+      object->isMarked = false;
+      object->isTraversed = false;
+      previous = object;
+      object = object->next;
+    } else {
+      Obj *unreached = object;
+      object = object->next;
+      if (previous != NULL) {
+        previous->next = object;
+      } else {
+        vm.longLive = object;
+      }
+      vm.bytesAllocatedLongLive -= getBytesByObj(unreached);
+      freeObject(unreached);
+    }
+  }
+}
Collection Cycles
C

 void collectGarbage() {
 #ifdef DEBUG_LOG_GC
-  printf("-- gc begin\n");
-  size_t before = vm.bytesAllocated;
+  printf("-- gc begin short lived objects\n");
+  size_t before = vm.bytesAllocated - vm.bytesAllocatedLongLive;
 #endif
   markRoots();
   traceReferences();
   tableRemoveWhite(&vm.strings);
   sweep();
-  vm.nextGC = vm.bytesAllocated * GC_HEAP_GROW_FACTOR;
+  vm.nextGC = (vm.bytesAllocated - vm.bytesAllocatedLongLive) * GC_HEAP_GROW_FACTOR;
+
+#ifdef DEBUG_LOG_GC
+  printf("-- gc end short lived objects\n");
+  size_t after = vm.bytesAllocated - vm.bytesAllocatedLongLive;
+  printf("  colllected %zu bytes (from %zu to %zu) next at %zu", before - after,
+          before, after, vm.nextGC);
+#endif
+}
+
+void collectLongLiveGarbage() {
+#ifdef DEBUG_LOG_GC
+  printf("-- gc begin long live objects\n");
+  size_t before = vm.bytesAllocatedLongLive;
+#endif
+  vm.isLongLiveGarbageCollection = true;
+  markRoots();
+  traceReferences();
+  tableRemoveWhite(&vm.strings);
+  sweepLongLive();
+  vm.isLongLiveGarbageCollection = false;
 
 #ifdef DEBUG_LOG_GC
-  printf("-- gc end\n");
-  printf("  colllected %zu bytes (from %zu to %zu) next at %zu",
-          before - vm.bytesAllocated, before, vm.bytesAllocated, vm.nextGC);
+  printf("-- gc end short lived objects\n");
+  size_t after = vm.bytesAllocatedLongLive;
+  printf("  colllected %zu bytes (from %zu to %zu) next at %zu", before - after,
+          before, after, vm.bytesAllocated * GC_HEAP_LONG_LIVE_MAX_FACTOR);
 #endif
 }
Marking and Reallocation
C

 void *reallocate(void *pointer, size_t oldSize, size_t newSize) {
+  vm.bytesAllocated += newSize - oldSize;
   if (newSize > oldSize) {
 #ifdef DEBUG_STRESS_GC
       collectGarbage();
 #endif
-    if (vm.bytesAllocated > vm.nextGC) {
+#ifdef DEBUG_STRESS_GC
+    collectLongLiveGarbage();
+#endif
+
+    if ((vm.bytesAllocated - vm.bytesAllocatedLongLive) > vm.nextGC) {
        collectGarbage();
      }
+    if (vm.bytesAllocated > 0 &&
        ((double)vm.bytesAllocatedLongLive / (double)vm.bytesAllocated) >
            GC_HEAP_LONG_LIVE_MAX_FACTOR) {
      collectLongLiveGarbage();
   }
   // ...
 }

 void markObject(Obj *object) {
   if (object == NULL)
     return;
-  if (object->isMarked)
+  if (object->isTraversed)
     return;
  
-  object->isMarked = true;
+  if ((vm.isLongLiveGarbageCollection && object->age > LONG_LIVE_AGE) ||
+      !vm.isLongLiveGarbageCollection && object->age <= LONG_LIVE_AGE) {
+    object->isMarked = true;
+  }
+
+  object->isTraversed = true;
   // ...
 }
```


table.c
Updated the interned string table cleanup to be generation-aware.

```c
 void tableRemoveWhite(Table *table) {
   for (int i = 0; i < table->capacity; i++) {
     Entry *entry = &table->entries[i];
     if (entry->key != NULL && !entry->key->obj.isMarked) {
-      tableDelete(table, entry->key);
+      if (!vm.isLongLiveGarbageCollection &&
+          entry->key->obj.age > LONG_LIVE_AGE) {
+        tableDelete(table, entry->key);
+      } else if (vm.isLongLiveGarbageCollection &&
+                 entry->key->obj.age > LONG_LIVE_AGE) {
+        tableDelete(table, entry->key);
+      }
     }
   }
 }
```

vm.c
Initialized the generational state and secondary heap list.
```c
void initVM() {
   resetStack();
   vm.objects = NULL;
+  vm.longLive = NULL;
   vm.grayCapacity = 0;
   vm.grayCount = 0;
   vm.grayStack = NULL;
   vm.bytesAllocated = 0;
+  vm.bytesAllocatedLongLive = 0;
   vm.nextGC = 1024 * 1024;
+  vm.isLongLiveGarbageCollection = false;
   initTable(&vm.strings);
   // ...
 }
```
