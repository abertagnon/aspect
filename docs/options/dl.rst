ASPECT Difference Logic
+++++++++++++++++++++++

ASPECT supports the use of difference-logic (DL) variables when running with the `clingo-dl <https://potassco.org/labs/clingoDL/>`_ solver.

In Answer Set Programming with difference logic, variables are introduced through constraints of the form:

.. code-block:: none

  &diff{ start(a) - start(b) } <= 5.

During solving, clingo-dl assigns concrete values to these variables. These assignments are reported via atoms of the form:

.. code-block:: none

  dl(start(a), 10).
  dl(start(b), 15).

Here, the second argument provides the numeric value associated with the DL variable.

ASPECT allows you to reference DL variable names directly in drawing atoms wherever a numeric argument is expected.

At translation time:
  - ASPECT automatically looks up the corresponding ``dl(Name, Value)`` atom in the answer set.
  - The symbolic variable is replaced with its numeric value.
  - The resulting number is then used to generate the TikZ code.

This means you can write visualization rules in terms of DL variables, without needing to manually extract their values.

Example:

The following program defines a DL constraint and a visualization rule:

.. code-block:: prolog

  % Definition of activities with their duration.
  activity(1, a1, 3).  % Activity a1 lasts 3 time units.
  activity(2, a2, 2).  % Activity a2 lasts 2 time units.
  activity(3, a3, 1).  % Activity a3 lasts 1 time unit.

  % Constraint: activity a2 cannot start before the end of a1.
  &diff{start(a2) - start(a1)} >= D :- activity(_, a1, D).

  % Constraint: activity a3 cannot start before the end of a2.
  &diff{start(a3) - start(a2)} >= D :- activity(_, a2, D).

  % We also define a reasonable domain for T for each activity.
  &diff{start(A)} >= 0 :- activity(_, A, _).
  &diff{start(A)} <= 10 :- activity(_, A, _).

  % Relation between start and end times of an activity.
  &diff{end(A)-start(A)} = D :- activity(_, A, D).

  % Visualization rule: draw a line representing the activity.
  aspect_line(start(A), ID, end(A), ID) :- activity(ID, A, _).

If the solver produces the assignments:

.. code-block:: none

  dl(end(a1),3) 
  dl(start(a1),0) 
  dl(end(a2),5) 
  dl(start(a2),3) 
  dl(end(a3),6) 
  dl(start(a3),5)

ASPECT will automatically substitute these values into the visualization command, resulting in the corresponding TikZ output.
