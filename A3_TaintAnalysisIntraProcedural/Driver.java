package TaintAnalysis;

import soot.Pack;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootResolver;
import soot.Transform;
import soot.options.Options;

public class Driver {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        if(args.length==0)
        {
            System.err.println("Usage: java Driver [options] classname");
            System.exit(0);
        }
        //SootClass abc=Scene.v().loadClassAndSupport("ReachingDefinition.Tester");
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().set_keep_line_number(true);
        Pack jtp=PackManager.v().getPack("jtp");
        jtp.add(new Transform("jtp.instrumenter", new TaintAnalysisWrapper()));
        SootResolver.v().resolveClass("java.lang.CloneNotSupportedException", SootClass.SIGNATURES);
        Options.v().set_output_format(Options.output_format_jimple);
        soot.Main.main(args);
    }
}
