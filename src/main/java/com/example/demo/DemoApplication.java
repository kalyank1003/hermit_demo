package com.example.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDataPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentDataPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentObjectPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredIndividualAxiomGenerator;
import org.semanticweb.owlapi.util.InferredInverseObjectPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredObjectPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubDataPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(DemoApplication.class, args);
		
		String path = getPath();
		
		String input = path+"/data/input/new_input.owl";

		String output = path+"/data/output/rdf_out.xml";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		File inputOntologyFile = new File(input);
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);
		ReasonerFactory factory = new ReasonerFactory();
		Configuration configuration = new Configuration();
		configuration.ignoreUnsupportedDatatypes = true;
		OWLReasoner reasoner = factory.createReasoner(ontology, configuration);

		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS,
				InferenceType.OBJECT_PROPERTY_HIERARCHY, InferenceType.DATA_PROPERTY_HIERARCHY,
				InferenceType.OBJECT_PROPERTY_ASSERTIONS);


		List<InferredAxiomGenerator<? extends OWLAxiom>> generators = new ArrayList<>();
		generators.add(new InferredSubClassAxiomGenerator());
		generators.add(new InferredClassAssertionAxiomGenerator());
		generators.add(new InferredDataPropertyCharacteristicAxiomGenerator());
		generators.add(new InferredEquivalentClassAxiomGenerator());
		generators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
		generators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
		generators.add(new InferredInverseObjectPropertiesAxiomGenerator());
		generators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());

		// NOTE: InferredPropertyAssertionGenerator significantly slows down
		// inference computation
		generators.add(new org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator());

		generators.add(new InferredSubClassAxiomGenerator());
		generators.add(new InferredSubDataPropertyAxiomGenerator());
		generators.add(new InferredSubObjectPropertyAxiomGenerator());
		List<InferredIndividualAxiomGenerator<? extends OWLIndividualAxiom>> individualAxioms = new ArrayList<>();
		generators.addAll(individualAxioms);

		//generators.add(new InferredDisjointClassesAxiomGenerator());

		InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, generators);
		OWLOntology inferredAxiomsOntology = manager.createOntology();
		iog.fillOntology(manager, inferredAxiomsOntology);
		File inferredOntologyFile = new File(output);
		if (!inferredOntologyFile.exists())
			inferredOntologyFile.createNewFile();
		inferredOntologyFile = inferredOntologyFile.getAbsoluteFile();
		OutputStream outputStream = new FileOutputStream(inferredOntologyFile);
		manager.saveOntology(inferredAxiomsOntology, manager.getOntologyFormat(ontology), outputStream);
		System.out.println("Completed");

	}
	
	private static String getPath() {
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		return s;
	}

}
