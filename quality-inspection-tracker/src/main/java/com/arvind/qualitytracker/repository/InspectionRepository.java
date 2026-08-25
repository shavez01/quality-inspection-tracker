package com.arvind.qualitytracker.repository;

import com.arvind.qualitytracker.model.DefectType;
import com.arvind.qualitytracker.model.Inspection;
import com.arvind.qualitytracker.model.InspectionStatus;
import com.arvind.qualitytracker.model.Severity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles database operations for inspection records.
 */
@Repository
public class InspectionRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Inspection> ROW_MAPPER = InspectionRepository::mapRow;

    public InspectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Inspection save(LocalDate inspectionDate, String machineLineId,
                           DefectType defectType, Severity severity, String remarks) {
        jdbcTemplate.update("""
                INSERT INTO inspections (
                    inspection_date, machine_line_id, defect_type,
                    severity, remarks, status, created_at
                )
                VALUES (?, ?, ?, ?, ?, 'OPEN', CURRENT_TIMESTAMP)
                """,
                inspectionDate.toString(),
                machineLineId,
                defectType.name(),
                severity.name(),
                remarks
        );

        Long id = jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
        return findById(id).orElseThrow();
    }

    public Optional<Inspection> findById(Long id) {
        return jdbcTemplate.query(
                "SELECT * FROM inspections WHERE id = ?",
                ROW_MAPPER,
                id
        ).stream().findFirst();
    }

    public List<Inspection> findAll(Severity severity, InspectionStatus status,
                                    LocalDate from, LocalDate to,
                                    String sortBy, String sortDirection) {
        StringBuilder sql = new StringBuilder("SELECT * FROM inspections WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        if (severity != null) {
            sql.append(" AND severity = ?");
            params.add(severity.name());
        }

        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }

        if (from != null) {
            sql.append(" AND inspection_date >= ?");
            params.add(from.toString());
        }

        if (to != null) {
            sql.append(" AND inspection_date <= ?");
            params.add(to.toString());
        }

        sql.append(" ORDER BY ")
                .append(toSafeSortColumn(sortBy))
                .append(" ")
                .append("asc".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC");

        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, params.toArray());
    }

    public int resolve(Long id, String resolutionNote) {
        return jdbcTemplate.update("""
                UPDATE inspections
                SET status = 'RESOLVED',
                    resolution_note = ?,
                    resolved_at = CURRENT_TIMESTAMP
                WHERE id = ? AND status = 'OPEN'
                """, resolutionNote, id);
    }

    public long countBy(Severity severity, InspectionStatus status) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM inspections
                WHERE severity = ? AND status = ?
                """, Long.class, severity.name(), status.name());
    }

    private static Inspection mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Inspection(
                rs.getLong("id"),
                LocalDate.parse(rs.getString("inspection_date")),
                rs.getString("machine_line_id"),
                DefectType.valueOf(rs.getString("defect_type")),
                Severity.valueOf(rs.getString("severity")),
                rs.getString("remarks"),
                InspectionStatus.valueOf(rs.getString("status")),
                rs.getString("resolution_note"),
                parseDateTime(rs.getString("created_at")),
                parseDateTime(rs.getString("resolved_at"))
        );
    }

    private static LocalDateTime parseDateTime(String value) {
        return value == null ? null : LocalDateTime.parse(value.replace(" ", "T"));
    }

    private String toSafeSortColumn(String sortBy) {
        return switch (sortBy == null ? "inspectionDate" : sortBy) {
            case "id" -> "id";
            case "machineLineId" -> "machine_line_id";
            case "severity" -> "severity";
            case "status" -> "status";
            case "createdAt" -> "created_at";
            default -> "inspection_date";
        };
    }
}
