package Controller.BackEnd.Importer.Report;

import Model.Reviews.FieldValue;

// Small text helpers the report sections share
public class ReportFormat {

    // "9", "?", "-" for missing or absent, and a + when there's a comment (replay only)
    public static String Rating(FieldValue value, boolean showComment){
        if(value == null || value.IsMissing()){
            return "-";
        }
        if(value.IsNotApplicable()){
            return "n/a";
        }
        return value.GetText() + (showComment && value.GetComment() != null ? " +" : "");
    }

    public static String Shorten(String text, int max){
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}
