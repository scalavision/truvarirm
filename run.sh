#!/usr/bin/env bash

: '
  Example of running Truvari Report Manager
'

SOFFICE=/nix/store/my0yz2s2n8vhn7345rlq5n6q6h3q2abf-libreoffice-6.0.7.3/bin/soffice
OUT=$HOME/tmp/report_ins_and_del_with_filters.ods
#TRUVARI_TEMPLATE=$HOME/stash/workspace/templates/truvari_template.ods
TRUVARI_TEMPLATE=/home/obiwan/Projects/truvarirm/truvari_report_template.ods
BASE_DEL=/workspace/results/tomato/truvari_results_DEL
BASE_INS=/workspace/results/tomato/INS_WORK

./bin/truvarirm \
  --soffice $SOFFICE \
  --truvariTemplate $TRUVARI_TEMPLATE \
  --out $OUT \
  --report $BASE_DEL/cnvnator_Diag-wgs1-HG002C2350bp-400M:CNVnatorDEL  \
           $BASE_DEL/cnvnator_whitelist_filtered:CNVnatorWLDEL  \
           $BASE_DEL/manta_Diag-wgs1-HG002C2350bp-400M_diploidSV:MantaDEL \
           $BASE_DEL/delly_Diag-wgs1-HG002C2350bp-400M:DellyDEL \
           $BASE_DEL/tiddit_Diag-wgs1-HG002C2350bp-400M:TidditDel \
           $BASE_INS/manta_Diag-wgs1-HG002C2350bp-400M_diploidSV_standardized:MantaINS \
           $BASE_INS/delly_Diag-wgs1-HG002C2350bp-400M_standardized:DellyINS
#           $BASE_INS/tiddit_Diag-wgs1-HG002C2350bp-400M_standardized:TIDDIT_INS

