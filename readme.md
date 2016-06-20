# Scale-Invariant Import Reuse from Input/Ouput-Tables

This is the repository for the term paper with the same title. It is structured as follows:

\tex contains the LaTeX files for the term paper for the International Trade Seminar
\tex\termpaper.pdf is the compiled term paper
\data contains input data, most notably the full dataset of WIOD (world input-output database).
\results contains output data in various formats
\results\all.csv contains all resulting processing trade propensities, import reuse, and others
\src contains the Java source code
\src\com.meissereconoimcs.seminar.run contains runnables that each produce an output with the same name and the .out extension

One way to compile and run the source code is to
1. Install a git client like source tree: https://www.sourcetreeapp.com/ and download the source code
2. Install a Java IDE like eclipse.org and import the downloaded source code as a Maven project
3. Run the runnables