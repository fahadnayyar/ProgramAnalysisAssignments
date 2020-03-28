package CallGraphCreation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;

public class CallGraphBuilder
{	
	Stack<SootMethod> callstack=new Stack<SootMethod>();	

	public Stack<SootMethod> obtaincallgraph(CallGraph cg, SootMethod src)
	{
		//System.out.println("src: "+src);
		 Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(src));
		 
	 	if(!targets.hasNext())
			 return callstack;
	       while (targets.hasNext()) {
	    	  
	           SootMethod tgt = (SootMethod)targets.next();
	                   
	           if(!tgt.isJavaLibraryMethod())
	           {
	        	  //System.out.println(src + " may call " + tgt.getName());
	        	  if(callstack.contains(tgt))
	        		   callstack.remove(tgt);

	        	  //callstack.add(tgt);
	        	  
	        	  callstack.add(tgt);
	        	  obtaincallgraph(cg,tgt);
	        	  }
	           }
		  
	       return callstack;
	}
}
