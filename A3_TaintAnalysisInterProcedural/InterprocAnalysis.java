package CallGraphCreation;


import java.util.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import soot.*;
import soot.options.*;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.baf.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;

public class InterprocAnalysis {
	
	//summaries accessible to my intra class for updation and access
	static HashMap<SootMethod, Pair<Integer, Integer> > summary;
    
	public InterprocAnalysis()
	{
		//initialize summary
		
	}
	public static void main(String[] args) {
		//* initializing the summary hashtable.
		summary = new HashMap<>();
		
		System.out.println("***----------------starting-analysis----------------***");
		
		//* taking class name to be analyzed as command line argument input.
		final String classname=args[0];//"CallGraphCreation.Sample_test";//args[0]; //class whose main method would be analysed
		
		//* don't understand.
		Options.v().set_keep_line_number(true);
    	Options.v().setPhaseOption("jb", "use-original-names:true");
    	List<String> argsList = new ArrayList<String>(Arrays.asList(args));
 	   argsList.addAll(Arrays.asList(new String[]{
 			   "-w","-no-bodies-for-excluded",
 			   "-main-class",   
 			   classname,//main-class
 			   classname//"CallGraphCreation.Sample_test"//argument classes
 	   }));
 	
 	   //* don't understand.
 	   //To perform transformation on whole application(interprocedural)
 	   PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {
 		  @Override
 			protected void internalTransform(String phaseName, Map options) {
 			       CHATransformer.v().transform();
 	               SootClass a = Scene.v().getSootClass(classname);
 	               SootMethod src = Scene.v().getMainClass().getMethodByName("main");    //root of cg
 			       CallGraph cg = Scene.v().getCallGraph();   //call graph generated
 			       Stack<SootMethod> stack=new CallGraphBuilder().obtaincallgraph(cg,src);	 //call graph sorted topologically
 			      // System.out.println();
 			       if(stack.peek().getName().contains("init")) {// doubt
 			    	   stack.pop(); //to eliminate some init method
 			       }
 			       new InterprocAnalysis().buildsummary(stack);
 			}
 			   
 		   }));
 	  // System.out.println("out at performin this");
 	   //To perform transformation on method(intraprocedural)    
 	    PackManager.v().getPack("jtp").add(new Transform("jtp.instrumenter", new BodyTransformer()
 	    {
			protected void internalTransform(Body body, String phase, Map options)
			{		
				if(body.getMethod().getName().equals("main"))
				{
					SootMethod sm=body.getMethod();
					TaintAnalysisMain analysis = new TaintAnalysisMain((new ExceptionalUnitGraph(body)),sm, summary);						
				}			
			}
	    }));
 	    
 	args = argsList.toArray(new String[0]);
	Options.v().set_output_format(Options.output_format_jimple);
	soot.Main.main(args);
    }
	
	
	 public void buildsummary(Stack<SootMethod> stack)
	 {    	   	
		 
	    	while(!stack.isEmpty()) 
	    	{
	    		 SootMethod sm=stack.pop();
	    		 TaintAnalysisInter analysis_p = new TaintAnalysisInter(new ExceptionalUnitGraph(sm.getActiveBody()),sm, summary);
	    	}
	    }


}
