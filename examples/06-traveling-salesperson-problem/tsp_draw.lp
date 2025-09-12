aspect_arrow(Xa,Ya,Xb,Yb,"style1"):- cycle(A,B), point(A,Xa,Ya), point(B,Xb,Yb).
aspect_circle(X,Y,1,"style2") :- point(_,X,Y).

aspect_style(style1, "dash pattern", "on 30pt off 20pt").
aspect_style(style1, "very thick", "").

aspect_style(style2, "fill", "gray").