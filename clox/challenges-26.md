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

