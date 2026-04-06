package vn.truongngo.apartcom.one.service.oauth2.application.iam_dashboard.overview;

/**
 * Read-side port — aggregate KPI counts cho IAM Overview Dashboard.
 * Bypass domain layer (CQRS read side).
 */
public interface IamOverviewQueryPort {
    IamOverviewData query();
}
