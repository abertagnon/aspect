node(0..5).

col(yellow).
col(orange).
col(green).
col(cyan).

edge(0, 1).
edge(0, 3).
edge(0, 2).
edge(0, 4).
edge(0, 5).
edge(1, 3).
edge(1, 4).
edge(2, 3).
edge(3, 4).
edge(3, 5).

1 {color(X, C): col(C)} 1 :- node(X).
:- edge(X, Y), col(C), color(X, C), color(Y, C).

%%%%%%%%%%%%%%%%%%%%%%%
% Draw
%%%%%%%%%%%%%%%%%%%%%%%

aspect_graphcolornode(X,Color,circle) :- color(X,Color).
aspect_graphdrawline(A,B) :- edge(A,B).


