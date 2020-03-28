package TaintAnalysis;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class TaintAnalysisWrapper extends BodyTransformer{

    @Override
    protected void internalTransform(Body body, String phase, Map options) {
        // TODO Auto-generated method stub
        SootMethod sootMethod = body.getMethod();

        if(!sootMethod.getName().equals("<init>")) {
            System.out.println("Analysing method " + sootMethod.getName());
            UnitGraph g = new BriefUnitGraph(sootMethod.getActiveBody());
            TaintAnalysis reach = new TaintAnalysis(g);
        }
    }
}