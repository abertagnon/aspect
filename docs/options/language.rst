ASPECT Language
+++++++++++++++

ASPECT is declarative sub-language of ASP that can be used to define the graphical representation of an answer set. 
ASPECT (:ref:`standard <standardModeAtoms>`) syntax consists of 21 special atomic formulas that define rendering of geometric primitives such as points, lines, polygons, ellipses, etc. 
The positioning of each element is determined by Cartesian coordinates.

The syntax of the ASPECT language is inspired by the popular PGF/TikZ language developed for drawing vector graphics in the markup language LaTeX.

A concise variant of the ASPECT language has been developed specifically for **graph visualization** (these are called :ref:`graph <graphModeAtoms>` atoms).
This variant allows the user to define graphical elements (e.g., nodes and edges) without the need to worry about specifying coordinates, 
which are instead handled and optimized by the TikZ package. This variant consist of 6 atoms. 

.. attention:: 
  The following two sets of ASPECT atoms (:ref:`standard <standardModeAtoms>` atoms and :ref:`graph <graphModeAtoms>` atoms) are incompatible 
  so they cannot be used in the same ASP program at the same time.


.. topic:: Animated graphics

  For certain problem types, each answer set represents a sequence of actions to be executed in order to achieve a goal starting from a defined initial state.
  In these problems, dynamic visualization through animation is especially valuable, providing insights into the transition from the initial state to the final state.
  In this regard, each atom in the ASPECT language supports an optional integer argument denoted by ``frame`` which represents the frame of the animation in which the graphical element should be inserted.
  
  .. hint:: 
    ASPECT atoms without the frame parameter will be inserted in all frames of the animation.


.. _standardModeAtoms:

Standard ASPECT Atoms
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
   :widths: 1 5 3
   :header-rows: 1

   * - | Graphic 
       | element
     - | ASPECT 
       | atoms
     - | Parameters 
       | description
   * - node 
     - | ``aspect_drawnode(x1,y1,text,[size],[frame])``
       | ``aspect_colornode(x1,y1,text,[size],color,[frame])``
       | ``aspect_imagenode(x1,y1,image,width,[frame])``
     - | ``x1`` x-coordinate
       | ``y1`` y-coordinate
       | ``text`` set the node label
       | ``size`` (opt.) font size
       | ``color`` sets the text color
       | ``image`` path of image
       | ``width`` image width (px)
       | ``frame`` (opt.) frame number

.. _colorDescription:

* ``text``:  quoted or unquoted text. **If text contains whitespaces or special characters, quotation marks are mandatory**.
* ``size``: (optional) LaTeX font size modifier-commands (``Huge``, ``huge``, ``LARGE``, ``Large``, ``large``, ``normalsize`` (default), ``small``, ``footnotesize``, ``scriptsize``, ``tiny``). Accepts values both with and without quotes. Font size modifier-commands can be redefined as needed in the LaTeX document.
*  ``color``: named colors provided by the xcolor package (``red``, ``green``, ``blue``, ``cyan``, ``magenta``, ``yellow``, ``black``, ``gray``, ``white``, ``darkgray``, ``lightgray``, ``brown``, ``lime``, ``olive``, ``orange``, ``pink``, ``purple``, ``teal``, ``violet``). Accepts values both with and without quotes. When quotation marks are used, this parameter provide support color mixing (e.g. ``green!10!orange``). The user can define other custom colors within the LaTeX document (see also free operating mode).

.. hint::
   * Text node with modified **font size**, e.g. ``aspect_drawnode(0, 0, "test", small)``, ``aspect_drawnode(0, 0, test, "small")``
   * Text node with modified **color**, e.g. ``aspect_colornode(0, 0, "test", red)``, ``aspect_colornode(0, 0, "test", "red")``, ``aspect_colornode(0, 0, "test", "red!20")``.


.. list-table::
   :widths: 1 5 3

   * - line
     - | ``aspect_drawline(x1,y1,x2,y2,[frame])``
       | ``aspect_colorline(x1,y1,x2,y2,color,[frame])``
       |
       | |line|
     - | ``x1`` start point x-coordinate
       | ``y1`` start point y-coordinate
       | ``x2`` end point x-coordinate
       | ``y2`` end point y-coordinate
       | ``color`` sets the line :ref:`color <colorDescription>`
       | ``frame`` (opt.) frame number
   * - arc
     - | ``aspect_drawarc(x1,y1,a1,a2,r1,[frame])``
       | ``aspect_colorarc(x1,y1,a1,a2,r1,color,[frame])``
       |
       | |arc|
     - | ``x1`` x-coordinate of the center
       | ``y1`` y-coordinate of the center
       | ``a1`` start angle
       | ``a2`` end angle
       | ``r1`` radius
       | ``color`` sets the line :ref:`color <colorDescription>`
       | ``frame`` (opt.) frame number
   * - | straight 
       | arrow
     - | ``aspect_drawarrow(x1,y1,x2,y2,[frame])``
       | ``aspect_drawarrow(x1,y1,x2,y2,color,[frame])``
       |
       | |arrow|
     - | ``x1`` tail x-coordinate
       | ``y1`` tail y-coordinate
       | ``x2`` tip x-coordinate
       | ``y2`` tip y-coordinate
       | ``color`` set the line :ref:`color <colorDescription>`
       | ``frame`` (opt.) frame number
   * - | square / 
       | rectangle
     - | ``aspect_drawrectangle(x1,y1,x2,y2,[frame])``
       | ``aspect_colorrectangle(x1,y1,x2,y2,color,[frame])``
       | ``aspect_fillrectangle(x1,y1,x2,y2,fill,[frame])``
       |
       | |square|
     - | ``x1`` first corner x-coordinate
       | ``y1`` first corner y-coordinate
       | ``x2`` second corner x-coord
       | ``y2`` second corner y-coord
       | ``color`` sets the line :ref:`color <colorDescription>`
       | ``fill`` sets the fill :ref:`color <colorDescription>`
       | ``frame`` (opt.) frame number
   * - | triangle
     - | ``aspect_drawtriangle(x1,y1,x2,y2,x3,y3,[frame])``
       | ``aspect_colortriangle(x1,y1,x2,y2,x3,y3,color,[frame])``
       | ``aspect_filltriangle(x1,y1,x2,y2,x3,y3,fill,[frame])``
       |
       | |triangle|
     - | ``x1`` first vertex x-coordinate
       | ``y1`` first vertex y-coordinate
       | ``x2`` second vertex x-coord
       | ``y2`` second vertex y-coord
       | ``x3`` third vertex x-coord
       | ``y3`` third vertex y-coord
       | ``color`` sets the line :ref:`color <colorDescription>`
       | ``fill`` sets the fill :ref:`color <colorDescription>`
       | ``frame`` (opt.) frame number
   * - | circle
     - | ``aspect_drawcircle(x1,y1,r1,[frame])``
       | ``aspect_colorcircle(x1,y1,r1,color,[frame])``
       | ``aspect_fillcircle(x1,y1,r1,fill,[frame])``
       |
       | |circle|
     - | ``x1`` center x-coordinate
       | ``y1`` center y-coordinate
       | ``r1`` radius
       | ``color`` sets the line :ref:`color <colorDescription>`
       | ``fill`` sets the fill :ref:`color <colorDescription>`
       | ``frame`` (opt.) frame number
   * - | ellipse
     - | ``aspect_drawellipse(x1,y1,r1,r2,[frame])``
       | ``aspect_colorellipse(x1,y1,r1,r2,color,[frame])``
       | ``aspect_fillellipse(x1,y1,r1,r2,fill,[frame])``
       |
       | |ellipse|
     - | ``x1`` center x-coordinate
       | ``y1`` center y-coordinate
       | ``r1`` x radius
       | ``r2`` y radius
       | ``color`` sets the line :ref:`color <colorDescription>`
       | ``fill`` sets the fill :ref:`color <colorDescription>`
       | ``frame`` (opt.) frame number

.. _graphModeAtoms:

Graph ASPECT Atoms
---------------------------------

.. attention:: 
  The two sets of ASPECT atoms (:ref:`standard <standardModeAtoms>` atoms and :ref:`graph <graphModeAtoms>` atoms) are incompatible 
  so they cannot be used in the same ASP program at the same time.

.. list-table::
   :widths: 20 53 31
   :header-rows: 1

   * - | Graphic 
       | element
     - | ASPECT 
       | atoms
     - | Parameters 
       | description
   * - node 
     - | ``aspect_graphdrawnode(A,[shape],[frame])``
       | ``aspect_graphcolornode(A,fill,[shape],[frame])``
     - | ``A`` node label (name)
       | ``shape`` (opt.) node shape 
       | ``fill`` sets fill :ref:`color <colorDescription>`

* ``shape``:  allows to choose the shape of the node, ``circle`` (default) or ``square``.

.. list-table::
   :widths: 20 53 31

   * - edge  
     - | ``aspect_graphdrawline(A,B,[frame])``
       | ``aspect_graphquoteline(A,B,"text",[frame])``
     - | ``A`` name first endpoint
       | ``B`` name second endpoint
       | ``"text"`` sets edge label
   * - arrow  
     - | ``aspect_graphdrawarrow(A,B,[frame])``
       | ``aspect_graphquotearrow(A,B,"text",[frame])``
     - | ``A`` name arrow tail
       | ``B`` name arrow tip
       | ``"text"`` sets edge label