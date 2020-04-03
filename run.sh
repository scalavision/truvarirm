#!/usr/bin/env bash

: '
  Example of running Truvari Report Manager
'
set -euo pipefail

SOFFICE="${HOME}"/.nix-profile/bin/soffice
OUT="${HOME}"/tmp/report_ins_and_del_with_filters.ods
#TRUVARI_TEMPLATE=$HOME/stash/workspace/templates/truvari_template.ods
TRUVARI_TEMPLATE="${HOME}"/Projects/homegrown/truvarirm/truvari_report_template.ods
# BASE="/stash/workspace/results/vcpipe-sv-run-2020_04_01_1330/result"
# SAMPLE="Diag-wgsTest350bp-HG002C2350bp400M-PM"
SAMPLE=$1
BASE="/stash/workspace/results/${SAMPLE}/result"
./bin/truvarirm \
  --soffice $SOFFICE \
  --truvariTemplate $TRUVARI_TEMPLATE \
  --out $OUT \
  --report "$BASE"/cnvnator_"${SAMPLE}"_DEL_RESULT:CNVnatorDEL  \
           "$BASE"/manta_"${SAMPLE}"_diploidSV_DEL_RESULT:MantaDEL \
           "$BASE"/manta_"${SAMPLE}"_diploidSV_INS_RESULT:MantaINS\
           "$BASE"/delly_"${SAMPLE}"_DEL_RESULT:DellyDEL \
           "$BASE"/delly_"${SAMPLE}"_INS_RESULT:DellyINS \
           "$BASE"/tiddit_"${SAMPLE}"_DEL_RESULT:TidditDEL

