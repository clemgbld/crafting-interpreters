#include "object.h"
#include "table.h"
#include "value.h"
#include <stdio.h>
#include <time.h>

static uint32_t hashString(const char *key, int length) {
  uint32_t hash = 2166136261u;
  for (int i = 0; i < length; i++) {
    hash ^= (uint32_t)key[i];
    hash *= 16777619;
  }
  return hash;
}

void benchmarkWithoutTombstone() {
  ObjString strs[] = {
      {{OBJ_STRING, NULL}, 5, "apple", hashString("apple", 5)},
      {{OBJ_STRING, NULL}, 4, "book", hashString("book", 4)},
      {{OBJ_STRING, NULL}, 3, "cat", hashString("cat", 3)},
      {{OBJ_STRING, NULL}, 3, "dog", hashString("dog", 3)},
      {{OBJ_STRING, NULL}, 8, "elephant", hashString("elephant", 8)},
      {{OBJ_STRING, NULL}, 11, "costarring", hashString("costarring", 11)},
      {{OBJ_STRING, NULL}, 6, "guitar", hashString("guitar", 6)},
      {{OBJ_STRING, NULL}, 6, "liquid", hashString("liquid", 6)},
      {{OBJ_STRING, NULL}, 6, "island", hashString("island", 6)},
      {{OBJ_STRING, NULL}, 6, "jungle", hashString("jungle", 6)},
      {{OBJ_STRING, NULL}, 7, "kitchen", hashString("kitchen", 7)},
      {{OBJ_STRING, NULL}, 9, "declinate", hashString("declinate", 9)},
      {{OBJ_STRING, NULL}, 8, "mountain", hashString("mountain", 8)},
      {{OBJ_STRING, NULL}, 5, "night", hashString("night", 5)},
      {{OBJ_STRING, NULL}, 5, "ocean", hashString("ocean", 5)},
      {{OBJ_STRING, NULL}, 6, "planet", hashString("planet", 6)},
      {{OBJ_STRING, NULL}, 5, "queen", hashString("queen", 5)},
      {{OBJ_STRING, NULL}, 5, "river", hashString("river", 5)},
      {{OBJ_STRING, NULL}, 3, "sun", hashString("sun", 3)},
      {{OBJ_STRING, NULL}, 4, "tree", hashString("tree", 4)},
      {{OBJ_STRING, NULL}, 8, "universe", hashString("universe", 8)},
      {{OBJ_STRING, NULL}, 7, "volcano", hashString("volcano", 7)},
      {{OBJ_STRING, NULL}, 6, "window", hashString("window", 6)},
      {{OBJ_STRING, NULL}, 11, "machinists", hashString("machinists", 11)},
      {{OBJ_STRING, NULL}, 6, "yellow", hashString("yellow", 6)},
      {{OBJ_STRING, NULL}, 5, "zebra", hashString("zebra", 5)}};
  Table table;
  initTable(&table);

  for (int i = 0; i < 26; i++) {
    tableSet(&table, &OBJ_VAL(&strs[i]), NUMBER_VAL(0.0));
  }

  clock_t start = clock();

  for (int i = 0; i < 26; i++) {
    tableGet(&table, &OBJ_VAL(&strs[i]), &NIL_VAL);
  }

  clock_t end = clock();
  double cpu_time = ((double)(end - start)) / CLOCKS_PER_SEC;
  printf("CPU time: %f seconds\n", cpu_time);
}

static void benchmarkWithTombstone() {
  ObjString strs[] = {
      {{OBJ_STRING, NULL}, 5, "apple", hashString("apple", 5)},
      {{OBJ_STRING, NULL}, 4, "book", hashString("book", 4)},
      {{OBJ_STRING, NULL}, 3, "cat", hashString("cat", 3)},
      {{OBJ_STRING, NULL}, 3, "dog", hashString("dog", 3)},
      {{OBJ_STRING, NULL}, 8, "elephant", hashString("elephant", 8)},
      {{OBJ_STRING, NULL}, 11, "costarring", hashString("costarring", 11)},
      {{OBJ_STRING, NULL}, 6, "guitar", hashString("guitar", 6)},
      {{OBJ_STRING, NULL}, 6, "liquid", hashString("liquid", 6)},
      {{OBJ_STRING, NULL}, 6, "island", hashString("island", 6)},
      {{OBJ_STRING, NULL}, 6, "jungle", hashString("jungle", 6)},
      {{OBJ_STRING, NULL}, 7, "kitchen", hashString("kitchen", 7)},
      {{OBJ_STRING, NULL}, 9, "declinate", hashString("declinate", 9)},
      {{OBJ_STRING, NULL}, 8, "mountain", hashString("mountain", 8)},
      {{OBJ_STRING, NULL}, 5, "night", hashString("night", 5)},
      {{OBJ_STRING, NULL}, 5, "ocean", hashString("ocean", 5)},
      {{OBJ_STRING, NULL}, 6, "planet", hashString("planet", 6)},
      {{OBJ_STRING, NULL}, 5, "queen", hashString("queen", 5)},
      {{OBJ_STRING, NULL}, 5, "river", hashString("river", 5)},
      {{OBJ_STRING, NULL}, 3, "sun", hashString("sun", 3)},
      {{OBJ_STRING, NULL}, 4, "tree", hashString("tree", 4)},
      {{OBJ_STRING, NULL}, 8, "universe", hashString("universe", 8)},
      {{OBJ_STRING, NULL}, 7, "volcano", hashString("volcano", 7)},
      {{OBJ_STRING, NULL}, 6, "window", hashString("window", 6)},
      {{OBJ_STRING, NULL}, 11, "machinists", hashString("machinists", 11)},
      {{OBJ_STRING, NULL}, 6, "yellow", hashString("yellow", 6)},
      {{OBJ_STRING, NULL}, 5, "zebra", hashString("zebra", 5)}};
  Table table;
  initTable(&table);

  for (int i = 0; i < 26; i++) {
    tableSet(&table, &OBJ_VAL(&strs[i]), NUMBER_VAL(0.0));
  }

  tableDelete(&table, &OBJ_VAL(&strs[5]));
  tableDelete(&table, &OBJ_VAL(&strs[11]));
  tableDelete(&table, &OBJ_VAL(&strs[3]));
  tableDelete(&table, &OBJ_VAL(&strs[24]));
  tableDelete(&table, &OBJ_VAL(&strs[10]));
  tableDelete(&table, &OBJ_VAL(&strs[9]));

  clock_t start = clock();

  for (int i = 0; i < 26; i++) {
    tableGet(&table, &OBJ_VAL(&strs[i]), &NIL_VAL);
  }

  clock_t end = clock();
  double cpu_time = ((double)(end - start)) / CLOCKS_PER_SEC;
  printf("CPU time: %f seconds with tombstone\n", cpu_time);
}

int main(void) {
  benchmarkWithoutTombstone();
  benchmarkWithTombstone();
  return 0;
}

// clang memory.c value.c chunk.c debug.c vm.c compiler.c scanner.c object.c
// table.c benchmark-table.c -o benchmark
