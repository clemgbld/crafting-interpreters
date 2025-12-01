#include "table.h"
#include "memory.h"
#include "object.h"
#include "value.h"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

double TABLE_MAX_LOAD = 0.75;

void initTable(Table *table) {
  table->count = 0;
  table->capacity = 0;
  table->entries = NULL;
}

void freeTable(Table *table) {
  FREE_ARRAY(Entry, table->entries, table->capacity);
  initTable(table);
}

static uint32_t getHash(Value value) {
  switch (value.type) {
  case VAL_NIL:
    return 0;
  case VAL_BOOL:
    return AS_BOOL(value) ? 1231 : 1237;
  case VAL_NUMBER:
    return AS_NUMBER(value);
  case VAL_OBJ: {
    switch (AS_OBJ(value)->type) {
    case OBJ_STRING:
      return AS_STRING(value)->hash;
    default:
      fprintf(stderr,
              "Unreachable statement reachted in getHash for type %d \n",
              value.type);
      exit(EXIT_FAILURE); // unreachable
    }
  }
  default:
    fprintf(stderr, "Unreachable statement reachted in getHash for type %d \n",
            value.type);
    exit(EXIT_FAILURE); // unreachable
  }
}

static Entry *findEntry(Entry *entries, int capacity, Value key) {
  uint32_t index = getHash(key) % capacity;
  Entry *tombstone = NULL;
  for (;;) {
    Entry *entry = &entries[index];
    if (entry->key == NULL) {
      if (IS_NIL(entry->value)) {
        return tombstone != NULL ? tombstone : entry;
      } else {
        if (tombstone == NULL) {
          tombstone = entry;
        }
      }
    } else if (valuesEqual(*entry->key, key)) {
      return entry;
    }
    index = (index + 1) % capacity;
  }
}

static bool isStringKeyEqual(Value key, const char *chars, int length,
                             uint32_t hash) {
  ObjString *stringKey = AS_STRING(key);
  return stringKey->length == length && stringKey->hash == hash &&
         memcmp(stringKey->chars, chars, length) == 0;
}

ObjString *tableFindString(Table *table, const char *chars, int length,
                           uint32_t hash) {
  if (table->count == 0)
    return NULL;
  uint32_t index = hash % table->capacity;

  for (;;) {
    Entry *entry = &table->entries[index];
    if (entry->key == NULL) {
      if (IS_NIL(entry->value))
        return NULL;
    } else if (isStringKeyEqual(*entry->key, chars, length, hash)) {
      return AS_STRING(*entry->key);
    }
    index = (index + 1) % table->capacity;
  }
}

static void adjustCapacity(Table *table, int capacity) {
  Entry *entries = ALLOCATE(Entry, capacity);
  for (int i = 0; i < capacity; i++) {
    entries[i].key = NULL;
    entries[i].value = NIL_VAL;
  }

  table->count = 0;
  for (int i = 0; i < table->capacity; i++) {
    Entry *entry = &table->entries[i];
    if (entry->key == NULL)
      continue;

    Entry *dest = findEntry(entries, capacity, *entry->key);
    dest->key = entry->key;
    dest->value = entry->value;
    table->count++;
  }
  FREE_ARRAY(Entry, table->entries, table->capacity);
  table->entries = entries;
  table->capacity = capacity;
};

bool tableSet(Table *table, Value *key, Value value) {
  if (table->count + 1 > table->capacity * TABLE_MAX_LOAD) {
    int capacity = GROW_CAPACITY(table->capacity);
    adjustCapacity(table, capacity);
  }
  Entry *entry = findEntry(table->entries, table->capacity, *key);
  bool isNewKey = entry->key == NULL;
  if (isNewKey && IS_NIL(entry->value)) {
    table->count++;
  }
  entry->key = key;
  entry->value = value;
  return isNewKey;
};

void tableAddAll(Table *from, Table *to) {
  for (int i = 0; i < from->capacity; i++) {
    Entry *entry = &from->entries[i];
    if (entry->key != NULL) {
      tableSet(to, entry->key, entry->value);
    }
  }
}

bool tableGet(Table *table, Value *key, Value *value) {
  if (table->count == 0)
    return false;
  Entry *entry = findEntry(table->entries, table->capacity, *key);
  if (entry->key == NULL)
    return false;
  *value = entry->value;
  return true;
}

bool tableDelete(Table *table, Value *key) {
  if (table->count == 0)
    return false;

  Entry *entry = findEntry(table->entries, table->capacity, *key);
  if (entry->key == NULL)
    return false;

  entry->key = NULL;
  entry->value = BOOL_VAL(true);

  return true;
}
