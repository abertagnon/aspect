#const n=8.

1 { queen(I,1..n) } 1 :- I = 1..n.
1 { queen(1..n,J) } 1 :- J = 1..n.

 :- 2 { queen(I,J) : D = I+J+1 }, D=1..2*n-1.
 :- 2 { queen(I,J) : D = I-J+n }, D=1..2*n-1.

%%%%%%%%%%%%%%%%%%%%%%%
% Draw
%%%%%%%%%%%%%%%%%%%%%%%

aspect_rectangle(2*I-1,2*J-1,2*I+1,2*J+1,dark):- I = 1..n, J = 1..n, I\2=J\2.
aspect_rectangle(2*I-1,2*J-1,2*I+1,2*J+1,light):- I = 1..n, J = 1..n, I\2!=J\2.
aspect_imagenode(2*I,2*J,"./examples/02-n-queens/queen.png",queen):- queen(I,J).

aspect_style(dark, "fill", "gray").
aspect_style(light, "fill", "white").

aspect_style(queen, "width", 50).

aspect_layer(dark, 0).
aspect_layer(light, 0).
aspect_layer(queen, 1).