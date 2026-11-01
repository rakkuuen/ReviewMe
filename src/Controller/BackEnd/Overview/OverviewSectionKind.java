package Controller.BackEnd.Overview;

// The short lists on the Overview, in the order they appear. Series has its own groups (see OverviewSeriesGroup)
public enum OverviewSectionKind {
    INCOMPLETE,          // 100%ed with a partial or missing review
    MORE_ACHIEVEMENTS,   // 100%ed that gained achievements since
    IN_PROGRESS,
    COMPLETED,           // 100%ed, complete review, no tag
    SHELVED,
    NOT_STARTED
}
