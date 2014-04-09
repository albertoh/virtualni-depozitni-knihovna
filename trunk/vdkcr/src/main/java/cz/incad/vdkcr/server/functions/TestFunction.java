package cz.incad.vdkcr.server.functions;

import java.util.Arrays;
import java.util.List;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.WizardPage;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

import java.util.logging.Logger;
import org.aplikator.client.shared.data.Record;

public class TestFunction extends Executable {


    @Override
    public WizardPage getWizardPage(String currentPage, boolean forwardFlag, Record currentProcessingRecord, Record clientParameters, Context context) {
        List<String> pages = Arrays.asList("first", "second", "third");
        String nextPage = currentPage.trim().equals("") ? "first" : pages.get(pages.indexOf(currentPage) + 1);

        boolean execFlag = false;
        boolean nextFlag = true;
        boolean prevFlag = true;

        if (forwardFlag) {
            if (nextPage.equals("third")) {
                execFlag = true;
                nextFlag = false;
            }
        } else {
            if (nextPage.equals("first")) {
                prevFlag = false;
            }
        }

        WizardPage p = this.function.getRegistredView(nextPage);
        p.setHasExecute(execFlag);
        p.setHasNext(nextFlag);
        p.setHasPrevious(prevFlag);
        return p;
    }

    @Override
    public FunctionResult execute(FunctionParameters parameters, Context context) {
//        context.getHttpServletRequest().getServletPath();
//
//        OSProcessConfiguration conf = processConf().classpathlib(System.getProperty("user.dir") + File.separator + "libs");
//        org.aplikator.server.processes.Process process = ProcessFactory.get(ProcessType.PROCESS).create(conf, new RunnableSerializationAware() {
//            @Override
//            public void run() {
//                System.out.println("... testik ... ");
//            }
//        });


        Record clientRecord = parameters.getClientParameters();
        Record procsRecord = parameters.getClientContext().getCurrentRecord();

        StringBuilder builder = new StringBuilder();
        builder.append("record:").append(clientRecord);
        builder.append("processingRecord:").append(procsRecord);
        return new FunctionResult(builder.toString(), true);
    }
}
