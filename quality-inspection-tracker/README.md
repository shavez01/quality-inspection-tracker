# Quality Inspection Tracker

A small full-stack web application for shop-floor supervisors to record and manage fabric quality inspections.

The original paper-based process is replaced with a simple mobile-friendly workflow: create an inspection, review inspections, filter/sort them, and resolve an open issue with a mandatory resolution note.

**Author:** Shavez Mohammad

## Tech stack

- Java 21
- Spring Boot
- Spring JDBC
- SQLite
- HTML, CSS and vanilla JavaScript
- Maven
- REST API

I kept the frontend deliberately simple. The assignment allows plain JavaScript, and this application does not need a large frontend framework to support the required workflow.

## Features

- Create a new inspection
- Inspection date
- Machine / line ID
- Defect type
- Severity
- Optional remarks
- View all inspections
- Filter by severity, status and date range
- Sort inspection records
- Resolve an open inspection
- Mandatory resolution note
- Summary of Open and Resolved inspections by severity
- Mobile-first UI for a 390px phone width

The assignment also lists offline support, a mock SAP webhook and basic authentication as optional bonus features. They are intentionally not part of this version; I chose to make the required workflow reliable and easy to review within the 72-hour limit.

## Run locally

### Prerequisites

- Java 21
- Maven 3.9+

No separate database server is required. The application creates the `data/` directory automatically and SQLite is stored in `data/quality-inspection.db`.

### Start

```bash
mvn spring-boot:run
```

On Windows, the same command works from a Maven-enabled terminal. If you add Maven Wrapper files to your repository, `mvnw.cmd spring-boot:run` can be used instead.

Open:

```text
http://localhost:8080
```

### Tests

```bash
mvn test
```

## REST API

### Create inspection

`POST /api/inspections`

```json
{
  "inspectionDate": "2026-08-25",
  "machineLineId": "LINE-04",
  "defectType": "WEAVE_DEFECT",
  "severity": "MAJOR",
  "remarks": "Uneven weave noticed near the left edge."
}
```

Returns `201 Created`.

### List inspections

`GET /api/inspections`

Optional query parameters:

```text
severity=CRITICAL
status=OPEN
from=2026-08-01
to=2026-08-25
sortBy=inspectionDate
sortDir=desc
```

### Resolve inspection

`PATCH /api/inspections/{id}/resolve`

```json
{
  "resolutionNote": "Machine tension was adjusted and the fabric was checked again."
}
```

A blank resolution note is rejected by the backend.

### Summary

`GET /api/inspections/summary`

Example:

```json
{
  "critical": { "open": 2, "resolved": 1 },
  "major": { "open": 4, "resolved": 3 },
  "minor": { "open": 5, "resolved": 8 }
}
```

## Architecture decisions

### Spring JDBC instead of an ORM

The data model is small and the required queries are straightforward. Spring JDBC keeps the repository layer explicit and avoids adding ORM complexity that is not useful for this assignment.

### SQLite

SQLite satisfies the assignment and keeps local setup simple. A reviewer can clone the repository and run the application without installing or configuring a separate database server.

### Vanilla JavaScript

The assignment allows plain JavaScript. The UI only needs forms, filters, cards and REST calls, so a framework would add setup and dependencies without providing much value for this small application.

### Simple layered backend

The controller handles HTTP concerns, the service owns business rules, and the repository handles SQL. This gives the project enough separation to remain easy to maintain without over-engineering it.

## Notes

The implementation intentionally focuses on the required workflow. The goal is a small application that is easy to run, easy to understand and reliable enough to demonstrate the complete full-stack flow.

## Important

Make sure port 8080 is not already in use. If another application is running on port 8080, close it before starting this application. Then open http://localhost:8080 in your browser.