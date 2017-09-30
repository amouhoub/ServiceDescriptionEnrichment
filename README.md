# ServiceDescriptionEnrichment
A framework for enriching Semantic Web Service descriptions with I/O relations from Ontologies and Text descriptions

It comes with a default set of SW services from OWLS-TCv4 for evaluation and experimentation purposes.

### Running the Application

Due to some dependencies problem with Apache Jena (It doesn't allow to be used for the moment from within an executable jar-with-depdendencies), the executable jar requires a seperate dependencies folder at /libs.

To execute: first call the following to copy all the dependencies into a separate folder (Do it ONLY once unless you change the POM)
```
mvn prepare-package
```
Then
```
mvn install
```
After that you can launch the app from the runnable jar.

### config.properties

The required URLs for our word2vec API endpoint (hosted somewhere warm) and for the OWLS-TCv4 ontologies SPARQL endpoint are set by default in the config.properties file. Only modify them if you have your own mirror of our word2vec API or if we have provided you with mirror links.

### Publications

For more details, please check our paper at COOPIS2017:

"Towards an Automatic Enrichment of Semantic Web Services Descriptions"

Mohamed LAmine Mouhoub, Daniela Grigori, Maude Manouvrier
