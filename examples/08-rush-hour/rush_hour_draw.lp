#const size = 10.

grid_xmax(Xmax + 1) :- Xmax = #max {I:grid(I,_)}.
grid_ymax(Ymax + 1) :- Ymax = #max {I:grid(_,I)}.

aspect_drawline(X*size,size,X*size,Ymax*size) :- X=1..Xmax, grid_xmax(Xmax), grid_ymax(Ymax).
aspect_drawline(size,Y*size,Xmax*size,Y*size) :- Y=1..Ymax, grid_xmax(Xmax), grid_ymax(Ymax).

aspect_fillrectangle((X+Size)*size, Y*size, ((X+Size)*size)+1, (Y+1)*size, black):-
	exit(X, Y), vehicle(1, Size, _).

aspect_fillrectangle((X*size)+1, (Y*size)+1, ((X+Length)*size)-1, ((Y+1)*size)-1, Color, T+1) :-  
	vehicle(Index, Length, horiz), position(Index, T, X, Y), color(Index, Color).
aspect_fillrectangle((X*size)+1, (Y*size)+1, ((X+1)*size)-1, ((Y+Length)*size)-1 , Color, T+1) :-  
	vehicle(Index, Length, vert), position(Index, T, X, Y), color(Index, Color).