#include "doubly-linked-list.c"
#include "./munit/munit.h"
#include <stdlib.h>
#include <string.h>

char *malloc_string(char *s) {
  size_t size = strlen(s) + 1;
  char *p = malloc(sizeof(char) * size);
  char *next = p;
  while (size != 0) {
    *next++ = *s++;
    size--;
  };
  return p;
}

static MunitResult insert_tests(const MunitParameter params[], void *fixture) {

  char *some_string = malloc_string("some string");
  char *a_string = malloc_string("a string");
  char *second_string = malloc_string("second string");
  char *third_string = malloc_string("third string");

  // insert when the doubly linked is empty
  struct Node *doubly_linked_list1 = malloc(sizeof(struct Node));
  doubly_linked_list1->next = NULL;
  doubly_linked_list1->previous = NULL;
  doubly_linked_list1->value = NULL;

  insert_tail(doubly_linked_list1, some_string);
  munit_assert_null(doubly_linked_list1->next);
  munit_assert_null(doubly_linked_list1->previous);
  munit_assert_string_equal(doubly_linked_list1->value, "some string");
  free(doubly_linked_list1);

  // insert when the doubly linked is not empty
  struct Node *doubly_linked_list2 = malloc(sizeof(struct Node));
  doubly_linked_list2->next = NULL;
  doubly_linked_list2->previous = NULL;
  doubly_linked_list2->value = a_string;

  insert_tail(doubly_linked_list2, some_string);
  munit_assert_null(doubly_linked_list2->previous);
  munit_assert_string_equal(doubly_linked_list2->value, "a string");
  munit_assert_null(doubly_linked_list2->next->next);
  munit_assert_null(doubly_linked_list2->next->previous->previous);
  munit_assert_string_equal(doubly_linked_list2->next->value, "some string");
  munit_assert_string_equal(doubly_linked_list2->next->previous->value,
                            "a string");
  free(doubly_linked_list2);

  // insert when the doubly linked is more than one
  struct Node *doubly_linked_list3 = malloc(sizeof(struct Node));
  doubly_linked_list3->next = NULL;
  doubly_linked_list3->previous = NULL;
  doubly_linked_list3->value = a_string;
  struct Node *next_node = malloc(sizeof(struct Node));
  next_node->value = second_string;
  next_node->next = NULL;
  next_node->previous = doubly_linked_list3;
  doubly_linked_list3->next = next_node;
  insert_tail(doubly_linked_list3, third_string);
  munit_assert_null(doubly_linked_list3->previous);
  munit_assert_string_equal(doubly_linked_list3->value, "a string");
  munit_assert_string_equal(doubly_linked_list3->next->value, "second string");
  munit_assert_string_equal(doubly_linked_list3->next->value, "second string");
  munit_assert_string_equal(doubly_linked_list3->next->previous->value,
                            "a string");
  munit_assert_null(doubly_linked_list3->next->previous->previous);
  munit_assert_string_equal(doubly_linked_list3->next->next->value,
                            "third string");
  munit_assert_null(doubly_linked_list3->next->next->next);
  munit_assert_string_equal(doubly_linked_list3->next->next->previous->value,
                            "second string");
  free(doubly_linked_list3);
  free(next_node);
  free(some_string);
  free(a_string);
  free(second_string);
  free(third_string);

  return MUNIT_OK;
}

static MunitResult delete_tests(const MunitParameter params[], void *fixture) {

  char *some_string = malloc_string("some string");
  // delete the first item value
  struct Node *doubly_linked_list1 = malloc(sizeof(struct Node));
  doubly_linked_list1->next = NULL;
  doubly_linked_list1->previous = NULL;
  doubly_linked_list1->value = some_string;
  delete_tail(doubly_linked_list1);
  munit_assert_null(doubly_linked_list1->value);
  munit_assert_null(doubly_linked_list1->previous);
  munit_assert_null(doubly_linked_list1->next);
  // delete tail when it is not the first item
  char *a_string = malloc_string("a_string");
  char *second_string = malloc_string("second string");

  doubly_linked_list1->value = a_string;
  struct Node *next_node1 = malloc(sizeof(struct Node));
  next_node1->value = second_string;
  next_node1->next = NULL;
  next_node1->previous = doubly_linked_list1;
  doubly_linked_list1->next = next_node1;
  delete_tail(doubly_linked_list1);
  munit_assert_null(doubly_linked_list1->previous);
  munit_assert_null(doubly_linked_list1->next);
  munit_assert_string_equal(doubly_linked_list1->value, "a_string");
  free(a_string);

  // delete tail when there is multiple item
  char *one = malloc_string("one");
  char *two = malloc_string("two");
  char *three = malloc_string("three");

  doubly_linked_list1->value = one;
  doubly_linked_list1->previous = NULL;

  struct Node *next_node2 = malloc(sizeof(struct Node));
  struct Node *next_node3 = malloc(sizeof(struct Node));
  doubly_linked_list1->next = next_node2;
  next_node2->value = two;
  next_node2->previous = doubly_linked_list1;
  next_node2->next = next_node3;
  next_node3->previous = next_node2;
  next_node3->value = three;
  next_node3->next = NULL;

  delete_tail(doubly_linked_list1);
  munit_assert_null(doubly_linked_list1->previous);
  munit_assert_string_equal(doubly_linked_list1->value, "one");
  munit_assert_string_equal(doubly_linked_list1->next->value, "two");
  munit_assert_string_equal(doubly_linked_list1->next->previous->value, "one");
  munit_assert_null(doubly_linked_list1->next->next);

  free(doubly_linked_list1);
  free(next_node2);

  return MUNIT_OK;
}

static MunitResult find_tests(const MunitParameter params[], void *fixture) {
  char *a_string = malloc_string("a string");
  char *second_string = malloc_string("second string");
  char *third_string = malloc_string("third string");
  struct Node *doubly_linked_list = malloc(sizeof(struct Node));
  doubly_linked_list->next = NULL;
  doubly_linked_list->previous = NULL;
  doubly_linked_list->value = a_string;
  struct Node *next_node = malloc(sizeof(struct Node));
  struct Node *next_node2 = malloc(sizeof(struct Node));
  next_node2->previous = next_node;
  next_node2->next = NULL;
  next_node2->value = third_string;
  next_node->value = second_string;
  next_node->next = next_node2;
  next_node->previous = doubly_linked_list;
  doubly_linked_list->next = next_node;
  // cannot find the index is looking for
  struct Node *not_found = find_by_index(doubly_linked_list, 5);
  munit_assert_null(not_found);

  // should find the first item
  struct Node *p1 = find_by_index(doubly_linked_list, 0);
  munit_assert_ptr_equal(p1, doubly_linked_list);

  struct Node *p2 = find_by_index(doubly_linked_list, 1);
  munit_assert_ptr_equal(p2, next_node);

  struct Node *p3 = find_by_index(doubly_linked_list, 2);
  munit_assert_ptr_equal(p3, next_node2);

  free(a_string);
  free(second_string);
  free(third_string);
  free(doubly_linked_list);
  free(next_node);
  free(next_node2);

  return MUNIT_OK;
}

MunitTest tests[] = {{
                         "/insert test",         /* name */
                         insert_tests,           /* test */
                         NULL,                   /* setup */
                         NULL,                   /* tear_down */
                         MUNIT_TEST_OPTION_NONE, /* options */
                         NULL                    /* parameters */
                     },
                     {
                         "/delete test",         /* name */
                         delete_tests,           /* test */
                         NULL,                   /* setup */
                         NULL,                   /* tear_down */
                         MUNIT_TEST_OPTION_NONE, /* options */
                         NULL                    /* parameters */
                     },
                     {
                         "/find test",           /* name */
                         find_tests,             /* test */
                         NULL,                   /* setup */
                         NULL,                   /* tear_down */
                         MUNIT_TEST_OPTION_NONE, /* options */
                         NULL                    /* parameters */
                     },
                     /* Mark the end of the array with an entry where the test
                      * function is NULL */
                     {NULL, NULL, NULL, NULL, MUNIT_TEST_OPTION_NONE, NULL}};

static const MunitSuite suite = {
    "/double-linked-list-test-suite ", /* name */
    tests,                             /* tests */
    NULL,                              /* suites */
    1,                                 /* iterations */
    MUNIT_SUITE_OPTION_NONE            /* options */
};

int main(int argc, char *argv[MUNIT_ARRAY_PARAM(argc + 1)]) {
  return munit_suite_main(&suite, (void *)"µnit", argc, argv);
}
