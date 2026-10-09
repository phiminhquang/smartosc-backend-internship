# Device data-scale baseline

Generated: 2026-10-09T20:48:44.329607410+07:00[Asia/Ho_Chi_Minh]

## Environment

| Field | Value |
|---|---|
| Commit | `9b9e4fdfb1fd5a76d43178e9b541d3e1555931da` |
| OS | Linux 7.0.0-34-generic amd64 |
| CPU | 13th Gen Intel(R) Core(TM) i5-1345U |
| Available processors | 12 |
| Physical RAM | 15633.9 MiB |
| JVM | OpenJDK 64-Bit Server VM 17.0.20.1 |
| JVM max heap | 512.0 MiB |
| MySQL | 8.4.11 |
| Datasource | Testcontainers `device_test` (disposable) |
| Datasets | [1000, 10000, 100000] rows |
| Warm-ups | 1 per read/export; import infrastructure warmed with smallest dataset |
| Recorded repetitions | 3 per read/export; 1 per import and dataset |
| Memory sampling | JVM heap + Linux `/proc/self/status` VmRSS every 10 ms |

`units` is imported rows, page result count, or exported bytes depending on operation. Memory deltas are relative to the post-GC baseline immediately before each recorded run.

## Raw measurements

| rows | operation | run | latency_ms | heap_base_mib | heap_peak_mib | heap_delta_mib | rss_base_mib | rss_peak_mib | rss_delta_mib | units |
|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 1000 | import-csv | 1 | 3751.707 | 53.346 | 102.346 | 49.000 | 477.809 | 480.125 | 2.316 | 1000 |
| 1000 | page-first | 1 | 32.402 | 53.436 | 53.436 | 0.000 | 481.363 | 481.430 | 0.066 | 100 |
| 1000 | page-first | 2 | 32.332 | 53.434 | 53.434 | 0.000 | 482.141 | 482.227 | 0.086 | 100 |
| 1000 | page-first | 3 | 32.919 | 53.434 | 53.434 | 0.000 | 482.691 | 482.773 | 0.082 | 100 |
| 1000 | page-deep | 1 | 32.763 | 53.436 | 53.436 | 0.000 | 482.754 | 482.887 | 0.133 | 100 |
| 1000 | page-deep | 2 | 33.270 | 53.437 | 53.437 | 0.000 | 483.141 | 483.211 | 0.070 | 100 |
| 1000 | page-deep | 3 | 32.612 | 53.437 | 53.437 | 0.000 | 483.535 | 483.613 | 0.078 | 100 |
| 1000 | keyword-tail | 1 | 22.459 | 53.451 | 53.451 | 0.000 | 483.617 | 483.711 | 0.094 | 1 |
| 1000 | keyword-tail | 2 | 22.046 | 53.451 | 54.451 | 1.000 | 484.230 | 484.316 | 0.086 | 1 |
| 1000 | keyword-tail | 3 | 22.718 | 53.451 | 53.451 | 0.000 | 484.254 | 484.336 | 0.082 | 1 |
| 1000 | export-csv | 1 | 65.698 | 53.493 | 58.493 | 5.000 | 484.691 | 484.758 | 0.066 | 178074 |
| 1000 | export-csv | 2 | 43.031 | 53.494 | 58.494 | 5.000 | 484.699 | 484.777 | 0.078 | 178074 |
| 1000 | export-csv | 3 | 54.221 | 53.495 | 58.495 | 5.000 | 485.023 | 485.090 | 0.066 | 178074 |
| 1000 | export-xlsx | 1 | 233.773 | 57.242 | 67.242 | 10.000 | 546.824 | 547.301 | 0.477 | 86749 |
| 1000 | export-xlsx | 2 | 181.588 | 57.257 | 67.257 | 10.000 | 549.578 | 549.855 | 0.277 | 86749 |
| 1000 | export-xlsx | 3 | 182.016 | 57.257 | 67.257 | 10.000 | 549.871 | 550.230 | 0.359 | 86749 |
| 10000 | import-csv | 1 | 29271.427 | 57.298 | 171.452 | 114.153 | 550.750 | 602.246 | 51.496 | 10000 |
| 10000 | page-first | 1 | 43.207 | 57.339 | 57.339 | 0.000 | 595.402 | 595.473 | 0.070 | 100 |
| 10000 | page-first | 2 | 43.028 | 57.339 | 57.339 | 0.000 | 596.043 | 596.105 | 0.063 | 100 |
| 10000 | page-first | 3 | 42.443 | 57.339 | 57.339 | 0.000 | 596.043 | 596.133 | 0.090 | 100 |
| 10000 | page-deep | 1 | 42.505 | 57.346 | 57.346 | 0.000 | 596.148 | 596.215 | 0.066 | 100 |
| 10000 | page-deep | 2 | 53.523 | 57.346 | 58.346 | 1.000 | 596.152 | 596.215 | 0.063 | 100 |
| 10000 | page-deep | 3 | 55.073 | 57.346 | 57.346 | 0.000 | 586.785 | 586.859 | 0.074 | 100 |
| 10000 | keyword-tail | 1 | 42.542 | 57.347 | 58.347 | 1.000 | 586.813 | 586.883 | 0.070 | 1 |
| 10000 | keyword-tail | 2 | 32.164 | 57.348 | 58.348 | 1.000 | 586.867 | 586.945 | 0.078 | 1 |
| 10000 | keyword-tail | 3 | 43.410 | 57.348 | 57.348 | 0.000 | 586.898 | 586.980 | 0.082 | 1 |
| 10000 | export-csv | 1 | 287.256 | 57.349 | 110.349 | 53.000 | 594.047 | 595.293 | 1.246 | 1780053 |
| 10000 | export-csv | 2 | 221.138 | 57.350 | 110.350 | 53.000 | 595.230 | 595.336 | 0.105 | 1780053 |
| 10000 | export-csv | 3 | 211.647 | 57.349 | 110.349 | 53.000 | 595.273 | 595.355 | 0.082 | 1780053 |
| 10000 | export-xlsx | 1 | 862.599 | 57.359 | 139.359 | 82.000 | 595.434 | 595.555 | 0.121 | 845449 |
| 10000 | export-xlsx | 2 | 883.614 | 57.359 | 139.359 | 82.000 | 595.504 | 595.613 | 0.109 | 845449 |
| 10000 | export-xlsx | 3 | 857.487 | 57.360 | 139.360 | 82.000 | 595.551 | 595.719 | 0.168 | 845449 |
| 100000 | import-csv | 1 | 225442.547 | 57.381 | 174.503 | 117.122 | 595.688 | 614.434 | 18.746 | 100000 |
| 100000 | page-first | 1 | 397.854 | 57.386 | 58.386 | 1.000 | 601.426 | 601.496 | 0.070 | 100 |
| 100000 | page-first | 2 | 481.482 | 57.386 | 58.386 | 1.000 | 601.438 | 601.512 | 0.074 | 100 |
| 100000 | page-first | 3 | 438.772 | 57.386 | 58.386 | 1.000 | 601.457 | 601.527 | 0.070 | 100 |
| 100000 | page-deep | 1 | 636.741 | 57.387 | 59.387 | 2.000 | 601.469 | 601.535 | 0.066 | 100 |
| 100000 | page-deep | 2 | 616.170 | 57.387 | 59.387 | 2.000 | 601.473 | 601.551 | 0.078 | 100 |
| 100000 | page-deep | 3 | 535.405 | 57.388 | 59.388 | 2.000 | 601.492 | 601.555 | 0.063 | 100 |
| 100000 | keyword-tail | 1 | 300.651 | 57.389 | 58.389 | 1.000 | 601.496 | 601.563 | 0.066 | 1 |
| 100000 | keyword-tail | 2 | 378.796 | 57.389 | 58.389 | 1.000 | 601.500 | 601.570 | 0.070 | 1 |
| 100000 | keyword-tail | 3 | 467.010 | 57.389 | 58.389 | 1.000 | 601.508 | 601.582 | 0.074 | 1 |
| 100000 | export-csv | 1 | 1669.511 | 57.390 | 174.764 | 117.374 | 601.555 | 601.637 | 0.082 | 17799744 |
| 100000 | export-csv | 2 | 1729.084 | 57.391 | 180.835 | 123.444 | 601.613 | 614.738 | 13.125 | 17799744 |
| 100000 | export-csv | 3 | 1660.550 | 57.388 | 175.897 | 118.508 | 605.004 | 605.078 | 0.074 | 17799744 |
| 100000 | export-xlsx | 1 | 6604.597 | 57.397 | 174.397 | 117.000 | 601.402 | 601.535 | 0.133 | 8561225 |
| 100000 | export-xlsx | 2 | 7125.901 | 57.397 | 173.997 | 116.600 | 601.492 | 601.602 | 0.109 | 8561225 |
| 100000 | export-xlsx | 3 | 6806.224 | 57.399 | 174.399 | 117.000 | 601.570 | 601.680 | 0.109 | 8561225 |

## Summary (median when repeated)

| rows | operation | median_latency_ms | max_heap_delta_mib | max_rss_delta_mib |
|---:|---|---:|---:|---:|
| 1000 | import-csv | 3751.707 | 49.000 | 2.316 |
| 1000 | page-first | 32.402 | 0.000 | 0.086 |
| 1000 | page-deep | 32.763 | 0.000 | 0.133 |
| 1000 | keyword-tail | 22.459 | 1.000 | 0.094 |
| 1000 | export-csv | 54.221 | 5.000 | 0.078 |
| 1000 | export-xlsx | 182.016 | 10.000 | 0.477 |
| 10000 | import-csv | 29271.427 | 114.153 | 51.496 |
| 10000 | page-first | 43.028 | 0.000 | 0.090 |
| 10000 | page-deep | 53.523 | 1.000 | 0.074 |
| 10000 | keyword-tail | 42.542 | 1.000 | 0.082 |
| 10000 | export-csv | 221.138 | 53.000 | 1.246 |
| 10000 | export-xlsx | 862.599 | 82.000 | 0.168 |
| 100000 | import-csv | 225442.547 | 117.122 | 18.746 |
| 100000 | page-first | 438.772 | 1.000 | 0.074 |
| 100000 | page-deep | 616.170 | 2.000 | 0.078 |
| 100000 | keyword-tail | 378.796 | 1.000 | 0.074 |
| 100000 | export-csv | 1669.511 | 123.444 | 13.125 |
| 100000 | export-xlsx | 6806.224 | 117.000 | 0.133 |

## EXPLAIN ANALYZE baseline

Plans are captured after measurements on the same dataset, before any PRD-209 index change.

### 1000 rows — page-deep-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
ORDER BY name, id
LIMIT 100 OFFSET 900
```

```text
-> Limit/Offset: 100/900 row(s)  (cost=104 rows=100) (actual time=3.56..3.59 rows=100 loops=1)
    -> Sort: devices.`name`, devices.id, limit input to 1000 row(s) per chunk  (cost=104 rows=1000) (actual time=2.96..3.21 rows=1000 loops=1)
        -> Table scan on devices  (cost=104 rows=1000) (actual time=0.0632..0.974 rows=1000 loops=1)

```

### 1000 rows — page-count

```sql
SELECT COUNT(id) FROM devices
```

```text
-> Count rows in devices  (actual time=0.339..0.34 rows=1 loops=1)

```

### 1000 rows — keyword-tail-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
WHERE LOWER(name) LIKE '%scale device 0001000%'
   OR LOWER(model) LIKE '%scale device 0001000%'
   OR LOWER(serial_number) LIKE '%scale device 0001000%'
ORDER BY name, id
LIMIT 100 OFFSET 0
```

```text
-> Limit: 100 row(s)  (cost=104 rows=100) (actual time=2.69..2.7 rows=1 loops=1)
    -> Sort: devices.`name`, devices.id, limit input to 100 row(s) per chunk  (cost=104 rows=1000) (actual time=2.69..2.69 rows=1 loops=1)
        -> Filter: ((lower(devices.`name`) like '%scale device 0001000%') or (lower(devices.model) like '%scale device 0001000%') or (lower(devices.serial_number) like '%scale device 0001000%'))  (cost=104 rows=1000) (actual time=0.488..2.67 rows=1 loops=1)
            -> Table scan on devices  (cost=104 rows=1000) (actual time=0.0647..1.04 rows=1000 loops=1)

```

### 1000 rows — keyword-tail-count

```sql
SELECT COUNT(id)
FROM devices
WHERE LOWER(name) LIKE '%scale device 0001000%'
   OR LOWER(model) LIKE '%scale device 0001000%'
   OR LOWER(serial_number) LIKE '%scale device 0001000%'
```

```text
-> Aggregate: count(devices.id)  (cost=204 rows=1) (actual time=2.94..2.94 rows=1 loops=1)
    -> Filter: ((lower(devices.`name`) like '%scale device 0001000%') or (lower(devices.model) like '%scale device 0001000%') or (lower(devices.serial_number) like '%scale device 0001000%'))  (cost=104 rows=1000) (actual time=0.537..2.93 rows=1 loops=1)
        -> Table scan on devices  (cost=104 rows=1000) (actual time=0.0644..0.934 rows=1000 loops=1)

```

### 10000 rows — page-deep-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
ORDER BY name, id
LIMIT 100 OFFSET 9900
```

```text
-> Limit/Offset: 100/9900 row(s)  (cost=301 rows=0) (actual time=36.8..36.8 rows=100 loops=1)
    -> Sort: devices.`name`, devices.id, limit input to 10000 row(s) per chunk  (cost=301 rows=2763) (actual time=33.3..36.1 rows=10000 loops=1)
        -> Table scan on devices  (cost=301 rows=2763) (actual time=0.231..9.7 rows=10000 loops=1)

```

### 10000 rows — page-count

```sql
SELECT COUNT(id) FROM devices
```

```text
-> Count rows in devices  (actual time=19.5..19.5 rows=1 loops=1)

```

### 10000 rows — keyword-tail-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
WHERE LOWER(name) LIKE '%scale device 0010000%'
   OR LOWER(model) LIKE '%scale device 0010000%'
   OR LOWER(serial_number) LIKE '%scale device 0010000%'
ORDER BY name, id
LIMIT 100 OFFSET 0
```

```text
-> Limit: 100 row(s)  (cost=301 rows=100) (actual time=25..25 rows=1 loops=1)
    -> Sort: devices.`name`, devices.id, limit input to 100 row(s) per chunk  (cost=301 rows=2763) (actual time=25..25 rows=1 loops=1)
        -> Filter: ((lower(devices.`name`) like '%scale device 0010000%') or (lower(devices.model) like '%scale device 0010000%') or (lower(devices.serial_number) like '%scale device 0010000%'))  (cost=301 rows=2763) (actual time=21.9..25 rows=1 loops=1)
            -> Table scan on devices  (cost=301 rows=2763) (actual time=0.0741..10 rows=10000 loops=1)

```

### 10000 rows — keyword-tail-count

```sql
SELECT COUNT(id)
FROM devices
WHERE LOWER(name) LIKE '%scale device 0010000%'
   OR LOWER(model) LIKE '%scale device 0010000%'
   OR LOWER(serial_number) LIKE '%scale device 0010000%'
```

```text
-> Aggregate: count(devices.id)  (cost=577 rows=1) (actual time=15.7..15.7 rows=1 loops=1)
    -> Filter: ((lower(devices.`name`) like '%scale device 0010000%') or (lower(devices.model) like '%scale device 0010000%') or (lower(devices.serial_number) like '%scale device 0010000%'))  (cost=301 rows=2763) (actual time=13.9..15.7 rows=1 loops=1)
        -> Table scan on devices  (cost=301 rows=2763) (actual time=0.0345..4.39 rows=10000 loops=1)

```

### 100000 rows — page-deep-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
ORDER BY name, id
LIMIT 100 OFFSET 99900
```

```text
-> Limit/Offset: 100/99900 row(s)  (cost=426 rows=0) (actual time=503..503 rows=100 loops=1)
    -> Sort: devices.`name`, devices.id, limit input to 100000 row(s) per chunk  (cost=426 rows=4020) (actual time=487..500 rows=100000 loops=1)
        -> Table scan on devices  (cost=426 rows=4020) (actual time=8.67..274 rows=100000 loops=1)

```

### 100000 rows — page-count

```sql
SELECT COUNT(id) FROM devices
```

```text
-> Count rows in devices  (actual time=80.1..80.1 rows=1 loops=1)

```

### 100000 rows — keyword-tail-data

```sql
SELECT id, category, description, model, name, serial_number, state,
       updated_by, updated_time, version
FROM devices
WHERE LOWER(name) LIKE '%scale device 0100000%'
   OR LOWER(model) LIKE '%scale device 0100000%'
   OR LOWER(serial_number) LIKE '%scale device 0100000%'
ORDER BY name, id
LIMIT 100 OFFSET 0
```

```text
-> Limit: 100 row(s)  (cost=426 rows=100) (actual time=332..332 rows=1 loops=1)
    -> Sort: devices.`name`, devices.id, limit input to 100 row(s) per chunk  (cost=426 rows=4020) (actual time=332..332 rows=1 loops=1)
        -> Filter: ((lower(devices.`name`) like '%scale device 0100000%') or (lower(devices.model) like '%scale device 0100000%') or (lower(devices.serial_number) like '%scale device 0100000%'))  (cost=426 rows=4020) (actual time=219..332 rows=1 loops=1)
            -> Table scan on devices  (cost=426 rows=4020) (actual time=0.0579..197 rows=100000 loops=1)

```

### 100000 rows — keyword-tail-count

```sql
SELECT COUNT(id)
FROM devices
WHERE LOWER(name) LIKE '%scale device 0100000%'
   OR LOWER(model) LIKE '%scale device 0100000%'
   OR LOWER(serial_number) LIKE '%scale device 0100000%'
```

```text
-> Aggregate: count(devices.id)  (cost=828 rows=1) (actual time=313..313 rows=1 loops=1)
    -> Filter: ((lower(devices.`name`) like '%scale device 0100000%') or (lower(devices.model) like '%scale device 0100000%') or (lower(devices.serial_number) like '%scale device 0100000%'))  (cost=426 rows=4020) (actual time=212..313 rows=1 loops=1)
        -> Table scan on devices  (cost=426 rows=4020) (actual time=0.0452..172 rows=100000 loops=1)

```
