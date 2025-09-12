% ASP model from: Answer Set Solving in Practice (by Martin Gebser, Roland Kaminski, Benjamin Kaufmann, and Torsten Schaub)
% doi:10.2200/S00457ED1V01Y201211AIM019

#const moves=15.

peg(a).
peg(b).
peg(c).
disk(1..4).
init_on(1..4,a).
goal_on(1..4,c).

% Generate
{ move(D,P,T) : disk(D), peg(P) } = 1 :- T = 1..moves.
% Define
move(D,T) :- move(D,_,T).
on(D,P,0) :- init_on(D,P).
on(D,P,T) :- move(D,P,T).
on(D,P,T+1) :- on(D,P,T), not move(D,T+1), T < moves.
blocked(D-1,P,T+1) :- on(D,P,T), T < moves.
blocked(D-1,P,T) :- blocked(D,P,T), disk(D).
% Test
:- move(D,P,T), blocked(D-1,P,T).
:- move(D,T), on(D,P,T-1), blocked(D,P,T).
:- goal_on(D,P), not on(D,P,moves).
:- { on(D,P,T) } != 1, disk(D), T = 1..moves.

%%%%%%%%%%%%%%%%%%%%%%%
% Draw
%%%%%%%%%%%%%%%%%%%%%%%

disk_width(1, 6).
disk_width(2, 4).
disk_width(3, 2).
disk_width(4, 1).
disk_color(1, red).
disk_color(2, green).
disk_color(3, blue).
disk_color(4, yellow).
peg_x(a, 6).
peg_x(b, 20).
peg_x(c, 34).

% draw pegs
aspect_rectangle(X, 0, X+2, 18, pegs) :- peg(P), peg_x(P, X).
% draw disks
disk_level(Count, D1, P, T) :- on(D1, P, T), #count { D2 : on(D2, P, T), disk(D2), D2 < D1} = Count.
aspect_rectangle(X-Width, L*2, X+2+Width, (L*2)+2, s(fill,Color), T+1) :- 
    on(D, P, T), peg(P), peg_x(P, X), disk_color(D, Color), disk_width(D, Width), disk_level(L, D, P, T).
% print peg label
aspect_drawnode(X+1, 19, P, "label"):- peg(P), peg_x(P,X).
% print move
aspect_drawnode(16, 22, "move ", "peg", T):- T = 1..moves.
aspect_drawnode(21, 22, Color, ("peg", s(text,Color)), T) :- move(D,_,T), disk_color(D,Color).
aspect_drawnode(25, 22, " in ", "peg", T):- T = 1 ..moves.
aspect_drawnode(28, 22, P, "peg", T) :- move(_,P,T).
% background
aspect_rectangle(0, 0, 42, 24, background).

% styles
aspect_style(pegs, "fill", "gray").

aspect_style(label, "font", "\\Huge").
aspect_style(peg, "font", "\\fontsize{80pt}{96pt}\\selectfont").
aspect_style(peg, "anchor", "base").

aspect_style(background, "fill", "none").
aspect_style(background, "draw", "none").

aspect_style(s(text,C), text, C) :- disk_color(_, C).
aspect_style(s(fill,C), fill, C) :- disk_color(_, C).
