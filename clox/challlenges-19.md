# challenges 19

## 1

Each string requires two separate dynamic allocations—one for the ObjString and a second for the character array. Accessing the characters from a value requires two pointer indirections, which can be bad for performance. A more efficient solution relies on a technique called flexible array members. Use that to store the ObjString and its character array in a single contiguous allocation.

in object.h

```c
struct ObjString {
  Obj obj;
  int length;
  char chars[];
};

ObjString *takeString(const char chars[], int lengths[], int numberOfString);

ObjString *copyString(const char *chars, int length);
```
```
```

in object.c

```c 
static ObjString *allocateString(const char *strs[], const int lengths[],
                                 int numberOfStr) {
  int length = 0;
  for (int i = 0; i < numberOfStr; i++) {
    length += lengths[i];
  }
  ObjString *string =
      reallocate(NULL, 0, sizeof(ObjString) + (length + 1) * sizeof(char));
  string->obj.type = OBJ_STRING;
  string->obj.next = vm.objects;
  vm.objects = (Obj *)string;
  string->length = length;

  int totalLength = 0;
  for (int i = 0; i < numberOfStr; i++) {
    totalLength = totalLength + lengths[i];
    int k = 0;
    for (int j = totalLength - lengths[i]; j < totalLength; j++) {
      string->chars[j] = *(strs[i] + k);
      k++;
    }
  }

  string->chars[length] = '\0';

  return string;
}

ObjString *takeString(const char *strs[], const int lengths[],
                      int numberOfStr) {
  return allocateString(strs, lengths, numberOfStr);
}

ObjString *copyString(const char *chars, int length) {
  const char *strs[] = {chars};
  const int lengths[] = {length};
  return allocateString(strs, lengths, 1);
}
```
```
```

in vm.c

```c
static void concatenate() {
  ObjString *b = AS_STRING(pop());
  ObjString *a = AS_STRING(pop());
  const char *strs[] = {a->chars, b->chars};
  const int lengths[] = {a->length, b->length};
  ObjString *result = takeString(strs, lengths, 2);
  push(OBJ_VAL(result));
};
```
```
```

in memory.c

```c
static void freeObject(Obj *object) {
  switch (object->type) {
  case OBJ_STRING: {
    ObjString *string = (ObjString *)object;
    reallocate(NULL, sizeof(ObjString) + (string->length + 1) * sizeof(char),0)
    break;
  }
  }
}
```

## 2

When we create the ObjString for each string literal, we copy the characters onto the heap. That way, when the string is later freed, we know it is safe to free the characters too.

This is a simpler approach but wastes some memory, which might be a problem on very constrained devices. Instead, we could keep track of which ObjStrings own their character array and which are “constant strings” that just point back to the original source string or some other non-freeable location. Add support for this


in object.h

```c

#define AS_CSTRING(value) (((ObjString *)AS_OBJ(value))->as.chars)

typedef enum { CONSTANT, OWNED } StringType;

struct ObjString {
  Obj obj;
  int length;
  StringType stringType;
  union {
    char *chars;
    const char *constant;
  } as;
};

ObjString *createConstantString(const char *chars, int length);

const char *getString(ObjString *string);
```
```
```

in object.c

```c
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
```
```
```

in vm.c

```c
static void concatenate() {
  ObjString *b = AS_STRING(pop());
  ObjString *a = AS_STRING(pop());

  int length = a->length + b->length;
  char *chars = ALLOCATE(char, length + 1);
  memcpy(chars, getString(a), a->length);
  memcpy(chars + a->length, getString(b), b->length);
  chars[length] = '\0';
  ObjString *result = takeString(chars, length);
  push(OBJ_VAL(result));
};
```
```
```

in memory.c

```c
static void freeObject(Obj *object) {
  switch (object->type) {
  case OBJ_STRING: {
    ObjString *string = (ObjString *)object;
    if (string->stringType == OWNED) {
      FREE_ARRAY(char, string->as.chars, string->length + 1);
    }
    FREE(ObjString, object);
    break;
  }
  }
}
```
```
```

in value.c

```c
bool valuesEqual(Value a, Value b) {
  if (a.type != b.type)
    return false;
  switch (a.type) {
  case VAL_BOOL:
    return AS_BOOL(a) == AS_BOOL(b);
  case VAL_NIL:
    return true;
  case VAL_NUMBER:
    return AS_NUMBER(a) == AS_NUMBER(b);
  case VAL_OBJ: {
    ObjString *aString = AS_STRING(a);
    ObjString *bString = AS_STRING(b);
    return aString->length == bString->length &&
           memcmp(getString(aString), getString(bString), aString->length) == 0;
  }
  default:
    return false; // Unreachable
  }
}
```
```
```

in compiler.c

```c
static void string() {
  emitConstant(OBJ_VAL(createConstantString(parser.previous.start + 1,
                                            parser.previous.length - 2)));
}
```
```
```

## 3

If Lox was your language, what would you have it do when a user tries to use + with one string operand and the other some other type? Justify your choice. 

I would convert convert the non string operand into a string automatically for convenience sake.
Because we often do operation like "string" + 1
The trade off is convenience vs type safety

What do other languages do?
Javascript automatically coerce the type and Java as well via the method toString() , but C for example as a lower level language doesn't allow that.
