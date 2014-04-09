/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.vdkcr.server.fast;

/**
 *
 * @author Administrator
 */


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.fastsearch.esp.content.IContentCallback;
import com.fastsearch.esp.content.IStatus;
import com.fastsearch.esp.content.errors.DocumentError;
import com.fastsearch.esp.content.errors.DocumentWarning;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BatchState is a utility class used to track the state of a batch that has
 * been submitted to FAST ESP
 */

class BatchState {

    /**
     * Constructor
     * @param s Stage of batch
     * @param subsystem Current subsystem
     * @param successful Is batch successful
     */
    public BatchState(Stage s, String subsystem, boolean successful) {

        this.stage = s;
        this.subsystem = subsystem;
        this.successful = successful;
    }

    /// Methods for setting/getting stage/subsystem/successfuř
    ///
    public Stage GetStage() {
        return stage;
    }

    public void SetStage(Stage s) {
        stage = s;
    }

    public String GetSubsystem() {
        return subsystem;
    }

    public void SetSubsystem(String subsystem) {
        this.subsystem = subsystem;
    }

    public void SetSuccessful(boolean val) {
        successful = val;
    }

    public boolean IsSuccessful() {
        return successful;
    }

    /// Member variables
    private Stage stage;

    private String subsystem;

    private boolean successful; //is processing of batch completed

}

/**
 * The Callback class implements the ESP Content API callback interface
 *
 */

public class Callback implements IContentCallback {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public Callback(boolean liveCallbackEnabled) {
        this.liveCallbackEnabled = liveCallbackEnabled;
        this.cbMap = new HashMap<String, BatchState>();
    }

    /**
     * Callback given when a subsystem has completed processing the content of a
     * batch
     * @param batchId id of batch
     * @param subsystem Subsystem batch has been secured in
     * @param successful True if all documents completed successfully
     * @param status Contains errors and warnings that occured during the
     *            completed stage
     */
    @Override
    public synchronized void completed(String batchId, String subsystem,
            boolean successful, IStatus status) {
        logger.log(Level.INFO, "Completed by {0}:{1} {2}", new Object[]{subsystem, batchId, successful ? "SUCCESS" : "FAILURE"});

        printErrorMessages(status.getDocumentErrors());
        printWarningMessages(status.getDocumentWarnings());
        setState(batchId, Stage.COMPLETED, subsystem, successful);

    }

    /**
     * Callback given when a subsystem has persisted the content of a batch
     * @param batchId id of batch
     * @param subsystem Subsystem batch has been secured in
     * @param successful True if all documents secured successfully
     * @param status Contains errors and warnings that occured during the
     *            secured stage
     */
    @Override
    public synchronized void secured(String batchId, String subsystem,
            boolean successful, IStatus status) {
        logger.log(Level.INFO, "Secured by {0}:{1} {2}", new Object[]{subsystem, batchId, successful ? "SUCCESS" : "FAILURE"});

        printErrorMessages(status.getDocumentErrors());
        printWarningMessages(status.getDocumentWarnings());
        setState(batchId, Stage.SECURED, subsystem, successful);
    }

    /**
     */

    /**
     * Callback given when dispatching of a batch to the next subsystem has
     * failed
     * @param batchId id of batch
     * @param subsystem Subsystem batch has been lost in
     * @param status Contains errors and warnings for the lost documents
     */
    @Override
    public synchronized void lost(String batchId, String subsystem,
            IStatus status) {

        logger.log(Level.SEVERE, "Lost in {0}", subsystem);

        printErrorMessages(status.getDocumentErrors());
        printWarningMessages(status.getDocumentWarnings());
        setState(batchId, Stage.LOST, subsystem, false);

    }

    /**
     * is given batch persisted by the indexing engine?
     * @param batchId id of batch
     * @return true if batch is completed by indexing engine
     */
    public boolean isSecured(String batchId) {

        BatchState batchState = (BatchState) cbMap.get(batchId);

        if (batchState != null) {
            return (batchState.GetStage() == Stage.SECURED
                    && batchState.GetSubsystem().equals("indexing") || batchState
                    .GetStage() == Stage.COMPLETED
                    && batchState.GetSubsystem().equals("indexing"));
        }
        return false;
    }

    /**
     * Is given batch completed by the indexing engine?
     * @param batchId id of batch
     * @return true if batch is completed by indexing engine
     */
    public boolean isCompleted(String batchId) {
        BatchState batchState = (BatchState) cbMap.get(batchId);

        if (batchState != null) {
            if (batchState.IsSuccessful() == false) {
                return true;
            } else if (batchState.GetStage() == Stage.COMPLETED
                    && batchState.GetSubsystem().equals("indexing")
                    && liveCallbackEnabled == true) {
                //we have gotten live callback from indexing
                return true;
            } else if (batchState.GetStage() == Stage.SECURED
                    && batchState.GetSubsystem().equals("indexing")
                    && liveCallbackEnabled == false) {
                //live callack not enabled. secured from indexing is the last
                // callback
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    //

    /**
     * was processing of given batch successful?
     * @param batchID id of batch
     * @return true if batch completed successfully
     */
    public boolean wasBatchSuccessful(String batchId) {

        BatchState batchState = (BatchState) cbMap.get(batchId);

        if (batchState == null) {
            return false;
        }
        return batchState.IsSuccessful();
    }

    /**
     * Set state of given batch
     * @param batchId id of batch
     * @param s Stage batch has completed
     * @param subsystem Subsystem stage applies to
     * @param successful Is batch successful
     */
    public void setState(String batchId, Stage s, String subsystem,
            boolean successful) {

        BatchState batchState = (BatchState) cbMap.get(batchId);
        if (batchState == null) {
            batchState = new BatchState(s, subsystem, successful);
            cbMap.put(batchId, batchState);
        } else {
            if (batchState.GetSubsystem().equals("indexing")
                    && subsystem.equals("processing")) {
                return;
            }

            batchState.SetStage(s);
            batchState.SetSubsystem(subsystem);
            if (batchState.IsSuccessful()) {
                //only overwrite if succesful is true
                batchState.SetSuccessful(successful);
            }
        }
    }

    /**
     * Print error messages received in callback
     * @param errors Collection of DocumentError objects
     */
    public void printErrorMessages(@SuppressWarnings("rawtypes") Collection errors) {

        if (errors.isEmpty()) {
            return;
        }

        logger.log(Level.SEVERE, "Errors: {0}", errors.size());
        @SuppressWarnings("rawtypes")
        Iterator it = errors.iterator();
        while (it.hasNext()) {
            logger.log(Level.SEVERE, " {0}", ((DocumentError) it.next()).toString());
        }
    }

    /**
     * Print warning messages received in callback
     * @param warnings Collection of DocumentWarning objects
     */
    public void printWarningMessages(@SuppressWarnings("rawtypes") Collection warnings) {
        if (warnings.isEmpty()) {
            return;
        }

        logger.log(Level.WARNING, "Warnings: {0}", warnings.size());
        @SuppressWarnings("rawtypes")
        Iterator it = warnings.iterator();
        while (it.hasNext()) {
            logger.log(Level.WARNING, " {0}", ((DocumentWarning) it.next()).toString());
        }
    }

    //does the indexing engine give completed (live) callbacks?
    private boolean liveCallbackEnabled;

    //map containing the state of the submitted batches
    private HashMap<String, BatchState> cbMap;

}
