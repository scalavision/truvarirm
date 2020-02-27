#!/usr/bin/env bash

RESULT="/stash/workspace/results/hg002_tsd/tir_25_02_2020"
RESULT_DIR="$RESULT"/result
bin/truvarirm \
  --soffice /nix/store/79vkpmhicpzh1150abjvxb4252nql3yy-libreoffice-6.2.6.2/bin/soffice \
  --truvariTemplate "$PWD"/truvari_report_template.ods \
  --out "$RESULT" \
  --report $RESULT_DIR/cnvnator_Diag-wgs-HG002_DEL_RESULT:cnvnator_del \
           $RESULT_DIR/manta_Diag-wgs-HG002_diploidSV_DEL_RESULT:manta_del \
           $RESULT_DIR/delly_Diag-wgs-HG002_DEL_RESULT:delly_del \
           $RESULT_DIR/tiddit_Diag-wgs-HG002_DEL_RESULT:tiddit_del \
           $RESULT_DIR/manta_Diag-wgs-HG002_diploidSV_INS_RESULT:manta_ins \
           $RESULT_DIR/delly_Diag-wgs-HG002_INS_RESULT:delly_ins

