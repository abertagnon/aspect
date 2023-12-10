% ASP model from: Modeling and Solving the Rush Hour puzzle (by Lorenzo Cian, Talissa Dreossi and Agostino Dovier)
% https://ceur-ws.org/Vol-3204/paper\_29.pdf

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Constants

#const t = 26.

grid(1..6, 1..6).
exit(6-1, 6/2 + 1).
move_amount(1..4).
direction(up; down; left; right).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Grid state

busy(X, Y..Y+S-1, T) :- vehicle(A, S, vert), position(A, T, X, Y),
	grid(X, Y), time(T).

busy(X..X+S-1, Y, T) :- vehicle(A, S, horiz), position(A, T, X, Y),
	grid(X, Y), time(T). 
	
free(X, Y, T) :- not busy(X, Y, T), grid(X, Y), time(T).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Encoding of the game

% move executability condition
movable(A, T, up, N) :- vehicle(A, S, vert), position(A, T, X, Y),
	N {free(X, Y+S..Y+S+N-1, T)} N, move_amount(N),
	grid(X, Y), grid(X, Y+S+N-1), time(T).

movable(A, T, down, N) :- vehicle(A, S, vert), position(A, T, X, Y),
	N {free(X, Y-N..Y-1, T)} N, move_amount(N),
	grid(X, Y), grid(X, Y-N), time(T).

movable(A, T, left, N) :- vehicle(A, S, horiz), position(A, T, X, Y),
	N {free(X-N..X-1, Y, T)} N, move_amount(N),
	grid(X, Y), grid(X-N, Y), time(T).

movable(A, T, right, N) :- vehicle(A, S, horiz), position(A, T, X, Y),
	N {free(X+S..X+S+N-1, Y, T)} N, move_amount(N),
	grid(X, Y), grid(X+S+N-1, Y), time(T).

% move effect
position(A, T+1, X, Y+N) :- move(A, T, up, N), movable(A, T, up, N), move_amount(N),
	vehicle(A, S, O), position(A, T, X, Y), grid(X, Y), time(T), time(T+1). 

position(A, T+1, X, Y-N) :- move(A, T, down, N), movable(A, T, down, N), move_amount(N),
	vehicle(A, S, O), position(A, T, X, Y), grid(X, Y), time(T), time(T+1). 

position(A, T+1, X-N, Y) :- move(A, T, left, N), movable(A, T, left, N), move_amount(N), 
	vehicle(A, S, O), position(A, T, X, Y), grid(X, Y), time(T), time(T+1).

position(A, T+1, X+N, Y) :- move(A, T, right, N), movable(A, T, right, N), move_amount(N),
	vehicle(A, S, O), position(A, T, X, Y), grid(X, Y), time(T), time(T+1).

% inertia for vehicles that did not move
position(A, T+1, X, Y) :- not moved(A, T), position(A, T, X, Y), 
	vehicle(A, S, O), grid(X, Y), time(T), time(T+1).

% one move per timestep if goal is not yet reached 
1 { move(A, T, D, N) : vehicle(A, S, O), direction(D), movable(A, T, D, N), move_amount(N) } 1 :- 
      time(T).
moved(A, T) :- move(A, T, D, N), direction(D), move_amount(N).

% no more moves after goal is reached
%position(A, T+1, X, Y) :- goal(T), time(T), time(T+1), vehicle(A, S, O), position(A, T, X, Y), grid(X, Y).

:- moved(A, T), moved(A, T+1), time(T), vehicle(A, S, O).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Goal 

goal :- position(1, t, X, Y), exit(X, Y).
:- not goal.