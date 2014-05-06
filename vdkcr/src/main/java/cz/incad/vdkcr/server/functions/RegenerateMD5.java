
package cz.incad.vdkcr.server.functions;

import java.util.logging.Logger;
import org.aplikator.server.Context;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

/**
 *
 * @author alberto
 */
public class RegenerateMD5 extends Executable {
    
    static final Logger log = Logger.getLogger(RegenerateMD5.class.getName());

    @Override
    public FunctionResult execute(FunctionParameters parameters, Context context) {

        try {
            RegenerateMD5Process ri = new RegenerateMD5Process();
            ri.runHarvestAsProcess(null, null, context);
            return new FunctionResult("Proces bezi ... ", true);
        } catch (Throwable t) {
            return new FunctionResult("Index db selhala: " + t, false);
        }
    }
    
}
