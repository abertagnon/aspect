#const size = 2.

aspect_fillrectangle(size*(X-1), size*(Y-1), size*X, size*Y, gray) :- schema(X,Y,N), black(X,Y).
aspect_drawrectangle(size*(X-1), size*(Y-1), size*X, size*Y) :- schema(X,Y,N).
aspect_drawnode(size*X-1, size*Y-1, N, "Huge") :- schema(X,Y,N).

#show aspect_drawrectangle/4.
#show aspect_fillrectangle/5.
#show aspect_drawnode/4.
