aspect_drawnode(1,1,1).
aspect_drawnode(1,2,"test1").
val1(test2).
aspect_drawnode(1,3,X) :- val1(X).
val2("test3").
aspect_drawnode(1,4,X) :- val2(X).
aspect_colornode(1,5,"color1",red). 
aspect_colornode(1,6,color2,blue). 
aspect_colornode(1,7,1,green). 
aspect_imagenode(1,8,"test.png",10).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% ERRORS:
% aspect_drawnode(1,2,"test1","test1").
% aspect_drawnode(1,2,"test1",red).
% aspect_drawnode(1,2,1,red).
% aspect_drawnode(1,2).
% aspect_drawnode(1).
% aspect_drawnode("test",2).
% aspect_drawnode(2,"test").

% aspect_colornode(1,5,"color1",5). 
% aspect_colornode(1,5,"color1",red,"test"). 
% aspect_colornode(1,5,"color1",red,test). 
% aspect_colornode(1,5,"color1","test").

% aspect_imagenode(1,8,"check.png",test).
% aspect_imagenode(1,8,"check.png","test").
% aspect_imagenode(1,8,10,red).
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

aspect_drawnode(5,1,1,1).
aspect_drawnode(5,2,"test1",2).
val1(test2).
aspect_drawnode(5,3,X,1) :- val1(X).
val2("test3").
aspect_drawnode(5,4,X,2) :- val2(X).
aspect_colornode(5,5,"color1",red,1). 
aspect_colornode(5,6,color2,blue,2). 
aspect_colornode(5,7,1,green,1). 
aspect_imagenode(5,8,"check.png",10,2).

aspect_drawline(10,1,15,1).
aspect_colorline(10,2,15,2,red).

aspect_drawline(10,3,15,3,1).
aspect_colorline(10,4,15,4,blue,2).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%ERRORS:
% aspect_drawline(10,1,15,1,test).
% aspect_drawline(10,1,15).
% aspect_drawline(10,1).
% aspect_drawline(10).
% aspect_drawline(10,t,15,1).
% aspect_drawline(10,"test",15,1).

% aspect_colorline(10,2,15,2,"red").
% aspect_colorline(10,2,15,2,5).
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

aspect_drawarc(12,4,0,90,2).
aspect_colorarc(12,8,90,180,2,red).

aspect_drawarc(14,4,0,90,2,1).
aspect_colorarc(14,8,90,180,2,blue,2).

aspect_drawarrow(10,8,15,8).
aspect_colorarrow(10,9,15,9,red).

aspect_drawarrow(15,10,10,10,2).
aspect_colorarrow(15,11,10,11,blue,2).

aspect_drawrectangle(16,1,18,4).
aspect_colorrectangle(16,5,18,8,red). 
aspect_fillrectangle(16,9,18,12,red).

aspect_drawrectangle(19,1,21,4,1).
aspect_colorrectangle(19,5,21,8,blue,2). 
aspect_fillrectangle(19,9,21,12,blue,2).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%ERRORS:
% aspect_drawrectangle(16,1,18).
% aspect_drawrectangle(16,t,18,4).
% aspect_drawrectangle(16,4,"t",4).
% aspect_drawrectangle(16,1,18,10,"test").
% aspect_drawrectangle(16,1,18,10,test).

% aspect_colorrectangle(16,4,18,8,100). 
% aspect_fillrectangle(16,9,18,12,100).

% aspect_colorrectangle(16,4,18,8). 
% aspect_fillrectangle(16,9,18,12).
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

aspect_drawtriangle(22,1,24,1,23,4).
aspect_colortriangle(22,5,24,5,23,8,red).
aspect_filltriangle(22,9,24,9,23,12,red).

aspect_drawtriangle(24,1,26,1,25,4,1).
aspect_colortriangle(24,5,26,5,25,8,blue,2).
aspect_filltriangle(24,9,26,9,25,12,blue,2).


aspect_drawcircle(27,2,1). 
aspect_colorcircle(27,5,1,red). 
aspect_fillcircle(27,8,1,red).

aspect_drawcircle(29,2,1,1). 
aspect_colorcircle(29,5,1,blue,2). 
aspect_fillcircle(29,8,1,blue,2).

aspect_drawellipse(32,2,2,1).
aspect_colorellipse(32,5,2,1,red). 
aspect_fillellipse(32,8,2,1,red).

aspect_drawellipse(36,2,2,1,1).
aspect_colorellipse(36,5,2,1,blue,2). 
aspect_fillellipse(36,8,2,1,blue,2).