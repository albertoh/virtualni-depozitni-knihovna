package cz.incad.vdkcr.server.datasources.oai;

/**
 *
 * @author Alberto
 */
public class ProgramArguments {

    public String configFile = "";
    public String log4jFile = "";
    public String from;
    public String to;
    public Boolean fullIndex = false;
    public Boolean updateKey = false;
    public int maxDocuments = 0;
    public String docId;
    public Boolean fromDisk = false;
    public Boolean onlyHarvest = false;
    public Boolean saveToDisk = false;
    public Boolean onlyIdentifiers = false;
    public Boolean dontIndex = false;
    public String resumptionToken = "";
    public String metadataPrefix = "";
    public String pathToData = "";
    public int startIndex = -1;
    public boolean continueOnDocError = false;

    public ProgramArguments() {
    }

    public Boolean parse(String[] args) {
        try {
            int total = args.length;
            int i = 0;
            while (i < total) {
                if (args[i].equalsIgnoreCase("-fullindex")) {
                    fullIndex = true;
                } else if (args[i].equalsIgnoreCase("-cfgFile")) {
                    i++;
                    configFile = args[i];
                } else if (args[i].equalsIgnoreCase("-log4jFile")) {
                    i++;
                    log4jFile = args[i];
                } else if (args[i].equalsIgnoreCase("-updateKey")) {
                    updateKey = true;
                } else if (args[i].equalsIgnoreCase("-fromDisk")) {
                    fromDisk = true;
                } else if (args[i].equalsIgnoreCase("-pathToData")) {
                    i++;
                    pathToData = args[i];
                } else if (args[i].equalsIgnoreCase("-continueOnDocError")) {
                    continueOnDocError = true;
                } else if (args[i].equalsIgnoreCase("-onlyHarvest")) {
                    onlyHarvest = true;
                    saveToDisk = true;
                    dontIndex = true;
                } else if (args[i].equalsIgnoreCase("-onlyIdentifiers")) {
                    onlyIdentifiers = true;
                } else if (args[i].equalsIgnoreCase("-saveToDisk")) {
                    saveToDisk = true;
                } else if (args[i].equalsIgnoreCase("-dontIndex")) {
                    dontIndex = true;
                } else if (args[i].equalsIgnoreCase("-docId")) {
                    i++;
                    docId = args[i];
                } else if (args[i].equalsIgnoreCase("-resumptionToken")) {
                    i++;
                    resumptionToken = args[i];
                } else if (args[i].equalsIgnoreCase("-maxDocuments")) {
                    i++;
                    maxDocuments = Integer.parseInt(args[i]);
                } else if (args[i].equalsIgnoreCase("-startIndex")) {
                    i++;
                    startIndex = Integer.parseInt(args[i]);
                } else if (args[i].equalsIgnoreCase("-metadataPrefix")) {
                    i++;
                    metadataPrefix = args[i];
                } else if (args[i].equalsIgnoreCase("-from")) {
                    i++;
                    from = args[i];
                } else if (args[i].equalsIgnoreCase("-to")) {
                    i++;
                    to = args[i];
                }

                i++;
            }
            if (configFile.equals("")) {
                return false;
            } else {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
