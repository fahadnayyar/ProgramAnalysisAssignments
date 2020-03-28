package ReachingDefinition;

import java.util.*;

import soot.Body;
import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.dava.internal.javaRep.DAssignStmt;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.jimple.parser.node.AReturnStatement;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Chain;

public class ReachingDefinitionAnalysis extends ForwardFlowAnalysis {
	Map <String, HashSet<AssignStmt>> map;
	Body b;
	Map <String, HashSet<AssignStmt>>  inval, outval;
	Chain<Local> locals;
	public ReachingDefinitionAnalysis(UnitGraph g)
	{
		super(g);
		b = g.getBody();
		locals = b.getLocals();
		map = new HashMap<>();
		for (Local l: locals)
		{
			map.put(l.getName(),new HashSet<AssignStmt>());
		}
		doAnalysis();
		for (Local ll : locals)
		{
			String l = ll.getName().toString();
			if (l.equals("this"))
				continue;
			System.out.print(l+": ");
			for (AssignStmt as: map.get(l))
			{
				if (as==null)
				{
					System.out.print("?, ");
				}else
				{
					System.out.print(as.getUseBoxes().get(0).getValue()+", ");
				}
			}
			System.out.println();
		}

	}
	@Override
	protected void flowThrough(Object in, Object unit, Object out) {
		inval = (Map <String, HashSet<AssignStmt>>) in;
		outval = (Map <String, HashSet<AssignStmt>>) out;
		Stmt u = (Stmt)unit;

		for (Local l : locals)
		{
			HashSet<AssignStmt> arri1 = outval.get(l.getName().toString());
			HashSet<AssignStmt> arri2 = inval.get(l.getName().toString());
			HashSet<AssignStmt> arr = new HashSet<AssignStmt>();
			for (AssignStmt as : arri1)
				arr.add(as);
			for (AssignStmt as : arri2)
				arr.add(as);
			outval.put(l.getName().toString(),arr);
		}

		if (u instanceof AssignStmt)
		{
					if (u.getDefBoxes().get(0).getValue() instanceof Local)
					{
						String left = ((Local) u.getDefBoxes().get(0).getValue()).getName().toString();
						if (inval.get(left).contains(null))
						{
							outval.get(left).remove(null);
							outval.get(left).add((AssignStmt) u);
						}
					}
		}
		if (u instanceof ReturnVoidStmt || u instanceof ReturnStmt)
		{

			for (Local l : locals)
			{
				HashSet<AssignStmt> arri1 = map.get(l.getName().toString());
				HashSet<AssignStmt> arri2 = inval.get(l.getName().toString());
				for (AssignStmt as : arri2)
					arri1.add(as);
			}
		}

	}
	@Override
	protected void copy(Object source, Object dest)
	{
		Map <String,HashSet<AssignStmt>> src =  (Map <String,HashSet<AssignStmt>>)source;
		Map <String,HashSet<AssignStmt>> dst =  (Map <String,HashSet<AssignStmt>>)dest;
		for (Local l : locals)
		{
			HashSet<AssignStmt> arr = src.get(l.getName().toString());
			HashSet<AssignStmt> arr1 = new HashSet<AssignStmt>();
			for (AssignStmt as : arr )
			{
				arr1.add(as);
			}
			dst.put(l.getName().toString(),arr1);
		}
	}
	@Override
	protected Object entryInitialFlow() {
		Map <String,HashSet<AssignStmt>> map =  new HashMap<String,HashSet<AssignStmt>>();
		for (Local l : locals)
		{
			map.put(l.getName().toString(),new HashSet<AssignStmt>());
			map.get(l.getName().toString()).add(null);
		}
		return map;
	}
	@Override
	protected void merge(Object in1, Object in2, Object out)
	{
		Map<String,HashSet<AssignStmt>> inset1 = (Map<String,HashSet<AssignStmt>>) in1;
		Map<String,HashSet<AssignStmt>> inset2 = (Map<String,HashSet<AssignStmt>>) in2;
		Map<String,HashSet<AssignStmt>> outset = (Map<String,HashSet<AssignStmt>>) out;
		for (Local l : locals)
		{
			HashSet<AssignStmt> arri1 = inset1.get(l.getName().toString());
			HashSet<AssignStmt> arri2 = inset2.get(l.getName().toString());
			HashSet<AssignStmt> arro = outset.get(l.getName().toString());
			HashSet<AssignStmt> arr = new HashSet<AssignStmt>();
			for (AssignStmt as : arri1)
				arr.add(as);
			for (AssignStmt as : arri2)
				arr.add(as);
			for (AssignStmt as : arro)
				arr.add(as);
			outset.put(l.getName().toString(),arr);
		}

	}
	@Override
	protected Object newInitialFlow()
	{
		Map <String, HashSet<AssignStmt>> map =  new HashMap<String,HashSet<AssignStmt>>();
		for (Local l : locals)
		{
			map.put(l.getName().toString(),new HashSet<AssignStmt>());
		}
		return map;
	}

}
 