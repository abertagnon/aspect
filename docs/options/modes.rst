Operating Modes
===============

.. code-block:: bash

   usage: ASPECT [options] [(opt.) clingo arguments] [input1 [input2 [input3]...]]
   -f, --free           enable free mode
   -g, --graph          enable graph mode
   -m, --merge          enable merge mode
   -r, --resize <arg>   set resize parameter for merge mode (default 5)


ASPECT supports 4 operating modes called **standard**, **graph**, **free**, **merge**.


* :ref:`standard-graph-section` 
* :ref:`merge-free-section` 

.. _standard-graph-section:

Standard and Graph modes
------------------------


The main operating modes of ASPECT are:

* Standard mode 
* Graph mode 

The difference between these operating modes is in the type of visualization they can produce.
Both these modes generate one separate LaTeX file (and PDF) for each answer set.

Normally ASPECT works in *standard* mode. Graph mode must be enabled via the appropriate 
flag (``-g, --graph``) and is used for graph visualization where the user does not need/want to specify 
the coordinates of the graph nodes. The problem of finding "good positions on the canvas" 
for the nodes of a graph is left to the TikZ LaTeX package and its graph drawing algorithms.

.. _merge-free-section:

Merge and Free modes
--------------------

The other two operating modes are:

* Merge mode
* Free mode

both are used to output a presentation via LaTeX's beamer class.

In merge mode, the only customization the user can introduce is through the resize option (``-r, --resize <arg>``), 
which allows the visualization to scale within the frame. 
The value of the resize parameter uses ``em`` (**em quadrat**) as its unit of measurement and is applied 
simultaneously to scale both the height and width of the visualization.



More personalizations of the output are granted in free mode. 
The user can specify in more detail the structure of each frame. 
Free mode uses two files called ``before.tex`` and ``after.tex`` that contain the LaTeX code 
inserted before and after each visualization, respectively.

.. code-block:: latex
   :caption: Example of ``before.tex`` file

   \begin{frame}
   \frametitle{Frame title}
   \framesubtitle{Frame subtitle}
   \begin{figure}[!h]
   \centering
   \resizebox{.5\textwidth}{!}{%


.. code-block:: latex
   :caption: Example of ``after.tex`` file

   }
   \end{figure}
   \end{frame}

