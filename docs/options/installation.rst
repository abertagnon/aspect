Installation
++++++++++++

Prerequisites
-------------

The current release of ASPECT depends on an ASP solver and a LaTeX distribution. 
We chose `clingo <https://potassco.org/clingo/>`_ as ASP solver and `TeX Live <https://www.tug.org/texlive/>`_ as LaTeX software distribution.

**Please refer to the manual of the various software for their installation.**
If you are in a hurry, an easy way to install the various dependencies is to run the following commands.

clingo (using conda):

.. code-block:: bash

    conda install -c potassco clingo

TeX Live (on Debian based Linux distributions):

.. code-block:: bash

    sudo apt-get install texlive-latex-base
    sudo apt-get install texlive-latex-extra

To make sure everything is ready try running in a shell the following commands:

* ``clingo --version`` 
* ``pdflatex --version``
* ``lualtex --version``

Install ASPECT
--------------

Two methods are available to install ASPECT:

* Download the Java archive of the latest release
* Build the sources


Download Latest Release from GitHub
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The easiest way to start using ASPECT is to download the latest release of the interpreter from the project's `GitHub Releases Page <https://github.com/abertagnon/aspect/releases/>`_. 
The release contains the ready-to-run Java archive (jar).
JDK version 11 or later is required to run the application.

.. code-block:: bash

    java -jar ASPECT.jar


Building the sources
^^^^^^^^^^^^^^^^^^^^

If you want to use the latest version of ASPECT, or you cannot use the precompiled archive, you can compile the project from sources.

The ASPECT interpreter source code is managed using `Maven <https://maven.apache.org/>`_.
If it is not already installed on your system first you need to install Maven, refer to the official guide for the installation procedure.
You can check the version of Maven installed on your system using the following command:

.. code-block:: bash

    mvn --version

Move inside the folder where the sources are contained and build the project.

.. code-block:: bash

    cd aspect-main
    mvn clean package


If the build is successful you can run the application with a command like:

.. code-block:: bash

    java -jar target/aspect-0.1.0-jar-with-dependencies.jar 


