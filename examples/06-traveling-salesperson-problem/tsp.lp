node(X):- point(X,_,_).
edge(X,Y):- cost(X,Y,_).

% Generate
{ cycle(X,Y) : edge(X,Y) } = 1 :- node(X).
{ cycle(X,Y) : edge(X,Y) } = 1 :- node(Y).
% Define
reached(Y) :- cycle(1,Y).
reached(Y) :- cycle(X,Y), reached(X).
% Test
:- node(Y), not reached(Y).

% Optimize
#minimize { C,X,Y : cycle(X,Y), cost(X,Y,C) }.
