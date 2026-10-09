package Controller.BackEnd.Importer.Report;

import java.util.List;

import Controller.BackEnd.Importer.ImportResult;

// One block of the dry-run report. Each section returns its own lines, including its heading and a trailing blank line
public interface ReportSection {
    List<String> Lines(ImportResult result);
}
