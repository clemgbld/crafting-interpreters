#include <stdlib.h>

struct Node {
  struct Node *next;
  struct Node *previous;
  char *value;
};
void insert_tail(struct Node *head, char *s) {
  if (head->value == NULL) {
    head->value = s;
    return;
  }
  for (; head->next != NULL; head = head->next)
    ;
  struct Node *node = malloc(sizeof(struct Node));
  node->next = NULL;
  node->value = s;
  node->previous = head;
  head->next = node;
}

void delete_tail(struct Node *head) {
  if (head->next == NULL) {
    free(head->value);
    head->value = NULL;
    return;
  }
  for (; head->next != NULL; head = head->next)
    ;
  head->previous->next = NULL;
  free(head->value);
  free(head);
};

struct Node *find_by_index(struct Node *head, int index) {
  for (int i = 0; head != NULL; i++, head = head->next) {
    if (i == index) {
      return head;
    };
  }
  return NULL;
}
