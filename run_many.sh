#!/usr/bin/env bash

: '
  Example of running Truvari Report Manager
'
set -euo pipefail

#SOFFICE="${HOME}"/.nix-profile/bin/soffice
SOFFICE="/run/current-system/sw/bin/soffice"
OUT="${HOME}"/tmp/report_ins_and_del_with_filters.ods
#TRUVARI_TEMPLATE=$HOME/stash/workspace/templates/truvari_template.ods

BASE="/home/obiwan/Documents/bio/sv/report"
SAMPLES=(Diag-ValidationWGS-HG002C2a-PM  Diag-ValidationWGS-HG002C2c-PM  Diag-ValidationWGS2-HG002C2-PM)
SAMPLE_ARG=()

for SAMPLE in "${SAMPLES[@]}"; do
  SAMPLE_ARG+="${BASE}/${SAMPLE}/result/manta_${SAMPLE}_diploidSV_DEL_RESULT:${SAMPLE//-/_}_MantaDel "
  SAMPLE_ARG+="${BASE}/${SAMPLE}/result/manta_${SAMPLE}_diploidSV_INS_RESULT:${SAMPLE//-/_}_MantaIns "
  SAMPLE_ARG+="${BASE}/${SAMPLE}/result/cnvnator_${SAMPLE}_DEL_RESULT:${SAMPLE//-/_}_CVNnatorDel "
  SAMPLE_ARG+="${BASE}/${SAMPLE}/result/tiddit_${SAMPLE}_DEL_RESULT:${SAMPLE//-/_}_TidditDel "
  SAMPLE_ARG+="${BASE}/${SAMPLE}/result/delly_${SAMPLE}_DEL_RESULT:${SAMPLE//-/_}_DellyDel "
  SAMPLE_ARG+="${BASE}/${SAMPLE}/result/delly_${SAMPLE}_INS_RESULT:${SAMPLE//-/_}_DellyIns "
done

TRUVARI_TEMPLATE="${HOME}"/Projects/homegrown/truvarirm/truvari_report_template.ods
# BASE="/stash/workspace/results/vcpipe-sv-run-2020_04_01_1330/result"
# SAMPLE="Diag-wgsTest350bp-HG002C2350bp400M-PM"
#SAMPLE=$1
#BASE="/stash/workspace/results/${SAMPLE}/result"
./bin/truvarirm \
  --soffice "${SOFFICE}" \
  --truvariTemplate "${TRUVARI_TEMPLATE}" \
  --out "${OUT}" \
  --report ${SAMPLE_ARG}
