# Database Backup Strategy (MySQL)

## Goal
Recover quickly if data is accidentally deleted or corrupted.

## Recommended approach
1. **Automated daily backups** (plus hourly incremental if possible).
2. **Keep multiple generations** (e.g., last 7 daily + 4 weekly + 12 monthly).
3. **Store backups off-server** (S3/another machine) and encrypt at rest.

## Local example command (manual)
> Run from the machine that has MySQL credentials.

```bash
mysqldump -h <host> -u <user> -p courseacademy \
  --single-transaction --routines --triggers --events \
  > ./backup-courseacademy-$(date +%F_%H-%M-%S).sql
```

## Restore example
```bash
mysql -h <host> -u <user> -p courseacademy < backup.sql
```

## Integrity verification
- After restore, verify row counts for critical tables.
- Run application smoke test.

## Notes
- In production, prefer `mysqlpump` or managed backup services when available.
- Ensure the DB user used for backups has least-privilege required (read-only dump).

