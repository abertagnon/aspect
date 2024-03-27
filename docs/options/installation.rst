Installation
++++++++++++

Prerequisites
-------------

| The current release of ASPECT depends on `pdfTeX <https://www.tug.org/applications/pdftex/>`_ and `LuaTeX <https://www.luatex.org/>`_.
| (Optional) `ImageMagick <https://imagemagick.org/>`_ is required for generating PNG and/or JPEG images.

.. note:: 
   If you cannot install the dependencies run the ASPECT interpreter with the ``--nobuild`` option. 
   ASPECT will not be able to output PDF documents, only LaTeX files will be generated.

Please refer to the manual of the various software for their installation on your system.

If you are in a hurry, an easy way to install dependencies is to run the following commands.

* On *Debian based Linux distributions*, install **TeX Live**:

.. code-block:: bash

    sudo apt-get install texlive-latex-base
    sudo apt-get install texlive-latex-extra

To make sure everything is ready try running in a shell the following commands:

* ``pdflatex --version``
* ``lualatex --version``

you should see something like

.. code-block:: bash

   pdfTeX 3.141592653-X (TeX Live X)
   kpathsea version X.X.X
   Copyright X Han The Thanh (pdfTeX) et al.

.. code-block:: bash

   This is LuaHBTeX, Version X.X.X (TeX Live X)
   Development id: X


* On *Debian based Linux distributions*, install **ImageMagick**:

.. code-block:: bash

    sudo apt install imagemagick

To make sure ImageMagick is installed correctly try running in a shell the following commands:

* ``magick -version``
* ``convert -version``

you should see something like

.. code-block:: bash

    Version: ImageMagick X.X.X-X https://imagemagick.org
    Copyright: (C) 1999 ImageMagick Studio LLC
    License: https://imagemagick.org/script/license.php
    Features: Cipher DPC HDRI Modules OpenMP(5.0)
    Delegates (built-in): bzlib fontconfig freetype gslib heic jng jp2 jpeg jxl lcms lqr ltdl lzma openexr png ps raw tiff webp xml zlib zstd
    Compiler: gcc (X.X)


Install ASPECT
--------------

Two methods are available to install ASPECT:

* :ref:`install_from_release` (recommended)
* :ref:`install_from_sources`

.. _install_from_release:

Download Java archive of the latest release
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The easiest way to start using ASPECT is to download the latest release of the interpreter from the project's `GitHub Releases Page <https://github.com/abertagnon/aspect/releases/>`_. 
The release contains the ready-to-run Java archive (jar).
JDK version 11 or later is required to run the application.

.. code-block:: bash

    java -jar ASPECT.jar

.. _install_from_sources:

Building the sources
^^^^^^^^^^^^^^^^^^^^

If you want to use the latest version of ASPECT, or you cannot use the precompiled archive, you can compile the project from sources.

The ASPECT interpreter source code is managed using `Maven <https://maven.apache.org/>`_.
If it is not already installed on your system first you need to install Maven (refer to the official guide for the installation procedure).
You can check the version of Maven installed on your system using the following command:

.. code-block:: bash

    mvn --version

Move inside the folder where the sources are located and build the project.

.. code-block:: bash

    cd aspect-main
    mvn clean package


If the build is successful you can run the application with a command like:

.. code-block:: bash

    java -jar target/aspect-X.X.X-jar-with-dependencies.jar 
