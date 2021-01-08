package contentAnalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class SentimentAnalysis {
	
	

	/**
	 * le pipeline Stanford NLP
	 */
    StanfordCoreNLP pipeline;

    /**
     * Dossiers d'entrée 
     */
    File inputDir;

    
    /**
     * Dossiers de sortie 
     */
    File outputDir;
    
    /**
     * les nouveaux dossier pour les sorties à créer
     */
	File posTaggedDir;
	File posTagFilteredDir;
	File lemmatizedDir;

	
	/**
	 * 	 le fichier de sortie (nouns,adjectives and adverbs seulement )
	 */
	BufferedWriter filteredPosWriter;
	/**
	 *  fichier de sortie pour le stemming
	 */
	BufferedWriter lemmatizedWriter;
	/**
	 *  ficheir de sortie incluant les tokens et leurs POS tags
	 */
	BufferedWriter posTagWriter;
	
	
	/**
	 * environnement pour les opérations NLP
	 * @param projectDirName
	 */
	public SentimentAnalysis(){
		
		this.setPropertiesForStanfordCoreNLP();
	}

	
	/**
	 * les propriétés pour Stanford NLP (quels outils à utiliser)
	 */
	private void setPropertiesForStanfordCoreNLP(){
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma");
	    pipeline = new StanfordCoreNLP(props);
	    
	}
	
	/**
	 *  crée des sous dossier pour les ficheirs de sortie 
	 */
	public void setOutputFolder(String dirOut){

		this.outputDir = new File(dirOut);
		
		this.posTaggedDir = new File(this.outputDir, "posTagged");
		this.posTagFilteredDir = new File(this.outputDir, "posTagFiltered");
		this.lemmatizedDir = new File (this.outputDir, "lemmatized");
		
		try {
			posTaggedDir.mkdir();
			posTagFilteredDir.mkdir();
			lemmatizedDir.mkdir();
		} catch (Exception e){
			//  s'ils existent déjà cela va être appelé
			e.printStackTrace();
		}
	}

	/**
	 *  ouvrir tous les ficheirs d'écriture pour les fichier d'ecriture
	 * @param name
	 * @throws IOException
	 */
	private void openWriters(String fileName) throws IOException{
				
		// initialiser les writers
		this.posTagWriter  = new BufferedWriter( new FileWriter( new File( this.posTaggedDir, fileName)));
		this.filteredPosWriter = new BufferedWriter( new FileWriter( new File( this.posTagFilteredDir, fileName )));
		this.lemmatizedWriter = new BufferedWriter( new FileWriter( new File( this.lemmatizedDir, fileName)));

	}
	
	/**
	 * fermer les fichiers de sortie
	 * @throws IOException
	 */
	private void closeWriters() throws IOException{
		this.filteredPosWriter.close();
		this.lemmatizedWriter.close();
		this.posTagWriter.close();
	}
	

	/**
	 * retourne le contenu de chaque fichier comme String
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private String readTextFromFile(String fileName) throws IOException{

	    String text = "";
		BufferedReader reader = new BufferedReader( new FileReader ( new File(this.inputDir, fileName)));
	    String line = reader.readLine();
	    while (line != null){
	    	text = text.concat(line);
	    	line = reader.readLine();
	    }
	    reader.close();
	    return text;
	}
	
	
	
	/*
	 * les choses interessantes se passent ici
	 */
	
	/**
	 * appliquer Stanford Core NLP tools au contenu du ficheir
	 *  écrire les sortie traitées dans un nouveau fichier
	 * @param fileName
	 * @throws IOException
	 */
	public void analyze(String fileName) throws IOException{

		this.openWriters(fileName);

	    //  lire des textes dans la variable texte
	    String text = this.readTextFromFile(fileName);  

	    // creer une annotation vide
	    Annotation document = new Annotation(text);
	    
	    // démarrer les annotations pour ce texte
	    this.pipeline.annotate(document);
	    
	    // les phrase de ce document
	    // Un CoreMap est essentiellement une carte qui utilise des objets de classe comme des clés et possède des valeurs avec des types personnalisés
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	      // Traversant les mots dans la phrase courante
	      // Un CoreLabel est un CoreMap avec des méthodes supplémentaires d'un Token Specifique 
	      for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        // le texte du token
	        String word = token.get(TextAnnotation.class);
	        // le POS tag du token
	        String pos = token.get(PartOfSpeechAnnotation.class);   
	        //  lemma pour le token
	        String lemma = token.get(LemmaAnnotation.class);
			this.lemmatizedWriter.write(lemma + "\n");
			this.posTagWriter.write(word+"__"+pos+"\n");

				if (pos.equalsIgnoreCase("JJ") || pos.equalsIgnoreCase("RB")){
					this.filteredPosWriter.write(word+"__"+pos+"\n");
				}


	      }

	    }
	    
	    //fermer les ficheirs
	    this.closeWriters();
		
	}
	
	/**
	 * itere sur tous les ficheir et les analyse
	 * @throws IOException
	 */
	public void analyzeAllFiles(String inputDirName) throws IOException{

		this.inputDir = new File (inputDirName);
		String[] fileNames = inputDir.list();
		for (String name: fileNames){
			System.out.println("Processing file: "  + name);
			this.analyze(name);			
		}
	}
	
	
	
	
	public static void main(String[] args) throws IOException{
		/*
		 * (input and) les dossier de sortie doivent figurer avant l'execution
		 * 
		 */
		
		SentimentAnalysis sAnalysis = new SentimentAnalysis();	
		
		//positive reviews
		sAnalysis.setOutputFolder("data/pos/output");
		sAnalysis.analyzeAllFiles("data/pos/input");
		
		//negative reviews
		sAnalysis.setOutputFolder("data/neg/output");
		sAnalysis.analyzeAllFiles("data/neg/input");
	}

}
