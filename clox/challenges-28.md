# CHALLENGES 28

## 1

The hash table lookup to find a class’s init() method is constant time, but still fairly slow. Implement something faster. Write a benchmark and measure the performance difference.

Crafted this little program as a benchmark.


```lox
class Point{
    init(x, y){
        this.x = x;
        this.y = y;
      }
  }
 

var start = clock();

for(var i = 0; i < 10000; i = i + 1){
      Point(1,2);
  }

var end = clock();

print "elapsed: ";
print end - start;
print " s";
```


Before the optimization it runs in arround 1.57989 s.

After the optimization there is small improvement 1.32934 s.

```c

 // in object.c
 ObjClass *newClass(ObjString *name) {
   ObjClass *klass = ALLOCATE_OBJ(ObjClass, OBJ_CLASS);
   klass->name = name;
+  klass->initMethod = NIL_VAL;
   initTable(&klass->methods);
   return klass;
 }

// in object.h
 typedef struct {
   Obj obj;
   ObjString *name;
   Table methods;
+  Value initMethod;
 } ObjClass;

 // in vm.c

 static bool callValue(Value callee, int argCount) {
     case OBJ_CLASS: {
       ObjClass *klass = AS_CLASS(callee);
       vm.stackTop[-argCount - 1] = OBJ_VAL(newInstance(klass));
-      Value initializer;
-      if (tableGet(&klass->methods, vm.initString, &initializer)) {
-        return call(AS_CLOSURE(initializer), argCount);
+      if (!IS_NIL(klass->initMethod)) {
+        return call(AS_CLOSURE(klass->initMethod), argCount);
       } else if (argCount != 0) {
         runtimeError("Expected 0 arguments but got %d", argCount);
         return false;
     //...
static void defineMethod(ObjString *name) {
   Value method = peek(0);
   ObjClass *klass = AS_CLASS(peek(1));
   tableSet(&klass->methods, name, method);
+  if (name == vm.initString) {
+    klass->initMethod = method;
+  }
   pop();
 }
 
```

## 2

In a dynamically typed language like Lox, a single callsite may invoke a variety of methods on a number of classes throughout a program’s execution. Even so, in practice, most of the time a callsite ends up calling the exact same method on the exact same class for the duration of the run. Most calls are actually not polymorphic even if the language says they can be.

How do advanced language implementations optimize based on that observation?

inline caching

## 3

When interpreting an OP_INVOKE instruction, the VM has to do two hash table lookups. First, it looks for a field that could shadow a method, and only if that fails does it look for a method. The former check is rarely useful—most fields do not contain functions. But it is necessary because the language says fields and methods are accessed using the same syntax, and fields shadow methods.

That is a language choice that affects the performance of our implementation. Was it the right choice? If Lox were your language, what would you do?

I would say it depends like most things in engineering everything is the tradeoff here the Lox author trade performance for flexibility the other choice would have been to not accept function has a field and just allow methods it would avoid the lookup in the fields table.
I would choose the option 1 for Lox because it is a dynamically typed language so having the flexibility to assign ever expression you want as a field feel more natural. 
