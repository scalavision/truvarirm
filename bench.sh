#!/usr/bin/env bash

set -x
set -euo pipefail

RESULT="/stash/workspace/results/hg002_raw_tsd"
RESULT_DIR="$RESULT"/result

bin/truvarirm \
  --soffice /nix/store/79vkpmhicpzh1150abjvxb4252nql3yy-libreoffice-6.2.6.2/bin/soffice \
  --truvariTemplate "$PWD"/truvari_report_template.ods \
  --out "$RESULT" \
  --report "$RESULT_DIR"/cnvnator_Diag-wgs-HG002_raw_result:cnvnator \
  "$RESULT_DIR"/delly_Diag-wgs-HG002_raw_result:delly \
  "$RESULT_DIR"/tiddit_Diag-wgs-HG002_raw_result:tiddit \
  "$RESULT_DIR"/manta_Diag-wgs-HG002_diploidSV_raw_result:manta

