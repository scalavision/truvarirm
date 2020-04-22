#!/usr/bin/env bash

: '
  Example of running Truvari Report Manager
'

set -x
set -euo pipefail

# BASE="/stash/workspace/results/Dragen/result"
BASE="/stash/workspace/results/Diag-ValidationWGS-HG002C2c-PM/result"

SOFFICE="${HOME}"/.nix-profile/bin/soffice
OUT="${BASE}"/manta_and_dragen.ods
TRUVARI_TEMPLATE="${HOME}"/Projects/homegrown/truvarirm/truvari_report_template.ods
SAMPLES=( Diag-ValidationWGS-HG002C2c-PM )

REPORT_ARGS=()
for S in "${SAMPLES[@]}"; do
  # Replace all `-` with '_` since - is not supported as open office excel doc links
  REPORT_ARGS+=( "$BASE"/cnvnator_"$S"_DEL_RESULT:del_cnvnator_"${S//-/_}" )
  REPORT_ARGS+=( "$BASE"/tiddit_"$S"_DEL_RESULT:del_tiddit_"${S//-/_}" )
  REPORT_ARGS+=( "$BASE"/delly_"$S"_DEL_RESULT:del_delly_"${S//-/_}" )
  REPORT_ARGS+=( "$BASE"/delly_"$S"_INS_RESULT:ins_delly_"${S//-/_}" )
  REPORT_ARGS+=( "$BASE"/manta_"$S"_diploidSV_DEL_RESULT:del_manta_"${S//-/_}" )
  REPORT_ARGS+=( "$BASE"/manta_"$S"_diploidSV_INS_RESULT:ins_manta_"${S//-/_}" )
done

./bin/truvarirm \
  --soffice "$SOFFICE" \
  --truvariTemplate "$TRUVARI_TEMPLATE" \
  --out "$OUT" \
  --report \
  "${REPORT_ARGS[@]}"

