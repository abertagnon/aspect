#const size = 2.

%%% 
% Using drawnode for numbers, and imagenode for mines
%%%

aspect_rectangle(size*(X-1), size*(Y-1), size*X, size*Y, tile) :- rows(X), cols(Y).
aspect_drawnode(size*X-1, size*Y-1, N, num) :- number(X,Y,N).
aspect_imagenode(size*X-1, size*Y-1, "./examples/04-minesweeper/mine.png", mine) :- mine(X,Y).

%%%
% Using labels instead of drawnode for numbers, and image_label for mines
%%%

% aspect_rectangle(size*(X-1), size*(Y-1), size*X, size*Y, tile) :- rows(X), cols(Y), not mine(X,Y), not number(X,Y,_).
% aspect_rectangle(size*(X-1), size*(Y-1), size*X, size*Y, (tile,N)) :- rows(X), cols(Y), not mine(X,Y), number(X,Y,N).
% aspect_rectangle(size*(X-1), size*(Y-1), size*X, size*Y, (tile,label)) :- rows(X), cols(Y), mine(X,Y), not number(X,Y,_).

% aspect_label(N, N, num) :- number(X,Y,N).
% aspect_image_label(label, "./examples/04-minesweeper/mine.png", mine).


% Style definitions
aspect_style(tile, "draw", "").
aspect_style(tile, "thick", "").
aspect_style(num, font, "\\Huge").
aspect_style(mine, "width", 40).