aspect_drawarrow(Xa,Ya,Xb,Yb):- cycle(A,B), point(A,Xa,Ya), point(B,Xb,Yb).
aspect_fillcircle(X,Y,1,gray) :- point(_,X,Y).