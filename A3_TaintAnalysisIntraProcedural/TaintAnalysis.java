package TaintAnalysis;
import soot.Local;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.Pair;
import soot.util.Chain;

import java.util.*;

public class TaintAnalysis extends BackwardFlowAnalysis {
    FlowSet inval, outval;
    Set<String> localsSet;
    Map< Stmt, Pair < FlowSet , FlowSet > > finalFlowSets;
    Set<String> inputSet;
    Set<String> paramSet;
    public TaintAnalysis(UnitGraph g)
    {
        super(g);
        localsSet = new HashSet<String>();
        inputSet = new HashSet<String>();
        paramSet = new HashSet<String>();
        finalFlowSets = new HashMap<Stmt, Pair < FlowSet , FlowSet >>();

        Chain<Local> locals =  g.getBody().getLocals();
        for (Local local : locals)
        {
            String localStr = local.toString();
            if ( !(( localStr.contains("this"))) )
            {
                localsSet.add(localStr);
            }
        }
        print("");
        print("All local variables of this method are: ");
        for (String localStr : localsSet)
        {
            print1(localStr);
            print1(", ");
        }
        print("");

        doAnalysis();

        print("***----------printing main output of this method------------***");
        //* printing finalFlowsets
        for (Stmt stmt : finalFlowSets.keySet())
        {
            if (stmt.toString().contains("this"))
            {
                continue;
            }

            print("");
            FlowSet inSet = finalFlowSets.get(stmt).getO1();
            FlowSet outSet = finalFlowSets.get(stmt).getO2();
            int flagin = 0;
            int flagout = 0;
            Iterator inSetItr = null;
            if (!inSet.isEmpty())
            {
              inSetItr = inSet.iterator();
                flagin=1;
            }
            Iterator outSetItr = null;
            if (!outSet.isEmpty())
            {
                outSetItr = outSet.iterator();
                flagout = 1;
            }

            //* printing inset:
            print1("inset: { ");
            if (flagin==1)
            {
                while (inSetItr.hasNext())
                {
                    String insetVar = inSetItr.next().toString();
//                    if (insetVar.contains("sc"))
//                    {
//                        continue;
//                    }
                    print1(insetVar);
                    print1(", ");
                }
            }
            print("}");


            //* printing statement:
            print1("Unit: ");
            print(stmt.toString());


            //* printintg outset:
            print1("outset: { ");
            if (flagout==1)
            {
                while (outSetItr.hasNext())
                {
                    String outsetVar = outSetItr.next().toString();
//                    if (outsetVar.contains("sc"))
//                    {
//                        continue;
//                    }
                    print1(outsetVar);
                    print1(", ");
                }
            }
            print("}");


            print("");
        }

        //* printing inputs on which sinks depends:
        print("//* sink depends on Source (inputs): ");
        for (String input: inputSet)
        {
            print1(input);
            print1(", ");
        }
        print("") ; print("");


        //* printing parameters on which sinks depends:
        print("//* sink depends on Source (parameters): ");
        for (String param: paramSet)
        {
            print1(param);
            print1(", ");
        }
        print("");
        print("***----------main output of this method ends here------------***");
        print("");
    }
    @Override
    protected void flowThrough(Object in, Object unit, Object out) {
        inval = (FlowSet) in;
        outval = (FlowSet) out;
        Stmt u = (Stmt) unit;
        inval.copy(outval);

        //* printint the statememnt:
        print("");
        print("current statement is: ");
        print(u);
        print1("class of statpement is: ");
        print(u.getClass());
        if (u instanceof JIdentityStmt)
        {
            JIdentityStmt idstmt = (JIdentityStmt)u;
            String paramvar = idstmt.getLeftOp().toString();
            if (localsSet.contains(paramvar))
            {
                if (outval.contains(paramvar))
                {
                    paramSet.add(paramvar);
                }
            }
        }
        if (u instanceof JReturnStmt)
        {
            JReturnStmt rstmt = ((JReturnStmt) u);
            print("this is a return statement: ");
            print("the variable being returned is: ");
            String retVar =  rstmt.getOp().toString(); //imp
            print(retVar);
            if (!outval.contains(retVar))
            {
//                print("hi");
//                print(retVar);
//                print("hello");
                outval.add(retVar);
            }
        }
        else if ( (u instanceof JInvokeStmt) && (u.toString().contains("print")))
        {
            JInvokeStmt ivstmt = (JInvokeStmt)u;
            List usedVars = ivstmt.getUseBoxes();
            for (Object o : usedVars)
            {
                if (o instanceof ImmediateBox)
                {
                    print(o.getClass());
                    String printedVar = ((ImmediateBox)o).getValue().toString(); //imp
                    if (localsSet.contains(printedVar))
                    {
                        print(printedVar);
                        if (!outval.contains(printedVar))
                        {
//                            print("hi");
//                            print(printedVar);
//                            print("hello");
                            outval.add(printedVar);
                        }
                    }

                }
            }
            print("this is a print statement");
        }
        else if (u instanceof JAssignStmt )
        {
            AssignStmt asStmt = (JAssignStmt)u;
            String assignedVar = asStmt.getLeftOp().toString();
            List<ValueBox> rightOps = asStmt.getUseBoxes();
            if (localsSet.contains(assignedVar) && outval.contains(assignedVar))
            {
                print(assignedVar);
                int gflag = 0;
                for (ValueBox val : rightOps)
                {
                    String usedVar = val.getValue().toString();
                    if (localsSet.contains(usedVar))
                    {
                        gflag = 1;
                        print(usedVar);
                        if (u.containsInvokeExpr())
                        {
                            if (!outval.contains(assignedVar))
                            {
//                                print("hi");
//                                print(assignedVar);
//                                print("hello");
                                outval.add(assignedVar);

                            }

                            inputSet.add(assignedVar);
                        }
                        else if (!outval.contains(usedVar))
                        {
//                            print("hi");
//                            print(usedVar);
//                            print("hello");
                            outval.add(usedVar);
                        }
                    }else
                    {
                        if (gflag==1){
                            continue;
                        }

                        int flag = 0;
                        try{
                            Integer.parseInt(usedVar);
                            flag = 1;
                        } catch(Exception e){
                            try {
                                Float.parseFloat(usedVar);
                                flag = 1;
                            }catch(Exception ee) {

                            }
                        }
                        if (flag==1)
                        {
                            // here it means this is statement of these types: a = 10; or a = 10.5
                            // kill
                            print(usedVar);
                            if (!u.containsInvokeExpr())
                            {
                                outval.remove(assignedVar);
                            }
                        }
                    }
                }

                // kill:
                if (gflag==0 && !u.containsInvokeExpr())
                {
                    outval.remove(assignedVar);
                }

                if (gflag==1)
                {
                    if (!outval.contains(assignedVar))
                    {
//                        print("hi");
//                        print(assignedVar);
//                        print("hello");
                        outval.add(assignedVar);
                    }
                }


            }


            print("this is an assignment statement");
        }
        print("");


        if (!finalFlowSets.containsKey(u))
        {
            finalFlowSets.put(u,new Pair<FlowSet, FlowSet>());
        }
        finalFlowSets.get(u).setO1(inval);
        finalFlowSets.get(u).setO2(outval);
//        print("niklo");
//        print(inval);
//        print(u);
//        print(outval);
//        print("niklo1");
    }

    @Override
    protected Object newInitialFlow() {
        ArraySparseSet nif = new ArraySparseSet();
        return nif;
    }

    @Override
    protected Object entryInitialFlow() {
        ArraySparseSet eif = new ArraySparseSet();
        return eif;
    }

    @Override
    protected void merge(Object in1, Object in2, Object out) {
        FlowSet inval1 = (FlowSet) in1;
        FlowSet inval2 = (FlowSet) in2;
        FlowSet outVal = (FlowSet) out;
        inval1.union(inval2, outVal); //Merging in may analysis
    }

    @Override
    protected void copy(Object source, Object dest) {
        FlowSet srcSet = (FlowSet) source;
        FlowSet destSet = (FlowSet) dest;
        srcSet.copy(destSet);
    }

    protected void print(Object s)
    {
        System.out.println(s.toString());
    }
    protected void print1(Object s)
    {
        System.out.print(s.toString().toString());
    }
}

//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Iterator;
//import java.util.List;
//import soot.Value;
//
//import soot.toolkits.graph.UnitGraph;
//import soot.Body;
//import soot.Local;
//import soot.Unit;
//import soot.ValueBox;
//import soot.jimple.AssignStmt;
//import soot.jimple.IdentityStmt;
//import soot.jimple.Stmt;
//import soot.tagkit.LineNumberTag;
//import soot.toolkits.graph.ExceptionalUnitGraph;
//import soot.toolkits.graph.UnitGraph;
//
//import soot.toolkits.scalar.FlowSet;
//import soot.toolkits.scalar.AbstractFlowSet;
//import soot.toolkits.scalar.ArraySparseSet;
//import soot.toolkits.scalar.ForwardFlowAnalysis;
//import soot.toolkits.scalar.Pair;
//import soot.util.Chain;
//
//public class TaintAnalysis extends ForwardFlowAnalysis {
//
//    Body b;
//    FlowSet inval, outval;
//    Map<String, ArrayList<String>> final_output;
//
//    public TaintAnalysis(UnitGraph g) {
//        super(g);
//        b = g.getBody();
//        final_output = new HashMap<String, ArrayList<String>>();
//        doAnalysis();
//        //System.out.println("\n\n--------------Final out set------------");
//        Iterator it = outval.iterator();
//        while (it.hasNext()) {
//            Pair inv = (Pair<String, String>) it.next();
//            //System.out.print("[" + inv.getO1().toString() + ", " + inv.getO2().toString() + "]  ");
//        }
//        //System.out.println();
//        for (String key : final_output.keySet())
//        {
//            System.out.print(key +" => ");
//            for (String val : final_output.get(key))
//            {
//                System.out.print(val+", ");
//            }
//            System.out.println();
//        }
//    }
//
//    @Override
//    protected void flowThrough(Object in, Object unit, Object out) {
//        inval = (FlowSet) in;
//        outval = (FlowSet) out;
//        Stmt u = (Stmt) unit;
//        inval.copy(outval);
//        String var;
//        LineNumberTag tag = (LineNumberTag) u.getTag("LineNumberTag");
//        if (u instanceof AssignStmt || u instanceof IdentityStmt) {
//            //System.out.println("\nStmt: " + u.toString() + " at " + tag);
//
//            //System.out.println("InSet: ");
//            Iterator it = inval.iterator();
//            while (it.hasNext()) {
//                Pair inv = (Pair<String, String>) it.next();
//                //System.out.print("[" + inv.getO1().toString() + ", " + inv.getO2().toString() + "]  ");
//            }
//            //System.out.println();
//        }
//
//        /*
//        Using identity statements to set the function parameters from ? to .
//        You may use other techniues/APIs to set parameters as . in entryInitialFlow itself
//        */
//        if (u instanceof IdentityStmt) {
//            //System.out.println("Identity: " + u.toString());
//            Iterator<ValueBox> defIt = u.getDefBoxes().iterator();
//            while (defIt.hasNext()) {
//                var = defIt.next().toString().substring(15);
//                var = var.substring(0, var.lastIndexOf(")"));
//                //System.out.println("defIt: " + var);
//                if (!var.equals("this")) {
//                    Pair p = new Pair(var, "?");
//                    if (outval.contains(p)) {
//                        outval.remove(p);
//                        Pair param = new Pair(var, ".");
//                        outval.add(param);
//                    }
//                }
//            }
//        }
//
//        //Kill all definitions of the var which is defined
//        if ((u instanceof AssignStmt) && (!(u instanceof IdentityStmt))) {
//            Iterator<ValueBox> defIt = u.getDefBoxes().iterator();
//            ArrayList<Pair<String, String>> pairsToBeKilled = new ArrayList<Pair<String, String>>();
//            if (defIt.hasNext()) {
//                String definedVar = defIt.next().getValue().toString();
//                Iterator itOut = outval.iterator();
//                while (itOut.hasNext()) {
//                    Pair p = (Pair<String, String>) itOut.next();
//                    if (p.getO1().toString().equals(definedVar)) {
//                        if (p.getO2().equals("?"))
//                        {
//                            if (!final_output.containsKey(definedVar))
//                            {
//                                final_output.put(definedVar,new ArrayList<String>());
//                            }
//                            if (!final_output.get(definedVar).contains(((AssignStmt) u).getRightOp().toString()))
//                            {
//                                final_output.get(definedVar).add(((AssignStmt) u).getRightOp().toString());
//                            }
//
//                        }
//                        //System.out.println(u.toString());
//                        pairsToBeKilled.add(p);
//
//                    }
//                }
//            }
//            Iterator<Pair<String, String>> pIt = pairsToBeKilled.iterator();
//            while (pIt.hasNext()) {
//                Pair<String, String> p = pIt.next();
//                outval.remove(p);
//                //System.out.println("Killing " + p.getO1() + " " + p.getO2());
//            }
//        }
//
//        //Gen the new definition: current one
//        if ((u instanceof AssignStmt) && (!(u instanceof IdentityStmt))) {
//            Iterator<ValueBox> defIt = u.getDefBoxes().iterator();
//            while (defIt.hasNext()) {
//                String first = defIt.next().getValue().toString();
//                if (!first.contains("$"))  //prune soot introduced intermediate variables
//                {
//                    Pair p = new Pair(first, tag.toString());
//                    outval.add(p);
//                    //System.out.println("Generating " + p.getO1() + " " + p.getO2());
//                }
//            }
//        }
//        if (u instanceof AssignStmt || u instanceof IdentityStmt) {
//            //System.out.println("\nOutset: ");
//            Iterator itOut = outval.iterator();
//            while (itOut.hasNext()) {
//                Pair inv = (Pair<String, String>) itOut.next();
//                //System.out.print("[" + inv.getO1().toString() + ", " + inv.getO2().toString() + "]  ");
//            }
//            //System.out.println();
//        }
//    }
//
//    @Override
//    protected void copy(Object source, Object dest) {
//        FlowSet srcSet = (FlowSet) source;
//        FlowSet destSet = (FlowSet) dest;
//        srcSet.copy(destSet);
//    }
//
//    //Setting the entry set for BInit
//    @Override
//    protected Object entryInitialFlow() {
//        ArraySparseSet as = new ArraySparseSet();
//        Chain<Local> locals = this.b.getMethod().getActiveBody().getLocals(); //Collecting all locals i the current method
//        Iterator<Local> i2 = locals.iterator();
//        while (i2.hasNext()) {
//            Local ll = i2.next();
//            String name = ll.getName();
//            if (!((name.equals("this")) || (name.charAt(0) == '$'))) //Filtering the intermediate variables created by Soot
//            {
//                Pair<String, String> p = new Pair<String, String>(name, "?"); //To retain more information you may use <Local,String>
//                as.add(p);
//                //System.out.println("Adding in entryInitial flow: " + p);
//            }
//        }
//        return as;
//    }
//
//    @Override
//    protected void merge(Object in1, Object in2, Object out) {
//        FlowSet inval1 = (FlowSet) in1;
//        FlowSet inval2 = (FlowSet) in2;
//        FlowSet outVal = (FlowSet) out;
//        inval1.union(inval2, outVal); //Merging in may analysis
//
//    }
//
//    @Override
//    protected Object newInitialFlow() //Initializing entryy set of each statement
//    {
//        ArraySparseSet as = new ArraySparseSet();
//        return as;
//    }
//
//}
