# CHALLENGES 29

## 1

A tenet of object-oriented programming is that a class should ensure new objects are in a valid state. In Lox, that means defining an initializer that populates the instance’s fields. Inheritance complicates invariants because the instance must be in a valid state according to all of the classes in the object’s inheritance chain.

The easy part is remembering to call super.init() in each subclass’s init() method. The harder part is fields. There is nothing preventing two classes in the inheritance chain from accidentally claiming the same field name. When this happens, they will step on each other’s fields and possibly leave you with an instance in a broken state.

If Lox was your language, how would you address this, if at all? If you would change the language, implement your change.

I would say that like JS, Python and Ruby i would not address that, maybe i could envisage to add private , public and protected field but it would require for consistency to add this for methods as well and could be a bit annoying for dynamically typed language but i would implement it like this:

```lox
class Doughnut {
    init(){
      this.#field = "private";
      this._field = "protected";
      this.field = "public"
      // same logic for the methods.
    }
}
```

## 2

Our copy-down inheritance optimization is valid only because Lox does not permit you to modify a class’s methods after its declaration. This means we don’t have to worry about the copied methods in the subclass getting out of sync with later changes to the superclass.

Other languages, like Ruby, do allow classes to be modified after the fact. How do implementations of languages like that support class modification while keeping method resolution efficient?

The repo is quite complicated but from what i understand Ruby use a global caching techique:

With 2 keys the class and the name of the method, if the method is found great we get the method in constant time.

If there is a cache miss then we traverse the inheritance linked list to find the method and put it in the cache.

Since ruby can "monkey patch" the method of a class, when that happen the entire cache is invalidated.

## 3

In the jlox chapter on inheritance, we had a challenge to implement the BETA language’s approach to method overriding. Solve the challenge again, but this time in clox. Here’s the description of the previous challenge:

In Lox, as in most other object-oriented languages, when looking up a method, we start at the bottom of the class hierarchy and work our way up—a subclass’s method is preferred over a superclass’s. In order to get to the superclass method from within an overriding method, you use super.

The language BETA takes the opposite approach. When you call a method, it starts at the top of the class hierarchy and works down. A superclass method wins over a subclass method. In order to get to the subclass method, the superclass method can call inner, which is sort of like the inverse of super. It chains to the next method down the hierarchy.

The superclass method controls when and where the subclass is allowed to refine its behavior. If the superclass method doesn’t call inner at all, then the subclass has no way of overriding or modifying the superclass’s behavior.

Take out Lox’s current overriding and super behavior, and replace it with BETA’s semantics. In short:

When calling a method on a class, the method highest on the class’s inheritance chain takes precedence.

Inside the body of a method, a call to inner looks for a method with the same name in the nearest subclass along the inheritance chain between the class containing the inner and the class of this. If there is no matching method, the inner call does nothing.

For example:

```lox
class Doughnut {
  cook() {
    print "Fry until golden brown.";
    inner();
    print "Place in a nice box.";
  }
}

class BostonCream < Doughnut {
  cook() {
    print "Pipe full of custard and coat with chocolate.";
  }
}

BostonCream().cook()
```

This should print:
Fry until golden brown.
Pipe full of custard and coat with chocolate.
Place in a nice box.

Since clox is about not just implementing Lox, but doing so with good performance, this time around try to solve the challenge with an eye towards efficiency.

I implemented the naive way of traversing the inheritance chain to find the expected inner method which is quite slow but the author managed a way to make an inner call as efficient as another method call and without increasing the memory of an ObjClass.

here the link of the author solution: https://github.com/munificent/craftinginterpreters/blob/master/note/answers/chapter29_superclasses/3.md

Here is the git diff:

```c
diff --git a/clox/chunk.h b/clox/chunk.h
index 73e031d..98470cf 100644
--- a/clox/chunk.h
+++ b/clox/chunk.h
@@ -20,7 +20,7 @@ typedef enum {
   OP_SET_UPVALUE,
   OP_GET_PROPERTY,
   OP_SET_PROPERTY,
-  OP_GET_SUPER,
+  OP_GET_INNER,
   OP_EQUAL,
   OP_GREATER,
   OP_LESS,
@@ -36,7 +36,7 @@ typedef enum {
   OP_LOOP,
   OP_CALL,
   OP_INVOKE,
-  OP_SUPER_INVOKE,
+  OP_INNER_INVOKE,
   OP_CLOSURE,
   OP_CLOSE_UPVALUE,
   OP_RETURN,
diff --git a/clox/compiler.c b/clox/compiler.c
index 348e315..ca50d50 100644
--- a/clox/compiler.c
+++ b/clox/compiler.c
@@ -528,28 +528,22 @@ static Token syntheticToken(const char *text) {
   return token;
 }
 
-static void super_(bool canAssign) {
+static void inner(bool canAssign) {
   (void)canAssign;
   if (currentClass == NULL) {
-    error("Can't use 'super' outside of a class.");
-  } else if (!currentClass->hasSuperclass) {
-    error("Can't use 'super' in a class with no superclass.");
+    error("Can't use 'inner' outside of a class.");
+  } else if (current->type != TYPE_METHOD &&
+             current->type != TYPE_INITIALIZER) {
+    error("can't use inner outside of a method");
   }
-  consume(TOKEN_DOT, "Expect '.' after 'super'.");
-  consume(TOKEN_IDENTIFIER, "Expect superclass method name.");
-  uint8_t name = identifierConstant(&parser.previous);
-
   namedVariable(syntheticToken("this"), false);
 
   if (match(TOKEN_LEFT_PAREN)) {
     uint8_t argCount = argumentList();
-    namedVariable(syntheticToken("super"), false);
-    emitBytes(OP_SUPER_INVOKE, name);
+    emitByte(OP_INNER_INVOKE);
     emitByte(argCount);
   } else {
-    namedVariable(syntheticToken("super"), false);
-
-    emitBytes(OP_GET_SUPER, name);
+    emitByte(OP_GET_INNER);
   }
 }
 
@@ -580,9 +574,6 @@ static void classDeclaration() {
     if (identifiersEqual(&className, &parser.previous)) {
       error("A class can't inherit from itselft");
     }
-    beginScope();
-    addLocal(syntheticToken("super"));
-    defineVariable(0);
     namedVariable(className, false);
     emitByte(OP_INHERIT);
     classCompiler.hasSuperclass = true;
@@ -595,9 +586,6 @@ static void classDeclaration() {
   }
   consume(TOKEN_RIGHT_BRACE, "Expect '}' after class body");
   emitByte(OP_POP);
-  if (classCompiler.hasSuperclass) {
-    endScope();
-  }
   currentClass = currentClass->enclosing;
 }
 
@@ -896,7 +884,7 @@ ParseRule rules[] = {
     [TOKEN_OR] = {NULL, or_, PREC_OR},
     [TOKEN_PRINT] = {NULL, NULL, PREC_NONE},
     [TOKEN_RETURN] = {NULL, NULL, PREC_NONE},
-    [TOKEN_SUPER] = {super_, NULL, PREC_NONE},
+    [TOKEN_INNER] = {inner, NULL, PREC_NONE},
     [TOKEN_THIS] = {this_, NULL, PREC_NONE},
     [TOKEN_TRUE] = {literal, NULL, PREC_NONE},
     [TOKEN_VAR] = {NULL, NULL, PREC_NONE},
diff --git a/clox/debug.c b/clox/debug.c
index f6bd304..ab9ee9b 100644
--- a/clox/debug.c
+++ b/clox/debug.c
@@ -50,6 +50,13 @@ static int invokeInstruction(const char *name, Chunk *chunk, int offset) {
   return offset + 3;
 }
 
+static int innerInvokeInstruction(const char *name, Chunk *chunk, int offset) {
+  uint8_t argCount = chunk->code[offset + 1];
+  printf("%-16s (%d args)", name, argCount);
+  printf("'\n");
+  return offset + 2;
+}
+
 int dissassembleInstruction(Chunk *chunk, int offset) {
   printf("%04d", offset);
   if (offset > 0 && chunk->lines[offset] == chunk->lines[offset - 1]) {
@@ -89,8 +96,8 @@ int dissassembleInstruction(Chunk *chunk, int offset) {
     return constantInstruction("OP_GET_PROPERTY", chunk, offset);
   case OP_SET_PROPERTY:
     return constantInstruction("OP_SET_PROPERTY", chunk, offset);
-  case OP_GET_SUPER:
-    return constantInstruction("OP_GET_SUPER", chunk, offset);
+  case OP_GET_INNER:
+    return simpleInstruction("OP_GET_INNER", offset);
   case OP_EQUAL:
     return simpleInstruction("OP_EQUAL", offset);
   case OP_LESS:
@@ -121,8 +128,8 @@ int dissassembleInstruction(Chunk *chunk, int offset) {
     return byteInstruction("OP_CALL", chunk, offset);
   case OP_INVOKE:
     return invokeInstruction("OP_INVOKE", chunk, offset);
-  case OP_SUPER_INVOKE:
-    return invokeInstruction("OP_SUPER_INVOKE", chunk, offset);
+  case OP_INNER_INVOKE:
+    return innerInvokeInstruction("OP_INNER_INVOKE", chunk, offset);
   case OP_CLOSURE: {
     offset++;
     uint8_t constant = chunk->code[offset++];
diff --git a/clox/memory.c b/clox/memory.c
index b64a28f..91ee938 100644
--- a/clox/memory.c
+++ b/clox/memory.c
@@ -58,6 +58,7 @@ static void blackenObject(Obj *object) {
     ObjClass *klass = (ObjClass *)object;
     markObject((Obj *)klass->name);
     markTable(&klass->methods);
+    markTable(&klass->classMethods);
     break;
   }
   case OBJ_CLOSURE: {
@@ -108,6 +109,7 @@ static void freeObject(Obj *object) {
   case OBJ_CLASS: {
     ObjClass *klass = (ObjClass *)object;
     freeTable(&klass->methods);
+    freeTable(&klass->classMethods);
     FREE(ObjClass, object);
     break;
   }
diff --git a/clox/object.c b/clox/object.c
index f116bbd..91a5687 100644
--- a/clox/object.c
+++ b/clox/object.c
@@ -41,7 +41,9 @@ ObjInstance *newInstance(ObjClass *klass) {
 ObjClass *newClass(ObjString *name) {
   ObjClass *klass = ALLOCATE_OBJ(ObjClass, OBJ_CLASS);
   klass->name = name;
+  klass->superclass = NULL;
   initTable(&klass->methods);
+  initTable(&klass->classMethods);
   return klass;
 }
 
diff --git a/clox/object.h b/clox/object.h
index 89fe95f..e4d48e4 100644
--- a/clox/object.h
+++ b/clox/object.h
@@ -77,10 +77,12 @@ typedef struct {
   int upvalueCount;
 } ObjClosure;
 
-typedef struct {
+typedef struct ObjClass {
   Obj obj;
   ObjString *name;
   Table methods;
+  Table classMethods;
+  struct ObjClass *superclass;
 } ObjClass;
 
 typedef struct {
diff --git a/clox/run.txt b/clox/run.txt
index acb7dc4..16cd566 100644
--- a/clox/run.txt
+++ b/clox/run.txt
@@ -1,19 +1,22 @@
-class A {
-  method() {
-    print "A method";
+class Doughnut {
+  cook() {
+    print "Fry until golden brown.";
+    inner();
+    print "Place in a nice box.";
   }
 }
 
-class B < A {
-  method() {
-    print "B method";
+class X < Doughnut {
+cook() {
+    print inner();
   }
+}
 
-  test() {
-    super.method();
+class BostonCream < X {
+  cook() {
+    print "Pipe full of custard and coat with chocolate.";
   }
 }
 
-class C < B {}
+BostonCream().cook();
 
-C().test();
diff --git a/clox/scanner.c b/clox/scanner.c
index d15829e..0d850fb 100644
--- a/clox/scanner.c
+++ b/clox/scanner.c
@@ -135,6 +135,15 @@ static TokenType identifierType() {
     }
     break;
   case 'i':
+    if (scanner.current - scanner.start > 1) {
+      switch (scanner.start[1]) {
+      case 'f':
+        return checkKeyword(2, 0, "", TOKEN_IF);
+      case 'n':
+        return checkKeyword(2, 3, "ner", TOKEN_INNER);
+      }
+    }
+    break;
     return checkKeyword(1, 1, "f", TOKEN_IF);
   case 'n':
     return checkKeyword(1, 2, "il", TOKEN_NIL);
@@ -144,8 +153,6 @@ static TokenType identifierType() {
     return checkKeyword(1, 4, "rint", TOKEN_PRINT);
   case 'r':
     return checkKeyword(1, 5, "eturn", TOKEN_RETURN);
-  case 's':
-    return checkKeyword(1, 4, "uper", TOKEN_SUPER);
   case 't':
     if (scanner.current - scanner.start > 1) {
       switch (scanner.start[1]) {
diff --git a/clox/scanner.h b/clox/scanner.h
index e5c89ca..bc42692 100644
--- a/clox/scanner.h
+++ b/clox/scanner.h
@@ -41,6 +41,7 @@ typedef enum {
   TOKEN_PRINT,
   TOKEN_RETURN,
   TOKEN_SUPER,
+  TOKEN_INNER,
   TOKEN_THIS,
   TOKEN_TRUE,
   TOKEN_VAR,
diff --git a/clox/vm.c b/clox/vm.c
index 7232a7b..8eb3e93 100644
--- a/clox/vm.c
+++ b/clox/vm.c
@@ -182,10 +182,46 @@ static void closeUpvalues(Value *last) {
 static void defineMethod(ObjString *name) {
   Value method = peek(0);
   ObjClass *klass = AS_CLASS(peek(1));
-  tableSet(&klass->methods, name, method);
+  if (!tableGet(&klass->methods, name, &NIL_VAL)) {
+    tableSet(&klass->methods, name, method);
+  }
+  tableSet(&klass->classMethods, name, method);
   pop();
 }
 
+static bool innerGet(Value *method) {
+  ObjClosure *currentClosure = vm.frames[vm.frameCount - 1].closure;
+  ObjString *name = currentClosure->function->name;
+  ObjClass *currentClass = AS_INSTANCE(peek(0))->klass;
+
+  *method = NIL_VAL;
+
+  if (tableGet(&currentClass->classMethods, name, method)) {
+    if (AS_CLOSURE(*method) == currentClosure) {
+      return false;
+    }
+  }
+
+  Value currentMethod = *method;
+  while (true) {
+    currentClass = currentClass->superclass;
+    if (!tableGet(&currentClass->classMethods, name, &currentMethod)) {
+      continue;
+    }
+    if (AS_CLOSURE(currentMethod) == currentClosure) {
+      break;
+    } else {
+      *method = currentMethod;
+    }
+  }
+
+  if (IS_NIL(*method)) {
+    return false;
+  }
+
+  return true;
+}
+
 static bool isFalsey(Value value) {
   return IS_NIL(value) || (IS_BOOL(value) && !AS_BOOL(value));
 }
@@ -368,14 +404,20 @@ static InterpretResult run() {
       break;
     }
 
-    case OP_GET_SUPER: {
-      ObjString *name = READ_STRING();
-      ObjClass *superclass = AS_CLASS(pop());
-
-      if (!bindMethod(superclass, name)) {
+    case OP_GET_INNER: {
+      Value innerMethod;
+      if (!innerGet(&innerMethod)) {
+        runtimeError(
+            "Can only use inner when the superclass has a subclass "
+            "with the same method name %s.",
+            vm.frames[vm.frameCount - 1].closure->function->name->chars);
         return INTERPRET_RUNTIME_ERROR;
       }
 
+      ObjBoundMethod *bound = newBoundMethod(peek(0), AS_CLOSURE(innerMethod));
+      pop();
+      push(OBJ_VAL(bound));
+
       break;
     }
 
@@ -469,17 +511,21 @@ static InterpretResult run() {
       frame = &vm.frames[vm.frameCount - 1];
       break;
     }
-    case OP_SUPER_INVOKE: {
-      ObjString *method = READ_STRING();
-      int argCount = READ_BYTE();
-      ObjClass *superclass = AS_CLASS(pop());
-      if (!invokeFromClass(superclass, method, argCount)) {
+    case OP_INNER_INVOKE: {
+      Value innerMethod;
+      if (!innerGet(&innerMethod)) {
+        runtimeError(
+            "Can only use inner when the superclass has a subclass "
+            "with the same method name %s.",
+            vm.frames[vm.frameCount - 1].closure->function->name->chars);
         return INTERPRET_RUNTIME_ERROR;
       }
-
+      int argCount = READ_BYTE();
+      call(AS_CLOSURE(innerMethod), argCount);
       frame = &vm.frames[vm.frameCount - 1];
       break;
     }
+
     case OP_CLOSURE: {
       ObjFunction *function = AS_FUNCTION(READ_CONSTANT());
       ObjClosure *closure = newClosure(function);
@@ -524,6 +570,7 @@ static InterpretResult run() {
       }
       ObjClass *subclass = AS_CLASS(peek(0));
       tableAddAll(&AS_CLASS(superclass)->methods, &subclass->methods);
+      subclass->superclass = AS_CLASS(superclass);
       pop(); // Subclass
       break;
     }
```
