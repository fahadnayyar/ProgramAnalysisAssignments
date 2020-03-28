package CallGraphCreation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.scalar.Pair;

public class TaintAnalysisMain extends ForwardFlowAnalysis{
	SootMethod currSootmethod;
	HashMap<SootMethod, Pair<Integer, Integer>> summary; 
	FlowSet inval, outval;
	Body b;
	Set<Local> inputs;
    Set<Local> parameters;
	public TaintAnalysisMain(UnitGraph graph) {
		super(graph);	
		b = graph.getBody();
		inputs = new HashSet<Local>();
		parameters = new HashSet<Local>();
		doAnalysis();
	}

	public TaintAnalysisMain(UnitGraph graph, SootMethod method, HashMap<SootMethod, Pair<Integer, Integer> > map) 
	{
		super(graph);	
		b = graph.getBody();
		currSootmethod=method;
		summary = map;
		inputs = new HashSet<Local>();
		parameters = new HashSet<Local>();
		System.out.println("\n\n\n Doing Analysis on method: "+method.toString());
		int mask1 = 0;
		int mask2 = 0;
		map.put(currSootmethod, new Pair<Integer, Integer>(mask1,mask2));
		doAnalysis();
	}
	
	@Override
	protected void flowThrough(Object in, Object unit, Object out) {
		inval = (FlowSet)in;
		outval = (FlowSet)out;
		Stmt u = (Stmt)unit;
		inval.copy(outval);
		
		System.out.println("\nStmt: " + u.toString()+" ");
        System.out.println("InSet: ");
        Iterator it = inval.iterator();
        while (it.hasNext()) {
            String inv = (String)it.next();
            System.out.print(inv+",");
        }
		
		//Kill
		if(u instanceof AssignStmt)
		{
			boolean flag = false;
			AssignStmt assign = (AssignStmt)u;
			if(!(assign.containsInvokeExpr()))
			{
				for(ValueBox box: assign.getUseBoxes()) {
	    			if(box.getValue() instanceof Local) {
	    				if(outval.contains(box.getValue().toString())) {
	    					flag = true;
		        		}
	    			}
	    		}
			}
			if(assign.containsInvokeExpr())
			{
				InvokeExpr expr = (InvokeExpr)assign.getInvokeExpr();
				SootMethod method = expr.getMethod();
        		if(!(method.isConstructor() || method.isJavaLibraryMethod()))
        		{        		
        			Pair<Integer, Integer> mask = summary.get(method);
					int i = 0;
					for(Value val:expr.getArgs())
					{
						if(val instanceof Local && (mask.getO1()&(1<<(i+1))) > 0) 
						{
							if(outval.contains(val.toString())) {
//								System.out.println(val.toString());
								flag = true;
	    	        		}
						}
						i+=1;
					}
					if((mask.getO1() & 1)!=0)
					{
//						System.out.println("hi");
						flag = true;
					}
        		}
			}
			if(flag == false)
			{
				outval.remove(assign.getLeftOp().toString());
			}
		}
		
		//Gen
		if(u instanceof AssignStmt)
		{
			boolean flag = false;
			AssignStmt assign = (AssignStmt)u;
			if(assign.containsInvokeExpr())
        	{
        		InvokeExpr expr = (InvokeExpr)assign.getInvokeExpr();
            	if(expr.toString().contains("java.util.Scanner"))
        		{
            		if(!(outval.contains(assign.getLeftOp().toString())))
            		{
                		outval.add(assign.getLeftOp().toString());            			
            		}
        		}
            }
			if(!(assign.containsInvokeExpr()))
			{
				for(ValueBox box: assign.getUseBoxes()) {
	    			if(box.getValue() instanceof Local) {
	    				if(outval.contains(box.getValue().toString())) {
	    					flag = true;
		        		}
	    			}
	    		}
			}
			if(assign.containsInvokeExpr())
			{
				InvokeExpr expr = (InvokeExpr)assign.getInvokeExpr();
				SootMethod method = expr.getMethod();
        		if(!(method.isConstructor() || method.isJavaLibraryMethod()))
        		{        		
        			Pair<Integer, Integer> mask = summary.get(method);
					int i = 0;
					for(Value val:expr.getArgs())
					{
						if(val instanceof Local && (mask.getO1()&(1<<(i+1))) > 0) 
						{
							if(outval.contains(val.toString())) {
								flag = true;
	    	        		}
						}
						i+=1;
					}
					if((mask.getO1() & 1)!=0)
					{
						flag = true;
					}
        		}
			}
			if(flag == true)
			{
				if(!(outval.contains(assign.getLeftOp().toString())))
				{
					outval.add(assign.getLeftOp().toString());
				}
			}
		}
		
		 System.out.println("\nOutset: ");
		 Iterator itOut = outval.iterator();
		 while (itOut.hasNext()) {
			 String inv = (String)itOut.next();
			 System.out.print(inv+",");
		 }
		 System.out.println();
		 System.out.println();
		
		//Print
		if(u instanceof ReturnStmt)
		{
			ReturnStmt ret = (ReturnStmt)u;
			for(ValueBox box:ret.getUseBoxes()) 
        	{
        		if(box.getValue() instanceof Local) {
        			if((outval.contains(box.getValue().toString()))) {
	        			System.out.println("Variable "+box.getValue().toString() + " is tainted because of return stmt");
        			}
    			}
        	}
		}
		 if(u instanceof InvokeStmt) {
	        	InvokeStmt invoke = (InvokeStmt)u;
	    		InvokeExpr vix = (InvokeExpr)invoke.getInvokeExpr();
	    		SootMethod method = vix.getMethod();
	    		if(vix.getMethod().toString().contains("java.io.PrintStream") || vix.getMethod().toString().contains("java.lang.String")) {
	    			for(Value val: vix.getArgs())
	    			{
	    				if(val instanceof Local) {
	        				if((outval.contains(val.toString()))) {
	    	        			System.out.println("\n Variable "+val.toString()+" is tainted because of print stmt");
	    	        		}
	        			}
	    			}
	    		}
		 }
		 if(u.containsInvokeExpr())
		 {
			InvokeExpr expr = (InvokeExpr)u.getInvokeExpr();
    		SootMethod method = expr.getMethod();
	 		if(!(method.isJavaLibraryMethod() || method.isConstructor()))
	 		{
 				Pair<Integer, Integer> mask = summary.get(method);
				int i = 0;
				for(Value val:expr.getArgs())
				{
					if(outval.contains(val.toString()) && val instanceof Local && (mask.getO2()&(1<<(i+1))) > 0) 
					{
						System.out.println("\n Variable "+val.toString()+" is tainted because of print stmt");
					}
					i+=1;
				}	 		
			}
		 }	
		 

	}

	@Override	
	protected void copy(Object source, Object dest) {
		FlowSet srcSet = (FlowSet)source;
		FlowSet	destSet = (FlowSet)dest;
		srcSet.copy(destSet);
	}
	@Override
	protected Object entryInitialFlow() {
		return new ArraySparseSet();
	}

	@Override
	protected void merge(Object in1, Object in2, Object out) {
		FlowSet inval1=(FlowSet)in1;
		FlowSet inval2=(FlowSet)in2;
		FlowSet outSet=(FlowSet)out;
		// May analysis
		inval1.union(inval2, outSet);
	}

	@Override
	protected Object newInitialFlow() {
		return new ArraySparseSet();
	}

}
