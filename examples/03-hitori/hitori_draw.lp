#const size = 2.

%%%
% Using drawnode for numbers
%%%

% aspect_rectangle(size*(X-1), size*(Y-1), size*X, size*Y, ("black")) :- schema(X,Y,N), black(X,Y).
% aspect_rectangle(size*(X-1), size*(Y-1), size*X, size*Y, ("white")) :- schema(X,Y,N).
% aspect_drawnode(size*X-1, size*Y-1, N, "num") :- schema(X,Y,N).

%%%
% Using labels instead of drawnode for numbers
%%%

aspect_rectangle(size*(X-1), size*(Y-1), size*X, size*Y, ("black", N)) :- schema(X,Y,N), black(X,Y).
aspect_rectangle(size*(X-1), size*(Y-1), size*X, size*Y, ("white", N)) :- schema(X,Y,N), not black(X,Y).
aspect_label(N, N, num) :- schema(X,Y,N).


% Definition of styles
aspect_style("black", "fill", "gray").
aspect_style("black", "draw", "").
aspect_style("black", "thick", "").

aspect_style(white, "draw", "").
aspect_style(white, "thick", "").

aspect_style(num, font, "\\Huge").

% Rendering order
aspect_layer(black, 1).
aspect_layer(white, 2).
aspect_layer(num, 3).
