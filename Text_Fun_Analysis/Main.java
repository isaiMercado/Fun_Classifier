import java.io.*;
import java.util.*;

import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations.PredictedClass;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.util.Properties;


public class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException {
       //test();
        trying();
    }

    private static void test() {
        PrintWriter out = new PrintWriter(System.out);
        
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment"); 
        props.setProperty("tokenize.options","normalizeCurrency=false");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
        
        Annotation annotation = new Annotation("This movie doesn't care about cleverness, wit or any other kind of intelligent humor.Those who find ugly meanings in beautiful things are corrupt without being charming.There are slow and repetitive parts, but it has just enough spice to keep it interesting.");
        

        pipeline.annotate(annotation);
        
        //pipeline.process("I kind of like school");
        pipeline.prettyPrint(annotation, out);


        // An Annotation is a Map and you can get and use the various analyses individually.
        // For instance, this gets the parse tree of the first sentence in the text.
        out.println();
        // The toString() method on an Annotation just prints the text of the Annotation
        // But you can see what is in it with other methods like toShorterString()
        out.println("The top level annotation's keys: ");
        out.println(annotation.keySet());
        out.flush();
    
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences != null && sentences.size() > 0) {
            ArrayCoreMap sentence = (ArrayCoreMap) sentences.get(0);


            out.println("Sentence's keys: ");
            out.println(sentence.keySet());
            out.println();
            out.flush();
            
            Tree tree2 = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            out.println("Sentiment class name:");
            out.println(sentence.get(SentimentCoreAnnotations.SentimentClass.class));
            out.println();
            out.flush();

            Iterator<Tree> it = tree2.iterator();
            while(it.hasNext()){
                Tree t = it.next();
                out.println(t.yield());
                out.println("nodestring:");
                out.println(t.nodeString());
                if(((CoreLabel) t.label()).containsKey(PredictedClass.class)){
                    out.println("Predicted Class: "+RNNCoreAnnotations.getPredictedClass(t));
                }
                out.println("Get Node Vector");
                out.println(RNNCoreAnnotations.getNodeVector(t));
                out.println("Get Node Predictions");
                out.println(RNNCoreAnnotations.getPredictions(t));
                out.println();
                out.flush();
            }
            
            out.println("The first sentence is:");
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            out.println();
            out.flush();
            out.println("The first sentence tokens are:");
            for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
              ArrayCoreMap aToken = (ArrayCoreMap) token;
              out.println(aToken.keySet());
              out.println(token.get(CoreAnnotations.LemmaAnnotation.class));
            }
            out.println();
            out.flush();

            out.println("The first sentence parse tree is:");
            tree.pennPrint(out);
            tree2.pennPrint(out);
            out.println();
            out.flush();

            out.println("The first sentence basic dependencies are:"); 
            out.println(sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class).toString(SemanticGraph.OutputFormat.LIST));
            out.println();
            out.flush();

            out.println("The first sentence collapsed, CC-processed dependencies are:");
            SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
            out.println(graph.toString(SemanticGraph.OutputFormat.LIST));
            out.println();
            out.flush();
        }
    }

    private static void trying() {
    
        PrintWriter out = new PrintWriter(System.out);
        
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment"); 
        props.setProperty("tokenize.options","normalizeCurrency=false");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
        
        Annotation annotation = new Annotation("I hate slow internet");
        pipeline.annotate(annotation);
        
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences != null && sentences.size() > 0) {
            ArrayCoreMap sentence = (ArrayCoreMap) sentences.get(0);


            out.println("Sentence's keys: ");
            
            out.println(sentence.keySet());
            out.println();
            out.flush();
            
            Tree tree2 = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            out.println("Sentiment class name:");
            out.println(sentence.get(SentimentCoreAnnotations.SentimentClass.class));
            out.println("Sentiment vector");
            out.println(RNNCoreAnnotations.getPredictions(tree2));
            out.flush();

        }

    }
    
}
