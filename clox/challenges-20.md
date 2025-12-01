# Challenges 20

## 1

In clox, we happen to only need keys that are strings, so the hash table we built is hardcoded for that key type. If we exposed hash tables to Lox users as a first-class collection, it would be useful to support different kinds of keys.

Add support for keys of the other primitive types: numbers, Booleans, and nil. Later, clox will support user-defined classes. 

Done in table.c and table.h and added test suite in table.test.c


If we want to support keys that are instances of those classes, what kind of complexity does that add?

i guess it would be tricky to know when to garbage collect the instance we would have to check if the instance is in a map to not garbage collect it.
We would have to think of a way of designing a hash algorithm for the instances as well and would have to think about identity, equality and mutablity.

## 2
Hash tables have a lot of knobs you can tweak that affect their performance. You decide whether to use separate chaining or open addressing. Depending on which fork in that road you take, you can tune how many entries are stored in each node, or the probing strategy you use. You control the hash function, load factor, and growth rate.

All of this variety wasn’t created just to give CS doctoral candidates something to publish theses on: each has its uses in the many varied domains and hardware scenarios where hashing comes into play. Look up a few hash table implementations in different open source systems, research the choices they made, and try to figure out why they did things that way.

Java use seperate chaining and Python open addressing with a random probing alogrithm.

## 3
Benchmarking a hash table is notoriously difficult. A hash table implementation may perform well with some keysets and poorly with others. It may work well at small sizes but degrade as it grows, or vice versa. It may choke when deletions are common, but fly when they aren’t. Creating benchmarks that accurately represent how your users will use the hash table is a challenge.

Write a handful of different benchmark programs to validate our hash table implementation. How does the performance vary between them? Why did you choose the specific test cases you chose?

I did two benchmark one with tombstone and one without tombstone and a second with tombstone. to measure the impact on the performance and suprisingly we've got good result with tombstone in the table.
if i had more time i would design two data set one with a lot of string that have collision and another without to measure the performance of our probing alogrithm.

:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000004 seconds
CPU time: 0.000003 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000003 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000002 seconds
CPU time: 0.000001 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000003 seconds with tombstone:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000004 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000005 seconds
CPU time: 0.000001 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000004 seconds
CPU time: 0.000001 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000001 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000002 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000001 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0:

CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000004 seconds
CPU time: 0.000003 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000004 seconds
CPU time: 0.000001 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000004 seconds
CPU time: 0.000003 seconds with tombstone
:clox cgombauld$ ./benchmark
CPU time: 0.000003 seconds
CPU time: 0.000002 seconds with tombstone


