file(red, "./examples/09-ricochet-robots/robot-red.png").
file(blue, "./examples/09-ricochet-robots/robot-blue.png").
file(green, "./examples/09-ricochet-robots/robot-green.png").
file(yellow, "./examples/09-ricochet-robots/robot-yellow.png").

#const size = 10.

% Grid
grid_max(Max + 1) :- Max = #max {I:dim(I)}.
aspect_line(X*size,size,X*size,Max*size) :- X=1..Max, grid_max(Max).
aspect_line(size,Y*size,Max*size,Y*size) :- Y=1..Max, grid_max(Max).

% Targets
aspect_rectangle(X*size+1, Y*size+1, (X+1)*size-1, (Y+1)*size-1, "target") :- target(_,X,Y).

% Barriers
aspect_rectangle((X*size)-1, (Y*size)-1, (X*size)+1, ((Y+1)*size)+1, barrier) :- barrier(X,Y,west).
aspect_rectangle((X*size)-1, (Y*size)-1, ((X+1)*size)+1, (Y*size)+1, barrier) :- barrier(X,Y,north).

% Robots
aspect_imagenode(X*size+(size/2), Y*size+(size/2), File, (robot), T+1) :- 
    pos(R, 1, X, T), pos(R, -1, Y, T), file(R, File).
	
aspect_style(target, "fill", "red!20").
aspect_style(target, "draw", "none").
aspect_style(barrier, "fill", "gray").
aspect_style(barrier, "draw", "none").
aspect_style(robot, "width", 200).