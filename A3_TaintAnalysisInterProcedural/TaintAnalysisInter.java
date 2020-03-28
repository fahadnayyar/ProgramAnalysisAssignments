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
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.Pair;

public class TaintAnalysisInter extends BackwardFlowAnalysis{

	//class members
	SootMethod currSootmethod;
	HashMap<SootMethod, Pair<Integer, Integer>> summary; 
	HashSet<String>[] inval, outval;
	Body b;

	Set<Local> inputs;
    Set<Local> parameters;
    
	public TaintAnalysisInter(UnitGraph g) {
		super(g);
		b = g.getBody();
		inputs = new HashSet<Local>();
		parameters = new HashSet<Local>();
		
		doAnalysis();
//		System.out.println("-------------------------------------------------");
//		System.out.println("\nInputs: ");
//		for(Local ll:inputs) {
//			System.out.print(ll.toString()+" ");
//		}
//		System.out.println("\nParameters: ");
//		for(Local ll:parameters) {
//			System.out.print(ll.toString()+" ");
//		}
//		System.out.println();
	}
	public TaintAnalysisInter(UnitGraph graph, SootMethod method, HashMap<SootMethod, Pair<Integer, Integer> > map) 
	{
		super(graph);	
		b = graph.getBody();
		currSootmethod=method;
		summary = map;
		inputs = new HashSet<Local>();
		parameters = new HashSet<Local>();
//		if(currSootmethod.getName().contains("main")) {
			System.out.println("\n\n\n Doing Analysis on method: "+method.toString());
//		}
		int mask1 = 0;
		int mask2 = 0;
		map.put(currSootmethod, new Pair<Integer, Integer>(mask1,mask2));
		doAnalysis();
//		if(currSootmethod.getName().contains("main")) {
			System.out.println("-------------------------------------------------");
			System.out.println("\nInputs: ");
			for(Local ll:inputs) {
				System.out.print(ll.toString()+" ");
			}
			System.out.println("\nParameters: ");
			for(Local ll:parameters) {
				System.out.print(ll.toString()+" ");
			}
			System.out.println();
			System.out.println("mask = "+ summary.get(method));
			System.out.println("Ended Analysis");
//		}
	}

	@Override
	protected void flowThrough(Object in, Object unit, Object out) {
		//declarations and castings
		inval = (HashSet<String>[]) in;
		outval = (HashSet<String>[]) out;
		Stmt u = (Stmt) unit;
		outval[0].clear();
		outval[0].addAll(inval[0]);
		outval[1].clear();
		outval[1].addAll(inval[1]);
//		inval.copy(outval);
//		if(currSootmethod.getName().contains("main")) {
	        System.out.println("\nStmt: " + u.toString()+" ");
	        System.out.println("InSet for return : ");
	        Iterator it = inval[0].iterator();
	        while (it.hasNext()) {
	            String inv = (String)it.next();
	            System.out.print(inv+",");
	        }
	        System.out.println("\nInSet for print : ");
	        it = inval[1].iterator();
	        while (it.hasNext()) {
	            String inv = (String)it.next();
	            System.out.print(inv+",");
	        }
	        System.out.println();
//		}
        
		//KILL 				
        if(u instanceof AssignStmt) {
        	AssignStmt assign = (AssignStmt)u;
        	outval[0].remove(assign.getLeftOp().toString());
        	outval[1].remove(assign.getLeftOp().toString());
        }
        
        // Gen
        if(u instanceof InvokeStmt) {
        	InvokeStmt invoke = (InvokeStmt)u;
    		InvokeExpr vix = (InvokeExpr)invoke.getInvokeExpr();
    		SootMethod method = vix.getMethod();
    		if(vix.getMethod().toString().contains("java.io.PrintStream") || vix.getMethod().toString().contains("java.lang.String")) {
    			for(Value val: vix.getArgs())
    			{
    				if(val instanceof Local) {
        				if(!(outval[1].contains(val.toString()))) {
    	        			outval[1].add(val.toString());
    	        		}
        			}
    			}
    		}
        	InvokeExpr expr = invoke.getInvokeExpr();
        	if(!(method.isJavaLibraryMethod()) && !(method.isConstructor()))
        	{
        		Pair<Integer, Integer> mask = summary.get(method);
				int i = 0;
				for(Value val:expr.getArgs())
				{
					if(val instanceof Local && (mask.getO2()&(1<<(i+1))) > 0) 
					{
						if(!(outval[1].contains(val.toString()))) 
						{
    	        			outval[1].add(val.toString());
    	        		}
					}
					i+=1;
				}
				if((mask.getO2() & 1) !=0)
				{
					Pair<Integer, Integer> p = summary.get(currSootmethod);
        			p.setO2(p.getO2() | 1);
        			summary.put(currSootmethod,p);
				}
        	}
        }
        if(u instanceof AssignStmt) {
        	AssignStmt assign = (AssignStmt)u;

        	if(inval[0].contains(assign.getLeftOp().toString()) && !(assign.containsInvokeExpr()))
        	{
        		for(ValueBox box: assign.getUseBoxes()) {
        			if(box.getValue() instanceof Local) {
        				if(!(outval[0].contains(box.getValue().toString()))) {
    	        			outval[0].add(box.getValue().toString());
    	        		}
        			}
        		}
        	}
        	if(inval[1].contains(assign.getLeftOp().toString()) && !(assign.containsInvokeExpr()))
        	{
        		for(ValueBox box: assign.getUseBoxes()) {
        			if(box.getValue() instanceof Local) {
        				if(!(outval[1].contains(box.getValue().toString()))) {
    	        			outval[1].add(box.getValue().toString());
    	        		}
        			}
        		}
        	}
        	
        	if(inval[1].contains(assign.getLeftOp().toString()) && assign.containsInvokeExpr()) {
        		InvokeExpr expr = (InvokeExpr)assign.getInvokeExpr();
//        		for(Value val: expr.getArgs()) {
//        			if(val instanceof Local) {
//        				if(!(outval.contains(val.toString()))) {
//    	        			outval.add(val.toString());
//    	        		}
//        			}
//        		}
        		if(expr.getMethod().toString().contains("java.lang.StringBuilder")) 
        		{
        			VirtualInvokeExpr vix = (VirtualInvokeExpr)expr;
//        			System.out.println(vix.getBase());
        			outval[1].add(vix.getBase().toString());
        			for(Value val: expr.getArgs()) {
            			if(val instanceof Local) {
            				if(!(outval[1].contains(val.toString()))) {
        	        			outval[1].add(val.toString());
        	        		}
            			}
            		}
        		}
        		if(expr.getMethod().toString().contains("java.lang.String")) 
        		{
        			for(Value val: expr.getArgs()) {
            			if(val instanceof Local) {
            				if(!(outval[1].contains(val.toString()))) {
        	        			outval[1].add(val.toString());
        	        		}
            			}
            		}
        		}
        		
        		SootMethod method = expr.getMethod();
        		if(!(method.isConstructor() || method.isJavaLibraryMethod()))
        		{        		
        			Pair<Integer, Integer> mask = summary.get(method);
					int i = 0;
					for(Value val:expr.getArgs())
					{
						if(val instanceof Local && (mask.getO2()&(1<<(i+1))) > 0) 
						{
							if(!(outval[1].contains(val.toString()))) {
	    	        			outval[1].add(val.toString());
	    	        		}
						}
						i+=1;
					}
					if((mask.getO2() & 1)!=0)
					{
						Pair<Integer, Integer> p = summary.get(currSootmethod);
						p.setO2(p.getO2() | 1);
						summary.put(currSootmethod, p);
					}
        		}
        	}
			if(inval[0].contains(assign.getLeftOp().toString()) && assign.containsInvokeExpr()) {
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
							if(!(outval[0].contains(val.toString()))) {
	    	        			outval[0].add(val.toString());
	    	        		}
						}
						i+=1;
					}
					if((mask.getO1() & 1)!=0)
					{
						Pair<Integer, Integer> p = summary.get(currSootmethod);
						p.setO1(p.getO1() | 1);
						summary.put(currSootmethod, p);
					}
	        	}
			}
        	if(assign.containsInvokeExpr())
        	{
        		InvokeExpr expr = (InvokeExpr)assign.getInvokeExpr();
            	if(expr.toString().contains("java.util.Scanner"))
        		{
        			inputs.add((Local)assign.getLeftOp());      
        			Pair<Integer, Integer> p = summary.get(currSootmethod);
        			if(inval[0].contains(assign.getLeftOp().toString()))
        			{
            			p.setO1(p.getO1() | 1);
        			}
        			if(inval[1].contains(assign.getLeftOp().toString()))
        			{
            			p.setO2(p.getO2() | 1);
        			}
        			
        			summary.put(currSootmethod,p);
        		}
        	}
        }
        
		if(u instanceof ReturnStmt)
		{
			ReturnStmt ret = (ReturnStmt)u;
        	for(ValueBox box:ret.getUseBoxes()) 
        	{
        		if(box.getValue() instanceof Local) {
        			if(!(outval[0].contains(box.getValue().toString()))) {
	        			outval[0].add(box.getValue().toString());
	        		}
    			}
        	}
		}
		
		if(u instanceof IdentityStmt)
		{
			
//			if(!currSootmethod.getName().equals("main"))
//			{
//				
//				if(u.toString().contains(" := @parameter"))
//				{				
////					inval.add(b.getParameterLocal(0).toString());		
//				}
//			}
//			else
//			{
//				if(u.toString().contains(" := @parameter"))
//				{			
//					int numparams=b.getMethod().getParameterCount();
//
//					for(int i=0;i<=numparams-1;i++)
//					{
//						
//					}	
//				}
//			}
			IdentityStmt ident = (IdentityStmt)u;
        	String var;
        	var = u.toString();
            if (var.contains("@parameter") ) 
            {
            	int start = var.indexOf("@parameter");
            	start = start+10;
            	int end = var.indexOf(":", start);
            	int parameter_num = Integer.parseInt(var.substring(start, end));
            	Pair<Integer, Integer> p = summary.get(currSootmethod);
            	if(outval[0].contains(ident.getLeftOp().toString()))
            	{
            		if(parameters.contains((Local)ident.getLeftOp()))
            		{
                    	parameters.add((Local)ident.getLeftOp());
            		}
            		p.setO1(p.getO1() | 1<<(parameter_num+1));
            	}
            	if(outval[1].contains(ident.getLeftOp().toString()))
            	{
            		if(parameters.contains((Local)ident.getLeftOp()))
            		{
                    	parameters.add((Local)ident.getLeftOp());
            		}
                	p.setO2(p.getO2() | 1<<(parameter_num+1));
            	}
            	summary.put(currSootmethod, p );
            }
		}
//		if(currSootmethod.getName().contains("main")) {
	        System.out.println("\nOutset: ");
	        System.out.println("For return :");
	        Iterator itOut = outval[0].iterator();
	        while (itOut.hasNext()) {
	            String inv = (String)itOut.next();
	            System.out.print(inv+",");
	        }
	        System.out.println();
	        System.out.println("For print :");
	        itOut = outval[1].iterator();
	        while (itOut.hasNext()) {
	            String inv = (String)itOut.next();
	            System.out.print(inv+",");
	        }
	        System.out.println();

//		}
	}

	@Override
	protected void copy(Object source, Object dest) {
		HashSet<String>[] srcSet = (HashSet<String>[]) source;
		HashSet<String>[] destSet = (HashSet<String>[]) dest;
		destSet[0].clear();
		destSet[0].addAll(srcSet[0]);
		destSet[1].clear();
		destSet[1].addAll(srcSet[1]);
//		srcSet.copy(destSet);
	}

	@Override
	protected Object entryInitialFlow() {
		HashSet<String> as[] = new HashSet[2];
		as[0] = new HashSet<String>();
		as[1] = new HashSet<String>();
		return as;
	}

	@Override
	protected void merge(Object out1, Object out2, Object in) {
		HashSet<String>[] inval1 = (HashSet<String>[]) out1;
        HashSet<String>[] inval2 = (HashSet<String>[]) out2;
        HashSet<String>[] outVal = (HashSet<String>[]) in;
        outVal[0].addAll(inval2[0]);
        outVal[0].addAll(inval1[0]);
        outVal[1].addAll(inval2[1]);
        outVal[1].addAll(inval1[1]);
//        inval1.union(inval2, outVal); //Merging in may analysis
	}

	@Override
	protected Object newInitialFlow() {
		HashSet<String> as[] = new HashSet[2];
		as[0] = new HashSet<String>();
		as[1] = new HashSet<String>();
		return as;
	}

}
