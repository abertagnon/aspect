ASPECT Attributes
+++++++++++++++++

ASPECT provides a set of attributes that let you customize the appearance and organization of your visualizations.
These attributes include:

  - :ref:`Styles <attributesStyle>` allow you to group graphical parameters (such as colors, line widths, or fill patterns) under a reusable name.
  - :ref:`Layers <attributesLayers>` control the drawing order, making it easy to place elements in the background or foreground.
  - :ref:`Labels <attributesLabels>` let you attach text or images to shapes without manually computing coordinates.


.. _attributesStyle:

Styles
------
Styles are named collections of graphics parameters. These styles correspond to TikZ graphical properties/options in LaTeX.

Syntax:

.. code-block:: none

  aspect_style(styleName, parameterKey, parameterValue)

- ``styleName``: the style identifier. Valid identifiers are integer number or generic strings. If an identifier contains whitespace, special characters, or starts with an uppercase letter, it **must** be quoted.
- ``parameterKey``: the key (name) of a TikZ parameter; e.g. fill, draw, color, width (for images), etc. If a key contains whitespace, special characters, or starts with an uppercase letter, it **must** be quoted.
- ``parameterValue``: the value of the graphic parameter; e.g. gray, black, 1pt, 50. If a value contains whitespace, special characters, or starts with an uppercase letter, it **must** be quoted.

Define multiple attributes for the same style by repeating ``aspect_style`` with the same ``styleName``. 
If a parameter is defined more than once for a style, the first occurrence takes precedence.

Examples:

.. code-block:: prolog

  aspect_style(black, fill, gray).
  aspect_style(black, draw, red).
  aspect_style("Queens", width, 50).


.. _attributesLayers:

Layers
------

Layers control the drawing order. Each layer is identified by a ``layerName``. The drawing order is controlled by ``index``, a positive integer: lower indices are rendered first (background), higher ones later (foreground). The ``layerName`` may be an integer number or a generic string. If it contains whitespace, special characters, or starts with an uppercase letter, it **must** be quoted.

Syntax:

.. code-block:: none

  aspect_layer(layerName, index)

Examples:

.. code-block:: prolog

  aspect_layer(tiles, 0).
  aspect_layer(queens, 1).


.. _attributesLabels:

Labels
------
Labels let you attach text or an image to a figure without computing coordinates yourself.
You define a label once, then reference its name in the attributes parameter of any drawing atom (together with style and layer ids). ASPECT and TikZ will handle alignment (e.g., centering) for you.


Text Labels
^^^^^^^^^^^

Syntax:

.. code-block:: none

  aspect_label(labelName, text, [attributes])

- ``labelName``: the label identifier. If an identifier contains whitespace, special characters, or starts with an uppercase letter, it **must** be quoted.
- ``text``: the label text (quote it if it contains spaces or special characters).
- ``attributes`` (optional): a single style attribute (see :ref:`Styles <attributesStyle>`) or a tuple of style attributes to control things like font size/color or to assign the label to a layer.

Examples:

.. code-block:: prolog

  % Define the label
  aspect_label(lab, "Q", labelStyle).

  % Style for the label text
  aspect_style(labelStyle, color, red).

  % Draw a rectangle, attach the label by name
  aspect_rectangle(0, 0, 2, 2, lab).


Image Labels
^^^^^^^^^^^^

Syntax:

.. code-block:: none

  aspect_image_label(labelName, image, [attributes])

- ``labelName``: the label identifier. If an identifier contains whitespace, special characters, or starts with an uppercase letter, it **must** be quoted.
- ``image``: the file path to the image.
- ``attributes`` (optional): a single style attribute (see :ref:`Styles <attributesStyle>`) or a tuple of style attributes to control things like width or to assign the label to a layer.

Examples:

.. code-block:: prolog

  % Define the image label
  aspect_image_label(imglab, "./queen.png", imgStyle).

  % Optional style for the image label (example: set a target size, opacity, etc.)
  aspect_style(imgStyle, opacity, 0.9).

  % Draw a rectangle, attach the image label
  aspect_rectangle(0, 0, 2, 2, imglab).