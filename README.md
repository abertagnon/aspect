# ASPECT: Answer Set rePresentation as vEctor graphiCs in laTex
### An intuitive and declarative way to graphically represent answer sets in LaTeX

> The ASPECT interpreter is under development, expect frequent updates and bugs.

## Installation

> The current implementation of ASPECT has been tested only on Ubuntu 22.04 other operating systems (e.g., Windows, macOS) are expected to be fully supported in upcoming releases.

The ASPECT interpreter is written in Java, and in the current release it depends on an ASP solver and a LaTeX distribution. We chose [clingo](https://potassco.org/clingo/) as ASP solver and [TeX Live](https://www.tug.org/texlive/) as LaTeX software distribution.

**Please refer to the manual of the various software for their installation**. If you are in a hurry, an easy way to install the various dependencies is to run the following commands:

clingo (using conda):

    conda install -c potassco clingo

TeX Live (on Debian based Linux distributions):

    sudo apt-get install texlive-latex-base
    sudo apt-get install texlive-latex-extra


To make sure everything is ready try running in the bash the `clingo --version` command and the `pdflatex --version` command.


### Compiling ASPECT

For the first run, ASPECT must be compiled using the Java compiler. To run the compiler, simply run the command:

    javac ASPect.java

## How to use

ASPECT supports various operating modes.

> The various operating modes of ASPECT will be described with future updates of this guide.

To try ASPECT run the following command:

    java ASPect ./examples/n_queens/queens.lp


## Note

This tool is described in the paper "ASPECT: Answer Set rePresentation as vEctor graphiCs in laTex" by Alessandro Bertagnon, Marco Gavanelli and Fabio Zanotti under review for publication at the 38th Italian Conference on Computational Logic (CILC 2023).
