package cz.incad.vdkcr.server.functions;

import java.util.logging.Logger;

import org.aplikator.server.Context;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

public class TestFunction implements Executable {

    Logger log = Logger.getLogger(TestFunction.class.getName());

    @Override
    public FunctionResult execute(FunctionParameters functionParameters, Context context) {
        //Record zdroj = functionParameters.getClientContext().getCurrentRecord();
        try {
            return new FunctionResult("Test doběhl", true);
        } catch (Throwable t) {

            return new FunctionResult("Test selhal: " + t, false);
        }
    }

}
