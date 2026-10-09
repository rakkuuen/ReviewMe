package Controller.BackEnd.Importer;

public class ImportIssue {
    private String file, message;
    private IssueSeverity severity;

    public ImportIssue(String file, IssueSeverity severity, String message){
        this.file = file;
        this.severity = severity;
        this.message = message;
    }

    public String GetFile(){ return file; }
    public IssueSeverity GetSeverity(){ return severity; }
    public String GetMessage(){ return message; }
}
