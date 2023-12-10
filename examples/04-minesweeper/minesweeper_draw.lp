#const size = 2.

aspect_drawrectangle(size*(X-1), size*(Y-1), size*X, size*Y) :- rows(X), cols(Y).
aspect_drawnode(size*X-1, size*Y-1, N, "Huge") :- number(X,Y,N).
aspect_imagenode(size*X-1, size*Y-1, "./examples/04-minesweeper/mine.png", 40) :- mine(X,Y).