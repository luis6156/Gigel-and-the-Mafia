# Gigel and Mafia - Copyright Micu Florian-Luis 321CA 2021

## About

Algorithm Analysis
2020-2021

<https://curs.upb.ro/pluginfile.php/479306/mod_resource/content/1/AA___Tema_2__Copy_%20%283%29.pdf>

This homework contains three tasks and a bonus, in which we need to create some
algorithms that can a transform a problem into a SAT problem. In order to 
understand my implementatiton, I will explain every task in detail.

The implementation was done in Java. All of the complexities were calculated
by looking at the for loops. 

Useful notations:
N - number of families;
M - number of relationships(or non relationships);
K - number of spies/size of clique;

## Task 1

In this task, I have to assign to every node a spy such that no two families 
that have a relationship will share the same spy.

### Reading the data

Using the Scanner object, I put into variables the number of nodes N 
(families), edges M (total relationships) and spies K. After this, I put into a
list of lists every edge, starting from index 0. More precisely, for 6 families
and the relationship between the families 1 and 2, the list of lists would have
its size equal to 6, and list 0 would have the element 1 (adjusted to start 
from index 0).

Every node will have K variables, noted in ascending order, where every 
variable assigned to the node will represent a particular spy assigned to that
node (eg. K = 3, node 1 will have the variables 1, 2, 3; node 2 will have the 
variables 4, 5, 6 etc.).

### Formulate Oracle Question

### First Clause

Every two nodes that have an edge should not be assigned the same spy. 

Rephrasal: 
"Between any two nodes that have an edge, spy 1 or spy 2 or spy 3 or ... or
spy K can only be assigned to one of the nodes".

Example:
Input: Node 1 and 2 have an edge and there are 3 spies.

=> (¬1 V ¬4) ∧ (¬2 V ¬5) ∧ (¬3 V ¬6)

Complexity: M * M * K

Total clauses: spies * edges

### Second Clause

Every node should not be left without a spy.

Rephrasal: 
"Spy 1 or spy 2 or ... or spy K should be assigned to node T, where 1<=T<=N".

Example:
There are 3 spies => Second clause for node 1 is: 1 V 2 V 3

Complexity: N * K

Total clauses: number of nodes

### Third Clause:

A node can have at most one spy.

Rephrasal:
"If a node has the spy T, then it cannot also have the spy I, where 1<=T<=K,
1<=I<=K and I != T."

There are 3 spies => Third clause for node 1 is: (¬1 V ¬2) ∧ (¬1 V ¬3) 
∧ (¬2 V ¬3)

Complexity: N * K * (K - 1) / 2

Total clauses: nodes * spies * (spies - 1) / 2 


The total number of variables is N * M. The total number of clauses is the sum
of all the total clauses calculated for each clause.

### Decipher Oracle

I create a list of integers names "solutions", where I will put only the 
positive values given by the Oracle if the answer is "True".

### Write Answer

I write to the output file the outcome of the Oracle (True/False) and if the
result is "True", I write to the output file the variables that are present in 
"solutions" mod number of spies (if the number % spies == 0, I write to the
output the number of spies). By doing this, I can easily determine which spy 
belongs where, as there are only K spies, and every family NEEDS to have a spy.

## Task 2

After successfully assigning each family a spy, I have to determine the biggest
network of connected families. This can be looked as finding the biggest clique
in a graph.

### Reading the data

The procedure is the same from the previous task, however this time I have to
calculate the total number of nodes that are not connected. In a connected 
graph, the total number of edges is N * (N - 1) / 2, thus from this total I can
substract the total number of connected nodes to determine the number of 
unconnected nodes. Furthermore, I create a list of lists of non-edges, where
each list corresponds to a node. It is prefilled with every node that could
be assigned to any node, without itself. When a relationship is introduced by 
the input, I remove the relationship from the non-edge list of lists. In 
addition, it does not contain the nodes that have an index smaller than the 
source node. Example (paranthesis contain the indexes normalized to start from 
0):

Input: 4 families, 1 - 3 (0 - 2) relationship, 2 - 3 (1 - 2) relationship

=> The size of the big list is 3 and every sublist is as follows:
list    nodes
0       1, 3
1       3
2       3
3       empty

These conditions make the second clause not need extra checks to avoid 
duplicates. 

### Formulate Oracle Question

### First Clause

Every node in a clique needs to be represented by one of the families.

Rephrasal:
If there is a clique of size k in a given graph G, then there must be exactly
one vertex v, in that clique for each i between 1 and k.

Examples:
Input: 3 families, clique of size 3

=> (1 V 4 V 7) ∧ (2 V 5 V 8) ∧ (3 V 6 V 9)

Complexity: O(K * N)

Total clauses: clique size

### Second Clause

Two nodes in a non-edge relationship cannot coexist as nodes with different 
indexes in a clique.

Rephrasal:
For every non-edge (v, w) ∈ E, v and w cannot both be in the clique (ATENTIE!!!)

Examples:
Input: 3 families, clique of size 3, 1 - 3 relationship, 2 - 3 relationship

=> (¬1 V ¬5) ∧ (¬1 V ¬6) ∧ (¬2 V ¬4) ∧ (¬2 V ¬6) ∧ (¬3 V ¬4) ∧ (¬3 V ¬5)

Complexity: number of no relationships = T => O(T * T * K * K)

Total clauses: clique size * (clique size - 1) * total number of non edges

### Third Clause

Any two nodes should not represent the same node index of a clique.

Rephrasal:
Two different vertices cannot both be the i-th node vertex in the clique.

Examples:
Input: 3 families, clique of size 3

=> (¬1 V ¬4) ∧ (¬1 V ¬7) ∧ (¬2 V ¬5) ∧ (¬2 V ¬8) ∧ (¬3 V ¬6) ∧ (¬3 V ¬9)
    ∧ (¬4 V ¬7) ∧ (¬5 V ¬8) ∧ (¬6 V ¬9)

Complexity: O(N * K * N)

Total clauses: clique size * number of families * (number of families - 1) / 2

### Forth Clause

A node can have represent at most one node of a clique. This clause is exactly
the same as the Third Clause from Task 1. Therefore, its complexity and total
clauses are the same (substitute spies for clique size).

Complexity: O(N * K * (K - 1) / 2)

The total number of variables is N * M (clique size). The total number of 
clauses is the sum of all the total clauses calculated for each clause.

### Decipher Oracle

I create a list of integers names "solutions", where I will put only the 
positive values given by the Oracle if the answer is "True".

### Write Answer

I write to the output file the outcome of the Oracle (True/False) and if the
result is "True", I write to the output file the variables that are present in
"solutions" divided by the clique size. The clique size represents the total
number of variables that a node has, thus if I divide by it I will get the 
family that is in the biggest clique of the graph. Moreover, the rounding is
done because not every division will result in an integer (eg. clique size is
3 and result is 2 => 2 / 3 = 0.66 <=> 1 after rounding).

## Task 3

This time, I have to determine the minimum number of arrests in order to only
have families that do not form a relationship. To do this, I need to find
the biggest clique in the complementary graph.

### Reading the data

The algorithm is the same, except that this time the list of lists that 
contains the non-edge relationships has the indexes of the previous nodes.
This time I remove the relationship interchanging the source node with the
destination (eg. relationship 1 - 2 => I remove 1 - 2 and 2 - 1). Using the
same example from before the list looks as follows:

Input: 4 families, 1 - 3 (0 - 2) relationship, 2 - 3 (1 - 2) relationship

=> The size of the big list is 3 and every sublist is as follows:
list    nodes
0       1, 3
1       0, 3
2       3
3       0, 1, 3 

Moreover, I create a list named "solutions" that contains the indexes of the
families that will be used to print the correct output.

In addition, I need to calculate the size of the clique. I could start by saying
that N (number of nodes) is the clique size and then decrement that number, 
until I find the right size, however that is not optimal. To find an optimal
number, I used the property that states that a complete graph has N * (N - 1) 
/ 2 edges (noted K), where N is the number of nodes. Since I am working on the 
complementary graph (no-edge graph), I can substitute the number of edges in
order to find N_prime that represents the biggest number of nodes that can 
create a complete graph with the edges given. All complete graphs are their own
maximal cliques, therefore I found a clique size that is more optimal than 
the initial ideea. This will result in a quadratic equation that can be reduced
to N_prime = (1 + sqrt (1 + 4 * 2 * K)) / 2 <=> N_prime = (1 + sqrt (1 + 8K)) 
/ 2. The result will be rounded to the floor because the clique size needs to
be an integer.

Going even further, I can deduce the size of the maximal clique by looking at
all of the nodes internal degrees. To find a clique of size N, there must be
N connected nodes with their internal degree at bigger or equal to N - 1. This 
is the reason why I modified the the no-edge list of lists, so that I can 
determine the right internal degree of every node. 

Using the two algorithms, I compare which one is smaller and use that as the
clique size (I also check to see if the second algorithm even found something).

### Reduce to Task 2

I iterate through every element of the non-edge list of lists in order to print
every non-edged nodes. I do a check before every print to make sure I do not
have repetitions (eg. 1 2 and later 2 1).

### Extract Answer from Task 2

I store the result of from Task 2 and if it is equal to "True", I remove every
family given as a positive from the "solutions" list. After this, the list will
only contain the families that need to be arrested.

### Solve

Using a for loop, I start from the clique dimension found previously, I 
reduce the problem to Task 2, I solve it, I extract the answer and if the 
Oracle does not output the message "True", I decrement the clique dimension,
until I find the right size.

### Write Answer

I write every family to the output as they represent the minimum number of
arrests needed.

## Bonus

So far, I resolved only SAT problem. This tasks purpouse is to reduce the
problems to a Weighted Partial Max-SAT problem by using soft clauses on 
the algorithms created at Task 2. 

### Reading the data

The data is read the same just like in task 3. The major problem for this task
would have been the clique size, since there are tests that can torment the CPU
if the size is not optimally chosen. However, the algorithms that I described 
above are efficient enough to not let that happen.

### Formulate Oracle Question

In chose the first clause from Task 2 to be a soft clause, because it is not
mandatory for all nodes to be a part of the maximal clique. Looking at the 
tests used in the checker, the size of the clique is smaller than the number of
nodes. The weights are in ascending order, starting from one, in order to take
advantage of the soft clauses in a Weighted Partial Max-SAT problem and to also
not give too much importance to a specific clause (I considered this fairly
balanced).

All of the clauses are implemented and have the same properties as the clauses
from Task 2. However, the second clause uses the edge list of lists instead of
the non-edge list of lists, because I am doing a hybrid between Task 2 and Task
3, therefore I am working with the complementary graph. Therefore, all of the
equations of this clause should have the total number of non-edges substituted
for the total number of edges.

### Decipher Oracle Answer

The Oracle outputs the number of total variables which is useful because I can
use it to calculate the correct size of the maximal clique. I take every 
positive integer, divide it by the size of the clique and round it to the ceil,
in order to find the index of the family that it represents. Then, just like
before, I remove that family from the list "solutions".

### Write Answer

I write every family to the output as they represent the minimum number of
arrests needed.

