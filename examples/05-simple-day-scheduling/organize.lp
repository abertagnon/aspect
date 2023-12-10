% ASP model from: http://www.hakank.org/answer_set_programming/organize_day.lp

#const begin = 9.
#const end = 17.
times(begin..end).

n_tasks(4).
tasks(1, bank, green).
tasks(2, shop, blue).
tasks(3, mail, red).
tasks(4, work, yellow).

% time of duration for each task
duration(work, 4).
duration(mail, 1).
duration(shop, 2).
duration(bank, 1).

% before(task1, task2): task1 must be finished before task2
before(bank, shop).
before(mail, work).

1 { task(Task, Start, End) : times(Start),times(End) } 1 :- tasks(_, Task, _).

% end time
:- task(Task, Start, End), duration(Task, Duration), Start+Duration != End.

% end times must be <= 17
:- task(Task, Start, End), End > end.

% No overlap of tasks
no_overlap(Task1, Task2) :-
        task(Task1, Start1, End1),
        task(Task2, Start2, End2),
        End1 <= Start2.

no_overlap(Task1, Task2) :-
        task(Task1, Start1, End1),
        task(Task2, Start2, End2),
        End2 <= Start1.

:- tasks(_, Task1, _),tasks(_, Task2, _), Task1 != Task2, not no_overlap(Task1, Task2).

% before(Task1, Task2): Task1 must be done before Task2
:- before(T1, T2), task(T1, S1, E1), task(T2, S2, E2), S2 < E1.




