% See instance
% schema(Row,Column,Value).

col(C):- schema(_,C,_).
row(R):- schema(R,_,_).

{hitori(R,C,V)} :- schema(R,C,V).

% alldifferent rows and columns
:- hitori(R,C1,V), hitori(R,C2,V), C1 != C2.
:- hitori(R1,C,V), hitori(R2,C,V), R1 != R2.

black(R,C):- schema(R,C,V), not hitori(R,C,V).

% blacks not connected
:- black(R,C1), black(R,C2), C1 = C2+1.
:- black(R1,C), black(R2,C), R1 = R2+1.

reachable(1,1):- not black(1,1).
reachable(1,2):- black(1,1).

reachable(R,C):- reachable(R,C1), |C-C1| = 1, not black(R,C), col(C).
reachable(R,C):- reachable(R1,C), |R-R1| = 1, not black(R,C), row(R).

:- hitori(R,C,V), not reachable(R,C).
