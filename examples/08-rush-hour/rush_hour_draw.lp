#const size = 10.

grid_xmax(Xmax + 1) :- Xmax = #max {I:grid(I,_)}.
grid_ymax(Ymax + 1) :- Ymax = #max {I:grid(_,I)}.

aspect_line(X*size,size,X*size,Ymax*size, (grid, background)) :- X=1..Xmax, grid_xmax(Xmax), grid_ymax(Ymax).
aspect_line(size,Y*size,Xmax*size,Y*size, (grid, background)) :- Y=1..Ymax, grid_xmax(Xmax), grid_ymax(Ymax).

aspect_rectangle((X+Size)*size, Y*size, ((X+Size)*size)+1, (Y+1)*size, (black, foreground)):-
	exit(X, Y), vehicle(1, Size, _).

aspect_rectangle((X*size)+1, (Y*size)+1, ((X+Length)*size)-1, ((Y+1)*size)-1, (Color, foreground), T+1) :-  
	vehicle(Index, Length, horiz), position(Index, T, X, Y), color(Index, Color).
aspect_rectangle((X*size)+1, (Y*size)+1, ((X+1)*size)-1, ((Y+Length)*size)-1 , (Color, foreground), T+1) :-  
	vehicle(Index, Length, vert), position(Index, T, X, Y), color(Index, Color).
	
aspect_style(red, "fill", red).
aspect_style(red, "line width", "12pt").

aspect_style(green, "fill", "green!60").
aspect_style(blue, "fill", "blue!60").
aspect_style(cyan, "fill", "cyan!60").
aspect_style(magenta, "fill", "magenta!60").
aspect_style(yellow, "fill", "yellow!60").
aspect_style(gray, "fill", "gray!60").
aspect_style(lime, "fill", "lime!60").
aspect_style(orange, "fill", "orange!60").
aspect_style(pink, "fill", "pink!60").
aspect_style(teal, "fill", "teal!60").
aspect_style(violet, "fill", "violet!60").
aspect_style(black, "fill", "black").

aspect_style(grid, "line width", "3pt").

aspect_layer(foreground, 1).
aspect_layer(background, 0).