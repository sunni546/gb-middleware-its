#!/usr/bin/env bash
# - 디렉토리별(prefix 상이) 어제자 시간대 로그를 합쳐 gzip
# - 원본 시간대 로그 삭제 + 보관기간 초과 gz 삭제

set -euo pipefail

# ===== 설정값 =====
LOG_DIR="./"

# 보관 기간 (일)
RETAIN_DAYS=30

# 어제 날짜 (GNU date)
YESTERDAY=$(date -d "yesterday" +"%Y-%m-%d")

declare -A DIR_PREFIXES=(
  ["edge"]="edge-requests"
  ["maria"]="maria-queries"
  ["middleware"]="middleware-its"
  ["tibero"]="tibero-queries"
  ["volt"]="volt-queries"
)

cd "$LOG_DIR" || exit 1

for SUBDIR in "${!DIR_PREFIXES[@]}"; do
  TARGET_DIR="${LOG_DIR}/${SUBDIR}"
  [[ -d "$TARGET_DIR" ]] || { echo "[SKIP] Missing dir: $TARGET_DIR"; continue; }

  echo "----------------------------------------"
  echo "[DIR ] $TARGET_DIR"

  # 해당 디렉토리에서 처리할 접두사들
  for PREFIX in ${DIR_PREFIXES[$SUBDIR]}; do
    # 시간대 로그 패턴 (예: edge-requests_2025-08-26_11.log)
    PATTERN="${TARGET_DIR}/${PREFIX}_${YESTERDAY}_*.log"
    # 합쳐서 압축할 파일명 (예: edge-requests_2025-08-26.log.gz)
    ARCHIVE_NAME="${TARGET_DIR}/${PREFIX}_${YESTERDAY}.log.gz"

    if ls $PATTERN 1> /dev/null 2>&1; then
        echo "[INFO] Compressing $PATTERN -> $(basename "$ARCHIVE_NAME")"
        # 시간순 정렬 후 병합 → gzip
        # (bash globs 가 시간순을 보장하지 않으므로 sort -V 로 보장)
        ls -1 ${TARGET_DIR}/${PREFIX}_${YESTERDAY}_*.log \
          | sort -V \
          | xargs -r cat \
          | gzip > "$ARCHIVE_NAME"

        # 원본 시간대 로그 삭제
        rm -f ${TARGET_DIR}/${PREFIX}_${YESTERDAY}_*.log
        echo "[DONE] Compressed and removed hourly logs for ${PREFIX}"
    else
        echo "[SKIP] No hourly logs to compress for $PREFIX on $YESTERDAY"
    fi

    # 오래된 압축 파일 삭제
    echo "[CLEANUP] Deleting ${PREFIX}_*.log.gz older than ${RETAIN_DAYS} days"
    find "$TARGET_DIR" -name "${PREFIX}_*.log.gz" -type f -mtime +$RETAIN_DAYS -exec rm -f {} \;
  done
done

echo "[SUCCESS] Finished compression & cleanup for ${YESTERDAY}"
