# NOTES

## Summary of changes

I reviewed the React frontend, Spring Boot backend, H2 queries, and Oracle SQL reference and focused on correctness, reliability, and performance.

- Fixed SQL AND/OR precedence in the repository query, H2 SQL reference, and Oracle package so archived rows cannot leak and status filtering is consistently applied.
- Removed artificial Thread.sleep() latency and replaced System.out logging with SLF4J.
- Added HTTP 400 validation for invalid status, page, and pageSize values.
- Moved pagination to the database using COUNT and LIMIT/OFFSET instead of loading all matching rows into memory.
- Added 300ms search debounce and AbortController cancellation to prevent unnecessary requests and stale results.
- Fixed loading/error handling and reset pagination to page 1 when search or status filters change.
- Improved API request parameter handling and pinned Vite to a compatible Vite 5 version.

## What I chose not to change

I did not rewrite the architecture or add authentication, RBAC, sorting, or full-text search because these were outside the timebox and lower priority than the correctness and reliability issues found.

## Biggest remaining risk

Search still uses a leading-wildcard LIKE query, which can become expensive as the dataset grows and may require full-text search or database-specific indexing.

## Tools / AI used

I used AI assistance to inspect the frontend, backend, and SQL for likely bugs and implementation options. I reviewed the suggestions against the original code and seed data, made the changes, and verified the application and API behavior locally.

## Verification evidence

Supporting technical screenshots are available in the `evidence/`
directory. They show the SQL correction, API validation responses,
database-side pagination, and frontend filtering/pagination behavior.