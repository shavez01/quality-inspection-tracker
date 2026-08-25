CREATE TABLE IF NOT EXISTS inspections (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    inspection_date TEXT NOT NULL,
    machine_line_id TEXT NOT NULL,
    defect_type TEXT NOT NULL CHECK (
        defect_type IN ('WEAVE_DEFECT', 'SHADE_VARIATION', 'HOLE_TEAR', 'COUNT_DEVIATION', 'OTHER')
    ),
    severity TEXT NOT NULL CHECK (
        severity IN ('CRITICAL', 'MAJOR', 'MINOR')
    ),
    remarks TEXT,
    status TEXT NOT NULL DEFAULT 'OPEN' CHECK (
        status IN ('OPEN', 'RESOLVED')
    ),
    resolution_note TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TEXT
);

CREATE INDEX IF NOT EXISTS idx_inspections_date ON inspections (inspection_date);
CREATE INDEX IF NOT EXISTS idx_inspections_status ON inspections (status);
CREATE INDEX IF NOT EXISTS idx_inspections_severity ON inspections (severity);
