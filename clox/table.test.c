#include "table.h"
#include "munit/munit.h"
#include "object.h"
#include "value.h"
#include <stdio.h>

static uint32_t hashString(const char *key, int length) {
  uint32_t hash = 2166136261u;
  for (int i = 0; i < length; i++) {
    hash ^= (uint32_t)key[i];
    hash *= 16777619;
  }
  return hash;
}

/* getLine */
static MunitResult can_add_to_table_with_string_key() {
  ObjString str1 = {{OBJ_STRING, NULL}, 6, "string", hashString("string", 6)};
  ObjString str2 = {{OBJ_STRING, NULL}, 5, "hello", hashString("string", 5)};
  ObjString str3 = {{OBJ_STRING, NULL}, 5, "world", hashString("world", 5)};
  ObjString str4 = {{OBJ_STRING, NULL}, 1, "w", hashString("w", 1)};
  Table table;
  initTable(&table);
  tableSet(&table, &OBJ_VAL(&str1), NUMBER_VAL(1.0));
  tableSet(&table, &OBJ_VAL(&str2), NUMBER_VAL(2.0));
  tableSet(&table, &OBJ_VAL(&str3), NUMBER_VAL(3.0));

  Value result1;
  Value result2;
  Value result3;
  bool isFound1 = tableGet(&table, &OBJ_VAL(&str1), &result1);
  bool isFound2 = tableGet(&table, &OBJ_VAL(&str2), &result2);
  bool isFound3 = tableGet(&table, &OBJ_VAL(&str3), &result3);

  munit_assert_true(isFound1);
  munit_assert_true(isFound2);
  munit_assert_true(isFound3);
  munit_assert_false(tableGet(&table, &OBJ_VAL(&str4), NULL));

  munit_assert_double(AS_NUMBER(result1), ==, 1.0);
  munit_assert_double(AS_NUMBER(result2), ==, 2.0);
  munit_assert_double(AS_NUMBER(result3), ==, 3.0);

  freeTable(&table);
  return MUNIT_OK;
}

static MunitResult can_delete_from_table_with_string_key() {
  ObjString str1 = {{OBJ_STRING, NULL}, 6, "string", hashString("string", 6)};
  Table table;
  initTable(&table);
  tableSet(&table, &OBJ_VAL(&str1), NUMBER_VAL(1.0));

  munit_assert_true(tableDelete(&table, &OBJ_VAL(&str1)));
  munit_assert_false(tableDelete(&table, &OBJ_VAL(&str1)));
  munit_assert_false(tableGet(&table, &OBJ_VAL(&str1), NULL));

  freeTable(&table);
  return MUNIT_OK;
}

static MunitResult can_add_to_table_with_number_key() {
  Table table;
  initTable(&table);
  tableSet(&table, &NUMBER_VAL(1.0), NUMBER_VAL(1.0));
  tableSet(&table, &NUMBER_VAL(2.0), NUMBER_VAL(2.0));
  tableSet(&table, &NUMBER_VAL(3.0), NUMBER_VAL(3.0));

  Value result1;
  Value result2;
  Value result3;
  bool isFound1 = tableGet(&table, &NUMBER_VAL(1.0), &result1);
  bool isFound2 = tableGet(&table, &NUMBER_VAL(2.0), &result2);
  bool isFound3 = tableGet(&table, &NUMBER_VAL(3.0), &result3);

  munit_assert_true(isFound1);
  munit_assert_true(isFound2);
  munit_assert_true(isFound3);
  munit_assert_false(tableGet(&table, &NUMBER_VAL(4.0), NULL));

  munit_assert_double(AS_NUMBER(result1), ==, 1.0);
  munit_assert_double(AS_NUMBER(result2), ==, 2.0);
  munit_assert_double(AS_NUMBER(result3), ==, 3.0);

  freeTable(&table);

  return MUNIT_OK;
}

static MunitResult can_delete_from_table_with_number_key() {
  Table table;
  initTable(&table);
  tableSet(&table, &NUMBER_VAL(1.0), NUMBER_VAL(1.0));

  munit_assert_true(tableDelete(&table, &NUMBER_VAL(1.0)));
  munit_assert_false(tableDelete(&table, &NUMBER_VAL(1.0)));
  munit_assert_false(tableGet(&table, &NUMBER_VAL(1.0), NULL));

  freeTable(&table);
  return MUNIT_OK;
}

static MunitResult can_add_to_table_with_boolean_key() {
  Table table;
  initTable(&table);
  tableSet(&table, &BOOL_VAL(true), NUMBER_VAL(1.0));
  tableSet(&table, &BOOL_VAL(false), NUMBER_VAL(2.0));

  Value result1;
  Value result2;
  bool isFound1 = tableGet(&table, &BOOL_VAL(true), &result1);
  bool isFound2 = tableGet(&table, &BOOL_VAL(false), &result2);

  munit_assert_true(isFound1);
  munit_assert_true(isFound2);

  munit_assert_double(AS_NUMBER(result1), ==, 1.0);
  munit_assert_double(AS_NUMBER(result2), ==, 2.0);

  freeTable(&table);

  return MUNIT_OK;
}

static MunitResult can_delete_from_table_with_boolean_key() {
  Table table;
  initTable(&table);
  tableSet(&table, &BOOL_VAL(true), NUMBER_VAL(1.0));

  munit_assert_true(tableDelete(&table, &BOOL_VAL(true)));
  munit_assert_false(tableDelete(&table, &BOOL_VAL(true)));
  munit_assert_false(tableGet(&table, &BOOL_VAL(true), NULL));

  freeTable(&table);
  return MUNIT_OK;
}

static MunitResult can_add_to_table_with_nil_key() {
  Table table;
  initTable(&table);
  tableSet(&table, &NIL_VAL, NUMBER_VAL(1.0));

  Value result1;
  bool isFound1 = tableGet(&table, &NIL_VAL, &result1);

  munit_assert_true(isFound1);

  munit_assert_double(AS_NUMBER(result1), ==, 1.0);

  freeTable(&table);

  return MUNIT_OK;
}

static MunitResult can_delete_from_table_with_nil_key() {
  Table table;
  initTable(&table);
  tableSet(&table, &NIL_VAL, NUMBER_VAL(1.0));

  munit_assert_true(tableDelete(&table, &NIL_VAL));
  munit_assert_false(tableDelete(&table, &NIL_VAL));
  munit_assert_false(tableGet(&table, &NIL_VAL, NULL));

  freeTable(&table);
  return MUNIT_OK;
}

// Define test suite
static MunitTest tests[] = {
    {"/can add to table with string key", can_add_to_table_with_string_key,
     NULL, NULL, MUNIT_TEST_OPTION_NONE, NULL},
    {"/can delete from table with string key",
     can_delete_from_table_with_string_key, NULL, NULL, MUNIT_TEST_OPTION_NONE,
     NULL},
    {"/can add to table with number key", can_add_to_table_with_number_key,
     NULL, NULL, MUNIT_TEST_OPTION_NONE, NULL},
    {"/can delete from table with number key",
     can_delete_from_table_with_number_key, NULL, NULL, MUNIT_TEST_OPTION_NONE,
     NULL},
    {"/can add to table with boolean key", can_add_to_table_with_boolean_key,
     NULL, NULL, MUNIT_TEST_OPTION_NONE, NULL},
    {"/can delete from table with boolean key",
     can_delete_from_table_with_boolean_key, NULL, NULL, MUNIT_TEST_OPTION_NONE,
     NULL},
    {"/can add to table with nil key", can_add_to_table_with_nil_key, NULL,
     NULL, MUNIT_TEST_OPTION_NONE, NULL},
    {"/can delete from table with nil key", can_delete_from_table_with_nil_key,
     NULL, NULL, MUNIT_TEST_OPTION_NONE, NULL},
    {NULL, NULL, NULL, NULL, MUNIT_TEST_OPTION_NONE, NULL} // Sentinel
};

// Define MUnit suite
static const MunitSuite suite = {"/table_test_suite", tests, NULL, 1,
                                 MUNIT_SUITE_OPTION_NONE};

// Main function
int main(int argc, char *argv[]) {
  return munit_suite_main(&suite, NULL, argc, argv);
}
