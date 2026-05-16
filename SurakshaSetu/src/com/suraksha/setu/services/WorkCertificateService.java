package com.suraksha.setu.services;

import com.suraksha.setu.dao.WorkHistoryDAO;
import com.suraksha.setu.dao.TrustScoreDAO;
import com.suraksha.setu.models.Worker;
import com.suraksha.setu.models.WorkHistory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates a text-based work certificate using StringBuffer (Unit II).
 * Displays in a JTextArea — no external PDF library required.
 */
public class WorkCertificateService {

    private final WorkHistoryDAO workHistoryDAO = new WorkHistoryDAO();
    private final TrustScoreDAO  trustScoreDAO  = new TrustScoreDAO();

    private static final String BORDER = "─".repeat(50);
    private static final String DOUBLE = "═".repeat(50);

    /**
     * Generate formatted certificate text for a worker.
     * Uses StringBuffer for all string construction (Unit II requirement).
     */
    public String generateCertificate(Worker worker) throws SQLException {
        List<WorkHistory> logs = workHistoryDAO.findByWorkerId(worker.getWorkerId());
        double avgRating       = trustScoreDAO.getAvgRating(worker.getWorkerId());

        // Compute totals using Stream + Lambda (Unit II)
        double totalEarnings = logs.stream()
                .mapToDouble(WorkHistory::getEarnings)
                .sum();
        double totalHours    = logs.stream()
                .mapToDouble(WorkHistory::getHoursLogged)
                .sum();
        long   totalGigs     = logs.size();

        // Build certificate text using StringBuffer
        StringBuffer sb = new StringBuffer();
        sb.append(DOUBLE).append("\n");
        sb.append(center("SURAKSHA SETU", 50)).append("\n");
        sb.append(center("DIGITAL WORK CERTIFICATE", 50)).append("\n");
        sb.append(center("Gig Economy Identity Trust Platform", 50)).append("\n");
        sb.append(DOUBLE).append("\n\n");

        sb.append("  Worker Name   : ").append(worker.getFullName()).append("\n");
        sb.append("  Digital ID    : ").append(worker.getDigitalWorkId()).append("\n");
        sb.append("  Phone         : ").append(worker.getPhone()).append("\n");
        sb.append("  Trust Score   : ").append(String.format("%.1f / 1000", worker.getCurrentTrustScore())).append("\n");
        sb.append("\n").append(BORDER).append("\n");
        sb.append("  WORK SUMMARY\n");
        sb.append(BORDER).append("\n");
        sb.append("  Total Gigs    : ").append(totalGigs).append("\n");
        sb.append(String.format("  Total Hours   : %.1f hrs%n", totalHours));
        sb.append(String.format("  Total Earnings: \u20B9%.2f%n", totalEarnings));
        sb.append(String.format("  Avg Rating    : %.2f / 5.0%n", avgRating));
        sb.append("\n").append(BORDER).append("\n");
        sb.append("  Certified on  : ").append(LocalDate.now()).append("\n");
        sb.append("  Issued by     : SurakshaSetu Platform\n");
        sb.append("\n").append(DOUBLE).append("\n");
        sb.append("  This certificate is digitally generated and\n");
        sb.append("  verifiable via the SurakshaSetu system.\n");
        sb.append(DOUBLE).append("\n");

        return sb.toString();
    }

    private String center(String text, int width) {
        int padding = (width - text.length()) / 2;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < padding; i++) sb.append(' ');
        sb.append(text);
        return sb.toString();
    }
}
