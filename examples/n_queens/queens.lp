#const n=8.

1 { queen(I,1..n) } 1 :- I = 1..n.
1 { queen(1..n,J) } 1 :- J = 1..n.

 :- 2 { queen(I,J) : D = I+J+1 }, D=1..2*n-1.
 :- 2 { queen(I,J) : D = I-J+n }, D=1..2*n-1.

aspect_fillrectangle(2*I-1,2*J-1,2*I+1,2*J+1,gray):- I = 1..n, J = 1..n, I\2=J\2.
aspect_drawrectangle(2*I-1,2*J-1,2*I+1,2*J+1):- I = 1..n, J = 1..n, I\2!=J\2.
aspect_imagenode(2*I,2*J,"./examples/n_queens/queen.png",50):- queen(I,J).

%#show aspect_drawrectangle/4.
%#show aspect_fillrectangle/5.
%#show aspect_imagenode/4.
