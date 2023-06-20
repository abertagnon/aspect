ASPECT Language
+++++++++++++++

ASPECT is declarative sub-language of ASP that can be used to define the graphical representation of an answer set. 
ASPECT syntax consists of special atomic formulas that define rendering of geometric primitives such as points, lines, polygons, ellipses, etc. 

The syntax of the ASPECT language is inspired by the popular PGF/TikZ language developed for drawing vector graphics in the markup language LaTeX. 

.. caution:: 
  Different operating modes of the ASPECT interpreter (see :doc:`Operating Modes </options/modes>`) supports different sets of ASPECT atoms.
  In particular, the graph mode supports a different set of atoms (see :ref:`here <graphModeAtoms>`).   

.. danger:: 
  The following two sets of ASPECT atoms (:ref:`"standard" <graphModeAtoms>` atoms and :ref:`graph <graphModeAtoms>` atoms) are incompatible 
  so they cannot be used in the same ASP program at the same time.

.. _standardModeAtoms:

List of ASPECT Atoms (Standard)
-------------------------------

.. |line| image:: ../_static/images/line.png
  :width: 35%
  :align: middle

.. |arc| image:: ../_static/images/arc.png
  :width: 35%
  :align: middle

.. |arrow| image:: ../_static/images/arrow.png
  :width: 35%
  :align: middle

.. |circle| image:: ../_static/images/circle.png
  :width: 35%
  :align: middle

.. |ellipse| image:: ../_static/images/ellipse.png
  :width: 40%
  :align: middle

.. |square| image:: ../_static/images/square.png
  :width: 35%
  :align: middle

.. |triangle| image:: ../_static/images/triangle.png
  :width: 45%
  :align: middle


.. list-table::
   :widths: 1 2 3
   :header-rows: 1

   * - | Graphic 
       | element
     - | ASPECT 
       | atoms
     - | Parameters 
       | description
   * - node 
     - | ``aspect_drawnode(x1,y1,"text")``
       | ``aspect_drawnode(x1,y1,X)``
       | ``aspect_colornode(x1,y1,"text",color)``
       | ``aspect_imagenode(x1,y1,image,width)``
     - | ``x1`` x-coordinate
       | ``y1`` y-coordinate
       | ``"text"`` set the node label
       | ``X`` ASP variable
       | ``color`` sets the line color
       | ``image`` path of image to include
       | ``width`` image width (px)
   * - line
     - | ``aspect_drawline(x1,y1,x2,y2)``
       | ``aspect_colorline(x1,y1,x2,y2,color)``
       |
       | |line|
     - | ``x1`` start point x-coordinate
       | ``y1`` start point y-coordinate
       | ``x2`` end point x-coordinate
       | ``y2`` end point y-coordinate
       | ``color`` sets the line color
   * - arc
     - | ``aspect_drawarc(x1,y1,a1,a2,r1)``
       | ``aspect_colorarc(x1,y1,a1,a2,r1,color)``
       |
       | |arc|
     - | ``x1`` x-coordinate of the center
       | ``y1`` y-coordinate of the center
       | ``a1`` start angle
       | ``a2`` end angle
       | ``r1`` radius
       | ``color`` sets the line color
   * - | straight 
       | arrow
     - | ``aspect_drawarrow(x1,y1,x2,y2)``
       | ``aspect_drawarrow(x1,y1,x2,y2,color)``
       |
       | |arrow|
     - | ``x1`` tail x-coordinate
       | ``y1`` tail y-coordinate
       | ``x2`` tip x-coordinate
       | ``y2`` tip y-coordinate
       | ``color`` set the line color
   * - | square / 
       | rectangle
     - | ``aspect_drawrectangle(x1,y1,x2,y2)``
       | ``aspect_colorrectangle(x1,y1,x2,y2,color)``
       | ``aspect_fillrectangle(x1,y1,x2,y2,fill)``
       |
       | |square|
     - | ``x1`` first corner x-coordinate
       | ``y1`` first corner y-coordinate
       | ``x2`` second (opp.) corner x-coord
       | ``y2`` second (opp.) corner y-coord
       | ``color`` sets the line color
       | ``fill`` sets the fill color
   * - | triangle
     - | ``aspect_drawtriangle(x1,y1,x2,y2,x3,y3)``
       | ``aspect_colortriangle(x1,y1,x2,y2,x3,y3,color)``
       | ``aspect_filltriangle(x1,y1,x2,y2,x3,y3,fill)``
       |
       | |triangle|
     - | ``x1`` first vertex x-coordinate
       | ``y1`` first vertex y-coordinate
       | ``x2`` second vertex x-coordinate
       | ``y2`` second vertex y-coordinate
       | ``x3`` third vertex x-coordinate
       | ``y3`` third vertex y-coordinate
       | ``color`` sets the line color
       | ``fill`` sets the fill color
   * - | circle
     - | ``aspect_drawcircle(x1,y1,r1)``
       | ``aspect_colorcircle(x1,y1,r1,color)``
       | ``aspect_fillcircle(x1,y1,r1,fill)``
       |
       | |circle|
     - | ``x1`` center x-coordinate
       | ``y1`` center y-coordinate
       | ``r1`` radius
       | ``color`` sets the line color
       | ``fill`` sets the fill color
   * - | ellipse
     - | ``aspect_drawellipse(x1,y1,r1,r2)``
       | ``aspect_colorellipse(x1,y1,r1,r2,color)``
       | ``aspect_fillellipse(x1,y1,r1,r2,fill)``
       |
       | |ellipse|
     - | ``x1`` center x-coordinate
       | ``y1`` center y-coordinate
       | ``r1`` x radius
       | ``r2`` y radius
       | ``color`` sets the line color
       | ``fill`` sets the fill color

.. _graphModeAtoms:

List of ASPECT Atoms (Graph)
---------------------------------

Graph mode as the name suggests allows for quick visualization of solutions that can be 
represented by a graph. Graph mode allows the user to insert graphical elements **without 
having to worry about specifying coordinates**, which will be handled automatically by the TikZ package.

.. list-table::
   :widths: 1 2 3
   :header-rows: 1

   * - | Graphic 
       | element
     - | ASPECT 
       | atoms
     - | Parameters 
       | description
   * - node 
     - | ``aspect_drawnode(A)``
       | ``aspect_colornode(A,fill)``
     - | ``A`` node name (label)
       | ``fill`` sets the fill color
   * - edge
     - | ``aspect_drawline(A,B)``
       | ``aspect_quoteline(A,B,"text")``
     - | ``A`` node name first endpoint
       | ``B`` node name second endpoint
       | ``"text"`` sets the edge label
   * - arrow
     - | ``aspect_drawarrow(A,B)``
       | ``aspect_quotearrow(A,B,"text")``
     - | ``A`` node name arrow tail
       | ``B`` node name arrow tip
       | ``"text"`` sets the edge label
