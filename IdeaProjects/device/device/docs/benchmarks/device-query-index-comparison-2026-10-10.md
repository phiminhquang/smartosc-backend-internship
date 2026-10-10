# Device query index comparison

Generated: 2026-10-10T11:26:19.958139780+07:00[Asia/Ho_Chi_Minh]

## Environment

| Field | Value |
|---|---|
| Base commit | `3179a16148d38d99c03811462d17fe9509961024` |
| Working tree | dirty (uncommitted changes present) |
| OS | Linux 7.0.0-34-generic amd64 |
| JVM | OpenJDK 64-Bit Server VM 17.0.20.1 |
| MySQL | 8.4.11 |
| Datasource | Testcontainers `device_test` (disposable) |
| Dataset | 100000 synthetic devices from the PRD-208 generator, loaded by multi-row JDBC inserts for query-only timing |
| Candidate | `CREATE INDEX idx_devices_name ON devices (name)` |
| Order | baseline then indexed, repeated for 2 alternating cycles |
| Warm-ups | 2 per operation and variant in each cycle |
| Recorded repetitions | 3 per operation and variant in each cycle |

The B-tree candidate targets deterministic `ORDER BY name, id` for unfiltered pages. Keyword pages use the collation-equivalent `ORDER BY LOWER(name), id`, preventing MySQL from choosing an expensive ordered index scan for a leading-wildcard predicate that cannot seek into this B-tree.

## Raw measurements

| variant | cycle | operation | run | latency_ms | units |
|---|---:|---|---:|---:|---:|
| baseline | 1 | page-first | 1 | 494.142 | 100 |
| baseline | 1 | page-first | 2 | 478.042 | 100 |
| baseline | 1 | page-first | 3 | 445.930 | 100 |
| baseline | 1 | page-deep | 1 | 616.049 | 100 |
| baseline | 1 | page-deep | 2 | 600.505 | 100 |
| baseline | 1 | page-deep | 3 | 661.699 | 100 |
| baseline | 1 | keyword-tail | 1 | 460.238 | 1 |
| baseline | 1 | keyword-tail | 2 | 431.333 | 1 |
| baseline | 1 | keyword-tail | 3 | 440.137 | 1 |
| indexed | 1 | page-first | 1 | 70.592 | 100 |
| indexed | 1 | page-first | 2 | 60.602 | 100 |
| indexed | 1 | page-first | 3 | 65.975 | 100 |
| indexed | 1 | page-deep | 1 | 713.160 | 100 |
| indexed | 1 | page-deep | 2 | 585.551 | 100 |
| indexed | 1 | page-deep | 3 | 601.119 | 100 |
| indexed | 1 | keyword-tail | 1 | 395.160 | 1 |
| indexed | 1 | keyword-tail | 2 | 403.865 | 1 |
| indexed | 1 | keyword-tail | 3 | 491.580 | 1 |
| baseline | 2 | page-first | 1 | 410.088 | 100 |
| baseline | 2 | page-first | 2 | 382.656 | 100 |
| baseline | 2 | page-first | 3 | 449.110 | 100 |
| baseline | 2 | page-deep | 1 | 632.600 | 100 |
| baseline | 2 | page-deep | 2 | 591.027 | 100 |
| baseline | 2 | page-deep | 3 | 597.097 | 100 |
| baseline | 2 | keyword-tail | 1 | 447.840 | 1 |
| baseline | 2 | keyword-tail | 2 | 469.073 | 1 |
| baseline | 2 | keyword-tail | 3 | 479.202 | 1 |
| indexed | 2 | page-first | 1 | 34.758 | 100 |
| indexed | 2 | page-first | 2 | 38.831 | 100 |
| indexed | 2 | page-first | 3 | 39.755 | 100 |
| indexed | 2 | page-deep | 1 | 566.651 | 100 |
| indexed | 2 | page-deep | 2 | 648.453 | 100 |
| indexed | 2 | page-deep | 3 | 835.433 | 100 |
| indexed | 2 | keyword-tail | 1 | 476.999 | 1 |
| indexed | 2 | keyword-tail | 2 | 384.679 | 1 |
| indexed | 2 | keyword-tail | 3 | 418.372 | 1 |

## Summary

| operation | baseline_median_ms | indexed_median_ms | change_percent |
|---|---:|---:|---:|
| page-first | 447.520 | 50.178 | +88.79% |
| page-deep | 608.277 | 624.786 | -2.71% |
| keyword-tail | 454.039 | 411.118 | +9.45% |

## Index creation on populated table

| cycle | create_index_ms |
|---:|---:|
| 1 | 916.747 |
| 2 | 869.184 |

## EXPLAIN ANALYZE

### baseline cycle 1 — page-first-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
ORDER BY name, id
LIMIT 100 OFFSET 0
```

```text
-> Limit: 100 row(s)  (cost=10076 rows=100) (actual time=351..351 rows=100 loops=1)
    -> Sort: devices.`name`, devices.id, limit input to 100 row(s) per chunk  (cost=10076 rows=93310) (actual time=351..351 rows=100 loops=1)
        -> Table scan on devices  (cost=10076 rows=93310) (actual time=0.368..242 rows=100000 loops=1)

```

### baseline cycle 1 — page-deep-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
ORDER BY name, id
LIMIT 100 OFFSET 99900
```

```text
-> Limit/Offset: 100/99900 row(s)  (cost=10190 rows=0) (actual time=519..519 rows=100 loops=1)
    -> Sort: devices.`name`, devices.id, limit input to 100000 row(s) per chunk  (cost=10190 rows=93310) (actual time=489..512 rows=100000 loops=1)
        -> Table scan on devices  (cost=10190 rows=93310) (actual time=0.549..207 rows=100000 loops=1)

```

### baseline cycle 1 — page-count

```sql
SELECT COUNT(id) FROM devices
```

```text
-> Count rows in devices  (actual time=52.1..52.1 rows=1 loops=1)

```

### baseline cycle 1 — keyword-tail-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
WHERE LOWER(name) LIKE '%scale device 0100000%'
   OR LOWER(model) LIKE '%scale device 0100000%'
   OR LOWER(serial_number) LIKE '%scale device 0100000%'
ORDER BY LOWER(name), id
LIMIT 100 OFFSET 0
```

```text
-> Limit: 100 row(s)  (cost=10423 rows=100) (actual time=480..480 rows=1 loops=1)
    -> Sort: lower(devices.`name`), devices.id, limit input to 100 row(s) per chunk  (cost=10423 rows=93310) (actual time=480..480 rows=1 loops=1)
        -> Filter: ((lower(devices.`name`) like '%scale device 0100000%') or (lower(devices.model) like '%scale device 0100000%') or (lower(devices.serial_number) like '%scale device 0100000%'))  (cost=10423 rows=93310) (actual time=255..480 rows=1 loops=1)
            -> Table scan on devices  (cost=10423 rows=93310) (actual time=0.53..287 rows=100000 loops=1)

```

### baseline cycle 1 — keyword-tail-count

```sql
SELECT COUNT(id)
FROM devices
WHERE LOWER(name) LIKE '%scale device 0100000%'
   OR LOWER(model) LIKE '%scale device 0100000%'
   OR LOWER(serial_number) LIKE '%scale device 0100000%'
```

```text
-> Aggregate: count(devices.id)  (cost=19506 rows=1) (actual time=421..421 rows=1 loops=1)
    -> Filter: ((lower(devices.`name`) like '%scale device 0100000%') or (lower(devices.model) like '%scale device 0100000%') or (lower(devices.serial_number) like '%scale device 0100000%'))  (cost=10175 rows=93310) (actual time=235..421 rows=1 loops=1)
        -> Table scan on devices  (cost=10175 rows=93310) (actual time=0.287..228 rows=100000 loops=1)

```

### indexed cycle 1 — page-first-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
ORDER BY name, id
LIMIT 100 OFFSET 0
```

```text
-> Limit: 100 row(s)  (cost=0.747 rows=100) (actual time=2.77..8.94 rows=100 loops=1)
    -> Index scan on devices using idx_devices_name  (cost=0.747 rows=100) (actual time=2.73..8.88 rows=100 loops=1)

```

### indexed cycle 1 — page-deep-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
ORDER BY name, id
LIMIT 100 OFFSET 99900
```

```text
-> Limit/Offset: 100/99900 row(s)  (cost=10841 rows=100) (actual time=522..522 rows=100 loops=1)
    -> Sort: devices.`name`, devices.id, limit input to 100000 row(s) per chunk  (cost=10841 rows=100870) (actual time=490..515 rows=100000 loops=1)
        -> Table scan on devices  (cost=10841 rows=100870) (actual time=0.432..207 rows=100000 loops=1)

```

### indexed cycle 1 — page-count

```sql
SELECT COUNT(id) FROM devices
```

```text
-> Count rows in devices  (actual time=47.1..47.1 rows=1 loops=1)

```

### indexed cycle 1 — keyword-tail-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
WHERE LOWER(name) LIKE '%scale device 0100000%'
   OR LOWER(model) LIKE '%scale device 0100000%'
   OR LOWER(serial_number) LIKE '%scale device 0100000%'
ORDER BY LOWER(name), id
LIMIT 100 OFFSET 0
```

```text
-> Limit: 100 row(s)  (cost=11176 rows=100) (actual time=397..397 rows=1 loops=1)
    -> Sort: lower(devices.`name`), devices.id, limit input to 100 row(s) per chunk  (cost=11176 rows=100870) (actual time=397..397 rows=1 loops=1)
        -> Filter: ((lower(devices.`name`) like '%scale device 0100000%') or (lower(devices.model) like '%scale device 0100000%') or (lower(devices.serial_number) like '%scale device 0100000%'))  (cost=11176 rows=100870) (actual time=196..397 rows=1 loops=1)
            -> Table scan on devices  (cost=11176 rows=100870) (actual time=0.769..226 rows=100000 loops=1)

```

### indexed cycle 1 — keyword-tail-count

```sql
SELECT COUNT(id)
FROM devices
WHERE LOWER(name) LIKE '%scale device 0100000%'
   OR LOWER(model) LIKE '%scale device 0100000%'
   OR LOWER(serial_number) LIKE '%scale device 0100000%'
```

```text
-> Aggregate: count(devices.id)  (cost=20918 rows=1) (actual time=359..359 rows=1 loops=1)
    -> Filter: ((lower(devices.`name`) like '%scale device 0100000%') or (lower(devices.model) like '%scale device 0100000%') or (lower(devices.serial_number) like '%scale device 0100000%'))  (cost=10831 rows=100870) (actual time=155..359 rows=1 loops=1)
        -> Table scan on devices  (cost=10831 rows=100870) (actual time=0.757..171 rows=100000 loops=1)

```

### baseline cycle 2 — page-first-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
ORDER BY name, id
LIMIT 100 OFFSET 0
```

```text
-> Limit: 100 row(s)  (cost=11214 rows=100) (actual time=303..303 rows=100 loops=1)
    -> Sort: devices.`name`, devices.id, limit input to 100 row(s) per chunk  (cost=11214 rows=103180) (actual time=303..303 rows=100 loops=1)
        -> Table scan on devices  (cost=11214 rows=103180) (actual time=0.264..205 rows=100000 loops=1)

```

### baseline cycle 2 — page-deep-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
ORDER BY name, id
LIMIT 100 OFFSET 99900
```

```text
-> Limit/Offset: 100/99900 row(s)  (cost=11064 rows=100) (actual time=558..558 rows=100 loops=1)
    -> Sort: devices.`name`, devices.id, limit input to 100000 row(s) per chunk  (cost=11064 rows=103180) (actual time=524..550 rows=100000 loops=1)
        -> Table scan on devices  (cost=11064 rows=103180) (actual time=0.437..223 rows=100000 loops=1)

```

### baseline cycle 2 — page-count

```sql
SELECT COUNT(id) FROM devices
```

```text
-> Count rows in devices  (actual time=40.7..40.7 rows=1 loops=1)

```

### baseline cycle 2 — keyword-tail-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
WHERE LOWER(name) LIKE '%scale device 0100000%'
   OR LOWER(model) LIKE '%scale device 0100000%'
   OR LOWER(serial_number) LIKE '%scale device 0100000%'
ORDER BY LOWER(name), id
LIMIT 100 OFFSET 0
```

```text
-> Limit: 100 row(s)  (cost=11735 rows=100) (actual time=436..436 rows=1 loops=1)
    -> Sort: lower(devices.`name`), devices.id, limit input to 100 row(s) per chunk  (cost=11735 rows=103180) (actual time=436..436 rows=1 loops=1)
        -> Filter: ((lower(devices.`name`) like '%scale device 0100000%') or (lower(devices.model) like '%scale device 0100000%') or (lower(devices.serial_number) like '%scale device 0100000%'))  (cost=11735 rows=103180) (actual time=177..436 rows=1 loops=1)
            -> Table scan on devices  (cost=11735 rows=103180) (actual time=0.669..268 rows=100000 loops=1)

```

### baseline cycle 2 — keyword-tail-count

```sql
SELECT COUNT(id)
FROM devices
WHERE LOWER(name) LIKE '%scale device 0100000%'
   OR LOWER(model) LIKE '%scale device 0100000%'
   OR LOWER(serial_number) LIKE '%scale device 0100000%'
```

```text
-> Aggregate: count(devices.id)  (cost=21696 rows=1) (actual time=469..469 rows=1 loops=1)
    -> Filter: ((lower(devices.`name`) like '%scale device 0100000%') or (lower(devices.model) like '%scale device 0100000%') or (lower(devices.serial_number) like '%scale device 0100000%'))  (cost=11378 rows=103180) (actual time=203..469 rows=1 loops=1)
        -> Table scan on devices  (cost=11378 rows=103180) (actual time=0.535..260 rows=100000 loops=1)

```

### indexed cycle 2 — page-first-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
ORDER BY name, id
LIMIT 100 OFFSET 0
```

```text
-> Limit: 100 row(s)  (cost=0.751 rows=100) (actual time=2.45..7.28 rows=100 loops=1)
    -> Index scan on devices using idx_devices_name  (cost=0.751 rows=100) (actual time=2.44..7.26 rows=100 loops=1)

```

### indexed cycle 2 — page-deep-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
ORDER BY name, id
LIMIT 100 OFFSET 99900
```

```text
-> Limit/Offset: 100/99900 row(s)  (cost=10814 rows=100) (actual time=532..532 rows=100 loops=1)
    -> Sort: devices.`name`, devices.id, limit input to 100000 row(s) per chunk  (cost=10814 rows=100590) (actual time=499..524 rows=100000 loops=1)
        -> Table scan on devices  (cost=10814 rows=100590) (actual time=0.317..246 rows=100000 loops=1)

```

### indexed cycle 2 — page-count

```sql
SELECT COUNT(id) FROM devices
```

```text
-> Count rows in devices  (actual time=45.3..45.3 rows=1 loops=1)

```

### indexed cycle 2 — keyword-tail-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
WHERE LOWER(name) LIKE '%scale device 0100000%'
   OR LOWER(model) LIKE '%scale device 0100000%'
   OR LOWER(serial_number) LIKE '%scale device 0100000%'
ORDER BY LOWER(name), id
LIMIT 100 OFFSET 0
```

```text
-> Limit: 100 row(s)  (cost=11151 rows=100) (actual time=387..387 rows=1 loops=1)
    -> Sort: lower(devices.`name`), devices.id, limit input to 100 row(s) per chunk  (cost=11151 rows=100590) (actual time=387..387 rows=1 loops=1)
        -> Filter: ((lower(devices.`name`) like '%scale device 0100000%') or (lower(devices.model) like '%scale device 0100000%') or (lower(devices.serial_number) like '%scale device 0100000%'))  (cost=11151 rows=100590) (actual time=161..387 rows=1 loops=1)
            -> Table scan on devices  (cost=11151 rows=100590) (actual time=0.327..198 rows=100000 loops=1)

```

### indexed cycle 2 — keyword-tail-count

```sql
SELECT COUNT(id)
FROM devices
WHERE LOWER(name) LIKE '%scale device 0100000%'
   OR LOWER(model) LIKE '%scale device 0100000%'
   OR LOWER(serial_number) LIKE '%scale device 0100000%'
```

```text
-> Aggregate: count(devices.id)  (cost=20864 rows=1) (actual time=397..397 rows=1 loops=1)
    -> Filter: ((lower(devices.`name`) like '%scale device 0100000%') or (lower(devices.model) like '%scale device 0100000%') or (lower(devices.serial_number) like '%scale device 0100000%'))  (cost=10805 rows=100590) (actual time=228..397 rows=1 loops=1)
        -> Table scan on devices  (cost=10805 rows=100590) (actual time=0.474..224 rows=100000 loops=1)

```

## Interpretation limits

- Results are local measurements, not a production capacity promise.
- Both variants use the same process, dataset and MySQL container; alternating cycles reduce but do not eliminate cache/order effects.
- The comparison measures application `Page` calls, so each latency includes both data and count queries.
- A normal B-tree cannot seek into a case-folded leading-wildcard search; that operation remains a table scan.
