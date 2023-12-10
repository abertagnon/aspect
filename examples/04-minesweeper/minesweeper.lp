% ASP model from: http://www.hakank.org/answer_set_programming/minesweeper.lp

nums(0..8).
rows(1..r).
cols(1..c).

% defining adjacency
adj(R,C,R1,C1) :- rows(R),rows(R1), cols(C),cols(C1), |R-R1| + |C-C1|==1.
adj(R,C,R1,C1) :- rows(R),rows(R1), cols(C),cols(C1), |R-R1|==1, |C-C1|==1.

% N mines around a number N
N {mine(R2, C2) : adj(R2,C2,R1,C1)} N :- number(R1,C1,N).

